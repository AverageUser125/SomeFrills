package com.somefrills.modules

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ModuleProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = ModuleProcessor(
        environment.codeGenerator,
        environment.logger,
        environment.options["somefrills.modver"] ?: "0.0.0",
        environment.options["somefrills.mcver"] ?: "26.1",
        environment.options["somefrills.buildpaths"],
        environment.options["somefrills.cachedir"],
    )
}
