package com.somefrills.features.misc.glowmob.chestui

import com.somefrills.features.misc.glowmob.MatchInfo
import com.somefrills.utils.TextUtils
import com.somefrills.utils.getCustomData
import com.somefrills.utils.plainCustomName
import com.somefrills.utils.setCustomData
import com.somefrills.utils.setCustomName
import com.somefrills.utils.stripPrefix
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Style
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.item.component.ItemLore

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

        for (entityType in BuiltInRegistries.ENTITY_TYPE) {
            val id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType)
            val spawnEggId = id.withSuffix("_spawn_egg")
            val spawnEggItem = BuiltInRegistries.ITEM.get(spawnEggId)

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
            val na = a?.plainCustomName ?: ""
            val nb = b?.plainCustomName ?: ""
            na.compareTo(nb, ignoreCase = true)
        })

        addItem(createEntityItemFromId("none", ItemStack(Items.STRUCTURE_VOID)))
        for (entry in entries) {
            addItem(entry)
        }
    }

    private fun createEntityItem(entityType: EntityType<*>, eggStack: ItemStack): ItemStack {
        // Delegate to the ID-based helper to avoid duplication
        val entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType)
        return createEntityItemFromId(entityTypeId.path, eggStack)
    }

    /**
     * Create an entity-selection item for entries that don't have spawn eggs (eg. player, armor_stand).
     */
    private fun createEntityItemFromId(entityTypeId: String, baseStack: ItemStack): ItemStack {
        val stack = baseStack.copy()

        val nbt = CompoundTag()
        nbt.putString("EntityType", entityTypeId)
        stack.setCustomData(nbt)

        val displayName = TextUtils.capitalizeType(entityTypeId)
        stack.setCustomName(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false), displayName)
        setLore(stack, entityTypeId)
        return stack
    }

    private fun setLore(stack: ItemStack, typeId: String?) {
        val lore: MutableList<Component> = ArrayList<Component>()
        if (info.type.contains(typeId)) {
            lore.add(Component.literal("✓ Currently selected").setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)))
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
        } else {
            lore.add(Component.literal("Click to select").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))
        }
        stack.set(DataComponents.LORE, ItemLore(lore, lore))
    }

    override fun onItemClick(stack: ItemStack?, button: Int) {
        if (stack == null || stack.isEmpty) return

        // Retrieve entity type from custom data
        val nbt = stack.getCustomData()
        if (nbt == null || !nbt.contains("EntityType")) return

        var entityTypeId = nbt.getString("EntityType").orElse(null) ?: return

        if (entityTypeId == "none") {
            info.type.clear()
        } else {
            entityTypeId = entityTypeId.stripPrefix("minecraft:")
            if (info.type.contains(entityTypeId)) {
                info.type.remove(entityTypeId)
            } else {
                info.type.add(entityTypeId)
            }
        }
        rebuild()
    }
}

