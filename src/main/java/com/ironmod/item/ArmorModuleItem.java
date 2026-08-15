package com.ironmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Item físico do módulo (ex: "Módulo de Propulsão") que o jogador
 * coloca na Bancada de Montagem junto com uma peça de armadura para
 * instalar a funcionalidade nela.
 */
public class ArmorModuleItem extends Item {

    private final ModuleType moduleType;

    public ArmorModuleItem(ModuleType moduleType, Properties properties) {
        super(properties);
        this.moduleType = moduleType;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ironmod.module." + moduleType.getId()));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
