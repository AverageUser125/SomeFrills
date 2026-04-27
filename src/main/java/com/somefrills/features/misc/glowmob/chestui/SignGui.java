package com.somefrills.features.misc.glowmob.chestui;

import com.somefrills.misc.Utils;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.block.entity.SignBlockEntityRenderer;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * A thin wrapper over the vanilla SignEditScreen that:
 * - sets the first line to the provided title
 * - starts editing on the 2nd line
 * - calls a callback with the final 4 lines when the screen is closed
 */
public class SignGui extends SignEditScreen {
    private final Consumer<String[]> onClose;
    private final boolean front;

    private SignGui(SignBlockEntity sign, boolean front, boolean filtered, Consumer<String[]> onClose) {
        super(sign, front, filtered);
        this.onClose = onClose;
        this.front = front;
    }

    public static void open(String[] rows, Consumer<String[]> onClose) {
        Text[] textRows = new Text[rows.length];
        for (int i = 0; i < rows.length; i++) {
            textRows[i] = Text.of(rows[i]);
        }
        SignBlockEntity sign = new FakeSign(textRows);
        SignGui gui = new SignGui(sign, true, false, onClose);
        Utils.setScreen(gui);
    }

    public static void open(Text[] rows, Consumer<String[]> onClose) {
        SignBlockEntity sign = new FakeSign(rows);
        SignGui gui = new SignGui(sign, true, false, onClose);
        Utils.setScreen(gui);
    }

    @Override
    protected void init() {
        // AbstractSignEditScreen.init() adds a "Done"
        // but calls this.close() instead of this.finishEditing()
        // this is so we can trigger our onClose callback
        this.addDrawableChild(
                ButtonWidget.builder(ScreenTexts.DONE, button -> this.close()).dimensions(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build()
        );
        this.selectionManager = new SelectionManager(
                () -> this.messages[this.currentRow],
                this::setCurrentRowMessage,
                SelectionManager.makeClipboardGetter(this.client),
                SelectionManager.makeClipboardSetter(this.client),
                textLine -> this.client.textRenderer.getWidth(textLine) <= this.blockEntity.getMaxTextWidth()
        );

        // SignEditScreen.init(), just here since we can't call super.init()
        boolean bl = this.blockEntity.getCachedState().getBlock() instanceof SignBlock;
        this.model = SignBlockEntityRenderer.createSignModel(this.client.getLoadedEntityModels(), this.signType, bl);

        this.currentRow = 1;
        this.selectionManager.putCursorAtEnd();
    }

    @Override
    public void close() {
        // Read final text from the sign block entity and pass to callback
        SignText finalText = this.blockEntity.getText(this.front);
        String[] out = new String[4];
        for (int i = 0; i < 4; i++) {
            out[i] = finalText.getMessage(i, false).getString();
        }

        if (this.onClose != null) this.onClose.accept(out);
    }

    public static class FakeSign extends SignBlockEntity {
        public FakeSign(Text[] lines) {
            super(BlockPos.ORIGIN, Registries.BLOCK.get(Identifier.tryParse("minecraft:oak_sign")).getDefaultState());
            if (lines.length < 4) {
                // pad with empty lines if less than 4
                Text[] padded = new Text[4];
                System.arraycopy(lines, 0, padded, 0, lines.length);
                for (int i = lines.length; i < 4; i++) {
                    padded[i] = Text.empty();
                }
                lines = padded;
            }

            SignText text = new SignText(lines, lines, DyeColor.WHITE, false);
            setText(text, true);
        }

        @Override
        public boolean setText(SignText text, boolean front) {
            return front ? this.setFrontText(text) : this.setBackText(text);
        }

        @Override
        public boolean isPlayerTooFarToEdit(UUID uuid) {
            return false;
        }

        private boolean setBackText(SignText backText) {
            if (backText != this.backText) {
                this.backText = backText;
                return true;
            } else {
                return false;
            }
        }

        private boolean setFrontText(SignText frontText) {
            if (frontText != this.frontText) {
                this.frontText = frontText;
                return true;
            } else {
                return false;
            }
        }
    }

}
