package com.somefrills;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class FeaturesGenerateTask extends DefaultTask {

    public record FeatureInfo(String className, boolean singleton) {}

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getInputFile();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generate() throws Exception {
        File input = getInputFile().getAsFile().get();

        if (!input.exists()) {
            throw new IllegalStateException(
                    "features-list.txt not found. Run scan task first."
            );
        }

        List<FeatureInfo> classes = new ArrayList<>();

        for (String line : Files.readAllLines(input.toPath())) {
            String[] split = line.split("\\|");

            classes.add(
                    new FeatureInfo(
                            split[0],
                            split[1].equals("object")
                    )
            );
        }

        classes.sort(Comparator.comparing(FeatureInfo::className));

        File outDir = getOutputDir().getAsFile().get();

        outDir.mkdirs();

        File outFile = new File(outDir, "FeaturesRegistry.java");

        StringBuilder sb = new StringBuilder();

        sb.append("package com.somefrills.features.core;\n\n");

        sb.append("/**\n");
        sb.append(" * Auto-generated registry of all feature classes.\n");
        sb.append(" * Do not edit manually.\n");
        sb.append(" */\n");

        sb.append("public final class FeaturesRegistry {\n\n");

        sb.append("    public static final AbstractFeature[] INSTANCES = new AbstractFeature[")
                .append(classes.size())
                .append("];\n\n");

        sb.append("    public static final ClassValue<AbstractFeature> CLASS_TO_INSTANCE = new ClassValue<>() {\n");

        sb.append("        @Override\n");
        sb.append("        protected AbstractFeature computeValue(Class<?> type) {\n");

        for (int i = 0; i < classes.size(); i++) {
            FeatureInfo info = classes.get(i);

            sb.append("            if (type == ")
                    .append(info.className())
                    .append(".class) return INSTANCES[")
                    .append(i)
                    .append("];\n");
        }

        sb.append("            throw new IllegalStateException(\"Unknown feature: \" + type);\n");
        sb.append("        }\n");
        sb.append("    };\n\n");

        sb.append("    public static void init() {\n");

        for (int i = 0; i < classes.size(); i++) {
            FeatureInfo info = classes.get(i);

            if (info.singleton()) {
                sb.append("        INSTANCES[")
                        .append(i)
                        .append("] = ")
                        .append(info.className())
                        .append(".INSTANCE;\n");
            } else {
                sb.append("        INSTANCES[")
                        .append(i)
                        .append("] = new ")
                        .append(info.className())
                        .append("();\n");
            }
        }

        sb.append("    }\n\n");

        sb.append("    private FeaturesRegistry() {}\n");

        sb.append("}\n");

        Files.writeString(
                outFile.toPath(),
                sb.toString(),
                StandardCharsets.UTF_8
        );
    }
}