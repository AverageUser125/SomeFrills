package com.somefrills;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public abstract class FeaturesScanTask extends DefaultTask {
    private static final String ROOT_PACKAGE = "com.somefrills.features";
    private static final String CORE_PACKAGE = "com.somefrills.features.core";
    @InputDirectory
    public abstract DirectoryProperty getSourceDir();
    @OutputFile
    public abstract RegularFileProperty getOutputFile();
    @TaskAction
    public void scan() throws Exception {
        File sourceRoot = getSourceDir().get().getAsFile();
        File outFile = getOutputFile().get().getAsFile();
        List<File> sourceFiles = new ArrayList<>();
        collectSourceFiles(sourceRoot, sourceFiles);
        Set<String> featureClasses = new LinkedHashSet<>();
        Set<String> baseFeatureClasses = new HashSet<>();
        // First pass: find all classes ending with "Feature" to know which are base feature types
        Pattern baseFeaturePattern = Pattern.compile(
            "class\\s+([\\w$]+)\\s+extends\\s+(?:.*\\.)?([\\w]+Feature)"
        );
        // Pattern to find abstract feature base classes and regular feature classes
        Pattern abstractFeaturePattern = Pattern.compile(
            "(?:public\\s+)?(?:abstract\\s+)?class\\s+([\\w$]+)\\s+extends\\s+(?:.*\\.)?(?:AbstractFeature|.*Feature)"
        );
        Pattern packagePattern = Pattern.compile("^\\s*package\\s+([\\w.]+)");
        for (File file : sourceFiles) {
            String code = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            // Extract package name
            Matcher packageMatcher = packagePattern.matcher(code);
            if (!packageMatcher.find()) continue;
            String packageName = packageMatcher.group(1);
            // Skip core package (those are base classes)
            if (packageName.startsWith(CORE_PACKAGE)) continue;
            if (!packageName.startsWith(ROOT_PACKAGE)) continue;
            // Skip abstract classes unless they end with Feature (those are intermediate base classes)
            if (code.contains("abstract class")) {
                Matcher abstractMatcher = abstractFeaturePattern.matcher(code);
                if (abstractMatcher.find()) {
                    String className = abstractMatcher.group(1);
                    // Only skip if it doesn't end with Feature (abstract intermediates like ChestUI shouldn't be in registry)
                    if (!className.endsWith("Feature")) {
                        continue;
                    }
                }
            }
            // Look for any class extending something ending with "Feature"
            Matcher extendsMatcher = abstractFeaturePattern.matcher(code);
            while (extendsMatcher.find()) {
                String className = extendsMatcher.group(1);
                String fullName = packageName + "." + className;
                featureClasses.add(fullName);
            }
        }
        getLogger().lifecycle("Found features: " + featureClasses.size());
        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }
        Files.write(outFile.toPath(), featureClasses);
    }
    private void collectSourceFiles(File dir, List<File> out) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectSourceFiles(f, out);
            } else if (f.getName().endsWith(".java") || f.getName().endsWith(".kt")) {
                out.add(f);
            }
        }
    }
}
