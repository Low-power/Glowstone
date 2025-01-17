package net.glowstone.block.entity;

import net.glowstone.EventFactory;
import net.glowstone.GlowServer;
import net.glowstone.block.GlowBlock;
import net.glowstone.block.GlowBlockState;
import net.glowstone.block.state.GlowFurnace;
import net.glowstone.inventory.GlowFurnaceInventory;
import net.glowstone.inventory.crafting.CraftingManager;
import net.glowstone.util.InventoryUtil;
import net.glowstone.util.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.InventoryView.Property;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.entity.HumanEntity;

public class FurnaceEntity extends ContainerEntity {

    private short burnTime;
    private short cookTime;
    private short burnTimeFuel;

    public FurnaceEntity(GlowBlock block) {
        super(block, new GlowFurnaceInventory(new GlowFurnace(block, (short) 0, (short) 0)));
        setSaveId("minecraft:furnace");
    }

    @Override
    public GlowBlockState getState() {
        return new GlowFurnace(block);
    }

    @Override
    public void saveNbt(CompoundTag tag) {
        super.saveNbt(tag);
        tag.putShort("BurnTime", burnTime);
        tag.putShort("CookTime", cookTime);
    }

    @Override
    public void loadNbt(CompoundTag tag) {
        super.loadNbt(tag);
        if (tag.isShort("BurnTime")) {
            burnTime = tag.getShort("BurnTime");
        }
        if (tag.isShort("CookTime")) {
            cookTime = tag.getShort("CookTime");
        }
    }

    public short getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(short burnTime) {
        this.burnTime = burnTime;
    }

    public short getCookTime() {
        return cookTime;
    }

    public void setCookTime(short cookTime) {
        this.cookTime = cookTime;
    }

    // TODO: Change block on burning
    public void burn() {
        GlowFurnaceInventory inv = (GlowFurnaceInventory) getInventory();
        if (burnTime > 0) burnTime--;
        boolean isBurnable = isBurnable();
        if (cookTime > 0 && isBurnable) {
            cookTime++;
        } else if (burnTime != 0) {
            cookTime = 0;
        }

        if (cookTime == 0 && isBurnable) {
            cookTime = 1;
        }

        if (burnTime == 0) {
            if (isBurnable) {
                CraftingManager cm = ((GlowServer) Bukkit.getServer()).getCraftingManager();
                FurnaceBurnEvent burnEvent = new FurnaceBurnEvent(block, inv.getFuel(), cm.getFuelTime(inv.getFuel().getType()));
                EventFactory.callEvent(burnEvent);
                if (!burnEvent.isCancelled() && burnEvent.isBurning()) {
                    burnTime = (short) burnEvent.getBurnTime();
                    burnTimeFuel = burnTime;
                    if (inv.getFuel().getAmount() == 1) {
                        if (inv.getFuel().getType().equals(Material.LAVA_BUCKET)) {
                            inv.setFuel(new ItemStack(Material.BUCKET));
                        } else {
                            inv.setFuel(null);
                        }
                    } else {
                        inv.getFuel().setAmount(inv.getFuel().getAmount() - 1);
                    }
                } else if (cookTime != 0) {
                    if (cookTime % 2 == 0) {
                        cookTime = (short) (cookTime - 2);
                    } else {
                        cookTime--;
                    }
                }
            } else if (cookTime != 0) {
                if (cookTime % 2 == 0) {
                    cookTime = (short) (cookTime - 2);
                } else {
                    cookTime--;
                }
            }
        }

        if (cookTime == 200) {
            CraftingManager cm = ((GlowServer) Bukkit.getServer()).getCraftingManager();
            Recipe recipe = cm.getFurnaceRecipe(inv.getSmelting());
            if (recipe != null) {
                FurnaceSmeltEvent smeltEvent = new FurnaceSmeltEvent(block, inv.getSmelting(), recipe.getResult());
                EventFactory.callEvent(smeltEvent);
                if (!smeltEvent.isCancelled()) {
                    if (inv.getSmelting().getType().equals(Material.SPONGE) && inv.getSmelting().getData().getData() == 1 && inv.getFuel() != null && inv.getFuel().getType().equals(Material.BUCKET) && inv.getFuel().getAmount() == 1) {
                        inv.setFuel(new ItemStack(Material.WATER_BUCKET));
                    }
                    if (inv.getResult() == null || inv.getResult().getType().equals(Material.AIR)) {
                        inv.setResult(smeltEvent.getResult());
                    } else if (inv.getResult().getType().equals(smeltEvent.getResult().getType())) {
                        inv.getResult().setAmount(inv.getResult().getAmount() + smeltEvent.getResult().getAmount());
                    }
                    if (inv.getSmelting().getAmount() == 1) {
                        inv.setSmelting(null);
                    } else {
                        inv.getSmelting().setAmount(inv.getSmelting().getAmount() - 1);
                    }
                }
                cookTime = 0;
            }
        }
		for(HumanEntity human : inv.getViewersSet()) {
			human.setWindowProperty(Property.BURN_TIME, burnTime);
			human.setWindowProperty(Property.TICKS_FOR_CURRENT_FUEL, burnTimeFuel);
			human.setWindowProperty(Property.COOK_TIME, cookTime);
			human.setWindowProperty(Property.TICKS_FOR_CURRENT_SMELTING, 200);
		}
        if (!isBurnable && burnTime == 0 && cookTime == 0) {
            getState().getBlock().getWorld().cancelPulse(getState().getBlock());
        }
    }

    private boolean isBurnable() {
        GlowFurnaceInventory inv = (GlowFurnaceInventory) getInventory();
        if ((burnTime != 0 || !InventoryUtil.isEmpty(inv.getFuel())) && !InventoryUtil.isEmpty(inv.getSmelting())) {
            if ((InventoryUtil.isEmpty(inv.getFuel()) || InventoryUtil.isEmpty(inv.getSmelting())) && burnTime == 0) {
                return false;
            }
            CraftingManager cm = ((GlowServer) Bukkit.getServer()).getCraftingManager();
            if (burnTime != 0 || cm.isFuel(inv.getFuel().getType())) {
                Recipe recipe = cm.getFurnaceRecipe(inv.getSmelting());
                if (recipe != null && (InventoryUtil.isEmpty(inv.getResult()) || inv.getResult().getType().equals(recipe.getResult().getType()) && inv.getResult().getAmount() + recipe.getResult().getAmount() <= recipe.getResult().getMaxStackSize())) {
                    return true;
                }
            }
        }
        return false;
    }
}
