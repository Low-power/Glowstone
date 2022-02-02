package net.glowstone.inventory;

import net.glowstone.util.nbt.CompoundTag;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.inventory.meta.FireworkEffectMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GlowMetaFireworkEffect extends GlowMetaItem implements FireworkEffectMeta {

    private FireworkEffect effect;

    public GlowMetaFireworkEffect(GlowMetaItem meta) {
        super(meta);

        if (!(meta instanceof GlowMetaFireworkEffect)) return;

        GlowMetaFireworkEffect effect = (GlowMetaFireworkEffect) meta;
        this.effect = effect.effect;
    }

    static FireworkEffect toEffect(CompoundTag explosion) {
        boolean flicker = false;
        boolean trail = false;
        Type type;
        List<Color> colors = new ArrayList<>();
        List<Color> fadeColors = new ArrayList<>();

        int[] colorInts = explosion.getIntArray("Colors");
        for (int color : colorInts) {
            colors.add(Color.fromRGB(color));
        }

        type = Type.values()[explosion.getByte("Type")];

        if (explosion.isByte("Flicker")) flicker = explosion.getBool("Flicker");
        if (explosion.isByte("Trail")) trail = explosion.getBool("Trail");

        if (explosion.isIntArray("FadeColors")) {
            int[] fadeInts = explosion.getIntArray("FadeColors");
            for (int fade : fadeInts) {
                fadeColors.add(Color.fromRGB(fade));
            }
        }

        return FireworkEffect.builder()
                .flicker(flicker)
                .trail(trail)
                .with(type)
                .withColor(colors)
                .withFade(fadeColors)
                .build();
    }

    static CompoundTag toExplosion(FireworkEffect effect) {
        CompoundTag explosion = new CompoundTag();

        if (effect.hasFlicker()) explosion.putBool("Flicker", true);
        if (effect.hasTrail()) explosion.putBool("Trail", true);

        explosion.putByte("Type", effect.getType().ordinal());

        List<Color> colors = effect.getColors();
		int[] color_ints = new int[colors.size()];
		for(int i = 0; i < color_ints.length; i++) {
			color_ints[i] = colors.get(i).asRGB();
		}
        explosion.putIntArray("Colors", color_ints);

        List<Color> fade = effect.getFadeColors();
        if (!fade.isEmpty()) {
			color_ints = new int[fade.size()];
			for(int i = 0; i < color_ints.length; i++) {
				color_ints[i] = fade.get(i).asRGB();
			}
            explosion.putIntArray("FadeColors", color_ints);
        }

        return explosion;
    }

    @Override
    public boolean isApplicable(Material material) {
        return material == Material.FIREWORK_CHARGE;
    }

    @Override
    public GlowMetaFireworkEffect clone() {
        return new GlowMetaFireworkEffect(this);
    }

    @Override
    void writeNbt(CompoundTag tag) {
        super.writeNbt(tag);

        if (hasEffect()) {
            tag.putCompound("Explosion", toExplosion(effect));
        }
    }

    @Override
    void readNbt(CompoundTag tag) {
        super.readNbt(tag);

        if (tag.isCompound("Explosion")) {
            effect = toEffect(tag.getCompound("Explosion"));
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("meta-type", "CHARGE");

        if (hasEffect()) {
            result.put("effect", effect.serialize());
        }

        return result;
    }

    @Override
    public boolean hasEffect() {
        return effect != null;
    }

    @Override
    public FireworkEffect getEffect() {
        return effect;
    }

    @Override
    public void setEffect(FireworkEffect effect) {
        this.effect = effect;
    }
}
