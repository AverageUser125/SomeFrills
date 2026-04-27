package com.somefrills;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;

public abstract class FeaturesGenerateTask extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getInputFile();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generate() throws Exception {

        File input = getInputFile().getAsFile().get();

        if (!input.exists()) {
            throw new IllegalStateException("features-list.txt not found. Run scan task first.");
        }

        List<String> classes = Files.readAllLines(input.toPath());
        classes.sort(Comparator.naturalOrder());

        File outDir = getOutputDir().getAsFile().get();
        outDir.mkdirs();

        File outFile = new File(outDir, "FeaturesRegistry.java");

        StringBuilder sb = new StringBuilder();

        // PACKAGE
        sb.append("package com.somefrills.features.core;\n\n");

        sb.append("final class FeaturesRegistry {\n\n");

        // INSTANCES
        sb.append("    static final AbstractFeature[] INSTANCES = new AbstractFeature[")
                .append(classes.size()).append("];\n\n");

        // CLASS -> INSTANCE
        sb.append("    static final ClassValue<AbstractFeature> CLASS_TO_INSTANCE = new ClassValue<>() {\n");
        sb.append("        @Override\n");
        sb.append("        protected AbstractFeature computeValue(Class<?> type) {\n");

        for (int i = 0; i < classes.size(); i++) {
            String cls = classes.get(i);
            sb.append("            if (type == ").append(cls).append(".class) return INSTANCES[")
                    .append(i).append("];\n");
        }

        sb.append("            throw new IllegalStateException(\"Unknown feature: \" + type);\n");
        sb.append("        }\n");
        sb.append("    };\n\n");

        // INIT
        sb.append("    static void init() {\n");

        for (int i = 0; i < classes.size(); i++) {
            String cls = classes.get(i);
            sb.append("        INSTANCES[").append(i).append("] = new ")
                    .append(cls).append("();\n");
        }

        sb.append("    }\n");

        sb.append("}\n");

        Files.writeString(outFile.toPath(), sb.toString());
    }
}