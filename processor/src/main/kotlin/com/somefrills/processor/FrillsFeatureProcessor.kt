package com.somefrills.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import java.io.File

class FrillsFeatureProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private var generated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {

        if (generated) return emptyList()
        generated = true

        val symbols = resolver.getSymbolsWithAnnotation(
            "com.somefrills.features.core.FrillsFeature"
        ).filterIsInstance<KSClassDeclaration>().toList()

        val features = symbols.mapNotNull { s ->
            val qn = s.qualifiedName?.asString() ?: return@mapNotNull null

            if (s.classKind != ClassKind.OBJECT) {
                logger.error(
                    "@FrillsFeature can only be applied to Kotlin objects (singleton). Found: $qn",
                    s
                )
                return@mapNotNull null
            }

            qn
        }.sorted()

        val current = features.toSet()

        val cacheFile = File(getCacheDir(), "features-cache.txt")
        val previous = load(cacheFile)

        val added = current - previous
        val removed = previous - current

        if (added.isNotEmpty() || removed.isNotEmpty()) {
            logger.info("Frills diff:")

            if (added.isNotEmpty()) {
                logger.info("added: " + added.joinToString { simpleName(it) })
            }

            if (removed.isNotEmpty()) {
                logger.info("removed: " + removed.joinToString { simpleName(it) })
            }
        }

        save(cacheFile, current)

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

            out.appendLine(
                "    val INSTANCES: Array<AbstractFeature> = arrayOf("
            )

            for (f in features) {
                // Kotlin objects are singletons → direct reference
                out.appendLine("        $f,")
            }

            out.appendLine("    )")
            out.appendLine()
            out.appendLine("    fun init() {")
            out.appendLine("        for (i in INSTANCES.indices) {")
            out.appendLine("            INSTANCES[i].initialize()")
            out.appendLine("        }")
            out.appendLine("    }")
            out.appendLine("}")
        }

        return emptyList()
    }

    private fun getCacheDir(): File {
        val path = options["frills.cache.dir"]
            ?: error("Missing frills.cache.dir KSP option")
        return File(path)
    }

    private fun load(file: File): Set<String> {
        if (!file.exists()) return emptySet()
        return file.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    private fun save(file: File, data: Set<String>) {
        file.parentFile.mkdirs()
        file.writeText(data.joinToString("\n"))
    }

    private fun simpleName(qn: String): String =
        qn.substringAfterLast('.')
}