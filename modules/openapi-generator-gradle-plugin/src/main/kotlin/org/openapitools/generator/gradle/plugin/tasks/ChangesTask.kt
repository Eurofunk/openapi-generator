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

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.api.tasks.options.Option
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import org.openapitools.codegen.CodegenConstants
import org.openapitools.codegen.DefaultGenerator
import org.openapitools.codegen.Generator
import org.openapitools.codegen.config.CodegenConfigurator
import org.openapitools.codegen.config.GlobalSettings
import org.openapitools.codegen.config.MergedSpecBuilder
import org.openapitools.generator.gradle.plugin.utils.isRemoteUri
import java.io.File
import java.util.ServiceConfigurationError
import java.util.ServiceLoader
import javax.inject.Inject

// =========================================================================================
// 1. WORKER API PARAMETERS
// Same shape as OpenApiWorkParameters but without dryRun/skipOverwrite (always dryRun=true).
// =========================================================================================
interface ChangesWorkParameters : WorkParameters {
    val resolvedInputSpec: Property<String>
    val outputDir: DirectoryProperty
    val configFile: RegularFileProperty
    val verbose: Property<Boolean>
    val validateSpec: Property<Boolean>
    val generatorName: Property<String>
    val auth: Property<String>
    val templateDir: DirectoryProperty
    val templateResourcePath: Property<String>
    val packageName: Property<String>
    val apiPackage: Property<String>
    val modelPackage: Property<String>
    val modelNamePrefix: Property<String>
    val modelNameSuffix: Property<String>
    val apiNameSuffix: Property<String>
    val invokerPackage: Property<String>
    val groupId: Property<String>
    val id: Property<String>
    val version: Property<String>
    val library: Property<String>
    val gitHost: Property<String>
    val gitUserId: Property<String>
    val gitRepoId: Property<String>
    val releaseNote: Property<String>
    val httpUserAgent: Property<String>
    val ignoreFileOverride: RegularFileProperty
    val removeOperationIdPrefix: Property<Boolean>
    val skipOperationExample: Property<Boolean>
    val logToStderr: Property<Boolean>
    val enablePostProcessFile: Property<Boolean>
    val skipValidateSpec: Property<Boolean>
    val generateAliasAsModel: Property<Boolean>
    val engine: Property<String>
    val codegenName: Property<String>
    val generateMetadata: Property<Boolean>

    val globalProperties: MapProperty<String, String>
    val instantiationTypes: MapProperty<String, String>
    val importMappings: MapProperty<String, String>
    val schemaMappings: MapProperty<String, String>
    val inlineSchemaNameMappings: MapProperty<String, String>
    val inlineSchemaOptions: MapProperty<String, String>
    val nameMappings: MapProperty<String, String>
    val parameterNameMappings: MapProperty<String, String>
    val modelNameMappings: MapProperty<String, String>
    val enumNameMappings: MapProperty<String, String>
    val operationIdNameMappings: MapProperty<String, String>
    val openapiNormalizer: MapProperty<String, String>
    val typeMappings: MapProperty<String, String>
    val additionalProperties: MapProperty<String, Any>
    val serverVariables: MapProperty<String, String>
    val reservedWordsMappings: MapProperty<String, String>
    val configOptions: MapProperty<String, String>

    val languageSpecificPrimitives: ListProperty<String>
    val openapiGeneratorIgnoreList: ListProperty<String>

    val supportingFilesConstrainedTo: ListProperty<String>
    val modelFilesConstrainedTo: ListProperty<String>
    val apiFilesConstrainedTo: ListProperty<String>
    val generateModelTests: Property<Boolean>
    val generateModelDocumentation: Property<Boolean>
    val generateApiTests: Property<Boolean>
    val generateApiDocumentation: Property<Boolean>
}

// =========================================================================================
// 2. WORKER API ACTION
// Runs generation in dry-run mode; fails if changes are detected.
// =========================================================================================
abstract class ChangesWorkAction : WorkAction<ChangesWorkParameters> {

    private val logger = Logging.getLogger(ChangesWorkAction::class.java)

    override fun execute() {
        val params = parameters

        val configurator = if (params.configFile.isPresent) {
            CodegenConfigurator.fromFile(params.configFile.get().asFile.absolutePath)
        } else {
            CodegenConfigurator()
        }

        try {
            if (params.supportingFilesConstrainedTo.orNull?.isNotEmpty() == true) {
                GlobalSettings.setProperty(CodegenConstants.SUPPORTING_FILES, params.supportingFilesConstrainedTo.get().joinToString(","))
            } else {
                GlobalSettings.clearProperty(CodegenConstants.SUPPORTING_FILES)
            }
            if (params.modelFilesConstrainedTo.orNull?.isNotEmpty() == true) {
                GlobalSettings.setProperty(CodegenConstants.MODELS, params.modelFilesConstrainedTo.get().joinToString(","))
            } else {
                GlobalSettings.clearProperty(CodegenConstants.MODELS)
            }
            if (params.apiFilesConstrainedTo.orNull?.isNotEmpty() == true) {
                GlobalSettings.setProperty(CodegenConstants.APIS, params.apiFilesConstrainedTo.get().joinToString(","))
            } else {
                GlobalSettings.clearProperty(CodegenConstants.APIS)
            }

            params.generateApiDocumentation.orNull?.let { GlobalSettings.setProperty(CodegenConstants.API_DOCS, it.toString()) }
            params.generateModelDocumentation.orNull?.let { GlobalSettings.setProperty(CodegenConstants.MODEL_DOCS, it.toString()) }
            params.generateModelTests.orNull?.let { GlobalSettings.setProperty(CodegenConstants.MODEL_TESTS, it.toString()) }
            params.generateApiTests.orNull?.let { GlobalSettings.setProperty(CodegenConstants.API_TESTS, it.toString()) }

            params.resolvedInputSpec.orNull?.let { configurator.setInputSpec(it) }
            params.outputDir.orNull?.let { configurator.setOutputDir(it.asFile.absolutePath) }
            params.verbose.orNull?.let { configurator.setVerbose(it) }
            params.validateSpec.orNull?.let { configurator.setValidateSpec(it) }
            params.generatorName.orNull?.let { configurator.setGeneratorName(it) }
            params.auth.orNull?.let { configurator.setAuth(it) }

            params.templateDir.orNull?.let { configurator.setTemplateDir(it.asFile.absolutePath) }
            params.templateResourcePath.orNull?.let {
                if (params.templateDir.isPresent) logger.warn("Both templateDir and templateResourcePath were configured. templateResourcePath overwrites templateDir.")
                configurator.setTemplateDir(it)
            }

            params.packageName.orNull?.let { configurator.setPackageName(it) }
            params.apiPackage.orNull?.let { configurator.setApiPackage(it) }
            params.modelPackage.orNull?.let { configurator.setModelPackage(it) }
            params.modelNamePrefix.orNull?.let { configurator.setModelNamePrefix(it) }
            params.modelNameSuffix.orNull?.let { configurator.setModelNameSuffix(it) }
            params.apiNameSuffix.orNull?.let { configurator.setApiNameSuffix(it) }
            params.invokerPackage.orNull?.let { configurator.setInvokerPackage(it) }
            params.groupId.orNull?.let { configurator.setGroupId(it) }
            params.id.orNull?.let { configurator.setArtifactId(it) }
            params.version.orNull?.let { configurator.setArtifactVersion(it) }
            params.library.orNull?.let { configurator.setLibrary(it) }
            params.gitHost.orNull?.let { configurator.setGitHost(it) }
            params.gitUserId.orNull?.let { configurator.setGitUserId(it) }
            params.gitRepoId.orNull?.let { configurator.setGitRepoId(it) }
            params.releaseNote.orNull?.let { configurator.setReleaseNote(it) }
            params.httpUserAgent.orNull?.let { configurator.setHttpUserAgent(it) }
            params.ignoreFileOverride.orNull?.let { configurator.setIgnoreFileOverride(it.asFile.absolutePath) }
            params.removeOperationIdPrefix.orNull?.let { configurator.setRemoveOperationIdPrefix(it) }
            params.skipOperationExample.orNull?.let { configurator.setSkipOperationExample(it) }
            params.logToStderr.orNull?.let { configurator.setLogToStderr(it) }
            params.enablePostProcessFile.orNull?.let { configurator.setEnablePostProcessFile(it) }
            params.skipValidateSpec.orNull?.let { configurator.setValidateSpec(!it) }
            params.generateAliasAsModel.orNull?.let { configurator.setGenerateAliasAsModel(it) }

            params.engine.orNull?.let {
                if ("handlebars".equals(it, ignoreCase = true)) configurator.setTemplatingEngineName("handlebars")
                else configurator.setTemplatingEngineName(it)
            }

            params.globalProperties.orNull?.forEach { (k, v) -> configurator.addGlobalProperty(k, v) }
            params.instantiationTypes.orNull?.forEach { (k, v) -> configurator.addInstantiationType(k, v) }
            params.importMappings.orNull?.forEach { (k, v) -> configurator.addImportMapping(k, v) }
            params.schemaMappings.orNull?.forEach { (k, v) -> configurator.addSchemaMapping(k, v) }
            params.inlineSchemaNameMappings.orNull?.forEach { (k, v) -> configurator.addInlineSchemaNameMapping(k, v) }
            params.inlineSchemaOptions.orNull?.forEach { (k, v) -> configurator.addInlineSchemaOption(k, v) }
            params.nameMappings.orNull?.forEach { (k, v) -> configurator.addNameMapping(k, v) }
            params.parameterNameMappings.orNull?.forEach { (k, v) -> configurator.addParameterNameMapping(k, v) }
            params.modelNameMappings.orNull?.forEach { (k, v) -> configurator.addModelNameMapping(k, v) }
            params.enumNameMappings.orNull?.forEach { (k, v) -> configurator.addEnumNameMapping(k, v) }
            params.operationIdNameMappings.orNull?.forEach { (k, v) -> configurator.addOperationIdNameMapping(k, v) }
            params.openapiNormalizer.orNull?.forEach { (k, v) -> configurator.addOpenapiNormalizer(k, v) }
            params.typeMappings.orNull?.forEach { (k, v) -> configurator.addTypeMapping(k, v) }
            params.additionalProperties.orNull?.forEach { (k, v) -> configurator.addAdditionalProperty(k, v) }
            params.serverVariables.orNull?.forEach { (k, v) -> configurator.addServerVariable(k, v) }
            params.reservedWordsMappings.orNull?.forEach { (k, v) -> configurator.addAdditionalReservedWordMapping(k, v) }

            params.languageSpecificPrimitives.orNull?.forEach { configurator.addLanguageSpecificPrimitive(it) }
            params.openapiGeneratorIgnoreList.orNull?.forEach { configurator.addOpenapiGeneratorIgnoreList(it) }

            val clientOptInput = configurator.toClientOptInput()
            val codegenConfig = clientOptInput.config

            params.configOptions.orNull?.let { userOptions ->
                codegenConfig.cliOptions().forEach {
                    if (userOptions.containsKey(it.opt)) {
                        clientOptInput.config.additionalProperties()[it.opt] = userOptions[it.opt]
                    }
                }
            }

            // Always use dryRun=true to detect changes without modifying files
            val codegenName = params.codegenName.getOrElse("default")
            val selectedCodegen = selectCodegen(codegenName)
            if (selectedCodegen != null) {
                val generateMetadata = params.generateMetadata.getOrElse(true)
                selectedCodegen.setGenerateMetadata(generateMetadata)
                selectedCodegen.opts(clientOptInput).generate()

                if (selectedCodegen.hasChanges()) {
                    throw GradleException("There were changes to the generated code.")
                } else {
                    logger.lifecycle("There were no changes to the generated code.")
                }
            } else {
                throw GradleException("The supplied codegen name or class does not implement org.openapitools.codegen.Generator.")
            }

        } catch (e: GradleException) {
            throw e
        } catch (e: Exception) {
            val errorMessage = e.message ?: e.javaClass.simpleName
            logger.error("OpenAPI changes check failed: $errorMessage", e)
            throw GradleException("OpenAPI changes check failed: $errorMessage", e)
        } finally {
            GlobalSettings.reset()
        }
    }

    private fun selectCodegen(codegenName: String): Generator? {
        // Always use dryRun=true so the TemplateManager accurately tracks file state without writing
        var genName = codegenName

        if (genName != "default" && !genName.contains(".")) {
            try {
                val availableCodegens = ServiceLoader.load(Generator::class.java)
                availableCodegens.forEach { item: Generator ->
                    if (item.name.equals(genName)) {
                        // since ServiceLoader does not let us use constructors with parameters
                        // we just collect the right name and then instantiate the class ourselves
                        genName = item::class.java.name
                    }
                }
            } catch (e: ServiceConfigurationError) {
                throw GradleException("Could not load codegen {$genName} via SPI.", e)
            }
        }

        if (genName.contains(".")) {
            try {
                val codegenInst = Class.forName(genName)
                    .getDeclaredConstructor(Boolean::class.javaObjectType)
                    .newInstance(true)
                return if (codegenInst is Generator) {
                    codegenInst
                } else {
                    null
                }
            } catch (e: ClassNotFoundException) {
                throw GradleException("Selected codegen class {$genName} could not be found.", e)
            } catch (e: NoSuchMethodException) {
                throw GradleException(
                    "Selected codegen class {$genName} does not have a suitable constructor. "
                        + "Have you selected the correct class?",
                    e
                )
            }
        }

        return DefaultGenerator(true)
    }
}

/**
 * A task which checks if code generation from an Open API 2.0 or 3.x specification
 * would change any existing generated files. Fails the build if changes are detected.
 *
 * Generation is run in dry-run mode so no files are written.
 *
 * Example (CLI):
 *
 * ./gradlew -q openApiChanges --input=/path/to/file
 */
@CacheableTask
abstract class ChangesTask : DefaultTask() {

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @get:Inject
    abstract val layout: ProjectLayout

    @get:Optional
    @get:Input
    abstract val verbose: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val validateSpec: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val generatorName: Property<String>

    /**
     * The output directory to check against. Treated as an input because this task
     * compares against existing files without writing new ones.
     */
    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val outputDir: DirectoryProperty

    @Suppress("unused")
    @Option(option = "input", description = "The input specification (local path or URL/URI).")
    fun setInput(value: String) {
        if (value.isNotEmpty()) {
            if (value.isRemoteUri()) {
                remoteInputSpec.set(value)
            } else {
                inputSpec.set(layout.projectDirectory.file(value))
            }
        }
    }

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputSpec: RegularFileProperty

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputSpecRootDirectory: DirectoryProperty

    @get:Input
    @get:Optional
    abstract val inputSpecRootDirectorySkipMerge: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val mergedFileName: Property<String>

    @get:Input
    @get:Optional
    abstract val remoteInputSpec: Property<String>

    @get:Optional
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val templateDir: DirectoryProperty

    @get:Optional
    @get:Input
    abstract val templateResourcePath: Property<String>

    @get:Optional
    @get:Input
    abstract val auth: Property<String>

    @get:Optional
    @get:Input
    abstract val globalProperties: MapProperty<String, String>

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val configFile: RegularFileProperty

    @get:Optional
    @get:Input
    abstract val packageName: Property<String>

    @get:Optional
    @get:Input
    abstract val apiPackage: Property<String>

    @get:Optional
    @get:Input
    abstract val modelPackage: Property<String>

    @get:Optional
    @get:Input
    abstract val modelNamePrefix: Property<String>

    @get:Optional
    @get:Input
    abstract val modelNameSuffix: Property<String>

    @get:Optional
    @get:Input
    abstract val apiNameSuffix: Property<String>

    @get:Optional
    @get:Input
    abstract val instantiationTypes: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val typeMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val additionalProperties: MapProperty<String, Any>

    @get:Optional
    @get:Input
    abstract val serverVariables: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val languageSpecificPrimitives: ListProperty<String>

    @get:Optional
    @get:Input
    abstract val openapiGeneratorIgnoreList: ListProperty<String>

    @get:Optional
    @get:Input
    abstract val importMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val schemaMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val inlineSchemaNameMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val inlineSchemaOptions: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val nameMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val parameterNameMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val modelNameMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val enumNameMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val operationIdNameMappings: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val openapiNormalizer: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val invokerPackage: Property<String>

    @get:Optional
    @get:Input
    abstract val groupId: Property<String>

    @get:Optional
    @get:Input
    abstract val id: Property<String>

    @get:Optional
    @get:Input
    abstract val version: Property<String>

    @get:Optional
    @get:Input
    abstract val library: Property<String>

    @get:Optional
    @get:Input
    abstract val gitHost: Property<String>

    @get:Optional
    @get:Input
    abstract val gitUserId: Property<String>

    @get:Optional
    @get:Input
    abstract val gitRepoId: Property<String>

    @get:Optional
    @get:Input
    abstract val releaseNote: Property<String>

    @get:Optional
    @get:Input
    abstract val httpUserAgent: Property<String>

    @get:Optional
    @get:Input
    abstract val reservedWordsMappings: MapProperty<String, String>

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ignoreFileOverride: RegularFileProperty

    @get:Optional
    @get:Input
    abstract val removeOperationIdPrefix: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val skipOperationExample: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val apiFilesConstrainedTo: ListProperty<String>

    @get:Optional
    @get:Input
    abstract val modelFilesConstrainedTo: ListProperty<String>

    @get:Optional
    @get:Input
    abstract val supportingFilesConstrainedTo: ListProperty<String>

    @get:Optional
    @get:Input
    abstract val generateModelTests: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val generateModelDocumentation: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val generateApiTests: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val generateApiDocumentation: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val logToStderr: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val enablePostProcessFile: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val skipValidateSpec: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val generateAliasAsModel: Property<Boolean>

    @get:Optional
    @get:Input
    abstract val configOptions: MapProperty<String, String>

    @get:Optional
    @get:Input
    abstract val engine: Property<String>

    @get:Optional
    @get:Input
    abstract val codegenName: Property<String>

    @get:Optional
    @get:Input
    abstract val generateMetadata: Property<Boolean>

    init {
        inputSpecRootDirectorySkipMerge.convention(false)
        mergedFileName.convention("merged")
    }

    @Suppress("unused")
    @TaskAction
    fun doWork() {
        var finalResolvedInputSpec = ""

        if (inputSpec.isPresent && remoteInputSpec.isPresent) {
            logger.warn("Both inputSpec and remoteInputSpec are specified. The remoteInputSpec takes priority.")
        }

        inputSpec.orNull?.let { finalResolvedInputSpec = it.asFile.absolutePath }

        remoteInputSpec.orNull?.takeIf { it.isNotEmpty() }?.let {
            finalResolvedInputSpec = it
            logger.warn("Using remoteInputSpec may result in stale build caches if the remote content changes.")
        }

        inputSpecRootDirectory.orNull?.let { inputDir ->
            if (!inputSpecRootDirectorySkipMerge.get()) {
                finalResolvedInputSpec = MergedSpecBuilder(
                    inputDir.asFile.absolutePath,
                    mergedFileName.get()
                ).buildMergedSpec()
                logger.info("Merge input spec used: {}", finalResolvedInputSpec)
            }
        }

        val workQueue = workerExecutor.classLoaderIsolation()

        workQueue.submit(ChangesWorkAction::class.java, object : Action<ChangesWorkParameters> {
            override fun execute(parameters: ChangesWorkParameters) {
                parameters.resolvedInputSpec.set(finalResolvedInputSpec)
                parameters.outputDir.set(outputDir)
                parameters.configFile.set(configFile)
                parameters.verbose.set(verbose)
                parameters.validateSpec.set(validateSpec)
                parameters.generatorName.set(generatorName)
                parameters.auth.set(auth)
                parameters.templateDir.set(templateDir)
                parameters.templateResourcePath.set(templateResourcePath)
                parameters.packageName.set(packageName)
                parameters.apiPackage.set(apiPackage)
                parameters.modelPackage.set(modelPackage)
                parameters.modelNamePrefix.set(modelNamePrefix)
                parameters.modelNameSuffix.set(modelNameSuffix)
                parameters.apiNameSuffix.set(apiNameSuffix)
                parameters.invokerPackage.set(invokerPackage)
                parameters.groupId.set(groupId)
                parameters.id.set(id)
                parameters.version.set(version)
                parameters.library.set(library)
                parameters.gitHost.set(gitHost)
                parameters.gitUserId.set(gitUserId)
                parameters.gitRepoId.set(gitRepoId)
                parameters.releaseNote.set(releaseNote)
                parameters.httpUserAgent.set(httpUserAgent)
                parameters.ignoreFileOverride.set(ignoreFileOverride)
                parameters.removeOperationIdPrefix.set(removeOperationIdPrefix)
                parameters.skipOperationExample.set(skipOperationExample)
                parameters.logToStderr.set(logToStderr)
                parameters.enablePostProcessFile.set(enablePostProcessFile)
                parameters.skipValidateSpec.set(skipValidateSpec)
                parameters.generateAliasAsModel.set(generateAliasAsModel)
                parameters.engine.set(engine)
                parameters.codegenName.set(codegenName)
                parameters.generateMetadata.set(generateMetadata)

                parameters.globalProperties.set(globalProperties)
                parameters.instantiationTypes.set(instantiationTypes)
                parameters.importMappings.set(importMappings)
                parameters.schemaMappings.set(schemaMappings)
                parameters.inlineSchemaNameMappings.set(inlineSchemaNameMappings)
                parameters.inlineSchemaOptions.set(inlineSchemaOptions)
                parameters.nameMappings.set(nameMappings)
                parameters.parameterNameMappings.set(parameterNameMappings)
                parameters.modelNameMappings.set(modelNameMappings)
                parameters.enumNameMappings.set(enumNameMappings)
                parameters.operationIdNameMappings.set(operationIdNameMappings)
                parameters.openapiNormalizer.set(openapiNormalizer)
                parameters.typeMappings.set(typeMappings)
                parameters.additionalProperties.set(additionalProperties)
                parameters.serverVariables.set(serverVariables)
                parameters.reservedWordsMappings.set(reservedWordsMappings)
                parameters.configOptions.set(configOptions)

                parameters.languageSpecificPrimitives.set(languageSpecificPrimitives)
                parameters.openapiGeneratorIgnoreList.set(openapiGeneratorIgnoreList)
                parameters.supportingFilesConstrainedTo.set(supportingFilesConstrainedTo)
                parameters.modelFilesConstrainedTo.set(modelFilesConstrainedTo)
                parameters.apiFilesConstrainedTo.set(apiFilesConstrainedTo)
                parameters.generateModelTests.set(generateModelTests)
                parameters.generateModelDocumentation.set(generateModelDocumentation)
                parameters.generateApiTests.set(generateApiTests)
                parameters.generateApiDocumentation.set(generateApiDocumentation)
            }
        })
    }

    // ========================================================================
    // Kotlin DSL extension functions
    // ========================================================================

    fun RegularFileProperty.set(path: String) {
        when (this) {
            inputSpec -> {
                if (path.isRemoteUri()) {
                    remoteInputSpec.set(path)
                } else {
                    this.set(layout.projectDirectory.file(path))
                }
            }
            else -> this.set(layout.projectDirectory.file(path))
        }
    }

    fun DirectoryProperty.set(path: String) {
        this.set(layout.projectDirectory.dir(path))
    }

    // ========================================================================
    // Groovy DSL bridge methods
    // ========================================================================

    fun setInputSpecAsString(path: String) {
        if (path.isRemoteUri()) {
            remoteInputSpec.set(path)
            inputSpec.set(null as File?)
        } else {
            inputSpec.set(layout.projectDirectory.file(path))
            remoteInputSpec.set(null as String?)
        }
    }

    fun setConfigFileAsString(path: String) { configFile.set(layout.projectDirectory.file(path)) }
    fun setIgnoreFileOverrideAsString(path: String) { ignoreFileOverride.set(layout.projectDirectory.file(path)) }
    fun setTemplateDirAsString(path: String) { templateDir.set(layout.projectDirectory.dir(path)) }
    fun setOutputDirAsString(path: String) { outputDir.set(layout.projectDirectory.dir(path)) }
    fun setInputSpecRootDirectoryAsString(path: String) { inputSpecRootDirectory.set(layout.projectDirectory.dir(path)) }
}

