package com.ironmod.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Emissor de feixe carregado, estilo "Unibeam". Segura o clique direito
 * pra carregar (precisa segurar pelo menos CHARGE_TIME_TICKS), solta pra
 * disparar um feixe forte em linha reta. Precisa do módulo CHARGED_BEAM
 * instalado na peitoral e a peitoral vestida.
 */
public class ChargedBeamItem extends Item {

    private static final int CHARGE_TIME_TICKS = 20;   // 1s pra carregar
    private static final int MAX_USE_DURATION = 72000; // "segura até soltar"
    private static final int COOLDOWN_TICKS = 40;       // 2s de recarga
    private static final double RANGE = 32.0;
    private static final float DAMAGE = 22.0F;
    private static final String COOLDOWN_TAG = "ironmod_beam_cooldown";

    public ChargedBeamItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !ModularArmorItem.hasModule(chest, ModuleType.CHARGED_BEAM)) {
            player.displayClientMessage(Component.translatable("message.ironmod.no_beam_module"), true);
            return InteractionResultHolder.fail(stack);
        }

        if (player.getPersistentData().getInt(COOLDOWN_TAG) > 0) {
            player.displayClientMessage(Component.translatable("message.ironmod.beam_recharging"), true);
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return MAX_USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(entity instanceof Player player)) return;

        int used = MAX_USE_DURATION - remainingUseDuration;
        if (used >= CHARGE_TIME_TICKS && level instanceof ServerLevel serverLevel) {
            // Partículas de "carregado", crescendo com o tempo
            Vec3 pos = player.getEyePosition().add(player.getLookAngle().scale(1.2));
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.01);
            if (used % 5 == 0) {
                level.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT,
                        SoundSource.PLAYERS, 0.3F, 2.0F);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide || !(entity instanceof Player player)) return;

        int used = MAX_USE_DURATION - timeLeft;
        if (used < CHARGE_TIME_TICKS) {
            return; // soltou cedo demais, o feixe "fizza" e não dispara
        }

        fireBeam(level, player);
        player.getPersistentData().putInt(COOLDOWN_TAG, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, (p) -> { });
    }

    /** Decai o cooldown do feixe a cada tick do jogador (chamado pelo ModuleEffectHandler). */
    public static void tickCooldown(Player player) {
        int cooldown = player.getPersistentData().getInt(COOLDOWN_TAG);
        if (cooldown > 0) {
            player.getPersistentData().putInt(COOLDOWN_TAG, cooldown - 1);
        }
    }

    private void fireBeam(Level level, Player player) {
        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = origin.add(look.scale(RANGE));

        AABB beamBox = new AABB(origin, end).inflate(1.0);
        List<Entity> hitEntities = level.getEntities(player, beamBox);

        for (Entity target : hitEntities) {
            if (target instanceof LivingEntity living) {
                Vec3 toTarget = living.position().subtract(origin).normalize();
                if (toTarget.dot(look) > 0.85) { // feixe estreito, precisa mirar bem
                    living.hurt(player.damageSources().playerAttack(player), DAMAGE);
                    living.knockback(0.6, -look.x, -look.z);
                }
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            for (double d = 0; d < RANGE; d += 0.5) {
                Vec3 point = origin.add(look.scale(d));
                serverLevel.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
                serverLevel.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS, 1.0F, 0.7F);
    }
}
