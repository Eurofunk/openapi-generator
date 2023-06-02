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

import javax.inject.Inject
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.property
import org.gradle.util.GradleVersion
import org.openapitools.codegen.config.CodegenConfigurator
import org.openapitools.codegen.config.GlobalSettings
import org.openapitools.codegen.config.MergedSpecBuilder

/**
 * A task which generates the desired code.
 *
 * Example (CLI):
 *
 * ./gradlew -q openApiGenerate --input=/path/to/file
 *
 * @author Jim Schubert
 */
@Suppress("UnstableApiUsage")
@CacheableTask
open class GenerateTask @Inject constructor(private val objectFactory: ObjectFactory) : CommonGenerateCheckTask() {
    /**
     * Specifies if the existing files should be overwritten during the generation.
     */
    @Optional
    @Input
    val skipOverwrite = project.objects.property<Boolean?>()

    /**
     * Defines whether the output dir should be cleaned up before generating the output.
     *
     */
    @Optional
    @Input
    val cleanupOutput = project.objects.property<Boolean>()

    /**
     * Defines whether the generator should run in dry-run mode.
     */
    @Optional
    @Input
    val dryRun = project.objects.property<Boolean>()

    private fun createFileSystemManager(): FileSystemManager {
        return if(GradleVersion.current() >= GradleVersion.version("6.0")) {
            objectFactory.newInstance(FileSystemManagerDefault::class.java)
        } else {
            objectFactory.newInstance(FileSystemManagerLegacy::class.java, project)
        }
    }

    @Suppress("unused")
    @TaskAction
   open fun doWork() {
        cleanupOutput.ifNotEmpty { cleanup ->
            if (cleanup) {
                createFileSystemManager().delete(outputDir)
                val out = services.get(StyledTextOutputFactory::class.java).create("openapi")
                out.withStyle(StyledTextOutput.Style.Success)
                out.println("Cleaned up output directory ${outputDir.get()} before code generation (cleanupOutput set to true).")
            }
        }

        val configurator: CodegenConfigurator = if (configFile.isPresent) {
            CodegenConfigurator.fromFile(configFile.get())
        } else createDefaultCodegenConfigurator()

        try {
            processConfig(configurator)

            skipOverwrite.ifNotEmpty { value ->
                configurator.setSkipOverwrite(value ?: false)
            }
            var generateMetadataSetting = true;
            generateMetadata.ifNotEmpty { setting ->
                generateMetadataSetting = setting
            }
            var dryRunSetting = false
            dryRun.ifNotEmpty { setting ->
                dryRunSetting = setting
            }

            val clientOptInput = configurator.toClientOptInput()
            processAdditionalProperties(clientOptInput)

            try {
                val out = services.get(StyledTextOutputFactory::class.java).create("openapi")
                out.withStyle(StyledTextOutput.Style.Success)

                val selectedCodegen = selectCodegen(dryRunSetting)
                if (selectedCodegen != null) {
                    selectedCodegen.setGenerateMetadata(generateMetadataSetting)

                    selectedCodegen.opts(clientOptInput).generate()
                    out.println("Successfully generated code to ${outputDir.get()}")
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

internal interface FileSystemManager {

    fun delete(outputDir: Property<String>)

}

internal open class FileSystemManagerLegacy @Inject constructor(private val project: Project): FileSystemManager {

    override fun delete(outputDir: Property<String>) {
        project.delete(outputDir)
    }
}

internal open class FileSystemManagerDefault @Inject constructor(private val fs: FileSystemOperations) : FileSystemManager {

    override fun delete(outputDir: Property<String>) {
        fs.delete { delete(outputDir) }
    }
}
