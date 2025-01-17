package net.glowstone.net.handler.play.player;

import com.flowpowered.network.Message;
import com.flowpowered.network.MessageHandler;
import net.glowstone.EventFactory;
import net.glowstone.entity.GlowPlayer;
import net.glowstone.net.GlowSession;
import net.glowstone.net.message.play.entity.AnimateEntityMessage;
import net.glowstone.net.message.play.player.PlayerSwingArmMessage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;

import java.util.Set;

public final class PlayerSwingArmHandler implements MessageHandler<GlowSession, PlayerSwingArmMessage> {
    @Override
    public void handle(GlowSession session, PlayerSwingArmMessage message) {
        GlowPlayer player = session.getPlayer();

        Block block;
        try {
            block = player.getTargetBlock((Set<Material>) null, 6);
        } catch (IllegalStateException ex) {
            // getTargetBlock failed to find any block at all
            block = null;
        }

        if (block == null || block.isEmpty()) {
            if (EventFactory.onPlayerInteract(player, Action.LEFT_CLICK_AIR).useItemInHand() == Result.DENY)
                return;
            // todo: item interactions with air
        }

        if (!EventFactory.callEvent(new PlayerAnimationEvent(player)).isCancelled()) {
            // play the animation to others
            Message toSend = new AnimateEntityMessage(player.getEntityId(), message.getHand() == 1 ? AnimateEntityMessage.SWING_OFF_HAND : AnimateEntityMessage.SWING_MAIN_HAND);
			for(GlowPlayer observer : player.getWorld().getRawPlayers()) {
				if(observer == player) continue;
				if(!observer.canSeeEntity(player)) continue;
				observer.getSession().send(toSend);
			}
        }
    }
}
