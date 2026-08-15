package com.ironmod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Projétil que "voa" até o dono carregando as peças da armadura e,
 * ao chegar perto, equipa tudo automaticamente. Inspirado na forma
 * como a armadura Mark 7 do Homem de Ferro voa até ele em cena.
 */
public class SummonedArmorProjectile extends Entity {

    private UUID ownerUUID;
    private Player cachedOwner;
    private final List<ItemStack> carriedArmor = new ArrayList<>();
    private int life = 0;
    private static final int MAX_LIFE = 400; // 20s de segurança, evita entidade fantasma

    public SummonedArmorProjectile(EntityType<? extends SummonedArmorProjectile> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void configure(Player owner, List<ItemStack> armorPieces, Vec3 spawnPos) {
        this.ownerUUID = owner.getUUID();
        this.cachedOwner = owner;
        this.carriedArmor.clear();
        this.carriedArmor.addAll(armorPieces);
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
    }

    @Override
    protected void defineSynchedData() {
        // Sem dados sincronizados necessários por enquanto; o efeito acontece no servidor.
    }

    @Override
    public void tick() {
        super.tick();
        life++;

        if (this.level().isClientSide) {
            // Rastro de partículas visual
            this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            return;
        }

        Player owner = getOwnerCached();
        if (owner == null || !owner.isAlive() || life > MAX_LIFE) {
            this.discard();
            return;
        }

        Vec3 target = owner.getEyePosition().subtract(0, 0.3, 0);
        Vec3 toTarget = target.subtract(this.position());
        double distance = toTarget.length();

        if (distance < 1.2) {
            equipOntoOwner(owner);
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.FIREWORK,
                    owner.getX(), owner.getY() + 1, owner.getZ(), 25, 0.3, 0.5, 0.3, 0.05);
            this.level().playSound(null, owner.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            this.discard();
            return;
        }

        // Movimento com "aceleração" pra parecer que ganha velocidade tipo a Mark 7
        double speed = Math.min(0.4 + (life * 0.01), 3.0);
        Vec3 motion = toTarget.normalize().scale(speed);
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
        this.setYRot((float) (Math.atan2(motion.x, motion.z) * (180 / Math.PI)));
    }

    private void equipOntoOwner(Player owner) {
        for (ItemStack piece : carriedArmor) {
            if (piece.isEmpty()) continue;
            EquipmentSlot slot = net.minecraft.world.entity.LivingEntity.getEquipmentSlotForItem(piece);
            if (slot != null && slot.getType() == EquipmentSlot.Type.ARMOR) {
                owner.setItemSlot(slot, piece.copy());
            }
        }
    }

    private Player getOwnerCached() {
        if (cachedOwner != null && cachedOwner.isAlive()) return cachedOwner;
        if (ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(ownerUUID);
            if (e instanceof Player player) {
                cachedOwner = player;
                return player;
            }
        }
        return null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
        this.life = tag.getInt("Life");
        this.carriedArmor.clear();
        ListTag list = tag.getList("CarriedArmor", 10);
        for (int i = 0; i < list.size(); i++) {
            this.carriedArmor.add(ItemStack.of(list.getCompound(i)));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
        tag.putInt("Life", this.life);
        ListTag list = new ListTag();
        for (ItemStack stack : carriedArmor) {
            list.add(stack.save(new CompoundTag()));
        }
        tag.put("CarriedArmor", list);
    }
}
