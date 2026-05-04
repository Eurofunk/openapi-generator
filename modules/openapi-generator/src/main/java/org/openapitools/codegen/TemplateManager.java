package org.openapitools.codegen;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.codegen.api.TemplatePathLocator;
import org.openapitools.codegen.api.TemplateProcessor;
import org.openapitools.codegen.api.TemplatingEngineAdapter;
import org.openapitools.codegen.api.TemplatingExecutor;
import org.openapitools.codegen.templating.TemplateManagerOptions;
import org.openapitools.codegen.templating.TemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Manages the lookup, compilation, and writing of template files
 */
public class TemplateManager implements TemplatingExecutor, TemplateProcessor {
    private final TemplateManagerOptions options;
    private final TemplatingEngineAdapter engineAdapter;
    private final TemplatePathLocator[] templateLoaders;
    private final Boolean dryRun;
    private final Map<String, DryRunStatus> fileStatusMap = new HashMap<>();

    private final Map<String, Map<String, Object>> capturedTemplateData = new HashMap<>();
    private boolean recordTemplateData = false;

    private final Logger LOGGER = LoggerFactory.getLogger(TemplateManager.class);

    /** Cache of resolved template path -> raw template content, populated on first read per run. */
    private final Map<String, String> templateContentCache = new ConcurrentHashMap<>();

    /**
     * Constructs a new instance of a {@link TemplateManager}
     *
     * @param options         The {@link TemplateManagerOptions} for reading and writing templates
     * @param engineAdapter   The adaptor to underlying templating engine
     * @param templateLoaders Loaders which define where we look for templates
     */
    public TemplateManager(
            TemplateManagerOptions options,
            TemplatingEngineAdapter engineAdapter,
            TemplatePathLocator[] templateLoaders) {
        this.options = options;
        this.engineAdapter = engineAdapter;
        this.templateLoaders = templateLoaders;
        this.dryRun = false;
    }

    /**
     * Constructs a new instance of a {@link TemplateManager}
     *
     * @param options         The {@link TemplateManagerOptions} for reading and writing templates
     * @param engineAdapter   The adaptor to underlying templating engine
     * @param templateLoaders Loaders which define where we look for templates
     * @param dryRun          Whether files should actually be written
     */
    public TemplateManager(
            TemplateManagerOptions options,
            TemplatingEngineAdapter engineAdapter,
            TemplatePathLocator[] templateLoaders,
            Boolean dryRun) {
        this.options = options;
        this.engineAdapter = engineAdapter;
        this.templateLoaders = templateLoaders;
        this.dryRun = Boolean.TRUE.equals(dryRun);
    }

    private String getFullTemplateFile(String name) {
        String template = Arrays.stream(this.templateLoaders)
                .map(i -> i.getFullTemplatePath(name))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");

        if (StringUtils.isEmpty(template)) {
            throw new TemplateNotFoundException(name);
        }

        if (name == null || name.contains("..")) {
            throw new IllegalArgumentException("Template location must be constrained to template directory.");
        }

        return template;
    }

    /**
     * returns the template content by name
     *
     * @param name the template name (e.g. model.mustache)
     * @return the contents of that template
     */
    @Override
    public String getFullTemplateContents(String name) {
        String fullPath = getFullTemplateFile(name);
        return templateContentCache.computeIfAbsent(fullPath, this::readTemplate);
    }

    /**
     * Returns the path of a template, allowing access to the template where consuming literal contents aren't desirable or possible.
     *
     * @param name the template name (e.g. model.mustache)
     * @return The {@link Path} to the template
     */
    @Override
    public Path getFullTemplatePath(String name) {
        return Paths.get(getFullTemplateFile(name));
    }

    /**
     * Pre-compiled pattern for replacing the OS file separator with '/' in classpath resource paths.
     * Only non-null on operating systems where {@link File#separator} is not already '/'.
     */
    private static final Pattern FILE_SEP_PATTERN =
            "/".equals(File.separator) ? null : Pattern.compile(Pattern.quote(File.separator));

    /**
     * Gets a normalized classpath resource location according to OS-specific file separator
     *
     * @param name The name of the resource file/directory to find
     * @return A normalized string according to OS-specific file separator
     */
    public static String getCPResourcePath(final String name) {
        if (FILE_SEP_PATTERN != null) {
            return FILE_SEP_PATTERN.matcher(name).replaceAll("/");
        }
        return name;
    }

    /**
     * Reads a template's contents from the specified location
     *
     * @param name The location of the template
     * @return The raw template contents
     */
    @SuppressWarnings("java:S112")
    // ignored rule java:S112 as RuntimeException is used to match previous exception type
    public String readTemplate(String name) {
        if (name == null || name.contains("..")) {
            throw new IllegalArgumentException("Template location must be constrained to template directory.");
        }
        try (Reader reader = getTemplateReader(name)) {
            if (reader == null) {
                throw new RuntimeException("no file found");
            }
            try (Scanner s = new Scanner(reader).useDelimiter("\\A")) {
                return s.hasNext() ? s.next() : "";
            }
        } catch (Exception e) {
            LOGGER.error("{}", e.getMessage(), e);
        }
        throw new RuntimeException("can't load template " + name);
    }

    @SuppressWarnings({"squid:S2095", "java:S112"})
    // ignored rule squid:S2095 as used in the CLI and it's required to return a reader
    // ignored rule java:S112 as RuntimeException is used to match previous exception type
    public Reader getTemplateReader(String name) {
        try {
            InputStream is = getInputStream(name);
            return new InputStreamReader(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException("can't load template " + name);
        }
    }

    private InputStream getInputStream(String name) throws IOException {
        if (name == null || name.contains("..")) {
            throw new IllegalArgumentException("Template location must be constrained to template directory.");
        }
        String cpResourcePath = getCPResourcePath(name);
        URL resource = this.getClass().getClassLoader().getResource(cpResourcePath);
        if (resource != null) {
            // Open a fresh, non-cached connection each time.
            // setUseCaches(false) prevents sharing the underlying JarFile across classloaders,
            // which avoids "Stream closed" errors when concurrent Gradle workers use isolated
            // classloaders that happen to point to the same JAR URL.
            URLConnection conn = resource.openConnection();
            conn.setUseCaches(false);
            return conn.getInputStream();
        }
        return new FileInputStream(name); // May throw but never return a null value
    }

    /**
     * Writes data to a compiled template
     *
     * @param data     Input data
     * @param template Input template location
     * @param target   The targeted file output location
     * @return The actual file
     */
    @Override
    public File write(Map<String, Object> data, String template, File target) throws IOException {
        if (recordTemplateData) {
            this.capturedTemplateData.put(target.getAbsolutePath(), data);
        }

        if (this.engineAdapter.handlesFile(template)) {
            // Only pass files with valid endings through template engine
            String templateContent = this.engineAdapter.compileTemplate(this, data, template);
            return writeToFile(target.getPath(), templateContent);
        } else {
            // Do a straight copy of the file if not listed as supported by the template engine.
            return writeToFile(target.getPath(), getFullTemplateContents(template));
        }
    }

    @Override
    public void ignore(Path path, String context) {
        fileStatusMap.put(path.toString(),
                new DryRunStatus(
                        path,
                        DryRunStatus.State.Ignored,
                        context
                ));
        if (!dryRun) {
            LOGGER.info("Ignored {} ({})", path, context);
        }
    }

    @Override
    public void skip(Path path, String context) {
        DryRunStatus status = new DryRunStatus(path, DryRunStatus.State.Skipped, context);
        if (this.options.isSkipOverwrite() && path.toFile().exists()) {
            status.setState(DryRunStatus.State.SkippedOverwrite);
        }
        fileStatusMap.put(path.toString(), status);

        if (!dryRun) {
            LOGGER.info("Skipped {} ({})", path, context);
        }
    }

    @Override
    public void error(Path path, String context) {
        fileStatusMap.put(path.toString(), new DryRunStatus(path, DryRunStatus.State.Error));
    }

    /**
     * Write String to a file, formatting as UTF-8
     *
     * @param filename The name of file to write
     * @param contents The contents string.
     * @return File representing the written file.
     * @throws IOException If file cannot be written.
     */
    public File writeToFile(String filename, String contents) throws IOException {
        // normalize line endings so that they do not depend on the line endings of the template file
        contents = contents.replace("\r\n", "\n");
        contents = contents.replace("\n", System.lineSeparator());
        return writeToFile(filename, contents.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Write bytes to a file
     *
     * @param filename The name of file to write
     * @param contents The contents bytes.  Typically, this is a UTF-8 formatted string.
     * @return File representing the written file.
     * @throws IOException If file cannot be written.
     */
    @Override
    public File writeToFile(String filename, byte[] contents) throws IOException {
        // Use Paths.get here to normalize path (for Windows file separator, space escaping on Linux/Mac, etc)
        File outputFile = Paths.get(filename).toFile();
        DryRunStatus status = new DryRunStatus(outputFile.toPath());
        File tempFile = null;
        String tempFilename = filename + ".tmp";

        try {
            tempFile = writeToFileRaw(tempFilename, contents);
            if (!filesEqual(tempFile, outputFile)) {
                if (outputFile.exists()) {
                    if (this.options.isSkipOverwrite()) {
                        status.setState(DryRunStatus.State.SkippedOverwrite);
                    } else {
                        status.setState(DryRunStatus.State.Updated);
                    }
                } else {
                    status.setState(DryRunStatus.State.Write);
                }
                if (!dryRun) {
                    if (this.options.isSkipOverwrite() && outputFile.exists()) {
                        LOGGER.info("skip overwrite of file {}", filename);
                    } else {
                        LOGGER.info("writing file {}", filename);
                        Files.move(tempFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        tempFile = null;
                    }
                }
            } else {
                status.setState(DryRunStatus.State.Uptodate);
                if (!dryRun) {
                    if (this.options.isMinimalUpdate()) {
                        LOGGER.info("skipping unchanged file {}", filename);
                    } else if (!this.options.isSkipOverwrite()) {
                        Files.move(tempFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        tempFile = null;
                    } else {
                        LOGGER.info("skip overwrite of file {}", filename);
                    }
                }
            }
        } catch (IOException e) {
            status.setState(DryRunStatus.State.Error);
            throw e;
        } finally {
            fileStatusMap.put(filename, status);
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                } catch (Exception ex) {
                    LOGGER.error("Error removing temporary file {}", tempFile, ex);
                }
            }
        }

        return outputFile;
    }

    /**
     * Enable capturing of data being passed to the files as they are being written.<br>
     * Call this method <b><u>before</u></b> calling {@link Generator#generate()}.
     */
    public TemplateManager enableTemplateDataCapturing() {
        recordTemplateData = true;
        return this;
    }

    /**
     * Retrieve the captured template data for a specific file. Capturing must have
     * been enabled via {@link #enableTemplateDataCapturing()} prior to generation.<br>
     * Note: Not all files have template data (e.g. Metadata files) – in such case an empty
     * map is returned.
     *
     * @param generatedFile An absolute path to the generated file
     * @return Typically one of the *Map types found in {@link org.openapitools.codegen.model}
     */
    public Map<String, Object> getCapturedTemplateData(Path generatedFile) {
        return capturedTemplateData.getOrDefault(generatedFile.toString(), Map.of());
    }

    /**
     * Gets the full status of this run.
     *
     * @return An immutable copy of the run status.
     */
    public Map<String, DryRunStatus> getFileStatusMap() {
        return Collections.unmodifiableMap(fileStatusMap);
    }

    private File writeToFileRaw(String filename, byte[] contents) throws IOException {
        // Use Paths.get here to normalize path (for Windows file separator, space escaping on Linux/Mac, etc)
        File output = Paths.get(filename).toFile();

        if (output.getParent() != null && !new File(output.getParent()).exists()) {
            File parent = Paths.get(output.getParent()).toFile();
            parent.mkdirs();
        }
        Files.write(output.toPath(), contents);

        return output;
    }

    private boolean filesEqual(File file1, File file2) throws IOException {
        if (!file1.exists() || !file2.exists()) return false;
        if (file1.length() != file2.length()) return false;
        return Arrays.equals(Files.readAllBytes(file1.toPath()), Files.readAllBytes(file2.toPath()));
    }
}
