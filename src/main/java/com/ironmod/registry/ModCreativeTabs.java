package com.ironmod.registry;

import com.ironmod.IronMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IronMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> IRON_SUIT_TAB = CREATIVE_TABS.register("iron_suit_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ironmod"))
                    .icon(() -> new ItemStack(ModItems.IRON_SUIT_HELMET.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.ASSEMBLY_BENCH_ITEM.get());
                        output.accept(ModItems.IRON_SUIT_HELMET.get());
                        output.accept(ModItems.IRON_SUIT_CHESTPLATE.get());
                        output.accept(ModItems.IRON_SUIT_LEGGINGS.get());
                        output.accept(ModItems.IRON_SUIT_BOOTS.get());
                        output.accept(ModItems.MODULE_PROPULSION.get());
                        output.accept(ModItems.MODULE_REPULSOR.get());
                        output.accept(ModItems.MODULE_ARMOR_PLATING.get());
                        output.accept(ModItems.MODULE_PROJECTILE_CALL.get());
                        output.accept(ModItems.MODULE_SHOCK_ABSORBER.get());
                        output.accept(ModItems.MODULE_NIGHT_VISION.get());
                        output.accept(ModItems.MODULE_NANOTECH.get());
                        output.accept(ModItems.MODULE_CHARGED_BEAM.get());
                        output.accept(ModItems.MODULE_FIRE_IMMUNITY.get());
                        output.accept(ModItems.MODULE_PROJECTILE_IMMUNITY.get());
                        output.accept(ModItems.MODULE_WATER_BREATHING.get());
                        output.accept(ModItems.MODULE_METAL_SKIN.get());
                        output.accept(ModItems.MODULE_SHIELD.get());
                        output.accept(ModItems.MODULE_SENTINEL_MODE.get());
                        output.accept(ModItems.MARK_CALL_DEVICE.get());
                        output.accept(ModItems.BEAM_EMITTER.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }
}
