package com.ironmod.item;

import com.ironmod.entity.ModEntities;
import com.ironmod.entity.SummonedArmorProjectile;
import com.ironmod.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Item "Dispositivo de Chamada" (estilo pulseira/relógio do Tony Stark).
 *
 * Funciona como uma alternância (toggle), igual a maleta/pulseira da
 * Mark 7 no filme:
 *
 *  - Se o jogador JA ESTIVER com a armadura completa equipada e a
 *    peitoral tiver o módulo "Chamada Automática" instalado: usar o
 *    item RETRAI a armadura (guarda ela dentro do próprio dispositivo,
 *    tira do corpo do jogador).
 *
 *  - Se o jogador NAO estiver de armadura: usar o item CONVOCA a
 *    armadura guardada (ou, se não tiver nada guardado ainda, procura
 *    as peças soltas no inventário como alternativa) - um projétil voa
 *    até o jogador e equipa tudo automaticamente.
 */
public class MarkCallItem extends Item {

    private static final String NBT_STORED_SUIT = "IronModStoredSuit";

    public MarkCallItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(heldItem);
        }

        if (isWearingFullSuitWithCallModule(player)) {
            retractSuit(player, heldItem);
            return InteractionResultHolder.success(heldItem);
        }

        List<ItemStack> pieces = getStoredSuit(heldItem);
        if (!pieces.isEmpty()) {
            clearStoredSuit(heldItem);
            summonSuit(level, player, pieces);
            return InteractionResultHolder.success(heldItem);
        }

        // Sem armadura guardada no dispositivo: tenta juntar peças soltas do inventário
        List<ItemStack> inventoryPieces = collectFromInventory(player);
        if (!inventoryPieces.isEmpty()) {
            summonSuit(level, player, inventoryPieces);
            return InteractionResultHolder.success(heldItem);
        }

        player.displayClientMessage(Component.translatable("message.ironmod.no_armor_found"), true);
        return InteractionResultHolder.fail(heldItem);
    }

    /** Verifica se o jogador está com as 4 peças da Iron Suit vestidas e a peitoral tem o módulo de chamada. */
    private boolean isWearingFullSuitWithCallModule(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        boolean fullSuit = helmet.getItem() == ModItems.IRON_SUIT_HELMET.get()
                && chest.getItem() == ModItems.IRON_SUIT_CHESTPLATE.get()
                && legs.getItem() == ModItems.IRON_SUIT_LEGGINGS.get()
                && boots.getItem() == ModItems.IRON_SUIT_BOOTS.get();

        return fullSuit && ModularArmorItem.hasModule(chest, ModuleType.PROJECTILE_CALL);
    }

    /** Tira a armadura do jogador e guarda dentro do próprio dispositivo. */
    private void retractSuit(Player player, ItemStack device) {
        List<ItemStack> pieces = new ArrayList<>();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack piece = player.getItemBySlot(slot);
            pieces.add(piece.copy());
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        setStoredSuit(device, pieces);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 0.8F, 1.2F);
        player.displayClientMessage(Component.translatable("message.ironmod.suit_retracted"), true);
    }

    /** Convoca o projétil que voa até o jogador e equipa a armadura. */
    private void summonSuit(Level level, Player player, List<ItemStack> pieces) {
        Vec3 look = player.getLookAngle();
        Vec3 spawnPos = player.position()
                .subtract(look.scale(20))
                .add(0, 12, 0);

        SummonedArmorProjectile projectile = ModEntities.SUMMONED_ARMOR_PROJECTILE.get().create(level);
        if (projectile != null) {
            projectile.configure(player, pieces, spawnPos);
            level.addFreshEntity(projectile);
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                    SoundSource.PLAYERS, 0.6F, 1.4F);
        }
    }

    private List<ItemStack> collectFromInventory(Player player) {
        int helmetSlot = findArmorPiece(player, ModItems.IRON_SUIT_HELMET.get());
        int chestSlot = findArmorPiece(player, ModItems.IRON_SUIT_CHESTPLATE.get());
        int legsSlot = findArmorPiece(player, ModItems.IRON_SUIT_LEGGINGS.get());
        int bootsSlot = findArmorPiece(player, ModItems.IRON_SUIT_BOOTS.get());

        if (chestSlot < 0) {
            return List.of();
        }

        ItemStack chestStack = player.getInventory().getItem(chestSlot);
        if (!ModularArmorItem.hasModule(chestStack, ModuleType.PROJECTILE_CALL)) {
            player.displayClientMessage(Component.translatable("message.ironmod.no_call_module"), true);
            return List.of();
        }

        List<ItemStack> pieces = new ArrayList<>();
        collectAndRemove(player, helmetSlot, pieces);
        collectAndRemove(player, chestSlot, pieces);
        collectAndRemove(player, legsSlot, pieces);
        collectAndRemove(player, bootsSlot, pieces);
        return pieces;
    }

    private int findArmorPiece(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private void collectAndRemove(Player player, int slot, List<ItemStack> out) {
        if (slot < 0) return;
        ItemStack stack = player.getInventory().getItem(slot);
        if (!stack.isEmpty()) {
            out.add(stack.copy());
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
    }

    // ---- Armazenamento da armadura guardada dentro do próprio dispositivo (NBT) ----

    private List<ItemStack> getStoredSuit(ItemStack device) {
        List<ItemStack> result = new ArrayList<>();
        CompoundTag tag = device.getTag();
        if (tag == null || !tag.contains(NBT_STORED_SUIT)) return result;

        ListTag list = tag.getList(NBT_STORED_SUIT, 10); // 10 = tipo compound
        for (int i = 0; i < list.size(); i++) {
            ItemStack stack = ItemStack.of(list.getCompound(i));
            if (!stack.isEmpty()) result.add(stack);
        }
        return result;
    }

    private void setStoredSuit(ItemStack device, List<ItemStack> pieces) {
        CompoundTag tag = device.getOrCreateTag();
        ListTag list = new ListTag();
        for (ItemStack piece : pieces) {
            if (!piece.isEmpty()) {
                list.add(piece.save(new CompoundTag()));
            }
        }
        tag.put(NBT_STORED_SUIT, list);
    }

    private void clearStoredSuit(ItemStack device) {
        CompoundTag tag = device.getTag();
        if (tag != null) tag.remove(NBT_STORED_SUIT);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        List<ItemStack> stored = getStoredSuit(stack);
        if (!stored.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ironmod.suit_stored").withStyle(net.minecraft.ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.translatable("tooltip.ironmod.suit_not_stored").withStyle(net.minecraft.ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
