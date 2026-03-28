package com.somefrills.hud;

import com.daqem.uilib.api.widget.IWidget;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.widget.ButtonWidget;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.somefrills.config.SettingJson;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.CharInput;
import net.minecraft.text.Text;

// no additional collections required

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.somefrills.Main.mc;

public class JsonEditorScreen extends AbstractScreen {
    private final SettingJson setting;
    private final Screen previous;
    private JsonTreeWidget treeWidget;
    // in-place editor for editing single values
    private EditBoxWidget inplaceEditor = null;
    private List<String> inplaceEditPath = null;
    private Text errorText = null;

    public JsonEditorScreen(SettingJson setting, Screen previous) {
        super(Text.of("Edit JSON"));
        this.setting = setting;
        this.previous = previous;
    }

    @Override
    protected void init() {
        super.init();
        int margin = 10;
        int w = this.width - margin * 2;
        int h = this.height - 80;

        JsonElement current = setting.get();
        if (current == null) current = com.google.gson.JsonNull.INSTANCE;
        treeWidget = new JsonTreeWidget(margin, margin, w, h, current);

        int btnW = 80;
        int btnY = margin + h + 10;

        ButtonWidget btnSave = new ButtonWidget(margin, btnY, btnW, 20, Text.of("Save"), b -> onSave());
        ButtonWidget btnReset = new ButtonWidget(margin + btnW + 6, btnY, btnW, 20, Text.of("Reset"), b -> onReset());
        ButtonWidget btnCancel = new ButtonWidget(margin + (btnW + 6) * 2, btnY, btnW, 20, Text.of("Cancel"), b -> onCancel());

        addWidget(treeWidget);
        addWidget(btnSave);
        addWidget(btnReset);
        addWidget(btnCancel);
    }

    private void onSave() {
        try {
            JsonElement el = treeWidget.getEdited();
            setting.set(el);
            mc.setScreen(previous);
        } catch (Throwable t) {
            errorText = Text.literal("Invalid JSON: " + t.getMessage());
        }
    }

    private void onReset() {
        setting.reset();
        JsonElement current = setting.get();
        if (current == null) current = com.google.gson.JsonNull.INSTANCE;
        treeWidget.setRoot(current);
    }

    private void onCancel() {
        mc.setScreen(previous);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        if (errorText != null) {
            context.drawText(mc.textRenderer, errorText, 10, this.height - 30, 0xFFFF5555, false);
        }
    }

    // ==========================
    // Helper: JSON tree widget
    // ==========================

    // A simple hierarchical viewer/editor for JsonElement. Supports expand/collapse for objects/arrays,
    // displays top-level keys and their contents indented, and allows in-place editing of primitive values.
    private class JsonTreeWidget extends ClickableWidget implements IWidget {
        private JsonElement root;
        private final int contentX, contentY, contentW, contentH;
        private int scroll = 0;
        // when editing a primitive, remember if we are editing a string so we don't require JSON quoting
        private boolean inplaceEditIsString = false;
        private final Map<List<String>, Boolean> expanded = new HashMap<>();
        // editors for primitive values, keyed by their path
        private final Map<List<String>, EditBoxWidget> editors = new HashMap<>();

        public JsonTreeWidget(int x, int y, int width, int height, JsonElement root) {
            super(x, y, width, height, Text.empty());
            this.root = root.deepCopy();
            this.contentX = x;
            this.contentY = y;
            this.contentW = width;
            this.contentH = height;
            // default: root expanded
            expanded.put(Collections.emptyList(), true);
        }

        public JsonElement getEdited() {
            return root;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            // render background
            context.fill(contentX, contentY, contentX + contentW, contentY + contentH, 0xFF0E0E12);
            int y = contentY - scroll;
            renderElement(context, Collections.emptyList(), root, 0, y, mouseY);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
            
        }

        private int renderElement(DrawContext ctx, List<String> path, JsonElement el, int indent, int y, int mouseY) {
            int lineH = mc.textRenderer.fontHeight + 4;
            // int startY = y; (unused)
                if (el.isJsonObject()) {
                // header for object
                String name = path.isEmpty() ? "root" : path.get(path.size() - 1);
                boolean isExpanded = expanded.getOrDefault(path, false);
                String prefix = isExpanded ? "▾ " : "▸ ";
                // hover highlight for this line
                if (mouseY >= y && mouseY < y + lineH) {
                    ctx.fill(contentX + indent * 10, y, contentX + contentW, y + lineH, 0x2233AAFF);
                }
                // indent guide
                for (int i = 0; i < indent; i++) {
                    int gx = contentX + i * 10 + 6;
                    ctx.fill(gx, y, gx + 1, y + lineH, 0xFF33333A);
                }
                ctx.drawText(mc.textRenderer, Text.literal(prefix).styled(s -> s), contentX + indent * 10 + 4, y + 2, 0xFF88CCFF, false);
                ctx.drawText(mc.textRenderer, Text.literal(name), contentX + indent * 10 + 20, y + 2, 0xFFFFFFFF, false);
                y += lineH;
                if (isExpanded) {
                    var obj = el.getAsJsonObject();
                    for (var entry : obj.entrySet()) {
                            var p = new ArrayList<>(path);
                                p.add(entry.getKey());
                                y = renderElement(ctx, p, entry.getValue(), indent + 1, y, mouseY);
                    }
                }
            } else if (el.isJsonArray()) {
                String name = path.isEmpty() ? "root" : path.get(path.size() - 1);
                boolean isExpanded = expanded.getOrDefault(path, false);
                String prefix = isExpanded ? "▾ " : "▸ ";
                if (mouseY >= y && mouseY < y + lineH) {
                    ctx.fill(contentX + indent * 10, y, contentX + contentW, y + lineH, 0x2233AAFF);
                }
                for (int i = 0; i < indent; i++) {
                    int gx = contentX + i * 10 + 6;
                    ctx.fill(gx, y, gx + 1, y + lineH, 0xFF33333A);
                }
                ctx.drawText(mc.textRenderer, Text.literal(prefix), contentX + indent * 10 + 4, y + 2, 0xFF88CCFF, false);
                ctx.drawText(mc.textRenderer, Text.literal(name + " [array]"), contentX + indent * 10 + 20, y + 2, 0xFFFFFFFF, false);
                y += lineH;
                if (isExpanded) {
                    var arr = el.getAsJsonArray();
                    for (int i = 0; i < arr.size(); i++) {
                        var p = new ArrayList<>(path);
                        p.add("[" + i + "]");
                        y = renderElement(ctx, p, arr.get(i), indent + 1, y, mouseY);
                    }
                }
            } else if (el.isJsonNull() || el.isJsonPrimitive()) {
                String key = path.isEmpty() ? "(value)" : path.get(path.size() - 1);
                // show key and place an EditBoxWidget for the value
                if (mouseY >= y && mouseY < y + lineH) {
                    ctx.fill(contentX + indent * 10, y, contentX + contentW, y + lineH, 0x2233AAFF);
                }
                for (int i = 0; i < indent; i++) {
                    int gx = contentX + i * 10 + 6;
                    ctx.fill(gx, y, gx + 1, y + lineH, 0xFF33333A);
                }
                ctx.drawText(mc.textRenderer, Text.literal(key + ": "), contentX + indent * 10 + 8, y + 2, 0xFF88DDFF, false);

                // compute editor position and size
                int keyWidth = mc.textRenderer.getWidth(key + ": ");
                int editorX = contentX + indent * 10 + 8 + keyWidth;
                int editorY = y + 2;
                int editorW = Math.max(50, contentX + contentW - editorX - 8);
                int editorH = lineH - 4;

                EditBoxWidget editor = editors.get(path);
                String rawText;
                if (el.isJsonNull()) rawText = "";
                else if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                    rawText = el.getAsJsonPrimitive().getAsString().replace("\r", "").replace("\n", "\\n");
                } else {
                    rawText = el.toString();
                }

                if (editor == null) {
                    // create editor that writes string values directly (user asked everything as string)
                    var pathCopy = new ArrayList<>(path);
                    EditBoxWidget e = new EditBoxWidget(mc.textRenderer, editorX, editorY, editorW, editorH, Text.literal(rawText)) {
                        @Override
                        public List<Text> validateInput(String input) {
                            List<Text> errors = new ArrayList<>();
                            try {
                                // ignore CR/LF in input
                                String cleaned = input == null ? "" : input.replace("\r", "");
                                // store as JSON string primitive (user requested everything as string)
                                setElementByPath(pathCopy, new com.google.gson.JsonPrimitive(cleaned.replace("\\n", "\n")));
                            } catch (Throwable t) {
                                errors.add(Text.literal("Invalid input: " + t.getMessage()));
                            }
                            return errors;
                        }
                    };
                    // apply live on change
                    e.setChangedListener(str -> {
                        try {
                            List<Text> errs = e.validateInput(str == null ? "" : str);
                            if (errs == null || errs.isEmpty()) errorText = null;
                        } catch (Throwable ignored) {}
                    });
                    editors.put(pathCopy, e);
                    JsonEditorScreen.this.addWidget(e);
                    editor = e;
                }

                // update editor position & text unless focused
                editor.setX(editorX);
                editor.setY(editorY);
                editor.setWidth(editorW);
                editor.setHeight(editorH);
                if (!editor.isFocused()) editor.setText(rawText);

                y += lineH;
            }
            return y;
        }

        private String primitiveToDisplay(JsonElement el) {
            if (el == null || el.isJsonNull()) return "null";
            if (el.isJsonPrimitive()) {
                var p = el.getAsJsonPrimitive();
                if (p.isString()) {
                    String s = p.getAsString();
                    // ignore CR and LF characters in display
                    s = s.replace("\r", "").replace("\n", "\\n");
                    return '"' + s + '"';
                } else if (p.isBoolean()) return Boolean.toString(p.getAsBoolean());
                else return p.getAsNumber().toString();
            }
            return el.toString();
        }

        @Override
        public boolean mouseClicked(Click click, boolean bl) {
            // compute which line was clicked and toggle expand or start edit
            try {
                double my = click.y();
                int relY = (int) my - contentY + scroll;
                int lineH = mc.textRenderer.fontHeight + 4;
                int lineIndex = relY / lineH;
                // find the path that corresponds to this line by scanning
                List<String> found = findPathByLine(Collections.emptyList(), root, 0, 0, lineIndex);
                if (found != null) {
                    JsonElement target = getElementByPath(found);
                    if (target.isJsonObject() || target.isJsonArray()) {
                        boolean cur = expanded.getOrDefault(found, false);
                        expanded.put(found, !cur);
                    } else {
                        // start in-place edit
                        startInplaceEdit(found, target);
                    }
                    return true;
                }
            } catch (Throwable ignored) {
            }
            return false;
        }

        private void startInplaceEdit(List<String> path, JsonElement target) {
            // Position the inplace editor at the appropriate line.
            inplaceEditPath = path;
            // Determine if editing a string
            inplaceEditIsString = target.isJsonPrimitive() && target.getAsJsonPrimitive().isString();

            String current;
            if (target.isJsonNull()) {
                current = "null";
            } else if (inplaceEditIsString) {
                // show raw string (no surrounding quotes) and preserve spaces; show escaped newlines as \n
                String s = target.getAsJsonPrimitive().getAsString();
                s = s.replace("\r", "").replace("\n", "\\n");
                current = s;
            } else {
                current = target.toString();
            }

            // compute line index and y position for the given path so the editor appears inline
            int lineIndex = getLineIndexForPath(path);
            int lineH = mc.textRenderer.fontHeight + 4;
            int editorY = contentY + lineIndex * lineH - scroll;

            // compute x position: indent + key width
            int indent = Math.max(0, path.size() - 1);
            String key = path.isEmpty() ? "(value)" : path.get(path.size() - 1);
            int keyWidth = mc.textRenderer.getWidth(key + ": ");
            int editorX = contentX + indent * 10 + 8 + keyWidth;

            // clamp to content area
            editorX = Math.max(contentX + 4, Math.min(editorX, contentX + contentW - 20));
            if (editorY < contentY) editorY = contentY;
            if (editorY > contentY + contentH - lineH) editorY = contentY + contentH - lineH;

            inplaceEditor = new EditBoxWidget(mc.textRenderer, editorX, editorY, Math.max(50, contentX + contentW - editorX - 4), lineH - 4, Text.literal(current)) {
                @Override
                public java.util.List<Text> validateInput(String input) {
                    java.util.List<Text> errors = new ArrayList<>();
                    try {
                        if (inplaceEditIsString) {
                            // keep spaces and unescape \n
                            String val = input.replace("\\n", "\n");
                            setElementByPath(inplaceEditPath, new com.google.gson.JsonPrimitive(val));
                        } else {
                            try {
                                JsonElement el = JsonParser.parseString(input);
                                setElementByPath(inplaceEditPath, el);
                            } catch (Throwable parseEx) {
                                // fallback to string primitive (accept spaces)
                                setElementByPath(inplaceEditPath, new com.google.gson.JsonPrimitive(input));
                            }
                        }
                    } catch (Throwable t) {
                        errors.add(Text.literal("Invalid value: " + t.getMessage()));
                    }
                    return errors;
                }
            };
            // Update JSON model live when text changes. Use validateInput() return value: if no errors,
            // the value was applied immediately (validateInput itself writes into the model), so clear any error message.
            inplaceEditor.setChangedListener(str -> {
                try {
                    java.util.List<Text> errors = inplaceEditor.validateInput(str == null ? "" : str);
                    if (errors == null || errors.isEmpty()) {
                        errorText = null;
                    }
                } catch (Throwable ignored) {
                }
            });
            addWidget(inplaceEditor);
            inplaceEditor.setFocused(true);
        }

        // Return the line index (0-based) where the given path's header/value is rendered
        private int getLineIndexForPath(List<String> path) {
            return computeLineIndexForPath(Collections.emptyList(), root, 0, path);
        }

        private int computeLineIndexForPath(List<String> curPath, JsonElement el, int currentIndex, List<String> targetPath) {
            // If this element corresponds to the target path, return currentIndex
            if (curPath.equals(targetPath)) return currentIndex;
            if (el.isJsonObject()) {
                currentIndex++; // header line
                var obj = el.getAsJsonObject();
                for (var entry : obj.entrySet()) {
                    var p = new ArrayList<>(curPath);
                    p.add(entry.getKey());
                    int subtreeLines = countLines(entry.getValue());
                    if (targetPath.size() >= p.size() && targetPath.subList(0, p.size()).equals(p)) {
                        // target inside this subtree, recurse
                        return computeLineIndexForPath(p, entry.getValue(), currentIndex, targetPath);
                    }
                    currentIndex += subtreeLines;
                }
                return -1;
            } else if (el.isJsonArray()) {
                currentIndex++; // header
                var arr = el.getAsJsonArray();
                for (int i = 0; i < arr.size(); i++) {
                    var p = new ArrayList<>(curPath);
                    p.add("[" + i + "]");
                    int subtreeLines = countLines(arr.get(i));
                    if (targetPath.size() >= p.size() && targetPath.subList(0, p.size()).equals(p)) {
                        return computeLineIndexForPath(p, arr.get(i), currentIndex, targetPath);
                    }
                    currentIndex += subtreeLines;
                }
                return -1;
            } else {
                // primitive line
                if (curPath.equals(targetPath)) return currentIndex;
                return -1;
            }
        }

        private JsonElement getElementByPath(List<String> path) {
            JsonElement cur = root;
            for (String p : path) {
                if (cur.isJsonObject()) {
                    cur = cur.getAsJsonObject().get(p);
                } else if (cur.isJsonArray()) {
                    // p like [i]
                    int idx = Integer.parseInt(p.substring(1, p.length() - 1));
                    cur = cur.getAsJsonArray().get(idx);
                } else return cur;
                if (cur == null) return com.google.gson.JsonNull.INSTANCE;
            }
            return cur;
        }

        private List<String> findPathByLine(List<String> path, JsonElement el, int indent, int currentLine, int targetLine) {
            // int lineH = mc.textRenderer.fontHeight + 4; (not needed here)
            if (currentLine > targetLine) return null;
            if (el.isJsonObject()) {
                if (currentLine == targetLine) return path;
                currentLine++;
                var obj = el.getAsJsonObject();
                for (var entry : obj.entrySet()) {
                    var p = new ArrayList<>(path);
                    p.add(entry.getKey());
                    var res = findPathByLine(p, entry.getValue(), indent + 1, currentLine, targetLine);
                    // compute lines consumed by this subtree to advance currentLine
                    int subtreeLines = countLines(entry.getValue());
                    if (res != null) return res;
                    currentLine += subtreeLines;
                }
                return null;
            } else if (el.isJsonArray()) {
                if (currentLine == targetLine) return path;
                currentLine++;
                var arr = el.getAsJsonArray();
                for (int i = 0; i < arr.size(); i++) {
                    var p = new ArrayList<>(path);
                    p.add("[" + i + "]");
                    var res = findPathByLine(p, arr.get(i), indent + 1, currentLine, targetLine);
                    int subtreeLines = countLines(arr.get(i));
                    if (res != null) return res;
                    currentLine += subtreeLines;
                }
                return null;
            } else {
                if (currentLine == targetLine) return path;
                return null;
            }
        }

        private int countLines(JsonElement el) {
            int line = 1;
            if (el.isJsonObject()) {
                line++;
                for (var e : el.getAsJsonObject().entrySet()) line += countLines(e.getValue());
            } else if (el.isJsonArray()) {
                line++;
                for (var e : el.getAsJsonArray()) line += countLines(e);
            }
            return line;
        }
        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            this.scroll = Math.max(0, this.scroll - (int) Math.signum(verticalAmount) * (mc.textRenderer.fontHeight + 4));
            return true;
        }

        @Override
        public boolean keyPressed(KeyInput keyEvent) {
            // handle commit of inplace editor
            if (inplaceEditor != null && keyEvent.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
                commitInplaceEdit();
                return true;
            }
            return false;
        }

        public void commitInplaceEdit() {
            if (inplaceEditor == null || inplaceEditPath == null) return;
            String text = inplaceEditor.getText();
            // remove CR characters; ignore raw LF characters unless the user explicitly typed the escape sequence
            text = text.replace("\r", "");
            text = text.replace("\n", "");
            try {
                JsonElement el;
                if (inplaceEditIsString) {
                    // user edited a string: accept the raw text (unescape explicit \n sequences to actual newlines)
                    String val = text.replace("\\n", "\n");
                    el = new com.google.gson.JsonPrimitive(val);
                } else {
                    try {
                        el = JsonParser.parseString(text);
                    } catch (Throwable parseEx) {
                        // Fall back to storing as a string if it's not valid JSON (accept spaces etc.)
                        el = new com.google.gson.JsonPrimitive(text);
                    }
                }
                // write el into root at inplaceEditPath
                setElementByPath(inplaceEditPath, el);
                // remove editor widget
                removeWidget(inplaceEditor);
                inplaceEditor = null;
                inplaceEditPath = null;
                errorText = null;
            } catch (Throwable t) {
                errorText = Text.literal("Invalid JSON value: " + t.getMessage());
            }
        }

        private void setElementByPath(List<String> path, JsonElement value) {
            JsonElement cur = root;
            for (int i = 0; i < path.size() - 1; i++) {
                String p = path.get(i);
                if (cur.isJsonObject()) cur = cur.getAsJsonObject().get(p);
                else if (cur.isJsonArray()) cur = cur.getAsJsonArray().get(Integer.parseInt(p.substring(1, p.length() - 1)));
            }
            String last = path.get(path.size() - 1);
            if (cur.isJsonObject()) {
                cur.getAsJsonObject().add(last, value);
            } else if (cur.isJsonArray()) {
                int idx = Integer.parseInt(last.substring(1, last.length() - 1));
                cur.getAsJsonArray().set(idx, value);
            }
        }

        public void setRoot(JsonElement current) {
            if (current == null) current = com.google.gson.JsonNull.INSTANCE;
            this.root = current.deepCopy();
            // reset expansion state
            this.expanded.clear();
            this.expanded.put(Collections.emptyList(), true);
            this.scroll = 0;
        }
    }

    @Override
    public boolean charTyped(CharInput characterEvent) {
        if (treeWidget != null && inplaceEditor != null) {
            return inplaceEditor.charTyped(characterEvent) || super.charTyped(characterEvent);
        }
        return super.charTyped(characterEvent);
    }

    @Override
    public boolean keyPressed(KeyInput keyEvent) {
        if (treeWidget != null) {
            // if an inplace editor exists, let it handle the key first
            if (inplaceEditor != null) {
                // Enter commits via treeWidget
                if (keyEvent.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER) {
                    treeWidget.commitInplaceEdit();
                    return true;
                }
                return inplaceEditor.keyPressed(keyEvent) || super.keyPressed(keyEvent);
            }
            return treeWidget.keyPressed(keyEvent) || super.keyPressed(keyEvent);
        }
        return super.keyPressed(keyEvent);
    }
    
    @Override
    public void close() {
        if (treeWidget != null && inplaceEditor != null) {
            treeWidget.commitInplaceEdit();
        }
        mc.setScreen(previous);
    }
}



