package com.somefrills.config.misc;

import io.github.notenoughupdates.moulconfig.GuiTextures;
import io.github.notenoughupdates.moulconfig.common.IMinecraft;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.gui.GuiComponent;
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext;
import io.github.notenoughupdates.moulconfig.gui.component.ColorSelectComponent;
import io.github.notenoughupdates.moulconfig.gui.component.TextFieldComponent;
import io.github.notenoughupdates.moulconfig.gui.editors.ComponentEditor;
import io.github.notenoughupdates.moulconfig.internal.TypeUtils;
import io.github.notenoughupdates.moulconfig.internal.Warnings;
import io.github.notenoughupdates.moulconfig.observer.GetSetter;
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption;
import com.somefrills.config.misc.MobGlowConfig.RuleData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public class GuiOptionEditorRuleList extends ComponentEditor {
    private final List<RuleData> rules;
    private GuiComponent delegate;

    @SuppressWarnings("unchecked")
    public GuiOptionEditorRuleList(ProcessedOption option) {
        super(option);
        Object obj = option.get();
        if(!(obj instanceof List)) {
            throw new IllegalArgumentException("GuiOptionEditorRuleList can only be used with List<RuleData> options");
        }
        Class<?> elementType = TypeUtils.resolveRawType(((ParameterizedType) option.getType()).getActualTypeArguments()[0]);
        if(elementType != RuleData.class) {
            throw new IllegalArgumentException("GuiOptionEditorRuleList can only be used with List<RuleData> options");
        }
        //noinspection unchecked
        rules = (List<RuleData>) obj;
    }

    @Override
    public @NotNull GuiComponent getDelegate() {
        if (delegate == null) {
            delegate = wrapComponent(new GuiComponent() {
                private final Map<Integer, TextFieldComponent> idFields = new HashMap<>();
                private final Map<Integer, TextFieldComponent> expressionFields = new HashMap<>();

                @Override
                public int getWidth() {
                    return 0;
                }

                @Override
                public int getHeight() {
                    int height = 5;
                    for (int i = 0; i < rules.size(); i++) {
                        height += 50;
                    }
                    height += 25; // Add button
                    return height;
                }

                private TextFieldComponent getOrCreateIdField(int index) {
                    return idFields.computeIfAbsent(index, i -> {
                        GetSetter<String> idSetter = new GetSetter<String>() {
                            @Override
                            public String get() {
                                return rules.get(i).id;
                            }

                            @Override
                            public void set(String value) {
                                rules.get(i).id = value;
                                option.explicitNotifyChange();
                            }
                        };
                        return new TextFieldComponent(idSetter, 100, GetSetter.constant(true), "", IMinecraft.INSTANCE.getDefaultFontRenderer(), new HashSet<>());
                    });
                }

                private TextFieldComponent getOrCreateExpressionField(int index) {
                    return expressionFields.computeIfAbsent(index, i -> {
                        GetSetter<String> exprSetter = new GetSetter<String>() {
                            @Override
                            public String get() {
                                return rules.get(i).matcherExpression;
                            }

                            @Override
                            public void set(String value) {
                                rules.get(i).matcherExpression = value;
                                option.explicitNotifyChange();
                            }
                        };
                        return new TextFieldComponent(exprSetter, 150, GetSetter.constant(true), "", IMinecraft.INSTANCE.getDefaultFontRenderer(), new HashSet<>());
                    });
                }


                @Override
                public void render(@NotNull GuiImmediateContext context) {
                    var renderContext = context.getRenderContext();
                    var fr = IMinecraft.INSTANCE.getDefaultFontRenderer();

                    int yOff = 0;
                    for (int i = 0; i < rules.size(); i++) {
                        // Draw add button (small + in square) - LEFT
                        renderContext.drawColoredRect(5, yOff + 2, 18, yOff + 15, 0xff00aa00);
                        renderContext.drawColoredRect(5, yOff + 2, 6, yOff + 15, 0xff006600);
                        renderContext.drawColoredRect(17, yOff + 2, 18, yOff + 15, 0xff006600);
                        renderContext.drawColoredRect(5, yOff + 2, 18, yOff + 3, 0xff006600);
                        renderContext.drawColoredRect(5, yOff + 14, 18, yOff + 15, 0xff006600);
                        renderContext.drawString(fr, StructuredText.of("+"), 9, yOff + 4, 0xffffffff, true);

                        // Render ID field - MIDDLE
                        var idContext = context.translated(25, yOff + 2, 150, 14);
                        getOrCreateIdField(i).render(idContext);

                        // Render expression field - MIDDLE
                        var exprContext = context.translated(25, yOff + 18, 150, 14);
                        getOrCreateExpressionField(i).render(exprContext);

                        // Draw color button - RIGHT
                        int colorRGB = rules.get(i).colorHex | 0xFF000000;
                        renderContext.drawComplexTexture(
                            GuiTextures.BUTTON_WHITE, 185, yOff + 18, 205, yOff + 32,
                            it -> it.color(colorRGB)
                        );

                        // Draw delete button (small X) - FAR RIGHT
                        renderContext.drawColoredRect(210, yOff + 18, 223, yOff + 31, 0xffcc0000);
                        renderContext.drawColoredRect(210, yOff + 18, 211, yOff + 31, 0xff660000);
                        renderContext.drawColoredRect(222, yOff + 18, 223, yOff + 31, 0xff660000);
                        renderContext.drawColoredRect(210, yOff + 18, 223, yOff + 19, 0xff660000);
                        renderContext.drawColoredRect(210, yOff + 30, 223, yOff + 31, 0xff660000);
                        renderContext.drawString(fr, StructuredText.of("X"), 213, yOff + 19, 0xffffffff, true);

                        yOff += 50;
                    }

                    // Draw add button at bottom (small + in square)
                    renderContext.drawColoredRect(5, yOff + 2, 18, yOff + 15, 0xff00aa00);
                    renderContext.drawColoredRect(5, yOff + 2, 6, yOff + 15, 0xff006600);
                    renderContext.drawColoredRect(17, yOff + 2, 18, yOff + 15, 0xff006600);
                    renderContext.drawColoredRect(5, yOff + 2, 18, yOff + 3, 0xff006600);
                    renderContext.drawColoredRect(5, yOff + 14, 18, yOff + 15, 0xff006600);
                    renderContext.drawString(fr, StructuredText.of("+"), 9, yOff + 4, 0xffffffff, true);
                }

                @Override
                public boolean mouseEvent(@NotNull io.github.notenoughupdates.moulconfig.gui.MouseEvent mouseEvent, @NotNull GuiImmediateContext context) {
                    if (!(mouseEvent instanceof io.github.notenoughupdates.moulconfig.gui.MouseEvent.Click click)) {
                        return false;
                    }

                    if (!click.getMouseState()) {
                        return false;
                    }

                    int yOff = 0;

                    // Check each rule row
                    for (int i = 0; i < rules.size(); i++) {
                        // Check add button (small + in square) - LEFT
                        if (context.getAbsoluteMouseX() >= 5 && context.getAbsoluteMouseX() < 18 &&
                            context.getAbsoluteMouseY() >= yOff + 2 && context.getAbsoluteMouseY() < yOff + 15) {
                            // Insert new rule at this position
                            rules.add(i, new RuleData("", "", 0xFFFFFF));
                            idFields.clear();
                            expressionFields.clear();
                            option.explicitNotifyChange();
                            return true;
                        }

                        // Check text fields - MIDDLE
                        var idContext = context.translated(25, yOff + 2, 150, 14);
                        if (getOrCreateIdField(i).mouseEvent(mouseEvent, idContext)) {
                            return true;
                        }

                        var exprContext = context.translated(25, yOff + 18, 150, 14);
                        if (getOrCreateExpressionField(i).mouseEvent(mouseEvent, exprContext)) {
                            return true;
                        }

                        // Check color button - RIGHT
                        if (context.getAbsoluteMouseX() >= 185 && context.getAbsoluteMouseX() < 205 &&
                            context.getAbsoluteMouseY() >= yOff + 18 && context.getAbsoluteMouseY() < yOff + 32) {
                            String colorStr = String.format("%06X", rules.get(i).colorHex & 0xFFFFFF);
                            int index = i;
                            ColorSelectComponent colorSelectComponent = new ColorSelectComponent(0, 0, colorStr, newString -> {
                                try {
                                    rules.get(index).colorHex = (int) (Long.parseLong(newString.replaceAll("[^0-9A-Fa-f]", ""), 16) & 0xFFFFFF);
                                    option.explicitNotifyChange();
                                } catch (NumberFormatException ignored) {
                                }
                                closeOverlay();
                            }, () -> closeOverlay());

                            int scaledHeight = context.getRenderContext().getMinecraft().getScaledHeight();
                            int clampedY = context.getAbsoluteMouseY() + colorSelectComponent.getHeight() > scaledHeight
                                ? scaledHeight - colorSelectComponent.getHeight()
                                : context.getAbsoluteMouseY();

                            openOverlay(colorSelectComponent, context.getAbsoluteMouseX(), clampedY);
                            return true;
                        }

                        // Check delete button (small X) - FAR RIGHT
                        if (context.getAbsoluteMouseX() >= 210 && context.getAbsoluteMouseX() < 223 &&
                            context.getAbsoluteMouseY() >= yOff + 18 && context.getAbsoluteMouseY() < yOff + 31) {
                            rules.remove(i);
                            idFields.remove(i);
                            expressionFields.remove(i);
                            option.explicitNotifyChange();
                            return true;
                        }

                        yOff += 50;
                    }

                    // Check add button at bottom (small + in square)
                    if (context.getAbsoluteMouseX() >= 5 && context.getAbsoluteMouseX() < 18 &&
                        context.getAbsoluteMouseY() >= yOff + 2 && context.getAbsoluteMouseY() < yOff + 15) {
                        rules.add(new RuleData("", "", 0xFFFFFF));
                        option.explicitNotifyChange();
                        return true;
                    }

                    return false;
                }

                @Override
                public boolean keyboardEvent(@NotNull io.github.notenoughupdates.moulconfig.gui.KeyboardEvent event, @NotNull GuiImmediateContext context) {
                    for (TextFieldComponent field : idFields.values()) {
                        if (field.isFocused() && field.keyboardEvent(event, context)) {
                            return true;
                        }
                    }
                    for (TextFieldComponent field : expressionFields.values()) {
                        if (field.isFocused() && field.keyboardEvent(event, context)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }
        return delegate;
    }

    @Override
    public int getHeight() {
        int height = 5;
        for (int i = 0; i < rules.size(); i++) {
            height += 50;
        }
        height += 25; // For add button
        return height;
    }
}


