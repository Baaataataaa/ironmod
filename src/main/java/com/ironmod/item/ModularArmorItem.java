package com.ironmod.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Peça de armadura da Iron Suit. Cada peça tem "slots de módulo" que
 * podem ser preenchidos na Bancada de Montagem com ArmorModuleItem.
 * Os módulos instalados ficam salvos na NBT do item (tag "IronModModules").
 */
public class ModularArmorItem extends ArmorItem {

    private static final String NBT_MODULES = "IronModModules";

    // Define quantos slots de módulo (e de quais tipos) cada peça de armadura aceita
    private static final Map<EquipmentSlot, EnumSet<ModuleType>> ALLOWED_MODULES = new EnumMap<>(EquipmentSlot.class);
    static {
        ALLOWED_MODULES.put(EquipmentSlot.HEAD, EnumSet.of(
                ModuleType.NIGHT_VISION, ModuleType.ARMOR_PLATING, ModuleType.NANOTECH, ModuleType.WATER_BREATHING));
        ALLOWED_MODULES.put(EquipmentSlot.CHEST, EnumSet.of(
                ModuleType.REPULSOR, ModuleType.PROJECTILE_CALL, ModuleType.ARMOR_PLATING, ModuleType.NANOTECH,
                ModuleType.CHARGED_BEAM, ModuleType.FIRE_IMMUNITY, ModuleType.SHIELD, ModuleType.SENTINEL_MODE,
                ModuleType.PROJECTILE_IMMUNITY));
        ALLOWED_MODULES.put(EquipmentSlot.LEGS, EnumSet.of(
                ModuleType.ARMOR_PLATING, ModuleType.SHOCK_ABSORBER, ModuleType.METAL_SKIN));
        ALLOWED_MODULES.put(EquipmentSlot.FEET, EnumSet.of(
                ModuleType.PROPULSION, ModuleType.SHOCK_ABSORBER));
    }

    // Quantos módulos cabem em cada peça. O peitoral aceita mais, já que
    // concentra a maioria dos poderes de combate/utilidade.
    private static final Map<EquipmentSlot, Integer> MAX_MODULES_PER_PIECE = new EnumMap<>(EquipmentSlot.class);
    static {
        MAX_MODULES_PER_PIECE.put(EquipmentSlot.HEAD, 2);
        MAX_MODULES_PER_PIECE.put(EquipmentSlot.CHEST, 3);
        MAX_MODULES_PER_PIECE.put(EquipmentSlot.LEGS, 2);
        MAX_MODULES_PER_PIECE.put(EquipmentSlot.FEET, 2);
    }

    // UUIDs fixos (um por slot) para os atributos extras dos módulos.
    // Precisam ser fixos e diferentes por slot pra não conflitar quando o
    // jogador usa mais de uma peça modificada ao mesmo tempo.
    private static final Map<EquipmentSlot, UUID> PLATING_ARMOR_UUID = new EnumMap<>(EquipmentSlot.class);
    private static final Map<EquipmentSlot, UUID> PLATING_TOUGHNESS_UUID = new EnumMap<>(EquipmentSlot.class);
    private static final Map<EquipmentSlot, UUID> NANOTECH_TOUGHNESS_UUID = new EnumMap<>(EquipmentSlot.class);
    static {
        PLATING_ARMOR_UUID.put(EquipmentSlot.HEAD, UUID.fromString("a1a1a1a1-0000-0000-0000-000000000001"));
        PLATING_ARMOR_UUID.put(EquipmentSlot.CHEST, UUID.fromString("a1a1a1a1-0000-0000-0000-000000000002"));
        PLATING_ARMOR_UUID.put(EquipmentSlot.LEGS, UUID.fromString("a1a1a1a1-0000-0000-0000-000000000003"));
        PLATING_ARMOR_UUID.put(EquipmentSlot.FEET, UUID.fromString("a1a1a1a1-0000-0000-0000-000000000004"));

        PLATING_TOUGHNESS_UUID.put(EquipmentSlot.HEAD, UUID.fromString("b2b2b2b2-0000-0000-0000-000000000001"));
        PLATING_TOUGHNESS_UUID.put(EquipmentSlot.CHEST, UUID.fromString("b2b2b2b2-0000-0000-0000-000000000002"));
        PLATING_TOUGHNESS_UUID.put(EquipmentSlot.LEGS, UUID.fromString("b2b2b2b2-0000-0000-0000-000000000003"));
        PLATING_TOUGHNESS_UUID.put(EquipmentSlot.FEET, UUID.fromString("b2b2b2b2-0000-0000-0000-000000000004"));

        NANOTECH_TOUGHNESS_UUID.put(EquipmentSlot.HEAD, UUID.fromString("c3c3c3c3-0000-0000-0000-000000000001"));
        NANOTECH_TOUGHNESS_UUID.put(EquipmentSlot.CHEST, UUID.fromString("c3c3c3c3-0000-0000-0000-000000000002"));
    }

    public ModularArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    public static EnumSet<ModuleType> getAllowedModules(EquipmentSlot slot) {
        return ALLOWED_MODULES.getOrDefault(slot, EnumSet.noneOf(ModuleType.class));
    }

    public static int getMaxModules(EquipmentSlot slot) {
        return MAX_MODULES_PER_PIECE.getOrDefault(slot, 2);
    }

    /** Retorna a lista de módulos instalados nessa peça de armadura. */
    public static List<ModuleType> getInstalledModules(ItemStack armorPiece) {
        List<ModuleType> result = new ArrayList<>();
        CompoundTag tag = armorPiece.getTag();
        if (tag == null || !tag.contains(NBT_MODULES)) return result;

        ListTag list = tag.getList(NBT_MODULES, 8); // 8 = tipo string
        for (int i = 0; i < list.size(); i++) {
            ModuleType type = ModuleType.fromId(list.getString(i));
            if (type != null) result.add(type);
        }
        return result;
    }

    public static boolean hasModule(ItemStack armorPiece, ModuleType type) {
        return getInstalledModules(armorPiece).contains(type);
    }

    /**
     * Tenta instalar um módulo na peça. Retorna true se funcionou.
     * Falha se o slot não aceitar esse tipo, se já estiver instalado, ou se
     * não houver espaço livre.
     */
    public static boolean installModule(ItemStack armorPiece, ModuleType type) {
        if (!(armorPiece.getItem() instanceof ModularArmorItem armorItem)) return false;

        EquipmentSlot slot = armorItem.getEquipmentSlot();
        if (!getAllowedModules(slot).contains(type)) return false;

        List<ModuleType> installed = getInstalledModules(armorPiece);
        if (installed.contains(type)) return false;
        if (installed.size() >= getMaxModules(slot)) return false;

        installed.add(type);
        saveModules(armorPiece, installed);
        return true;
    }

    public static boolean removeModule(ItemStack armorPiece, ModuleType type) {
        List<ModuleType> installed = getInstalledModules(armorPiece);
        boolean removed = installed.remove(type);
        if (removed) saveModules(armorPiece, installed);
        return removed;
    }

    private static void saveModules(ItemStack armorPiece, List<ModuleType> modules) {
        CompoundTag tag = armorPiece.getOrCreateTag();
        ListTag list = new ListTag();
        for (ModuleType type : modules) {
            list.add(StringTag.valueOf(type.getId()));
        }
        tag.put(NBT_MODULES, list);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create(super.getAttributeModifiers(slot, stack));

        if (slot == this.getEquipmentSlot()) {
            List<ModuleType> modules = getInstalledModules(stack);

            // Blindagem Reforçada: +3 de armadura e +1 de resistência (toughness)
            if (modules.contains(ModuleType.ARMOR_PLATING)) {
                map.put(Attributes.ARMOR, new AttributeModifier(
                        PLATING_ARMOR_UUID.get(slot), "Blindagem reforçada", 3.0, AttributeModifier.Operation.ADDITION));
                map.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                        PLATING_TOUGHNESS_UUID.get(slot), "Blindagem reforçada (resistência)", 1.0, AttributeModifier.Operation.ADDITION));
            }

            // Nanotecnologia: +1 de resistência a mais (o escudo/dano em si é tratado no ModuleEffectHandler)
            if (modules.contains(ModuleType.NANOTECH) && NANOTECH_TOUGHNESS_UUID.containsKey(slot)) {
                map.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                        NANOTECH_TOUGHNESS_UUID.get(slot), "Nanotecnologia (resistência)", 1.0, AttributeModifier.Operation.ADDITION));
            }
        }

        return map;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        List<ModuleType> modules = getInstalledModules(stack);
        if (modules.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ironmod.no_modules").withStyle(net.minecraft.ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.ironmod.installed_modules").withStyle(net.minecraft.ChatFormatting.GOLD));
            for (ModuleType type : modules) {
                tooltip.add(Component.literal(" - " + type.getDisplayName()).withStyle(net.minecraft.ChatFormatting.AQUA));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
