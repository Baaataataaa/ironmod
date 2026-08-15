package com.ironmod.registry;

import com.ironmod.IronMod;
import com.ironmod.item.ArmorModuleItem;
import com.ironmod.item.MarkCallItem;
import com.ironmod.item.ModArmorMaterials;
import com.ironmod.item.ModularArmorItem;
import com.ironmod.item.ModuleType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, IronMod.MOD_ID);

    // ---- Peças da armadura modular ----
    public static final RegistryObject<Item> IRON_SUIT_HELMET = ITEMS.register("iron_suit_helmet",
            () -> new ModularArmorItem(ModArmorMaterials.IRON_SUIT, ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> IRON_SUIT_CHESTPLATE = ITEMS.register("iron_suit_chestplate",
            () -> new ModularArmorItem(ModArmorMaterials.IRON_SUIT, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> IRON_SUIT_LEGGINGS = ITEMS.register("iron_suit_leggings",
            () -> new ModularArmorItem(ModArmorMaterials.IRON_SUIT, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> IRON_SUIT_BOOTS = ITEMS.register("iron_suit_boots",
            () -> new ModularArmorItem(ModArmorMaterials.IRON_SUIT, ArmorItem.Type.BOOTS,
                    new Item.Properties().stacksTo(1)));

    // ---- Módulos instaláveis ----
    public static final RegistryObject<Item> MODULE_PROPULSION = ITEMS.register("module_propulsion",
            () -> new ArmorModuleItem(ModuleType.PROPULSION, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_REPULSOR = ITEMS.register("module_repulsor",
            () -> new ArmorModuleItem(ModuleType.REPULSOR, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_ARMOR_PLATING = ITEMS.register("module_armor_plating",
            () -> new ArmorModuleItem(ModuleType.ARMOR_PLATING, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_PROJECTILE_CALL = ITEMS.register("module_projectile_call",
            () -> new ArmorModuleItem(ModuleType.PROJECTILE_CALL, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_SHOCK_ABSORBER = ITEMS.register("module_shock_absorber",
            () -> new ArmorModuleItem(ModuleType.SHOCK_ABSORBER, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_NIGHT_VISION = ITEMS.register("module_night_vision",
            () -> new ArmorModuleItem(ModuleType.NIGHT_VISION, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_NANOTECH = ITEMS.register("module_nanotech",
            () -> new ArmorModuleItem(ModuleType.NANOTECH, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_CHARGED_BEAM = ITEMS.register("module_charged_beam",
            () -> new ArmorModuleItem(ModuleType.CHARGED_BEAM, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_FIRE_IMMUNITY = ITEMS.register("module_fire_immunity",
            () -> new ArmorModuleItem(ModuleType.FIRE_IMMUNITY, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_PROJECTILE_IMMUNITY = ITEMS.register("module_projectile_immunity",
            () -> new ArmorModuleItem(ModuleType.PROJECTILE_IMMUNITY, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_WATER_BREATHING = ITEMS.register("module_water_breathing",
            () -> new ArmorModuleItem(ModuleType.WATER_BREATHING, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_METAL_SKIN = ITEMS.register("module_metal_skin",
            () -> new ArmorModuleItem(ModuleType.METAL_SKIN, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_SHIELD = ITEMS.register("module_shield",
            () -> new ArmorModuleItem(ModuleType.SHIELD, new Item.Properties().stacksTo(8)));

    public static final RegistryObject<Item> MODULE_SENTINEL_MODE = ITEMS.register("module_sentinel_mode",
            () -> new ArmorModuleItem(ModuleType.SENTINEL_MODE, new Item.Properties().stacksTo(8)));

    // ---- Item "Chamar Armadura" (estilo Mark 7 - convoca a armadura voando até você) ----
    public static final RegistryObject<Item> MARK_CALL_DEVICE = ITEMS.register("mark_call_device",
            () -> new MarkCallItem(new Item.Properties().stacksTo(1)));

    // ---- Item "Emissor de Feixe" (Unibeam - segura pra carregar, solta pra disparar) ----
    public static final RegistryObject<Item> BEAM_EMITTER = ITEMS.register("beam_emitter",
            () -> new com.ironmod.item.ChargedBeamItem(new Item.Properties().stacksTo(1).durability(200)));

    // ---- BlockItem da bancada ----
    public static final RegistryObject<Item> ASSEMBLY_BENCH_ITEM = ITEMS.register("assembly_bench",
            () -> new BlockItem(ModBlocks.ASSEMBLY_BENCH.get(), new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
