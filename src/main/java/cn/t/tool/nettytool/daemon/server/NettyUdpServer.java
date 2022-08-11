package cn.t.tool.nettytool.daemon.server;

import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.tool.nettytool.initializer.NettyUdpChannelInitializer;
import cn.t.util.common.CollectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyUdpServer extends AbstractDaemonServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyUdpServer.class);

    private NettyUdpChannelInitializer channelInitializer;
    private final EventLoopGroup workerGroup;
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
            ChannelFuture openFuture = bootstrap.bind(port);
            openFuture.addListener(f -> {
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
            serverChannel = openFuture.channel();
            ChannelFuture closeFuture = serverChannel.closeFuture();
            closeFuture.addListener(f -> {
                logger.info(String.format("UDP Server: [%s] is closed, port: %d ", name, port));
                if (!CollectionUtil.isEmpty(daemonListenerList)) {
                    for (DaemonListener listener: daemonListenerList) {
                        listener.close(this);
                    }
                }
            });
            closeFuture.sync();
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

    public NettyUdpServer(String name, int port, NettyUdpChannelInitializer channelInitializer) {
        super(name, port);
        this.channelInitializer = channelInitializer;
        this.workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new DefaultThreadFactory("NettyServerWorker", true));
    }

    public NettyUdpServer(String name, int port, NettyUdpChannelInitializer channelInitializer, EventLoopGroup workerGroup) {
        super(name, port);
        this.channelInitializer = channelInitializer;
        this.workerGroup = workerGroup;
    }

    public NettyUdpServer setChannelInitializer(NettyUdpChannelInitializer channelInitializer) {
        this.channelInitializer = channelInitializer;
        return this;
    }
}
