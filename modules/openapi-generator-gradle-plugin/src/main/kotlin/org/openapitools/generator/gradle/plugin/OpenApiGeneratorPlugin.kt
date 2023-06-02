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

package org.openapitools.generator.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.openapitools.generator.gradle.plugin.extensions.*
import org.openapitools.generator.gradle.plugin.tasks.*

/**
 * A plugin providing common Open API Generator use cases.
 *
 * @author Jim Schubert
 */
@Suppress("unused")
class OpenApiGeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.run {
            val meta = extensions.create(
                "openApiMeta",
                OpenApiGeneratorMetaExtension::class.java,
                project
            )

            val validate = extensions.create(
                "openApiValidate",
                OpenApiGeneratorValidateExtension::class.java,
                project
            )

            val generate = extensions.create(
                "openApiGenerate",
                OpenApiGeneratorGenerateExtension::class.java,
                project
            )

            val generators = extensions.create(
                "openApiGenerators",
                OpenApiGeneratorGeneratorsExtension::class.java,
                project
            )

            val changes = extensions.create(
                "openApiChanges",
                OpenApiGeneratorChangesExtension::class.java,
                project
            )

            generate.outputDir.set("$buildDir/generate-resources/main")
            changes.outputDir.set("$buildDir/generate-resources/main")

            tasks.apply {
                register("openApiGenerators", GeneratorsTask::class.java).configure {
                    group = pluginGroup
                    description = "Lists generators available via Open API Generators."

                    include.set(generators.include)
                }

                register("openApiMeta", MetaTask::class.java).configure {
                    group = pluginGroup
                    description = "Generates a new generator to be consumed via Open API Generator."

                    generatorName.set(meta.generatorName)
                    packageName.set(meta.packageName)
                    outputFolder.set(meta.outputFolder)
                }

                register("openApiValidate", ValidateTask::class.java).configure {
                    group = pluginGroup
                    description = "Validates an Open API 2.0 or 3.x specification document."

                    inputSpec.set(validate.inputSpec)
                    recommend.set(validate.recommend)
                }

                register("openApiGenerate", GenerateTask::class.java).configure {
                    group = pluginGroup
                    description =
                        "Generate code via Open API Tools Generator for Open API 2.0 or 3.x specification documents."

                    verbose.set(generate.verbose)
                    validateSpec.set(generate.validateSpec)
                    generatorName.set(generate.generatorName)
                    codegenName.set(generate.codegenName)
                    outputDir.set(generate.outputDir)
                    inputSpec.set(generate.inputSpec)
                    inputSpecRootDirectory.set(generate.inputSpecRootDirectory)
                    remoteInputSpec.set(generate.remoteInputSpec)
                    templateDir.set(generate.templateDir)
                    auth.set(generate.auth)
                    globalProperties.set(generate.globalProperties)
                    configFile.set(generate.configFile)
                    skipOverwrite.set(generate.skipOverwrite)
                    packageName.set(generate.packageName)
                    apiPackage.set(generate.apiPackage)
                    modelPackage.set(generate.modelPackage)
                    modelNamePrefix.set(generate.modelNamePrefix)
                    modelNameSuffix.set(generate.modelNameSuffix)
                    apiNameSuffix.set(generate.apiNameSuffix)
                    instantiationTypes.set(generate.instantiationTypes)
                    typeMappings.set(generate.typeMappings)
                    additionalProperties.set(generate.additionalProperties)
                    serverVariables.set(generate.serverVariables)
                    languageSpecificPrimitives.set(generate.languageSpecificPrimitives)
                    importMappings.set(generate.importMappings)
                    schemaMappings.set(generate.schemaMappings)
                    inlineSchemaNameMappings.set(generate.inlineSchemaNameMappings)
                    inlineSchemaOptions.set(generate.inlineSchemaOptions)
                    nameMappings.set(generate.nameMappings)
                    modelNameMappings.set(generate.modelNameMappings)
                    parameterNameMappings.set(generate.parameterNameMappings)
                    openapiNormalizer.set(generate.openapiNormalizer)
                    invokerPackage.set(generate.invokerPackage)
                    groupId.set(generate.groupId)
                    id.set(generate.id)
                    version.set(generate.version)
                    library.set(generate.library)
                    gitHost.set(generate.gitHost)
                    gitUserId.set(generate.gitUserId)
                    gitRepoId.set(generate.gitRepoId)
                    releaseNote.set(generate.releaseNote)
                    httpUserAgent.set(generate.httpUserAgent)
                    reservedWordsMappings.set(generate.reservedWordsMappings)
                    ignoreFileOverride.set(generate.ignoreFileOverride)
                    removeOperationIdPrefix.set(generate.removeOperationIdPrefix)
                    skipOperationExample.set(generate.skipOperationExample)
                    apiFilesConstrainedTo.set(generate.apiFilesConstrainedTo)
                    modelFilesConstrainedTo.set(generate.modelFilesConstrainedTo)
                    supportingFilesConstrainedTo.set(generate.supportingFilesConstrainedTo)
                    generateModelTests.set(generate.generateModelTests)
                    generateModelDocumentation.set(generate.generateModelDocumentation)
                    generateApiTests.set(generate.generateApiTests)
                    generateApiDocumentation.set(generate.generateApiDocumentation)
                    withXml.set(generate.withXml)
                    configOptions.set(generate.configOptions)
                    logToStderr.set(generate.logToStderr)
                    enablePostProcessFile.set(generate.enablePostProcessFile)
                    skipValidateSpec.set(generate.skipValidateSpec)
                    generateAliasAsModel.set(generate.generateAliasAsModel)
                    engine.set(generate.engine)
                    cleanupOutput.set(generate.cleanupOutput)
                    dryRun.set(generate.dryRun)
                    generateMetadata.set(generate.generateMetadata)
                }

                register("openApiChanges", ChangesTask::class.java).configure {
                    group = pluginGroup
                    description =
                        "Checks if code generation from Open API 2.0 or 3.x specification documents would change existing generated files."

                    verbose.set(changes.verbose)
                    validateSpec.set(changes.validateSpec)
                    generatorName.set(changes.generatorName)
                    codegenName.set(changes.codegenName)
                    outputDir.set(changes.outputDir)
                    inputSpec.set(changes.inputSpec)
                    inputSpecRootDirectory.set(changes.inputSpecRootDirectory)
                    remoteInputSpec.set(changes.remoteInputSpec)
                    templateDir.set(changes.templateDir)
                    auth.set(changes.auth)
                    globalProperties.set(changes.globalProperties)
                    configFile.set(changes.configFile)
                    packageName.set(changes.packageName)
                    apiPackage.set(changes.apiPackage)
                    modelPackage.set(changes.modelPackage)
                    modelNamePrefix.set(changes.modelNamePrefix)
                    modelNameSuffix.set(changes.modelNameSuffix)
                    apiNameSuffix.set(changes.apiNameSuffix)
                    instantiationTypes.set(changes.instantiationTypes)
                    typeMappings.set(changes.typeMappings)
                    additionalProperties.set(changes.additionalProperties)
                    serverVariables.set(changes.serverVariables)
                    languageSpecificPrimitives.set(changes.languageSpecificPrimitives)
                    importMappings.set(changes.importMappings)
                    schemaMappings.set(changes.schemaMappings)
                    inlineSchemaNameMappings.set(changes.inlineSchemaNameMappings)
                    inlineSchemaOptions.set(changes.inlineSchemaOptions)
                    openapiNormalizer.set(changes.openapiNormalizer)
                    invokerPackage.set(changes.invokerPackage)
                    groupId.set(changes.groupId)
                    id.set(changes.id)
                    version.set(changes.version)
                    library.set(changes.library)
                    gitHost.set(changes.gitHost)
                    gitUserId.set(changes.gitUserId)
                    gitRepoId.set(changes.gitRepoId)
                    releaseNote.set(changes.releaseNote)
                    httpUserAgent.set(changes.httpUserAgent)
                    reservedWordsMappings.set(changes.reservedWordsMappings)
                    ignoreFileOverride.set(changes.ignoreFileOverride)
                    removeOperationIdPrefix.set(changes.removeOperationIdPrefix)
                    skipOperationExample.set(changes.skipOperationExample)
                    apiFilesConstrainedTo.set(changes.apiFilesConstrainedTo)
                    modelFilesConstrainedTo.set(changes.modelFilesConstrainedTo)
                    supportingFilesConstrainedTo.set(changes.supportingFilesConstrainedTo)
                    generateModelTests.set(changes.generateModelTests)
                    generateModelDocumentation.set(changes.generateModelDocumentation)
                    generateApiTests.set(changes.generateApiTests)
                    generateApiDocumentation.set(changes.generateApiDocumentation)
                    withXml.set(changes.withXml)
                    configOptions.set(changes.configOptions)
                    logToStderr.set(changes.logToStderr)
                    enablePostProcessFile.set(changes.enablePostProcessFile)
                    skipValidateSpec.set(changes.skipValidateSpec)
                    generateAliasAsModel.set(changes.generateAliasAsModel)
                    engine.set(changes.engine)
                    generateMetadata.set(changes.generateMetadata)
                }
            }
        }
    }

    companion object {
        const val pluginGroup = "OpenAPI Tools"
    }
}



