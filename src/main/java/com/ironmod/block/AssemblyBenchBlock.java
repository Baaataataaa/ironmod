package com.ironmod.block;

import com.ironmod.menu.AssemblyBenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Bloco "Bancada de Montagem". Ao clicar com botão direito, abre a GUI
 * onde o jogador coloca uma peça de armadura + um módulo e recebe a
 * peça modificada.
 */
public class AssemblyBenchBlock extends Block {

    public AssemblyBenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                  InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (windowId, inventory, p) -> new AssemblyBenchMenu(windowId, inventory),
                    Component.translatable("block.ironmod.assembly_bench")
            ));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
