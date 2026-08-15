package com.ironmod.menu;

import com.ironmod.item.ArmorModuleItem;
import com.ironmod.item.ModularArmorItem;
import com.ironmod.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Container da Bancada de Montagem.
 * Slot 0: peça de armadura (input)
 * Slot 1: módulo (input)
 * Slot 2: resultado (output, não colocável, só retirável)
 * Slots seguintes: inventário do jogador
 */
public class AssemblyBenchMenu extends AbstractContainerMenu {

    private final Container inputContainer = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            AssemblyBenchMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer outputContainer = new ResultContainer();

    public static final int ARMOR_SLOT = 0;
    public static final int MODULE_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    public AssemblyBenchMenu(int windowId, Inventory playerInventory) {
        super(ModMenuTypes.ASSEMBLY_BENCH_MENU.get(), windowId);

        // Slot da armadura
        this.addSlot(new Slot(inputContainer, 0, 26, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ModularArmorItem;
            }
        });

        // Slot do módulo
        this.addSlot(new Slot(inputContainer, 1, 76, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ArmorModuleItem;
            }
        });

        // Slot de resultado
        this.addSlot(new Slot(outputContainer, 0, 134, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                // Consome a peça de armadura e o módulo do input ao retirar o resultado
                inputContainer.removeItem(ARMOR_SLOT, 1);
                inputContainer.getItem(MODULE_SLOT).shrink(1);
                inputContainer.setChanged();
                super.onTake(player, stack);
            }
        });

        // Inventário do jogador
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        ItemStack armorStack = inputContainer.getItem(ARMOR_SLOT);
        ItemStack moduleStack = inputContainer.getItem(MODULE_SLOT);

        if (!armorStack.isEmpty() && !moduleStack.isEmpty()
                && moduleStack.getItem() instanceof ArmorModuleItem moduleItem) {
            ItemStack resultCopy = armorStack.copy();
            boolean installed = ModularArmorItem.installModule(resultCopy, moduleItem.getModuleType());
            outputContainer.setItem(0, installed ? resultCopy : ItemStack.EMPTY);
        } else {
            outputContainer.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (index == OUTPUT_SLOT) {
                if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else if (index != ARMOR_SLOT && index != MODULE_SLOT) {
                // Do inventário do jogador para os slots de input
                if (slotStack.getItem() instanceof ModularArmorItem) {
                    if (!this.moveItemStackTo(slotStack, ARMOR_SLOT, ARMOR_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotStack.getItem() instanceof ArmorModuleItem) {
                    if (!this.moveItemStackTo(slotStack, MODULE_SLOT, MODULE_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, 3, 39, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, inputContainer);
    }
}
