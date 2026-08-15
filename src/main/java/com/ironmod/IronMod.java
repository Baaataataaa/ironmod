package com.ironmod;

import com.ironmod.client.renderer.SummonedArmorProjectileRenderer;
import com.ironmod.client.screen.AssemblyBenchScreen;
import com.ironmod.entity.ModEntities;
import com.ironmod.registry.ModBlocks;
import com.ironmod.registry.ModCreativeTabs;
import com.ironmod.registry.ModItems;
import com.ironmod.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(IronMod.MOD_ID)
public class IronMod {

    public static final String MOD_ID = "ironmod";

    public IronMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRenderers);
        // ModuleEffectHandler é registrado automaticamente via @Mod.EventBusSubscriber
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                MenuScreens.register(ModMenuTypes.ASSEMBLY_BENCH_MENU.get(), AssemblyBenchScreen::new)
        );
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SUMMONED_ARMOR_PROJECTILE.get(), SummonedArmorProjectileRenderer::new);
    }
}
