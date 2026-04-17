package com.somefrills.chestui;

import com.somefrills.features.misc.matcher.MatchInfo;
import com.somefrills.misc.Utils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class EntityTypesMenu extends ChestUI {
    private final MatchInfo info;

    public EntityTypesMenu(ChestUI previousMenu, MatchInfo info) {
        super("Select Entity Type", previousMenu);
        this.info = info;
        rebuild();
    }

    @Override
    protected void build() {
        // Collect all entity entries (including special-case entries) then sort alphabetically by display name
        List<ItemStack> entries = new ArrayList<>();

        for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
            var id = Registries.ENTITY_TYPE.getId(entityType);
            var spawnEggId = id.withSuffixedPath("_spawn_egg");
            var spawnEggItem = Registries.ITEM.get(spawnEggId);

            if (!(spawnEggItem instanceof SpawnEggItem)) {
                continue;
            }

            ItemStack eggStack = new ItemStack(spawnEggItem);
            if (!eggStack.isEmpty()) {
                entries.add(createEntityItem(entityType, eggStack));
            }
        }

        // Add special entries that don't have spawn eggs: armor_stand and player
        entries.add(createEntityItemFromId("armor_stand", new ItemStack(Items.ARMOR_STAND)));
        entries.add(createEntityItemFromId("player", new ItemStack(Items.PLAYER_HEAD)));
        // Sort by the plain custom name (case-insensitive). Fallback to empty string if missing.
        entries.sort((a, b) -> {
            String na = Utils.getPlainCustomName(a);
            String nb = Utils.getPlainCustomName(b);
            if (na == null) na = "";
            if (nb == null) nb = "";
            return na.compareToIgnoreCase(nb);
        });

        addItem(createEntityItemFromId("none", new ItemStack(Items.STRUCTURE_VOID)));
        for (ItemStack entry : entries) {
            addItem(entry);
        }
    }

    private ItemStack createEntityItem(EntityType<?> entityType, ItemStack eggStack) {
        // Delegate to the ID-based helper to avoid duplication
        var entityTypeId = Registries.ENTITY_TYPE.getId(entityType);
        return createEntityItemFromId(entityTypeId.getPath(), eggStack);
    }

    /**
     * Create an entity-selection item for entries that don't have spawn eggs (eg. player, armor_stand).
     */
    private ItemStack createEntityItemFromId(String entityTypeId, ItemStack baseStack) {
        ItemStack stack = baseStack.copy();

        var nbt = new NbtCompound();
        nbt.putString("EntityType", entityTypeId);
        Utils.setCustomData(stack, nbt);

        String displayName = Utils.capitalizeType(entityTypeId);
        Utils.setCustomName(stack, Style.EMPTY.withColor(Formatting.GREEN).withItalic(false), displayName);
        setLore(stack, entityTypeId);
        return stack;
    }

    private void setLore(ItemStack stack, String typeId) {
        List<Text> lore = new ArrayList<>();
        if (info.type.equals(typeId)) {
            lore.add(Text.literal("✓ Currently selected").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)));
        } else {
            lore.add(Text.literal("Click to select").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
        }
        stack.set(DataComponentTypes.LORE, new LoreComponent(lore, lore));
    }

    @Override
    protected void onItemClick(ItemStack stack, int button) {
        if (stack.isEmpty()) return;

        // Retrieve entity type from custom data
        var nbt = Utils.getCustomData(stack);
        if (nbt == null || !nbt.contains("EntityType")) return;

        String entityTypeId = nbt.getString("EntityType").orElse(null);
        if (entityTypeId == null) return;

        if(entityTypeId.equals("none")) {
            info.type = "";
        } else {
            info.type = entityTypeId;
        }
        close();
    }
}

