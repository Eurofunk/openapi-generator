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

package org.openapitools.generator.gradle.plugin.extensions

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.openapitools.generator.gradle.plugin.utils.isRemoteUri
import java.io.File

/**
 * Gradle project level extension object definition for the `openApiChanges` task.
 *
 * This extension shares the same configuration options as [OpenApiGeneratorGenerateExtension]
 * minus the generate-only options (cleanupOutput, dryRun, skipOverwrite).
 */
open class OpenApiGeneratorChangesExtension(private val project: Project) {

    val verbose = project.objects.property<Boolean>()
    val validateSpec = project.objects.property<Boolean>()
    val generatorName = project.objects.property<String>()
    val codegenName = project.objects.property<String>()
    val outputDir: DirectoryProperty = project.objects.directoryProperty()
    val inputSpec: RegularFileProperty = project.objects.fileProperty()
    val inputSpecRootDirectory: DirectoryProperty = project.objects.directoryProperty()
    val inputSpecRootDirectorySkipMerge = project.objects.property<Boolean>()
    val remoteInputSpec = project.objects.property<String>()
    val templateDir: DirectoryProperty = project.objects.directoryProperty()
    val templateResourcePath = project.objects.property<String>()
    val auth = project.objects.property<String>()
    val globalProperties = project.objects.mapProperty<String, String>()
    val configFile: RegularFileProperty = project.objects.fileProperty()
    val packageName = project.objects.property<String>()
    val apiPackage = project.objects.property<String>()
    val modelPackage = project.objects.property<String>()
    val modelNamePrefix = project.objects.property<String>()
    val modelNameSuffix = project.objects.property<String>()
    val apiNameSuffix = project.objects.property<String>()
    val instantiationTypes = project.objects.mapProperty<String, String>()
    val typeMappings = project.objects.mapProperty<String, String>()
    val additionalProperties = project.objects.mapProperty<String, Any>()
    val serverVariables = project.objects.mapProperty<String, String>()
    val languageSpecificPrimitives = project.objects.listProperty<String>()
    val openapiGeneratorIgnoreList = project.objects.listProperty<String>()
    val importMappings = project.objects.mapProperty<String, String>()
    val schemaMappings = project.objects.mapProperty<String, String>()
    val inlineSchemaNameMappings = project.objects.mapProperty<String, String>()
    val inlineSchemaOptions = project.objects.mapProperty<String, String>()
    val nameMappings = project.objects.mapProperty<String, String>()
    val parameterNameMappings = project.objects.mapProperty<String, String>()
    val modelNameMappings = project.objects.mapProperty<String, String>()
    val enumNameMappings = project.objects.mapProperty<String, String>()
    val operationIdNameMappings = project.objects.mapProperty<String, String>()
    val openapiNormalizer = project.objects.mapProperty<String, String>()
    val invokerPackage = project.objects.property<String>()
    val groupId = project.objects.property<String>()
    val id = project.objects.property<String>()
    val version = project.objects.property<String>()
    val library = project.objects.property<String>()
    val gitHost = project.objects.property<String>()
    val gitUserId = project.objects.property<String>()
    val gitRepoId = project.objects.property<String>()
    val releaseNote = project.objects.property<String>()
    val httpUserAgent = project.objects.property<String>()
    val reservedWordsMappings = project.objects.mapProperty<String, String>()
    val ignoreFileOverride: RegularFileProperty = project.objects.fileProperty()
    val removeOperationIdPrefix = project.objects.property<Boolean>()
    val skipOperationExample = project.objects.property<Boolean>()
    val apiFilesConstrainedTo = project.objects.listProperty<String>()
    val modelFilesConstrainedTo = project.objects.listProperty<String>()
    val supportingFilesConstrainedTo = project.objects.listProperty<String>()
    val generateModelTests = project.objects.property<Boolean>()
    val generateModelDocumentation = project.objects.property<Boolean>()
    val generateApiTests = project.objects.property<Boolean>()
    val generateApiDocumentation = project.objects.property<Boolean>()
    val logToStderr = project.objects.property<Boolean>()
    val enablePostProcessFile = project.objects.property<Boolean>()
    val skipValidateSpec = project.objects.property<Boolean>()
    val generateAliasAsModel = project.objects.property<Boolean>()
    val configOptions = project.objects.mapProperty<String, String>()
    val engine = project.objects.property<String>()
    val generateMetadata = project.objects.property<Boolean>()

    init {
        applyDefaults()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun applyDefaults() {
        releaseNote.convention("Minor update")
        inputSpecRootDirectorySkipMerge.convention(false)
        modelNamePrefix.convention("")
        modelNameSuffix.convention("")
        apiNameSuffix.convention("")
        codegenName.convention("default")
        generateModelTests.convention(true)
        generateModelDocumentation.convention(true)
        generateApiTests.convention(true)
        generateApiDocumentation.convention(true)
        configOptions.convention(mapOf())
        validateSpec.convention(true)
        logToStderr.convention(false)
        enablePostProcessFile.convention(false)
        skipValidateSpec.convention(false)
        generateAliasAsModel.convention(false)
        generateMetadata.convention(true)
    }

    // ========================================================================
    // Backwards-compatibility bridge setters for Groovy DSL
    // ========================================================================

    fun setOutputDir(path: String) {
        outputDir.set(project.layout.projectDirectory.dir(path))
    }

    fun setInputSpec(path: String) {
        if (path.isRemoteUri()) {
            remoteInputSpec.set(path)
            inputSpec.set(null as File?)
        } else {
            inputSpec.set(project.layout.projectDirectory.file(path))
            remoteInputSpec.set(null as String?)
        }
    }

    fun setInputSpecRootDirectory(path: String) {
        inputSpecRootDirectory.set(project.layout.projectDirectory.dir(path))
    }

    fun setTemplateDir(path: String) {
        templateDir.set(project.layout.projectDirectory.dir(path))
    }

    fun setConfigFile(path: String) {
        configFile.set(project.layout.projectDirectory.file(path))
    }

    fun setIgnoreFileOverride(path: String) {
        ignoreFileOverride.set(project.layout.projectDirectory.file(path))
    }

    // ========================================================================
    // Kotlin DSL extension functions for property setters
    // ========================================================================

    fun RegularFileProperty.set(path: String) {
        if (this === inputSpec) {
            setInputSpec(path)
        } else if (this === configFile) {
            setConfigFile(path)
        } else if (this === ignoreFileOverride) {
            setIgnoreFileOverride(path)
        } else {
            this.set(project.layout.projectDirectory.file(path))
        }
    }

    fun DirectoryProperty.set(path: String) {
        if (this === outputDir) {
            setOutputDir(path)
        } else if (this === inputSpecRootDirectory) {
            setInputSpecRootDirectory(path)
        } else if (this === templateDir) {
            setTemplateDir(path)
        } else {
            this.set(project.layout.projectDirectory.dir(path))
        }
    }
}

