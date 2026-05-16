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

        Pattern packagePattern = Pattern.compile("^\\s*package\\s+([\\w.]+)", Pattern.MULTILINE);

        Pattern javaFeaturePattern = Pattern.compile(
                "(?:public\\s+)?(?:abstract\\s+)?class\\s+([\\w$]+)\\s+extends\\s+(?:.*\\.)?(?:AbstractFeature|.*Feature)"
        );

        Pattern kotlinClassPattern = Pattern.compile(
                "(?:abstract\\s+)?class\\s+([\\w$]+)\\s*:\\s*(?:.*\\.)?(?:AbstractFeature|.*Feature)"
        );

        Pattern kotlinObjectPattern = Pattern.compile(
                "object\\s+([\\w$]+)\\s*:\\s*(?:.*\\.)?(?:AbstractFeature|.*Feature)"
        );

        for (File file : sourceFiles) {
            String code = Files.readString(file.toPath(), StandardCharsets.UTF_8);

            Matcher packageMatcher = packagePattern.matcher(code);

            if (!packageMatcher.find()) continue;

            String packageName = packageMatcher.group(1);

            if (!packageName.startsWith(ROOT_PACKAGE)) continue;
            if (packageName.startsWith(CORE_PACKAGE)) continue;

            boolean isKotlin = file.getName().endsWith(".kt");

            if (isKotlin) {
                Matcher objectMatcher = kotlinObjectPattern.matcher(code);

                while (objectMatcher.find()) {
                    String className = objectMatcher.group(1);

                    String fullName = packageName + "." + className;

                    featureClasses.add(fullName + "|object");
                }

                Matcher classMatcher = kotlinClassPattern.matcher(code);

                while (classMatcher.find()) {
                    String className = classMatcher.group(1);

                    if (code.contains("abstract class " + className)) {
                        if (!className.endsWith("Feature")) {
                            continue;
                        }
                    }

                    String fullName = packageName + "." + className;

                    featureClasses.add(fullName + "|class");
                }
            } else {
                Matcher classMatcher = javaFeaturePattern.matcher(code);

                while (classMatcher.find()) {
                    String className = classMatcher.group(1);

                    if (code.contains("abstract class " + className)) {
                        if (!className.endsWith("Feature")) {
                            continue;
                        }
                    }

                    String fullName = packageName + "." + className;

                    featureClasses.add(fullName + "|class");
                }
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