package com.somefrills.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.util.concurrent.atomic.AtomicBoolean

class FrillsFeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private data class FeatureEntry(
        val qualifiedName: String,
        val isObject: Boolean
    )

    private val seen = linkedMapOf<String, FeatureEntry>()
    private val wrote = AtomicBoolean(false)

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val symbols = resolver.getSymbolsWithAnnotation(
            "com.somefrills.features.core.FrillsFeature"
        )
            .filterIsInstance<KSClassDeclaration>()

        for (s in symbols) {
            val qn = s.qualifiedName?.asString() ?: continue
            seen[qn] = FeatureEntry(
                qualifiedName = qn,
                isObject = s.classKind == ClassKind.OBJECT
            )
        }

        val symbolCount = symbols.count()
        if (symbolCount > 0) {
            println("FOUND symbols: $symbolCount")
            println("TOTAL cached: ${seen.size}")
        }

        val allSymbols = seen.values.sortedBy { it.qualifiedName }

        // ONLY WRITE ONCE (prevents FileAlreadyExistsException)
        if (!wrote.compareAndSet(false, true)) {
            return emptyList()
        }

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "com.somefrills.features.core",
            fileName = "FeaturesRegistry",
            extensionName = "kt"
        )

        file.writer().use { out ->

            out.appendLine("package com.somefrills.features.core")
            out.appendLine()
            out.appendLine("import com.somefrills.features.*")
            out.appendLine()

            out.appendLine("object FeaturesRegistry {")

            out.appendLine("    val CREATORS: Array<() -> AbstractFeature> = arrayOf(")

            allSymbols.forEach {
                val ref = if (it.isObject) {
                    it.qualifiedName
                } else {
                    "${it.qualifiedName}()"
                }
                out.appendLine("        { $ref },")
            }

            out.appendLine("    )")
            out.appendLine()

            out.appendLine("    val INSTANCES: Array<AbstractFeature?> = arrayOfNulls(CREATORS.size)")
            out.appendLine()

            out.appendLine("    val CLASS_TO_INSTANCE: ClassValue<AbstractFeature> = object : ClassValue<AbstractFeature>() {")
            out.appendLine("        override fun computeValue(type: Class<*>): AbstractFeature {")

            allSymbols.forEachIndexed { i, e ->
                out.appendLine("            if (type == ${e.qualifiedName}::class.java) return INSTANCES[$i]!!")
            }

            out.appendLine("            throw IllegalStateException(\"Unknown feature: \" + type)")
            out.appendLine("        }")
            out.appendLine("    }")
            out.appendLine()

            out.appendLine("    fun init() {")
            out.appendLine("        for (i in CREATORS.indices) {")
            out.appendLine("            INSTANCES[i] = CREATORS[i]()")
            out.appendLine("        }")
            out.appendLine("    }")

            out.appendLine("}")
        }

        return emptyList()
    }
}