package com.somefrills.modules

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.File
import java.io.OutputStreamWriter

class ModuleProcessor(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    modVersion: String,
    private val mcVersion: String,
    private val buildPaths: String?,
    cacheDir: String?,
) : BaseProcessor(codeGenerator, logger, modVersion) {

    private var skyHanniEvent: KSType? = null
    private val warnings = mutableListOf<String>()
    private val cache = KspIncrementalCache(cacheDir, mcVersion, "ksp-module-state")

    override fun processSymbols(resolver: Resolver): List<KSAnnotated> {
        skyHanniEvent = resolver.getClassDeclarationByName("com.somefrills.events.FrillsEvent")?.asStarProjectedType()

        val symbols = processBuildPaths(resolver.getSymbolsWithAnnotation(FrillsFeature::class.qualifiedName!!).toList())
        val filePaths = symbols.mapNotNull { it.containingFile?.filePath }.toSet()
        val outputFile = cache.outputFile("at/hannibal2/skyhanni/FrillsFeature", "LoadedModules")
        val dirtyFilePaths = cache.evaluate(filePaths, outputFile)

        val dirtyCount = symbols.count { it.containingFile?.filePath in (dirtyFilePaths ?: emptySet()) }
        val cachedCount = symbols.size - dirtyCount
        logger.warn("Found ${symbols.size} symbols with @FrillsFeature for mc $mcVersion ($dirtyCount revalidated, $cachedCount from cache)")

        if (dirtyFilePaths == null) {
            logger.warn("No @FrillsFeature files changed, skipping LoadedModules regeneration")
            cache.commit()
            return emptyList()
        }

        if (dirtyFilePaths.isEmpty()) {
            logger.warn("No @FrillsFeature files changed but LoadedModules.kt is missing, regenerating")
        }

        val validSymbols = symbols.mapNotNull { validateSymbol(it, it.containingFile?.filePath in dirtyFilePaths) }
        if (validSymbols.isNotEmpty()) generateFile(validSymbols)
        cache.commit()
        return emptyList()
    }

    private fun processBuildPaths(symbols: List<KSAnnotated>): List<KSAnnotated> {
        val buildPathsFile = buildPaths?.let { File(it) }?.takeIf { it.exists() } ?: return symbols
        val validPaths = buildPathsFile.readText().lineSequence()
            .map { it.substringBefore("#").replace(Regex("\\.(?!kt|java|\\()"), "/").trim() }
            .filter { it.isNotBlank() }
            .toSet()
        return symbols.filter {
            val path = it.containingFile?.filePath ?: return@filter false
            path.substringAfter("/main/kotlin/") !in validPaths
        }
    }

    /**
     * Validates that a symbol is a valid `@FrillsFeature` target.
     *
     * @param symbol The annotated symbol to validate.
     * @param isDirty Whether the symbol's source file is new or modified since the last build.
     *                If false, expensive type resolution is skipped as the symbol was already validated.
     */
    private fun validateSymbol(symbol: KSAnnotated, isDirty: Boolean): KSClassDeclaration? {
        if (!symbol.validate()) {
            logger.warn("Symbol is not valid: $symbol")
            return null
        }
        if (symbol !is KSClassDeclaration) {
            logger.error("@FrillsFeature is only valid on class declarations", symbol)
            return null
        }
        if (symbol.classKind != ClassKind.OBJECT) {
            logger.error("@FrillsFeature is only valid on kotlin objects", symbol)
            return null
        }

        if (isDirty) {
            val className = symbol.qualifiedName?.asString() ?: "unknown"
            for (function in symbol.getDeclaredFunctions()) {
                if (function.annotations.any { it.shortName.asString() == "EventHandle" }) {
                    val event = skyHanniEvent ?: return symbol
                    val firstParam = function.parameters.firstOrNull()?.type?.resolve()
                    val eventType = function.annotations.find { it.shortName.asString() == "EventHandle" }
                        ?.arguments?.find { it.name?.asString() == "eventType" }?.value
                    if ((firstParam == null && eventType == null) || (firstParam != null && !event.isAssignableFrom(firstParam)))
                        warnings.add("Function in $className must have an event assignable from $event because it is annotated with @EventHandle")
                }
            }
        }

        return symbol
    }

    private fun generateFile(symbols: List<KSClassDeclaration>) {
        if (warnings.isNotEmpty()) {
            warnings.forEach { logger.warn(it) }
            error("${warnings.size} errors related to event annotations found, please fix them before continuing. Click on the kspKotlin build log for more information.")
        }

        val sources = symbols.mapNotNull { it.containingFile }.toTypedArray()
        val file = codeGenerator.createNewFile(
            Dependencies(true, *sources),
            "com.somefrills.modules",
            "LoadedModules"
        )
        OutputStreamWriter(file).use {
            it.write("package com.somefrills.modules\n\n")
            it.write("@Suppress(\"LargeClass\")\n")
            it.write("object LoadedModules {\n")
            it.write("    val modules: List<Any> = buildList {\n")
            symbols.forEach { symbol ->
                it.write("        add(${symbol.qualifiedName!!.asString()})\n")
            }
            it.write("    }\n")
            it.write("}\n")
        }
        logger.warn("Generated LoadedModules file with ${symbols.size} modules")
    }
}
