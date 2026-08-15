package com.ironmod.registry;

import com.ironmod.IronMod;
import com.ironmod.menu.AssemblyBenchMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, IronMod.MOD_ID);

    public static final RegistryObject<MenuType<AssemblyBenchMenu>> ASSEMBLY_BENCH_MENU =
            MENU_TYPES.register("assembly_bench_menu",
                    () -> IForgeMenuType.create((windowId, inv, data) -> new AssemblyBenchMenu(windowId, inv)));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
