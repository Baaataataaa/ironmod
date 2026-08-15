package com.ironmod.event;

import com.ironmod.IronMod;
import com.ironmod.item.ChargedBeamItem;
import com.ironmod.item.ModularArmorItem;
import com.ironmod.item.ModuleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Aplica os efeitos de cada módulo instalado nas peças de armadura
 * que o jogador está vestindo: voo, repulsor, escudo, imunidades,
 * modo sentinela, etc.
 */
@Mod.EventBusSubscriber(modid = IronMod.MOD_ID)
public class ModuleEffectHandler {

    private static final int NANOTECH_REPAIR_INTERVAL = 100; // 5s
    private static final float NANOTECH_SHIELD_REDUCTION = 0.25F; // reduz 25% do dano

    private static final int REPULSOR_COOLDOWN_TICKS = 30; // 1.5s entre disparos
    private static final float REPULSOR_DAMAGE = 12.0F;
    private static final double REPULSOR_RANGE = 20.0;
    private static final String REPULSOR_COOLDOWN_TAG = "ironmod_repulsor_cooldown";

    private static final float SHIELD_MAX_ABSORPTION = 14.0F; // 7 corações extras
    private static final float SHIELD_REGEN_PER_TICK = 0.05F; // recarrega aos poucos
    private static final int SHIELD_RECOVERY_DELAY = 60; // 3s sem levar dano pra começar a recarregar
    private static final String SHIELD_LAST_HIT_TAG = "ironmod_shield_last_hit";

    private static final String SENTINEL_ACTIVE_TAG = "ironmod_sentinel_active";
    private static final String SENTINEL_LAST_SNEAK_TAG = "ironmod_sentinel_last_sneak";
    private static final String SENTINEL_WAS_SNEAKING_TAG = "ironmod_sentinel_was_sneaking";
    private static final String SENTINEL_COOLDOWN_TAG = "ironmod_sentinel_cooldown";
    private static final int SENTINEL_DOUBLE_TAP_WINDOW = 10; // ticks pra contar como duplo-toque
    private static final int SENTINEL_TOGGLE_COOLDOWN = 40; // 2s entre ligar/desligar

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player.level().isClientSide) return;

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);

        handlePropulsion(player, boots);
        handleNightVision(player, helmet);
        handleNanotechRepair(player);
        handleFireImmunity(player, chest);
        handleWaterBreathing(player, helmet);
        handleMetalSkin(player, legs);
        handleShield(player, chest, helmet);
        handleSentinelToggleDetection(player, chest);
        handleSentinelActiveEffects(player);
        tickCooldown(player, REPULSOR_COOLDOWN_TAG);
        tickCooldown(player, SENTINEL_COOLDOWN_TAG);
        ChargedBeamItem.tickCooldown(player);
    }

    private static void tickCooldown(Player player, String tag) {
        int value = player.getPersistentData().getInt(tag);
        if (value > 0) {
            player.getPersistentData().putInt(tag, value - 1);
        }
    }

    // ---------------- Propulsão (voo) ----------------

    private static void handlePropulsion(Player player, ItemStack boots) {
        boolean hasPropulsion = !boots.isEmpty() && ModularArmorItem.hasModule(boots, ModuleType.PROPULSION);
        if (hasPropulsion && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        } else if (!hasPropulsion && player.getAbilities().mayfly && !player.isCreative() && !player.isSpectator()) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        if (hasPropulsion && player.getAbilities().flying) {
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.15, 0.05, 0.15, 0.01);
            }
            if (player.tickCount % 10 == 0) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                        SoundSource.PLAYERS, 0.4F, 1.8F);
            }
            if (player.isSprinting()) {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(player.getDeltaMovement().add(look.x * 0.06, look.y * 0.06, look.z * 0.06));
            }
        }
    }

    // ---------------- Visão noturna ----------------

    private static void handleNightVision(Player player, ItemStack helmet) {
        if (!helmet.isEmpty() && ModularArmorItem.hasModule(helmet, ModuleType.NIGHT_VISION)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
        }
    }

    // ---------------- Nanotecnologia: auto-reparo ----------------

    private static void handleNanotechRepair(Player player) {
        if (player.tickCount % NANOTECH_REPAIR_INTERVAL != 0) return;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack piece = player.getItemBySlot(slot);
            if (!piece.isEmpty() && ModularArmorItem.hasModule(piece, ModuleType.NANOTECH) && piece.isDamaged()) {
                piece.setDamageValue(Math.max(0, piece.getDamageValue() - 1));
            }
        }
    }

    // ---------------- Imunidade a fogo ----------------

    private static void handleFireImmunity(Player player, ItemStack chest) {
        if (!chest.isEmpty() && ModularArmorItem.hasModule(chest, ModuleType.FIRE_IMMUNITY)) {
            if (player.getRemainingFireTicks() > 0) {
                player.setRemainingFireTicks(0);
            }
        }
    }

    // ---------------- Respiração aquática ----------------

    private static void handleWaterBreathing(Player player, ItemStack helmet) {
        if (!helmet.isEmpty() && ModularArmorItem.hasModule(helmet, ModuleType.WATER_BREATHING)) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 220, 0, false, false, false));
        }
    }

    // ---------------- Pele de metal: imune a veneno/wither ----------------

    private static void handleMetalSkin(Player player, ItemStack legs) {
        if (!legs.isEmpty() && ModularArmorItem.hasModule(legs, ModuleType.METAL_SKIN)) {
            if (player.hasEffect(MobEffects.POISON)) player.removeEffect(MobEffects.POISON);
            if (player.hasEffect(MobEffects.WITHER)) player.removeEffect(MobEffects.WITHER);
        }
    }

    // ---------------- Escudo de energia ----------------

    private static void handleShield(Player player, ItemStack chest, ItemStack helmet) {
        boolean hasShield = (!chest.isEmpty() && ModularArmorItem.hasModule(chest, ModuleType.SHIELD));
        if (!hasShield) return;

        int lastHitTick = player.getPersistentData().getInt(SHIELD_LAST_HIT_TAG);
        int ticksSinceHit = player.tickCount - lastHitTick;

        if (ticksSinceHit >= SHIELD_RECOVERY_DELAY && player.getAbsorptionAmount() < SHIELD_MAX_ABSORPTION) {
            player.setAbsorptionAmount(Math.min(SHIELD_MAX_ABSORPTION, player.getAbsorptionAmount() + SHIELD_REGEN_PER_TICK));
        }
    }

    // ---------------- Modo Sentinela ----------------

    /** Detecta duplo-toque no Shift (sneak) pra ligar/desligar o Modo Sentinela. */
    private static void handleSentinelToggleDetection(Player player, ItemStack chest) {
        boolean hasSentinelModule = !chest.isEmpty() && ModularArmorItem.hasModule(chest, ModuleType.SENTINEL_MODE);
        boolean wasSneaking = player.getPersistentData().getBoolean(SENTINEL_WAS_SNEAKING_TAG);
        boolean isSneaking = player.isShiftKeyDown();

        if (isSneaking && !wasSneaking) {
            int lastTap = player.getPersistentData().getInt(SENTINEL_LAST_SNEAK_TAG);
            int sinceLastTap = player.tickCount - lastTap;

            if (hasSentinelModule && sinceLastTap <= SENTINEL_DOUBLE_TAP_WINDOW
                    && player.getPersistentData().getInt(SENTINEL_COOLDOWN_TAG) <= 0) {
                toggleSentinelMode(player);
            }
            player.getPersistentData().putInt(SENTINEL_LAST_SNEAK_TAG, player.tickCount);
        }

        player.getPersistentData().putBoolean(SENTINEL_WAS_SNEAKING_TAG, isSneaking);

        // Se perdeu o módulo (tirou a peitoral) e o modo tava ligado, desliga automaticamente
        if (!hasSentinelModule && player.getPersistentData().getBoolean(SENTINEL_ACTIVE_TAG)) {
            deactivateSentinel(player);
        }
    }

    private static void toggleSentinelMode(Player player) {
        boolean active = player.getPersistentData().getBoolean(SENTINEL_ACTIVE_TAG);
        if (active) {
            deactivateSentinel(player);
        } else {
            activateSentinel(player);
        }
        player.getPersistentData().putInt(SENTINEL_COOLDOWN_TAG, SENTINEL_TOGGLE_COOLDOWN);
    }

    private static void activateSentinel(Player player) {
        player.getPersistentData().putBoolean(SENTINEL_ACTIVE_TAG, true);
        player.level().playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR,
                SoundSource.PLAYERS, 0.6F, 0.7F);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 1, player.getZ(), 40, 0.5, 0.8, 0.5, 0.05);
        }
    }

    private static void deactivateSentinel(Player player) {
        player.getPersistentData().putBoolean(SENTINEL_ACTIVE_TAG, false);
        player.removeEffect(MobEffects.DAMAGE_BOOST);
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.6F, 1.2F);
    }

    private static void handleSentinelActiveEffects(Player player) {
        if (!player.getPersistentData().getBoolean(SENTINEL_ACTIVE_TAG)) return;

        // Reaplica os buffs continuamente enquanto ativo (duração curta, então
        // se o modo for desligado ou o jogador morrer, o efeito acaba rápido)
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30, 1, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30, 0, false, false, false));

        if (player.tickCount % 15 == 0 && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    player.getX(), player.getY() + 1, player.getZ(), 6, 0.4, 0.6, 0.4, 0.02);
        }
    }

    // ---------------- Eventos de dano ----------------

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        boolean hasShockAbsorber = (!legs.isEmpty() && ModularArmorItem.hasModule(legs, ModuleType.SHOCK_ABSORBER))
                || (!boots.isEmpty() && ModularArmorItem.hasModule(boots, ModuleType.SHOCK_ABSORBER));

        if (hasShockAbsorber) {
            event.setDamageMultiplier(0.0F);
        }
    }

    /** Cancela dano de projéteis (flechas, bolas de fogo, etc.) se o módulo PROJECTILE_IMMUNITY estiver instalado. */
    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        Entity direct = event.getSource().getDirectEntity();
        if (!(direct instanceof Projectile)) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.isEmpty() && ModularArmorItem.hasModule(chest, ModuleType.PROJECTILE_IMMUNITY)) {
            event.setCanceled(true);
        }
    }

    /** Reduz o knockback recebido se a peça de pernas tiver o módulo METAL_SKIN. */
    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        if (!legs.isEmpty() && ModularArmorItem.hasModule(legs, ModuleType.METAL_SKIN)) {
            event.setStrength(event.getStrength() * 0.4F);
        }
    }

    /**
     * Escudo de nanotecnologia (redução de dano) e marca o tick do último
     * golpe pra controlar a recarga do escudo de energia.
     */
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        player.getPersistentData().putInt(SHIELD_LAST_HIT_TAG, player.tickCount);

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        boolean hasNanotech = (!chest.isEmpty() && ModularArmorItem.hasModule(chest, ModuleType.NANOTECH))
                || (!helmet.isEmpty() && ModularArmorItem.hasModule(helmet, ModuleType.NANOTECH));

        if (hasNanotech) {
            event.setAmount(event.getAmount() * (1.0F - NANOTECH_SHIELD_REDUCTION));

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1, player.getZ(), 8, 0.4, 0.5, 0.4, 0.02);
            }
        }
    }

    // ---------------- Repulsor ----------------

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (!player.isShiftKeyDown()) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !ModularArmorItem.hasModule(chest, ModuleType.REPULSOR)) return;

        if (player.getPersistentData().getInt(REPULSOR_COOLDOWN_TAG) > 0) {
            return; // ainda recarregando
        }

        player.getPersistentData().putInt(REPULSOR_COOLDOWN_TAG, REPULSOR_COOLDOWN_TICKS);
        fireRepulsorBlast(player);
    }

    private static void fireRepulsorBlast(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 origin = player.getEyePosition();
        Vec3 end = origin.add(look.scale(REPULSOR_RANGE));

        AABB blastBox = new AABB(origin, end).inflate(1.2);
        List<Entity> hitEntities = player.level().getEntities(player, blastBox);

        for (Entity target : hitEntities) {
            if (target instanceof LivingEntity living) {
                Vec3 toTarget = living.position().subtract(origin).normalize();
                if (toTarget.dot(look) > 0.5) {
                    living.hurt(player.damageSources().playerAttack(player), REPULSOR_DAMAGE);
                    living.knockback(1.2, -look.x, -look.z);
                }
            }
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 1; i <= (int) REPULSOR_RANGE; i++) {
                Vec3 point = origin.add(look.scale(i));
                serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 2, 0.1, 0.1, 0.1, 0.01);
            }
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.PLAYERS, 0.8F, 1.6F);
    }
}
