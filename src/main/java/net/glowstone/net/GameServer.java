package net.glowstone.net;

import com.flowpowered.network.ConnectionManager;
import com.flowpowered.network.session.Session;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.glowstone.GlowServer;
import net.glowstone.net.pipeline.GlowChannelInitializer;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;


public final class GameServer extends GlowSocketServer implements ConnectionManager {
    public GameServer(GlowServer server, CountDownLatch latch) {
        super(server, latch);
        bootstrap.childHandler(new GlowChannelInitializer(this));
    }

    public ChannelFuture bind(InetSocketAddress address) {
        GlowServer.logger.info("Binding server to " + address + "...");
        return super.bind(address);
    }

    @Override
    public void onBindSuccess(InetSocketAddress address) {
        getServer().setPort(address.getPort());
        getServer().setAddress(address.getHostString());
        GlowServer.logger.info("Successfully bound server to " + address + '.');
        super.onBindSuccess(address);
    }

    @Override
    public void onBindFailure(InetSocketAddress address, Throwable t) {
        GlowServer.logger.severe("Failed to bind server to " + address + '.');
        if (t.getMessage().contains("Cannot assign requested address")) {
            GlowServer.logger.severe("The 'server.address' in your configuration may not be valid.");
            GlowServer.logger.severe("Unless you are sure you need it, try removing it.");
            GlowServer.logger.severe(t.getLocalizedMessage());
        } else if (t.getMessage().contains("Address already in use")) {
            GlowServer.logger.severe("The address was already in use. Check that no server is");
            GlowServer.logger.severe("already running on that port.");
            GlowServer.logger.severe(t.getLocalizedMessage());
        } else {
            GlowServer.logger.log(Level.SEVERE, "An unknown bind error has occurred.", t);
        }
        System.exit(1);
    }

    @Override
    public GlowSession newSession(Channel c) {
        GlowSession session = new GlowSession(getServer(), c, this);
        getServer().getSessionRegistry().add(session);
        return session;
    }

    @Override
    public void sessionInactivated(Session session) {
        getServer().getSessionRegistry().remove((GlowSession) session);
    }
}

