package com.somefrills.modules

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class PrimaryFunctionProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = PrimaryFunctionProcessor(
        environment.codeGenerator,
        environment.logger,
        environment.options["somefrills.modver"] ?: "0.0.0",
        environment.options["somefrills.mcver"] ?: "26.1",
        environment.options["somefrills.cachedir"],
    )
}
