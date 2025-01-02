/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.generator.gradle.plugin.tasks

import org.gradle.api.GradleException
import org.gradle.api.tasks.VerificationException
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.openapitools.codegen.config.CodegenConfigurator
import org.openapitools.codegen.config.GlobalSettings
import org.openapitools.codegen.config.MergedSpecBuilder

/**
 * A task which checks if there are changes to the generated code.
 *
 * Example (CLI):
 *
 * ./gradlew -q openApiCheck --input=/path/to/file
 */
@Suppress("UnstableApiUsage")
@CacheableTask
open class ChangesTask : CommonGenerateCheckTask() {
    @TaskAction
    fun doWork() {
        inputSpecRootDirectory.ifNotEmpty { inputSpecRootDirectoryValue -> {
            inputSpec.set(MergedSpecBuilder(inputSpecRootDirectoryValue, mergedFileName.get()).buildMergedSpec())
            logger.info("Merge input spec would be used - {}", inputSpec.get())
        }}

        val configurator: CodegenConfigurator = if (configFile.isPresent) {
            CodegenConfigurator.fromFile(configFile.get())
        } else createDefaultCodegenConfigurator()

        try {
            processConfig(configurator)
            var generateMetadataSetting = true;
            generateMetadata.ifNotEmpty { setting ->
                generateMetadataSetting = setting
            }

            val clientOptInput = configurator.toClientOptInput()
            processAdditionalProperties(clientOptInput)

            try {
                val out = services.get(StyledTextOutputFactory::class.java).create("openapi")
                out.withStyle(StyledTextOutput.Style.Success)

                val selectedCodegen = selectCodegen(true)
                if (selectedCodegen != null) {
                    selectedCodegen.setGenerateMetadata(generateMetadataSetting)

                    val gen = selectedCodegen.opts(clientOptInput)
                    gen.generate()
                    if (gen.hasChanges()) {
                        val changedFiles = gen.getChangedFiles()
                        val output = buildString {
                            appendLine("There were changes to the generated code.")
                            appendLine()
                            appendLine("Changed files:")
                            append(changedFiles.joinToString(separator = System.lineSeparator()))
                        }

                        throw VerificationException(output)
                    } else {
                        out.println("There were no changes to the generated code.")
                    }
                } else {
                    throw GradleException("The supplied codegen name or class does implement org.openapitools.codegen.Generator.")
                }
            } catch (e: GradleException) {
                throw e
            } catch (e: RuntimeException) {
                throw GradleException("Code generation failed.", e)
            }
        } finally {
            GlobalSettings.reset()
        }
    }
}
