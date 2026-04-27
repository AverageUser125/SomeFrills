package com.somefrills;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

        List<File> javaFiles = new ArrayList<>();
        collectJavaFiles(sourceRoot, javaFiles);
        Set<String> featureClasses = new LinkedHashSet<>();

        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

        JavaParser parser = new JavaParser(config);

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

                String className = cls.getNameAsString();
                String fullName = packageName + "." + className;

                cls.getExtendedTypes().stream()
                        .map(NodeWithSimpleName::getNameAsString)
                        .filter(n -> n.endsWith("Feature"))
                        .findAny().ifPresent(n -> featureClasses.add(fullName));
            });
        }

        getLogger().lifecycle("Found features: " + featureClasses.size());

        outFile.getParentFile().mkdirs();
        Files.write(outFile.toPath(), featureClasses);
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