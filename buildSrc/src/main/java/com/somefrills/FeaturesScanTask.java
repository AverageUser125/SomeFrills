package com.somefrills;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class FeaturesScanTask extends DefaultTask {

    private static final String ROOT_PACKAGE = "com.somefrills.features";
    private static final String CORE_PACKAGE = "com.somefrills.features.core";

    @InputDirectory
    public File getSourceDir() {
        return new File(getProject().getProjectDir(),
                "src/main/java/com/somefrills/features");
    }

    @OutputFile
    public File getOutputFile() {
        return new File(getProject().getLayout().getBuildDirectory().get().getAsFile(),
                "tmp/features-list.txt");
    }

    @TaskAction
    public void scan() throws Exception {
        File sourceRoot = getSourceDir();

        List<File> javaFiles = new ArrayList<>();
        collectJavaFiles(sourceRoot, javaFiles);

        Set<String> coreTypes = new HashSet<>();
        Set<String> featureClasses = new LinkedHashSet<>();

        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

        JavaParser parser = new JavaParser(config);

        // -----------------------------
        // PASS 1: collect CORE types
        // -----------------------------
        for (File file : javaFiles) {

            String code = Files.readString(file.toPath());

            CompilationUnit cu = parser.parse(code)
                    .getResult()
                    .orElseThrow(() -> new RuntimeException("Parse error"));

            String packageName = cu.getPackageDeclaration()
                    .map(NodeWithName::getNameAsString)
                    .orElse("");

            if (!packageName.equals(CORE_PACKAGE)) continue;

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
                if (cls.isInterface()) return;

                coreTypes.add(cls.getNameAsString());
            });
        }

        // -----------------------------
        // PASS 2: collect FEATURES
        // -----------------------------
        for (File file : javaFiles) {

            String code = Files.readString(file.toPath());

            CompilationUnit cu = parser.parse(code)
                    .getResult()
                    .orElseThrow(() -> new RuntimeException("Parse error"));

            String packageName = cu.getPackageDeclaration()
                    .map(NodeWithName::getNameAsString)
                    .orElse("");

            if (packageName.startsWith(CORE_PACKAGE)) continue;
            if (!packageName.startsWith(ROOT_PACKAGE)) continue;

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {

                if (cls.isInterface()) return;
                if (cls.isAbstract()) return;

                if (cls.getExtendedTypes().isEmpty()) return;

                String className = cls.getNameAsString();
                String fullName = packageName + "." + className;

                String parent = cls.getExtendedTypes(0).getNameAsString();

                // ✔ CORE RULE
                if (coreTypes.contains(parent)) {
                    featureClasses.add(fullName);
                }
            });
        }

        getLogger().lifecycle("Found features: " + featureClasses.size());

        File out = getOutputFile();
        out.getParentFile().mkdirs();
        Files.write(out.toPath(), featureClasses);
    }

    private void collectJavaFiles(File dir, List<File> out) {
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                collectJavaFiles(f, out);
            } else if (f.getName().endsWith(".java")) {
                out.add(f);
            }
        }
    }
}