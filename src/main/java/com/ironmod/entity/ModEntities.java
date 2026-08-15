package com.ironmod.entity;

import com.ironmod.IronMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, IronMod.MOD_ID);

    public static final RegistryObject<EntityType<SummonedArmorProjectile>> SUMMONED_ARMOR_PROJECTILE =
            ENTITY_TYPES.register("summoned_armor_projectile",
                    () -> EntityType.Builder.<SummonedArmorProjectile>of(SummonedArmorProjectile::new, MobCategory.MISC)
                            .sized(0.4F, 0.4F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .noSave()
                            .build("summoned_armor_projectile"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
