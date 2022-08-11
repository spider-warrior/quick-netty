package cn.t.tool.nettytool.daemon.server;

import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.tool.nettytool.initializer.NettyUdpChannelInitializer;
import cn.t.util.common.CollectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyUdpServer extends AbstractDaemonServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyUdpServer.class);

    private final NettyUdpChannelInitializer channelInitializer;
    private final EventLoopGroup workerGroup;
    private final boolean syncBind;
    private final boolean syncClose;
    private Channel serverChannel;

    public void doStart() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
            .group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(channelInitializer);

        try {
            logger.info("UDP Server: [{}] is going start", name);
            ChannelFuture bindFuture = bootstrap.bind(port).addListener(f -> {
                if(f.isSuccess()) {
                    logger.info("UDP Server: {} has been started successfully, port: {}", name, port);
                    if (!CollectionUtil.isEmpty(daemonListenerList)) {
                        for (DaemonListener listener: daemonListenerList) {
                            listener.startup(this);
                        }
                    }
                } else {
                    logger.error("UDP Server: {} failed to start, port: {}", name, port, f.cause());
                }
            });
            if(syncBind) {
                bindFuture.sync();
            }
            serverChannel = bindFuture.channel();
            ChannelFuture closeFuture = serverChannel.closeFuture().addListener(f -> {
                logger.info(String.format("UDP Server: [%s] is closed, port: %d ", name, port));
                if (!CollectionUtil.isEmpty(daemonListenerList)) {
                    for (DaemonListener listener: daemonListenerList) {
                        listener.close(this);
                    }
                }
            });
            if(syncClose) {
                closeFuture.sync();
            }
        } catch (Exception e) {
            logger.error(String.format("UDP Server: [%s] is Down", name), e);
        }
    }

    @Override
    public void doClose() {
        if(serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
    }

    public NettyUdpServer(String name, int port, NettyUdpChannelInitializer channelInitializer, EventLoopGroup workerGroup, boolean syncBind, boolean syncClose) {
        super(name, port);
        this.channelInitializer = channelInitializer;
        this.workerGroup = workerGroup;
        this.syncBind = syncBind;
        this.syncClose = syncClose;
    }
}
