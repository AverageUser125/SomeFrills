package com.somefrills.processor

import com.google.devtools.ksp.processing.*

class FrillsFeatureProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FrillsFeatureProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
}