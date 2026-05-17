package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.misc.Utils
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.SpawnEggItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class EntityTypesMenu(previousMenu: ChestUI?, private val info: MatchInfo) :
    ChestUI("Select Entity Type", previousMenu) {
    init {
        addAddon(SearchAddon())
        addAddon(PagingAddon())
        rebuild()
    }

    override fun build() {
        // Collect all entity entries (including special-case entries) then sort alphabetically by display name
        val entries: MutableList<ItemStack> = ArrayList()

        for (entityType in Registries.ENTITY_TYPE) {
            val id = Registries.ENTITY_TYPE.getId(entityType)
            val spawnEggId = id.withSuffixedPath("_spawn_egg")
            val spawnEggItem = Registries.ITEM.get(spawnEggId)

            if (spawnEggItem !is SpawnEggItem) {
                continue
            }

            val eggStack = ItemStack(spawnEggItem)
            if (!eggStack.isEmpty) {
                entries.add(createEntityItem(entityType, eggStack))
            }
        }

        // Add special entries that don't have spawn eggs: armor_stand and player
        entries.add(createEntityItemFromId("armor_stand", ItemStack(Items.ARMOR_STAND)))
        entries.add(createEntityItemFromId("player", ItemStack(Items.PLAYER_HEAD)))
        // Sort by the plain custom name (case-insensitive). Fallback to empty string if missing.
        entries.sortWith(Comparator { a: ItemStack?, b: ItemStack? ->
            var na = Utils.getPlainCustomName(a)
            var nb = Utils.getPlainCustomName(b)
            if (na == null) na = ""
            if (nb == null) nb = ""
            na.compareTo(nb, ignoreCase = true)
        })

        addItem(createEntityItemFromId("none", ItemStack(Items.STRUCTURE_VOID)))
        for (entry in entries) {
            addItem(entry)
        }
    }

    private fun createEntityItem(entityType: EntityType<*>?, eggStack: ItemStack): ItemStack {
        // Delegate to the ID-based helper to avoid duplication
        val entityTypeId = Registries.ENTITY_TYPE.getId(entityType)
        return createEntityItemFromId(entityTypeId.path, eggStack)
    }

    /**
     * Create an entity-selection item for entries that don't have spawn eggs (eg. player, armor_stand).
     */
    private fun createEntityItemFromId(entityTypeId: String, baseStack: ItemStack): ItemStack {
        val stack = baseStack.copy()

        val nbt = NbtCompound()
        nbt.putString("EntityType", entityTypeId)
        Utils.setCustomData(stack, nbt)

        val displayName = Utils.capitalizeType(entityTypeId)
        Utils.setCustomName(stack, Style.EMPTY.withColor(Formatting.GREEN).withItalic(false), displayName)
        setLore(stack, entityTypeId)
        return stack
    }

    private fun setLore(stack: ItemStack, typeId: String?) {
        val lore: MutableList<Text?> = ArrayList<Text?>()
        if (info.type.contains(typeId)) {
            lore.add(Text.literal("✓ Currently selected").setStyle(Style.EMPTY.withColor(Formatting.YELLOW)))
            stack.set<Boolean?>(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
        } else {
            lore.add(Text.literal("Click to select").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
        }
        stack.set<LoreComponent?>(DataComponentTypes.LORE, LoreComponent(lore, lore))
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        if (stack == null || stack.isEmpty) return

        // Retrieve entity type from custom data
        val nbt = Utils.getCustomData(stack)
        if (nbt == null || !nbt.contains("EntityType")) return

        var entityTypeId = nbt.getString("EntityType").orElse(null)
        if (entityTypeId == null) return

        if (entityTypeId == "none") {
            info.type.clear()
        } else {
            entityTypeId = Utils.stripPrefix(entityTypeId, "minecraft:")
            if (info.type.contains(entityTypeId)) {
                info.type.remove(entityTypeId)
            } else {
                info.type.add(entityTypeId)
            }
        }
        rebuild()
    }
}

