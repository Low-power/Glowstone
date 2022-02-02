package net.glowstone.util;

import net.glowstone.GlowWorld;
import net.glowstone.entity.GlowPlayer;
import org.bukkit.Location;
import org.bukkit.Sound;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class SoundUtil {

    public static void playSoundAtLocationExcept(Location location, Sound sound, float volume, float pitch, GlowPlayer... exclude) {
        if (location == null || sound == null) return;
        GlowWorld world = (GlowWorld) location.getWorld();
        double radiusSquared = volume * volume * 256;
		List<GlowPlayer> exclude_list = Arrays.asList(exclude);
		for(GlowPlayer player : world.getRawPlayers()) {
			if(player.getLocation().distanceSquared(location) > radiusSquared) continue;
			if(exclude_list.contains(player)) continue;
			player.playSound(location, sound, volume, pitch);
		}
    }

    public static void playSoundPitchRange(Location location, Sound sound, float volume, float pitchBase, float pitchRange, boolean allowNegative, GlowPlayer... exclude) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        float pitch = pitchBase;
        if (allowNegative) {
            pitch += randomReal(pitchRange);
        } else {
            pitch += rand.nextFloat() * pitchRange;
        }
        playSoundAtLocationExcept(location, sound, volume, pitch, exclude);
    }

    public static void playSoundPitchRange(Location location, Sound sound, float volume, float pitchBase, float pitchRange, GlowPlayer... exclude) {
        playSoundPitchRange(location, sound, volume, pitchBase, pitchRange, true, exclude);
    }

    public static float randomReal(float range) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        return (rand.nextFloat() - rand.nextFloat()) * range;
    }
}
