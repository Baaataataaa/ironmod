package com.ironmod.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.function.Supplier;

/**
 * Material de armadura da Iron Suit: defesa alta, parecido com Diamante
 * mas um pouco mais forte, já que a defesa "real" vem dos módulos.
 */
public enum ModArmorMaterials implements ArmorMaterial {
    IRON_SUIT("ironmod_iron_suit", 40,
            Util.map(ArmorItem.Type.BOOTS, 4, ArmorItem.Type.LEGGINGS, 7, ArmorItem.Type.CHESTPLATE, 9, ArmorItem.Type.HELMET, 4),
            18, SoundEvents.ARMOR_EQUIP_IRON, 2.0F, 0.1F,
            () -> Ingredient.of(net.minecraft.world.item.Items.IRON_INGOT));

    private static final EnumMap<ArmorItem.Type, Integer> BASE_DURABILITY = Util.map(
            ArmorItem.Type.BOOTS, 13, ArmorItem.Type.LEGGINGS, 15,
            ArmorItem.Type.CHESTPLATE, 16, ArmorItem.Type.HELMET, 11);

    private final String name;
    private final int durabilityMultiplier;
    private final EnumMap<ArmorItem.Type, Integer> protectionValues;
    private final int enchantmentValue;
    private final SoundEvent soundEvent;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    ModArmorMaterials(String name, int durabilityMultiplier, EnumMap<ArmorItem.Type, Integer> protectionValues,
                       int enchantmentValue, SoundEvent soundEvent, float toughness, float knockbackResistance,
                       Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.protectionValues = protectionValues;
        this.enchantmentValue = enchantmentValue;
        this.soundEvent = soundEvent;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return BASE_DURABILITY.get(type) * durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return protectionValues.get(type);
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return soundEvent;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }

    private static final class Util {
        static EnumMap<ArmorItem.Type, Integer> map(ArmorItem.Type t1, int v1, ArmorItem.Type t2, int v2,
                                                      ArmorItem.Type t3, int v3, ArmorItem.Type t4, int v4) {
            EnumMap<ArmorItem.Type, Integer> map = new EnumMap<>(ArmorItem.Type.class);
            map.put(t1, v1);
            map.put(t2, v2);
            map.put(t3, v3);
            map.put(t4, v4);
            return map;
        }
    }
}
