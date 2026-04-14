package com.somefrills.config.misc;


import io.github.notenoughupdates.moulconfig.common.IMinecraft;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.gui.GuiComponent;
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext;
import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent;
import io.github.notenoughupdates.moulconfig.gui.editors.ComponentEditor;
import io.github.notenoughupdates.moulconfig.internal.Warnings;
import io.github.notenoughupdates.moulconfig.observer.GetSetter;
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

// LABEL1 (editable, assume just default "label" with increment) - EDIT TEXT
// LABEL2 - EDIT TEXT
// LABEL3 - EDIT TEXT
// there is always a last one which is empty, and is created when the last one is filled in
// if some element is empty completely, it is removed (except the last one)
public class GuiOptionEditorEditTextList extends ComponentEditor {
    private final List<Pair<String, String>> pairs;
    private GuiComponent delegate;
    private String labelPrefix = "Label ";

    @SuppressWarnings("unchecked")
    public GuiOptionEditorEditTextList(ProcessedOption option, ConfigEditTextList annotation) {
        super(option);
        Object obj = option.get();
        if(option.getType() != List.class) {
            Warnings.warn("GuiOptionEditorEditTextList can only be used with List<Pair<String, String>> options");
        }
        //noinspection unchecked
        pairs = (List<Pair<String, String>>) obj;
        String annotationLabelPrefix = annotation.labelPrefix();
        if(annotationLabelPrefix != null && !annotationLabelPrefix.isEmpty()) {
            labelPrefix = annotationLabelPrefix;
        }
        initializePairs();
    }

    private void initializePairs() {
        // Ensure there's always an empty last entry
        if (pairs.isEmpty() || !pairs.getLast().getSecond().isEmpty()) {
            pairs.add(new Pair<>("", ""));
        }
    }

    private void saveChanges() {
        // Remove empty entries except the last one
        while (pairs.size() > 1 && pairs.get(pairs.size() - 2).getSecond().isEmpty()) {
            pairs.remove(pairs.size() - 2);
        }

        // Ensure there's always an empty last entry
        if (pairs.getLast().getSecond().isEmpty()) {
            option.explicitNotifyChange();
            return;
        }

        pairs.add(new Pair<>("", ""));
        option.explicitNotifyChange();
    }

    @Override
    public @NotNull GuiComponent getDelegate() {
        if (delegate == null) {
            delegate = wrapComponent(new GuiComponent() {
                private final Map<Integer, TextFieldComponent> textFields = new HashMap<>();

                @Override
                public int getWidth() {
                    return 0;
                }

                @Override
                public int getHeight() {
                    var fr = IMinecraft.INSTANCE.getDefaultFontRenderer();
                    int height = 5;
                    for (Pair<String, String> ignored : pairs) {
                        height += fr.getHeight() + 12;
                    }
                    return height;
                }

                private TextFieldComponent getOrCreateTextField(int index) {
                    return textFields.computeIfAbsent(index, i -> {
                        Pair<String, String> pair = pairs.get(i);
                        GetSetter<String> valueSetter = new GetSetter<>() {
                            @Override
                            public String get() {
                                return pairs.get(i).getSecond();
                            }

                            @Override
                            public void set(String value) {
                                pairs.set(i, new Pair<>(pair.getFirst(), value));
                                saveChanges();
                            }
                        };
                        return new TextFieldComponent(valueSetter, 150, GetSetter.constant(true), "", IMinecraft.INSTANCE.getDefaultFontRenderer(), new HashSet<>());
                    });
                }

                @Override
                public void render(@NotNull GuiImmediateContext context) {
                    var renderContext = context.getRenderContext();
                    var width = context.getWidth();
                    var fr = IMinecraft.INSTANCE.getDefaultFontRenderer();

                    int yOff = 0;
                    for (int i = 0; i < pairs.size(); i++) {
                        Pair<String, String> pair = pairs.get(i);
                        String label = pair.getFirst();
                        int itemHeight = fr.getHeight() + 12;

                        // Draw label
                        String displayLabel = label.isEmpty() ? labelPrefix + (i + 1) : label;
                        renderContext.drawString(fr, StructuredText.of(displayLabel), 5, yOff + 2, 0xffa0a0a0, true);

                        // Render text field
                        TextFieldComponent textField = getOrCreateTextField(i);
                        var fieldContext = context.translated(80, yOff + 2, Math.max(1, width - 85), 14);
                        textField.render(fieldContext);

                        yOff += itemHeight;
                    }
                }

                @Override
                public boolean mouseEvent(@NotNull io.github.notenoughupdates.moulconfig.gui.MouseEvent mouseEvent, @NotNull GuiImmediateContext context) {
                    var fr = IMinecraft.INSTANCE.getDefaultFontRenderer();
                    int yOff = 0;
                    for (int i = 0; i < pairs.size(); i++) {
                        int itemHeight = fr.getHeight() + 12;
                        var fieldContext = context.translated(80, yOff + 2, Math.max(1, context.getWidth() - 85), 14);
                        
                        TextFieldComponent textField = getOrCreateTextField(i);
                        if (textField.mouseEvent(mouseEvent, fieldContext)) {
                            return true;
                        }

                        yOff += itemHeight;
                    }
                    return super.mouseEvent(mouseEvent, context);
                }

                @Override
                public boolean keyboardEvent(@NotNull io.github.notenoughupdates.moulconfig.gui.KeyboardEvent event, @NotNull GuiImmediateContext context) {
                    for (TextFieldComponent textField : textFields.values()) {
                        if (textField.isFocused() && textField.keyboardEvent(event, context)) {
                            return true;
                        }
                    }
                    return super.keyboardEvent(event, context);
                }
            });
        }
        return delegate;
    }

    @Override
    public int getHeight() {
        var fr = IMinecraft.INSTANCE.getDefaultFontRenderer();
        int height = 5;
        for (Pair<String, String> ignored : pairs) {
            height += fr.getHeight() + 12;
        }
        return height;
    }
}
