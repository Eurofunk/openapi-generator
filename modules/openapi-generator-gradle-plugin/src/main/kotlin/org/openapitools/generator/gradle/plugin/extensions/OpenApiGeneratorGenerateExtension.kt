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
import org.gradle.kotlin.dsl.property

/**
 * Gradle project level extension object definition for the `generate` task
 *
 * @author Jim Schubert
 */
open class OpenApiGeneratorGenerateExtension(project: Project) : OpenApiGeneratorCommonGenerateChangesExtension(project) {
    /**
     * Specifies if the existing files should be overwritten during the generation.
     */
    val skipOverwrite = project.objects.property<Boolean?>()

    /**
     * Defines whether the output dir should be cleaned up before generating the output.
     *
     */
    val cleanupOutput = project.objects.property<Boolean>()

    /**
     * Defines whether the generator should run in dry-run mode.
     */
    val dryRun = project.objects.property<Boolean>()

    init {
        cleanupOutput.set(false)
        dryRun.set(false)
    }
}
