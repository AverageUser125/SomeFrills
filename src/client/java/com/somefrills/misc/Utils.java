package com.somefrills.misc;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.somefrills.Main;
import com.somefrills.events.WorldRenderEvent;
import com.somefrills.mixin.BossBarHudAccessor;
import com.somefrills.mixin.HandledScreenAccessor;
import com.somefrills.mixin.PlayerListHudAccessor;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.entity.SimpleEntityLookup;
import org.joml.Vector2d;

import java.io.IOException;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.somefrills.Main.mc;

@SuppressWarnings("unused")
public class Utils {
    public static final MessageIndicator someFrillsIndicator = new MessageIndicator(0x5ca0bf, null, Text.of("Message from SomeFrills mod."), "SomeFrills Mod");
    private static final HashSet<String> lootIslands = Sets.newHashSet(
            "Catacombs",
            "Kuudra",
            "Dungeon Hub",
            "Crimson Isle"
    );
    // Helper data and utilities for player/name validation and color parsing
    private static final java.util.Set<String> EXTRA_DISPLAY_NPC_BY_NAME = java.util.Set.of(
            "Guy ",
            "vswiblxdxg",
            "anrrtqytsl"
    );
    private static final Pattern DISPLAY_NPC_COMPRESSED_NAME_PATTERN = Pattern.compile("[a-z0-9]{10}");

    public static void clickSlot(int slotIdx) {
        if (mc.interactionManager == null || mc.player == null) {
            return;
        }

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slotIdx,
                0,
                SlotActionType.PICKUP,
                mc.player
        );
    }

    public static void showTitle(String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        mc.inGameHud.setTitle(Text.of(title));
        mc.inGameHud.setSubtitle(Text.of(subtitle));
        mc.inGameHud.setTitleTicks(fadeInTicks, stayTicks, fadeOutTicks);
    }

    public static void showTitleCustom(String title, int stayTicks, int yOffset, float scale, RenderColor color) {
        ((TitleRendering) mc.inGameHud).somefrills_mod$setRenderTitle(title, stayTicks, yOffset, scale, color);
    }

    public static boolean isRenderingCustomTitle() {
        return ((TitleRendering) mc.inGameHud).somefrills_mod$isRenderingTitle();
    }

    public static boolean isNearlyEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }

    public static boolean isNearlyEqual(double a, double b) {
        return isNearlyEqual(a, b, 1e-9);
    }

    public static boolean isNearlyEqual(float a, float b) {
        return isNearlyEqual(a, b, 1e-5f);
    }

    public static void playSound(SoundEvent event, float volume, float pitch) {
        mc.getSoundManager().play(PositionedSoundInstance.master(event, pitch, volume));
    }

    public static void playSound(RegistryEntry.Reference<SoundEvent> event, float volume, float pitch) {
        playSound(event.value(), volume, pitch);
    }

    public static void playSound(String event, float volume, float pitch) {
        playSound(SoundEvent.of(Identifier.of(event)), volume, pitch);
    }

    public static String humanize(String in) {
        if (in == null || in.isEmpty()) return "";
        String withSpaces = in.replace('_', ' ').replace('-', ' ');
        StringBuilder out = new StringBuilder();
        char prev = ' ';
        for (int i = 0; i < withSpaces.length(); i++) {
            char c = withSpaces.charAt(i);
            if (i > 0 && Character.isUpperCase(c) && (Character.isLowerCase(prev) || Character.isDigit(prev)))
                out.append(' ');
            out.append(c);
            prev = c;
        }
        String res = out.toString().trim();
        if (res.isEmpty()) return res;
        return Character.toUpperCase(res.charAt(0)) + res.substring(1);
    }

    public static void sendMessage(String message) {
        if (mc.player != null && !message.isEmpty()) {
            if (message.startsWith("/")) {
                mc.player.networkHandler.sendChatCommand(message.substring(1));
            } else {
                mc.player.networkHandler.sendChatMessage(message);
            }
        }
    }

    public static Formatting parseColor(String input) {
        if (input == null) return null;

        input = input.toLowerCase().replace(" ", "_");

        switch (input) {
            case "black":
                return Formatting.BLACK;
            case "dark_blue":
            case "navy":
                return Formatting.DARK_BLUE;
            case "dark_green":
                return Formatting.DARK_GREEN;
            case "dark_aqua":
            case "cyan":
                return Formatting.DARK_AQUA;
            case "dark_red":
                return Formatting.DARK_RED;
            case "dark_purple":
            case "purple":
                return Formatting.DARK_PURPLE;
            case "gold":
            case "orange":
                return Formatting.GOLD;
            case "gray":
            case "grey":
                return Formatting.GRAY;
            case "dark_gray":
            case "dark_grey":
                return Formatting.DARK_GRAY;
            case "blue":
                return Formatting.BLUE;
            case "green":
            case "lime":
                return Formatting.GREEN;
            case "aqua":
            case "light_aqua":
                return Formatting.AQUA;
            case "red":
                return Formatting.RED;
            case "light_purple":
            case "pink":
                return Formatting.LIGHT_PURPLE;
            case "yellow":
                return Formatting.YELLOW;
            case "white":
                return Formatting.WHITE;
        }

        // fallback: try enum directly (only return if it's actually a color)
        try {
            Formatting f = Formatting.valueOf(input.toUpperCase());
            return f.isColor() ? f : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void refillItem(String refill_query, int amount) {
        int total = 0;
        if (mc.player == null) return;
        PlayerInventory inv = mc.player.getInventory();
        String query = refill_query.replace("_", " ");
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            String id = Utils.getSkyblockId(stack).replace("_", " ");
            String name = Utils.toPlain(stack.getName());
            if (query.equalsIgnoreCase(id) || query.equalsIgnoreCase(name)) {
                total += stack.getCount();
            }
        }
        if (total < amount) {
            Utils.sendMessage(Utils.format("/gfs {} {}", refill_query, amount - total));
        }
    }

    public static boolean isStainedGlass(ItemStack stack) {
        return stack.getItem().toString().endsWith("stained_glass");
    }

    public static boolean isStainedGlassPane(ItemStack stack) {
        return stack.getItem().toString().endsWith("stained_glass_pane");
    }

    public static boolean isStainedGlass(BlockState state) {
        return isStainedGlass(state.getBlock());
    }

    public static boolean isStainedGlass(Block block) {
        return block instanceof StainedGlassBlock || block instanceof StainedGlassPaneBlock;
    }

    public static List<String> getToolTip(ItemStack stack) {
        List<String> tooltip = new ArrayList<>();
        if(mc.player == null) return tooltip;
        Item.TooltipContext context = Item.TooltipContext.create(mc.player.getRegistryManager());
        List<Text> toolTipLines = stack.getTooltip(context, mc.player, TooltipType.ADVANCED);
        for (Text line : toolTipLines) {
            String text = line.getString();
            tooltip.add(text);
        }
        return tooltip;
    }

    public static MutableText getTag() {
        return Text.literal("[SomeFrills] ").withColor(0x5ca0bf);
    }

    public static MutableText getShortTag() {
        return Text.literal("[SF] ").withColor(0x5ca0bf);
    }

    public static void info(String message) {
        infoRaw(Text.literal(message));
    }

    public static void infoButton(String message, String command) {
        ClickEvent click = new ClickEvent.RunCommand(command);
        infoRaw(Text.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static void infoLink(String message, String url) {
        ClickEvent click = new ClickEvent.OpenUrl(URI.create(url));
        infoRaw(Text.literal(message).setStyle(Style.EMPTY.withClickEvent(click)));
    }

    public static void infoRaw(MutableText message) {
        if (message.getStyle() == null || message.getStyle().getColor() == null) {
            message = message.withColor(0xffffff);
        }
        mc.inGameHud.getChatHud().addMessage(getTag().append(message), null, someFrillsIndicator);
    }

    public static void infoFormat(String message, Object... values) {
        infoRaw(Text.literal(format(message, values)));
    }

    public static String getCordsFormatted(String format) {
        if (mc.player == null) return "";
        BlockPos pos = mc.player.getBlockPos();
        return format(format, pos.getX(), pos.getY(), pos.getZ());

    }

    public static boolean isInZone(String zone, boolean containsCheck) {
        if (containsCheck) {
            return SkyblockData.getLocation().contains(zone);
        }
        return SkyblockData.getLocation().startsWith(zone);
    }

    /**
     * Checks if the provided location matches with the current area on the tab list. For example, isInArea("Private Island") is true if "Area: Private Island" is on the tab list.
     */
    public static boolean isInArea(String area) {
        return SkyblockData.getArea().equals(area);
    }

    public static boolean isInDungeons() {
        return isInArea("Catacombs");
    }

    /**
     * Returns true if the current island has either loot chests or the Croesus NPC.
     */
    public static boolean isInLootArea() {
        return lootIslands.contains(SkyblockData.getArea());
    }

    public static boolean isInKuudra() {
        return isInArea("Kuudra");
    }

    public static boolean isInChateau() {
        return isInZone(Symbols.zoneRift + " Stillgore Château", false) || isInZone(Symbols.zoneRift + " Oubliette", false);
    }

    public static boolean isOnPrivateIsland() {
        return isInZone(Symbols.zone + " Your Island", false);
    }

    /**
     * Returns true if the player is on any of their garden plots, which doesn't count the barn.
     */
    public static boolean isOnGardenPlot() {
        return SkyblockData.getLines().stream().anyMatch(line -> line.contains("Plot -"));
    }

    /**
     * Returns true if the player is anywhere on their garden
     */
    public static boolean isInGarden() {
        return isInArea("Garden");
    }

    public static boolean isInHub() {
        return isInArea("Hub");
    }

    public static boolean isInstanceOver() {
        return SkyblockData.isInstanceOver();
    }

    public static boolean isInSkyblock() {
        return SkyblockData.isInSkyblock();
    }

    public static boolean isOnHypixel() {
        ServerInfo info = mc.getCurrentServerEntry();
        return info != null && toLower(info.address).endsWith("hypixel.net");
    }

    /**
     * Checks if a PlayerEntity is a real player, and not an enemy or NPC. Some NPCs might falsely return true for a few seconds after spawning.
     */
    public static boolean IsRealPlayer(PlayerEntity entity) {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return entity == mc.player;

        var uuid = entity.getUuid();
        if (uuid == null || uuid.version() != 4) return false;

        PlayerListEntry listEntry = handler.getPlayerListEntry(uuid);
        if (listEntry == null) return entity == mc.player;

        String displayName = listEntry.getProfile().name();
        if (displayName == null) return false;

        String name = Formatting.strip(displayName);
        return !name.isEmpty() && !name.contains(" ");
    }

    /**
     * Check if the provided entity is a living entity (and in the case of player entities, if it isn't a real player).
     */
    public static boolean isMob(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            return !IsRealPlayer(player);
        }
        return entity instanceof LivingEntity;
    }

    public static boolean isBaseHealth(LivingEntity entity, float health) {
        float current = entity.getHealth();
        float difference = current - health;
        return current >= health && (current % health == 0 || (current - difference) % health == 0);
    }

    public static Box getLerpedBox(Entity entity, WorldRenderEvent event) {
        return Utils.getLerpedBox(entity, event.tickCounter.getTickProgress(true));
    }

    /**
     * Returns the entity's bounding box at their interpolated position.
     */
    public static Box getLerpedBox(Entity entity, float tickProgress) {
        return entity.getDimensions(EntityPose.STANDING).getBoxAt(entity.getLerpedPos(tickProgress));
    }

    public static List<Entity> getEntities() {
        if (mc.world != null) {
            SimpleEntityLookup<Entity> lookup = (SimpleEntityLookup<Entity>) mc.world.entityManager.getLookup();
            return new ArrayList<>(lookup.index.idToEntity.values());
        }
        return new ArrayList<>();
    }

    public static List<Entity> getOtherEntities(Entity except, Box box, Predicate<? super Entity> filter) {
        List<Entity> entities = new ArrayList<>();
        for (Entity ent : getEntities()) {
            if (ent != null && ent != except && (filter == null || filter.test(ent)) && ent.getBoundingBox().intersects(box)) {
                entities.add(ent);
            }
        }
        return entities;
    }

    public static List<Entity> getOtherEntities(Entity from, double distX, double distY, double distZ, Predicate<? super Entity> filter) {
        return getOtherEntities(from, Box.of(from.getEntityPos(), distX, distY, distZ), filter);
    }

    public static List<Entity> getOtherEntities(Entity from, double dist, Predicate<? super Entity> filter) {
        return getOtherEntities(from, Box.of(from.getEntityPos(), dist, dist, dist), filter);
    }

    public static float getTextScale(double dist, float base, float scaling) {
        float distScale = (float) (1 + dist * scaling);
        return Math.max(base * distScale, base);
    }

    public static float getTextScale(double dist, float base) {
        return getTextScale(dist, base, 0.1f);
    }

    public static float getTextScale(Vec3d pos, float base, float scaling) {
        if (mc.player != null) {
            return getTextScale(mc.player.getEntityPos().distanceTo(pos), base, scaling);
        }
        return 0.0f;
    }

    public static float getTextScale(Vec3d pos, float base) {
        return getTextScale(pos, base, 0.1f);
    }

    public static boolean matchesKey(KeyBinding binding, KeyInput keyInput, MouseInput mouseInput) {
        return (keyInput != null && binding.matchesKey(keyInput)) || (mouseInput != null && binding.matchesMouse(new Click(0, 0, mouseInput)));
    }

    public static boolean matchesKey(KeyBinding binding, KeyInput keyInput) {
        return matchesKey(binding, keyInput, null);
    }

    public static boolean matchesKey(KeyBinding binding, MouseInput mouseInput) {
        return matchesKey(binding, null, mouseInput);
    }

    public static void sendPingPacket() {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler != null) {
            handler.sendPacket(new QueryPingC2SPacket(Util.getMeasuringTimeMs()));
        }
    }


    /**
     * Returns the armor that the entity is wearing.
     */
    public static List<ItemStack> getEntityArmor(LivingEntity entity) {
        return List.of(
                entity.getEquippedStack(EquipmentSlot.HEAD),
                entity.getEquippedStack(EquipmentSlot.CHEST),
                entity.getEquippedStack(EquipmentSlot.LEGS),
                entity.getEquippedStack(EquipmentSlot.FEET)
        );
    }

    /**
     * Returns the custom data compound of the provided ItemStack, or else null.
     */
    public static NbtCompound getCustomData(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
            if (data != null) {
                return data.nbt;
            }
        }
        return null;
    }

    /**
     * Returns the Skyblock item ID from the provided NbtCompound, or else an empty string.
     */
    public static String getSkyblockId(NbtCompound customData) {
        if (customData != null && customData.contains("id")) {
            return customData.getString("id").orElse("");
        }
        return "";
    }

    /**
     * Returns the Skyblock item ID from the provided ItemStack, or else an empty string.
     */
    public static String getSkyblockId(ItemStack stack) {
        return getSkyblockId(getCustomData(stack));
    }

    /**
     * Tries to parse the Bazaar/Auction ID tied to the name of the item.
     */
    public static String getMarketId(Text text) {
        String name = toPlain(text);
        if (hasItemQuantity(name)) {
            name = name.substring(0, name.lastIndexOf(" ")).trim();
        }
        if (name.startsWith("Enchanted Book (") && name.endsWith(")")) {
            String enchant = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
            String enchantName = toID(enchant.substring(0, enchant.lastIndexOf(" ")));
            int enchantLevel = parseRoman(enchant.substring(enchant.lastIndexOf(" ") + 1));
            Optional<Style> style = getStyle(text, enchant::equals);
            if (style.isPresent() && hasColor(style.get(), Formatting.LIGHT_PURPLE) && !enchantName.startsWith("ULTIMATE_")) {
                return format("ENCHANTMENT_ULTIMATE_{}_{}", enchantName, enchantLevel);
            }
            return format("ENCHANTMENT_{}_{}", enchantName, enchantLevel);
        }
        if (name.endsWith(" Essence")) {
            return format("ESSENCE_{}", toID(name.substring(0, name.lastIndexOf(" "))));
        }
        if (name.endsWith(" Dye")) {
            return format("DYE_{}", toID(name.substring(0, name.lastIndexOf(" "))));
        }
        if (name.startsWith("Master Skull - Tier ")) {
            return toID(name.replace(" - ", " "));
        }
        if (name.startsWith("[Lvl 1] ")) {
            String petName = name.substring(name.indexOf("]") + 2);
            Optional<Style> styleOptional = getStyle(text, petName::equals);
            String rarity = "COMMON";
            if (styleOptional.isPresent()) {
                Style style = styleOptional.get();
                if (hasColor(style, Formatting.GOLD)) rarity = "LEGENDARY";
                if (hasColor(style, Formatting.DARK_PURPLE)) rarity = "EPIC";
                if (hasColor(style, Formatting.BLUE)) rarity = "RARE";
                if (hasColor(style, Formatting.GREEN)) rarity = "UNCOMMON";
            }
            return format("{}_PET_{}", toID(petName), rarity);
        }
        if (name.endsWith(" Shard")) {
            return ShardData.getId(name);
        }
        return switch (name) {
            case "Shadow Warp" -> "SHADOW_WARP_SCROLL";
            case "Wither Shield" -> "WITHER_SHIELD_SCROLL";
            case "Implosion" -> "IMPLOSION_SCROLL";
            case "Giant's Sword" -> "GIANTS_SWORD";
            case "Warped Stone" -> "AOTE_STONE";
            case "Spirit Boots" -> "THORNS_BOOTS";
            case "Spirit Shortbow" -> "ITEM_SPIRIT_BOW";
            case "Spirit Stone" -> "SPIRIT_DECOY";
            case "Adaptive Blade" -> "STONE_BLADE";
            default -> toID(name);
        };
    }

    /**
     * Returns the Bazaar/Auction ID tied to the item.
     */
    public static String getMarketId(ItemStack stack) {
        NbtCompound data = getCustomData(stack);
        String id = getSkyblockId(data);
        String shardId = ShardData.getId(stack);
        if (!shardId.isEmpty()) {
            return shardId;
        }
        switch (id) {
            case "PET" -> {
                String petInfo = data.getString("petInfo").orElse("");
                if (!petInfo.isEmpty()) {
                    JsonObject petData = JsonParser.parseString(petInfo).getAsJsonObject();
                    return format("{}_PET_{}", petData.get("type").getAsString(), petData.get("tier").getAsString());
                }
                return "UNKNOWN_PET";
            }
            case "RUNE", "UNIQUE_RUNE" -> {
                NbtCompound runeData = data.getCompound("runes").orElse(null);
                if (runeData != null) {
                    String runeId = (String) runeData.getKeys().toArray()[0];
                    return format("{}_{}_RUNE", runeId, runeData.getInt(runeId).orElse(0));
                }
                return "EMPTY_RUNE";
            }
            case "ENCHANTED_BOOK" -> {
                NbtCompound enchantData = data.getCompound("enchantments").orElse(null);
                if (enchantData != null) {
                    Set<String> enchants = enchantData.getKeys();
                    if (enchants.size() == 1) {
                        String enchantId = (String) enchantData.getKeys().toArray()[0];
                        int enchantLevel = enchantData.getInt(enchantId).orElse(0);
                        return format("ENCHANTMENT_{}_{}", toUpper(enchantId), enchantLevel);
                    }
                }
                return "ENCHANTMENT_UNKNOWN";
            }
            case "POTION" -> {
                String potion = data.getString("potion").orElse("");
                if (!potion.isEmpty()) {
                    return format("{}_{}_POTION",
                            toUpper(potion),
                            data.getInt("potion_level").orElse(0)
                    );
                }
                return "UNKNOWN_POTION";
            }
        }
        return id;
    }

    public static boolean hasItemQuantity(String name) {
        return Pattern.matches(".* x[0-9]*", name);
    }

    public static GameProfile getTextures(ItemStack stack) {
        ProfileComponent profile = stack.getComponents().get(DataComponentTypes.PROFILE);
        if (!stack.isEmpty() && profile != null) {
            return profile.getGameProfile();
        }
        return null;
    }

    public static String getTextureUrl(GameProfile profile) {
        if (profile != null) {
            MinecraftSessionService service = mc.getApiServices().sessionService();
            Property property = service.getPackedTextures(profile);
            MinecraftProfileTextures textures = service.unpackTextures(property);
            if (textures.skin() != null) {
                return textures.skin().getUrl();
            }
        }
        return "";
    }

    public static String getTextureUrl(ItemStack stack) {
        return getTextureUrl(getTextures(stack));
    }

    public static boolean isTextureEqual(GameProfile profile, String textureId) {
        String url = getTextureUrl(profile);
        if (url != null) {
            return url.endsWith("texture/" + textureId);
        }
        return false;
    }

    public static List<Text> getLoreText(ItemStack stack) {
        LoreComponent lore = stack.getComponents().get(DataComponentTypes.LORE);
        if (lore != null) {
            return lore.lines();
        }
        return new ArrayList<>();
    }

    /**
     * Returns every line of the stack's lore with no formatting, or else an empty list.
     */
    public static List<String> getLoreLines(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        for (Text line : getLoreText(stack)) {
            lines.add(toPlain(line).trim());
        }
        return lines;
    }

    /**
     * Returns the right click ability line if found in the item's lore, or else an empty string.
     */
    public static String getRightClickAbility(ItemStack stack) {
        for (String line : getLoreLines(stack)) {
            if (line.contains("Ability: ") && line.endsWith("RIGHT CLICK")) {
                return line;
            }
        }
        return "";
    }

    public static boolean hasRightClickAbility(ItemStack stack) {
        return !getRightClickAbility(stack).isEmpty();
    }

    public static boolean hasEitherStat(ItemStack stack, String... stats) {
        List<String> lines = getLoreLines(stack);
        Iterator<String> iterator = Arrays.stream(stats).iterator();
        while (iterator.hasNext()) {
            String stat = iterator.next();
            for (String line : lines) {
                if (line.startsWith(stat + ":")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to find ground (any block that isn't air) below the specified BlockPos, and returns the BlockPos of that block if found. Otherwise, returns the same BlockPos.
     *
     * @param maxDistance The maximum downward Y distance the check will travel
     */
    public static BlockPos findGround(BlockPos pos, int maxDistance) {
        int dist = Math.clamp(maxDistance, 0, 256);
        for (int i = 0; i <= dist; i++) {
            BlockPos below = pos.down(i);
            if (mc.world != null && !mc.world.getBlockState(below).isAir()) {
                return below;
            }
        }
        return pos;
    }

    /**
     * Makes the 1st letter of each word in the string uppercase.
     *
     * @param replaceUnderscores if true, automatically replace all underscores with spaces
     */
    public static String uppercaseFirst(String text, boolean replaceUnderscores) {
        return Arrays.stream(replaceUnderscores ? text.replace("_", " ").split("\\s") : text.split("\\s"))
                .map(word -> Character.toTitleCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" ")).trim();
    }

    public static void atomicWrite(Path path, String content) throws IOException {
        Path parent = path.getParent();
        String fileName = path.getFileName().toString();
        Path tempPath = parent.resolve(Utils.format("{}-Temp-{}.{}",
                fileName.substring(0, fileName.indexOf(".")),
                Util.getMeasuringTimeMs(),
                fileName.substring(fileName.indexOf(".") + 1)
        ));
        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        Files.writeString(tempPath, content);
        try {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
        }
        Files.deleteIfExists(tempPath);
    }


    /**
     * Checks if our player entity is currently within an area, made from 2 sets of coordinates.
     */
    public static boolean isInZone(double x1, double y1, double z1, double x2, double y2, double z2) {
        if (mc.player == null) return false;
        Box area = new Box(x1, y1, z1, x2, y2, z2);
        return area.contains(mc.player.getEntityPos());
    }

    /**
     * Used for Entity mixins to check if the mixin is being applied to our own player entity.
     * <p>
     * <code>
     * if (isSelf(this)) {
     * do stuff...
     * }
     * </code>
     */
    public static boolean isSelf(Object entity) {
        return entity == mc.player;
    }

    public static float horizontalDistance(Vec3d from, Vec3d to) {
        float x = (float) (from.getX() - to.getX());
        float z = (float) (from.getZ() - to.getZ());
        return MathHelper.sqrt(x * x + z * z);
    }

    public static float horizontalDistance(Entity from, Entity to) {
        return horizontalDistance(from.getEntityPos(), to.getEntityPos());
    }

    /**
     * Tries to find the entity that the provided Armor Stand belongs to, based on horizontal distance.
     */
    public static Entity findNametagOwner(Entity ArmorStandEntity, List<Entity> otherEntities) {
        Entity entity = null;
        float lowestDist = 2.0f;
        double maxY = ArmorStandEntity.getEntityPos().getY();
        for (Entity ent : otherEntities) {
            float dist = horizontalDistance(ent.getEntityPos(), ArmorStandEntity.getEntityPos());
            if (!(ent instanceof ArmorStandEntity) && ent.getEntityPos().getY() < maxY && dist < lowestDist) {
                entity = ent;
                lowestDist = dist;
            }
        }
        return entity;
    }

    /**
     * Checks if the provided ItemStack has a glint override flag. Ignores the default flag to work correctly with items such as Nether Stars.
     */
    public static boolean hasGlint(ItemStack stack) {
        Optional<? extends Boolean> component = stack.getComponentChanges().get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        return component != null && component.isPresent();
    }

    public static List<String> getTabListLines() {
        return SkyblockData.getTabListLines();
    }

    /**
     * Returns every line of text from the tab list footer, otherwise an empty list.
     */
    public static List<String> getFooterLines() {
        List<String> list = new ArrayList<>();
        Text footer = ((PlayerListHudAccessor) mc.inGameHud.getPlayerListHud()).getFooter();
        if (footer != null) {
            String[] lines = footer.getString().split("\n");
            for (String line : lines) {
                String l = line.trim();
                if (!l.isEmpty()) {
                    list.add(l);
                }
            }
        }
        return list;
    }

    public static List<ClientBossBar> getBossBars() {
        return ((BossBarHudAccessor) mc.inGameHud.getBossBarHud()).getBossBars().values().stream().toList();
    }

    /**
     * Returns every slot that is part of the container screen handler, excluding the player inventory slots.
     *
     * @param inverse if true, returns the slots that are part of the player inventory instead of the container itself.
     */
    public static List<Slot> getContainerSlots(GenericContainerScreenHandler handler, boolean inverse) {
        if (inverse) {
            return handler.slots.stream().filter(slot -> slot.id >= handler.getRows() * 9).toList();
        }
        return handler.slots.stream().filter(slot -> slot.id < handler.getRows() * 9).toList();
    }

    public static List<Slot> getContainerSlots(GenericContainerScreenHandler handler) {
        return getContainerSlots(handler, false);
    }

    public static ItemStack getHeldItem() {
        return mc.player != null ? mc.player.getMainHandStack() : ItemStack.EMPTY;
    }

    public static Slot getFocusedSlot() {
        return mc.currentScreen != null ? ((HandledScreenAccessor) mc.currentScreen).getFocusedSlot() : null;
    }

    private static int romanToInt(Character roman) {
        return switch (Character.toUpperCase(roman)) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            case 'L' -> 50;
            case 'C' -> 100;
            case 'D' -> 500;
            case 'M' -> 1000;
            default -> 0;
        };
    }

    /**
     * Converts a roman numeral to an integer. Returns 0 if the numeral couldn't be parsed.
     */
    public static int parseRoman(String roman) {
        int result = 0;
        for (int i = 0; i < roman.length(); i++) {
            int number = romanToInt(roman.charAt(i));
            if (number == 0) {
                return 0;
            }
            if (i != roman.length() - 1) {
                int nextNumber = romanToInt(roman.charAt(i + 1));
                if (number < nextNumber) {
                    result -= number;
                } else {
                    result += number;
                }
            } else {
                result += number;
            }
        }
        return result;
    }

    public static String toLower(String string) {
        return string.toLowerCase(Locale.ROOT);
    }

    public static String toUpper(String string) {
        return string.toUpperCase(Locale.ROOT);
    }

    public static String toID(String string) {
        return toUpper(string.replace("'s", "").replace(" ", "_"));
    }

    /**
     * Gets the string out of a Text object and removes any formatting codes.
     */
    public static String toPlain(Text text) {
        if (text != null) {
            return Formatting.strip(text.getString());
        }
        return "";
    }
    public static String toPlain(String text) {
        if (text != null) {
            return Formatting.strip(text);
        }
        return "";
    }
    public static Optional<Style> getStyle(Text text, Predicate<String> predicate) {
        return text.visit((textStyle, textString) -> {
            if (predicate.test(textString)) {
                return Optional.of(textStyle);
            }
            return Optional.empty();
        }, Style.EMPTY);
    }

    public static boolean hasColor(Style style, Formatting color) {
        return color.getColorValue() != null && hasColor(style, color.getColorValue());
    }

    public static boolean hasColor(Style style, int hex) {
        return style != null && style.getColor() != null && style.getColor().getRgb() == hex;
    }

    public static Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseHex(String value) {
        try {
            return Optional.of((int) Long.parseLong(value.replace("0x", ""), 16));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Double> parseDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static String parseDate(Calendar calendar) {
        return format("{} {}",
                calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()),
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault()).format(calendar.getTime())
        );
    }

    public static int difference(int first, int second) {
        return Math.abs(Math.abs(first) - Math.abs(second));
    }

    /**
     * Formats the string by replacing each set of curly brackets "{}" with one of the values in order, similarly to Rust's format macro.
     */
    public static String format(String string, Object... values) {
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (String section : Splitter.on("{}").split(string)) {
            builder.append(section);
            if (index < values.length) {
                builder.append(values[index]);
            }
            index++;
        }
        return builder.toString();
    }

    public static String formatDecimal(double number) {
        return formatDecimal(number, 2);
    }

    public static String formatDecimal(float number) {
        return formatDecimal(number, 2);
    }

    public static String formatDecimal(double number, int spaces) {
        return new DecimalFormat("0." + "0".repeat(spaces)).format(number);
    }

    public static String formatDecimal(float number, int spaces) {
        return formatDecimal((double) number, spaces);
    }

    public static String formatSeparator(long number) {
        return new Formatter().format(Locale.ENGLISH, "%,d", number).toString();
    }

    public static String formatSeparator(int number) {
        return formatSeparator((long) number);
    }

    public static String formatSeparator(double number) {
        return new Formatter().format(Locale.ENGLISH, "%,.1f", number).toString();
    }

    public static String formatSeparator(float number) {
        return formatSeparator((double) number);
    }

    public static String ticksToTime(long ticks) {
        if (ticks < 20) {
            return "0s";
        }
        StringBuilder builder = new StringBuilder();
        long current = ticks;
        String[] units = new String[]{"h", "m", "s"};
        int[] durations = new int[]{72000, 1200, 20};
        for (int i = 0; i <= 2; i++) {
            int amount = 0;
            while (current >= durations[i]) {
                amount++;
                current -= durations[i];
            }
            if (amount > 0) {
                builder.append(amount).append(units[i]);
            }
        }
        return builder.toString();
    }

    public static void setScreen(Screen screen) {
        mc.send(() -> mc.setScreen(screen));
    }

    public static void showGui() {
        setScreen(getGuiScreen());

    }

    public static Screen getGuiScreen() {
        return getGuiScreen(null);
    }

    public static Screen getGuiScreen(Screen previous) {
        var editor = Main.config.getEditor();
        GuiContext guiContext = new GuiContext(new GuiElementComponent(editor));
        return new MoulConfigScreenComponent(Text.empty(), guiContext, previous);
    }

    public static void runCommand(String string) {
        if (mc.player != null && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand(string);
        }
    }

    public static Vector2d getMousePos() {
        return new Vector2d(mc.mouse.getScaledX(mc.getWindow()), mc.mouse.getScaledY(mc.getWindow()));
    }

    public static void setGlowing(Entity ent, boolean shouldGlow, RenderColor color) {
        ((EntityRendering) ent).somefrills_mod$setGlowingColored(shouldGlow, color);
    }

    /**
     * Checks if an entity is drawing the glow effect. Does not account for vanilla/server applied glows.
     */
    public static boolean isGlowing(Entity ent) {
        return ((EntityRendering) ent).somefrills_mod$getGlowing();
    }

    public static String stripPrefix(String str, String prefix) {
        if (str.startsWith(prefix)) {
            return str.substring(prefix.length());
        }
        return str;
    }

    public static class Symbols {
        public static String zone = "⏣";
        public static String zoneRift = "ф";
        public static String star = "✯";
        public static String heart = "❤";
        public static String skull = "☠";
        public static String format = "§";
        public static String vampLow = "҉";
        public static String check = "✔";
        public static String cross = "✖";
        public static String bingo = "Ⓑ";
        public static String aquatic = "⚓";
        public static String magmatic = "♆";
    }
}
