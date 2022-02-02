package net.glowstone.block.state;

import net.glowstone.block.GlowBlock;
import net.glowstone.block.GlowBlockState;
import org.bukkit.Nameable;
import org.bukkit.block.Lockable;

import java.util.UUID;

public class GlowLootableBlock extends GlowBlockState implements Lockable, Nameable {
    public GlowLootableBlock(GlowBlock block) {
        super(block);
    }

    @Override
    public String getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(String s) {

    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public String getLock() {
        return null;
    }

    @Override
    public void setLock(String s) {

    }
}
