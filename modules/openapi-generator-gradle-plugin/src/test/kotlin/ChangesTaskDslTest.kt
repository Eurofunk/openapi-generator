package org.openapitools.generator.gradle.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.testng.annotations.Test
import java.io.File
import java.nio.file.Files.createDirectory
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChangesTaskDslTest : TestBase() {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun buildGradleWithChanges(
        outputDir: String = "build/changes-output",
        extraConfig: String = ""
    ) = """
        plugins {
          id 'org.openapi.generator'
        }
        openApiChanges {
            generatorName = "kotlin"
            inputSpec     = file("spec.yaml").absolutePath
            outputDir     = file("$outputDir").absolutePath
            apiPackage    = "org.openapitools.example.api"
            invokerPackage = "org.openapitools.example.invoker"
            modelPackage  = "org.openapitools.example.model"
            configOptions = [ dateLibrary: "java8" ]
            $extraConfig
        }
    """.trimIndent()

    private fun buildGradleWithGenerate(outputDir: String = "build/changes-output") = """
        plugins {
          id 'org.openapi.generator'
        }
        openApiGenerate {
            generatorName  = "kotlin"
            inputSpec      = file("spec.yaml").absolutePath
            outputDir      = file("$outputDir").absolutePath
            apiPackage     = "org.openapitools.example.api"
            invokerPackage = "org.openapitools.example.invoker"
            modelPackage   = "org.openapitools.example.model"
            configOptions  = [ dateLibrary: "java8" ]
        }
    """.trimIndent()

    /** Recursively collect all file paths relative to [root], excluding Gradle's own cache dir. */
    private fun collectFiles(root: File): Set<String> =
        root.walkTopDown()
            .onEnter { it.name != ".gradle" }
            .filter { it.isFile }
            .map { it.relativeTo(root).path }
            .toHashSet()

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    fun `openApiChanges does not write any files when output directory is empty`() {
        // Arrange – spec present, output dir deliberately left empty
        val projectFiles = mapOf(
            "spec.yaml" to javaClass.classLoader.getResourceAsStream("specs/petstore-v3.0.yaml")!!
        )
        withProject(buildGradleWithChanges(), projectFiles)

        val outputDir = File(temp, "build/changes-output")
        outputDir.mkdirs()

        // Snapshot before – only build.gradle and spec.yaml exist
        val filesBefore = collectFiles(temp)

        // Act – the task must FAIL because every generated file would be new (Write state)
        val result = GradleRunner.create()
            .withProjectDir(temp)
            .withArguments("openApiChanges", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Assert – task reported detected changes
        assertTrue(
            result.output.contains("There were changes to the generated code"),
            "Expected changes-detected message in output, but got:\n${result.output}"
        )
        assertEquals(
            TaskOutcome.FAILED,
            result.task(":openApiChanges")?.outcome,
            "Expected task to fail when there are pending writes"
        )

        // Assert – no files were written to disk
        val filesAfter = collectFiles(temp)
        val newFiles = filesAfter - filesBefore

        if (newFiles.isNotEmpty()) {
            println("=== NEW FILES FOUND (should be empty) ===")
            newFiles.sorted().forEach { println("  $it") }
        }

        assertTrue(
            newFiles.isEmpty(),
            "openApiChanges wrote files to disk but should not have. New files found:\n${newFiles.joinToString("\n")}"
        )
    }

    @Test
    fun `openApiChanges does not write any files when spec changes would cause updates`() {
        // Arrange – first generate the output normally
        val projectFiles = mapOf(
            "spec.yaml" to javaClass.classLoader.getResourceAsStream("specs/petstore-v3.0.yaml")!!
        )
        withProject(buildGradleWithGenerate(), projectFiles)

        GradleRunner.create()
            .withProjectDir(temp)
            .withArguments("openApiGenerate")
            .withPluginClasspath()
            .build()

        // Tamper with a generated file so that it differs from what would be regenerated
        val tamperedFile = File(temp, "build/changes-output/README.md")
        assertTrue(tamperedFile.exists(), "Expected README.md to exist after generation")
        tamperedFile.writeText("# TAMPERED")

        // Switch build script to openApiChanges
        File(temp, "build.gradle").writeText(buildGradleWithChanges())

        val filesBefore = collectFiles(temp)

        // Act – task must FAIL; README.md would be Updated
        val result = GradleRunner.create()
            .withProjectDir(temp)
            .withArguments("openApiChanges", "--stacktrace")
            .withPluginClasspath()
            .buildAndFail()

        // Assert – changes detected
        assertTrue(
            result.output.contains("There were changes to the generated code"),
            "Expected changes-detected message in output, but got:\n${result.output}"
        )

        // Assert – tampered file still has our sentinel content (not overwritten)
        assertEquals(
            "# TAMPERED",
            tamperedFile.readText(),
            "openApiChanges overwrote a file it must not touch"
        )

        // Assert – no new files appeared on disk either
        val newFiles = collectFiles(temp) - filesBefore
        assertTrue(
            newFiles.isEmpty(),
            "openApiChanges created new files on disk but should not have:\n${newFiles.joinToString("\n")}"
        )
    }

    @Test
    fun `openApiChanges succeeds and writes nothing when output is already up to date`() {
        // Arrange – generate first so files are on disk and up to date
        val projectFiles = mapOf(
            "spec.yaml" to javaClass.classLoader.getResourceAsStream("specs/petstore-v3.0.yaml")!!
        )
        withProject(buildGradleWithGenerate(), projectFiles)

        GradleRunner.create()
            .withProjectDir(temp)
            .withArguments("openApiGenerate")
            .withPluginClasspath()
            .build()

        // Switch to openApiChanges build script
        File(temp, "build.gradle").writeText(buildGradleWithChanges())

        val filesBefore = collectFiles(temp)
        val modifiedTimesBefore = collectFiles(temp)
            .associateWith { File(temp, it).lastModified() }

        // Act – task must SUCCEED; all files are Uptodate
        val result = GradleRunner.create()
            .withProjectDir(temp)
            .withArguments("openApiChanges", "--stacktrace")
            .withPluginClasspath()
            .build()

        // Assert – success path
        assertTrue(
            result.output.contains("There were no changes to the generated code"),
            "Expected no-changes message in output, but got:\n${result.output}"
        )
        assertEquals(
            TaskOutcome.SUCCESS,
            result.task(":openApiChanges")?.outcome,
            "Expected task to succeed when output is already up to date"
        )

        // Assert – no new files, no files modified
        val filesAfter = collectFiles(temp)
        val newFiles = filesAfter - filesBefore

        val modifiedTimesAfter = filesAfter.associateWith { File(temp, it).lastModified() }
        val modifiedFiles = modifiedTimesAfter.entries
            .filter { (path, ts) -> modifiedTimesBefore[path] != null && modifiedTimesBefore[path] != ts }
            .map { it.key }

        if (newFiles.isNotEmpty()) {
            println("=== NEW FILES FOUND in up-to-date test (should be empty) ===")
            newFiles.sorted().forEach { println("  $it") }
        }
        if (modifiedFiles.isNotEmpty()) {
            println("=== MODIFIED FILES FOUND (should be empty) ===")
            modifiedFiles.sorted().forEach { println("  $it") }
        }

        assertTrue(
            newFiles.isEmpty(),
            "openApiChanges created new files but should not have:\n${newFiles.joinToString("\n")}"
        )
        assertTrue(
            modifiedFiles.isEmpty(),
            "openApiChanges modified files on disk but should not have:\n${modifiedFiles.joinToString("\n")}"
        )
    }

    @Test
    fun `openApiChanges task is registered and visible in task list`() {
        withProject(buildGradleWithChanges())

        val result = GradleRunner.create()
            .withProjectDir(temp)
            .withArguments("tasks", "--group=OpenAPI Tools")
            .withPluginClasspath()
            .build()

        assertTrue(
            result.output.contains("openApiChanges"),
            "Task 'openApiChanges' should appear in the 'OpenAPI Tools' task group"
        )
    }
}

