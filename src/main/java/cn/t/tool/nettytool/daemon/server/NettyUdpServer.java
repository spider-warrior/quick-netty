package cn.t.tool.nettytool.daemon.server;

import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.util.common.CollectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyUdpServer extends AbstractDaemonServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyUdpServer.class);

    private ChannelInitializer<DatagramChannel> channelInitializer;
    private final EventLoopGroup workerGroup;
    private final boolean shutdownWorkerGroup;
    private Channel serverChannel;
    private final Map<ChannelOption<?>, Object> childOptions = new ConcurrentHashMap<>();
    private final Map<AttributeKey<?>, Object> childAttrs = new ConcurrentHashMap<>();

    public void doStart() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true);
        //child option
        if(!CollectionUtil.isEmpty(childOptions)) {
            for(Map.Entry<ChannelOption<?>, Object> entry: childOptions.entrySet()) {
                @SuppressWarnings("unchecked")
                ChannelOption<Object> key = (ChannelOption<Object>) entry.getKey();
                bootstrap.option(key, entry.getValue());
            }
        }
        //childAttr
        if(!CollectionUtil.isEmpty(childAttrs)) {
            for(Map.Entry<AttributeKey<?>, Object> entry: childAttrs.entrySet()) {
                @SuppressWarnings("unchecked")
                AttributeKey<Object> key = (AttributeKey<Object>) entry.getKey();
                bootstrap.attr(key, entry.getValue());
            }
        }
        bootstrap.handler(channelInitializer);
        try {
            logger.info("UDP Server: [{}] is going start", name);
            ChannelFuture openFuture = bootstrap.bind(port);
            openFuture.addListener(f -> {
                logger.info("UDP Server: {} has been started successfully, port: {}", name, port);
                if (!CollectionUtil.isEmpty(daemonListenerList)) {
                    for (DaemonListener listener: daemonListenerList) {
                        listener.startup(this);
                    }
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
        } finally {
            if(shutdownWorkerGroup) {
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public void doClose() {
        if(serverChannel != null) {
            serverChannel.close();
        }
    }

    public NettyUdpServer(String name, int port, ChannelInitializer<DatagramChannel> channelInitializer) {
        super(name, port);
        this.channelInitializer = channelInitializer;
        this.workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors(), new DefaultThreadFactory("NettyUdpServerWorker", true));
        this.shutdownWorkerGroup = true;
    }

    public NettyUdpServer(String name, int port, ChannelInitializer<DatagramChannel> channelInitializer, EventLoopGroup workerGroup) {
        super(name, port);
        this.channelInitializer = channelInitializer;
        this.workerGroup = workerGroup;
        this.shutdownWorkerGroup = false;
    }

    public NettyUdpServer setChannelInitializer(ChannelInitializer<DatagramChannel> channelInitializer) {
        this.channelInitializer = channelInitializer;
        return this;
    }

    public <T> NettyUdpServer childAttr(AttributeKey<T> childKey, T value) {
        childAttrs.put(childKey, value);
        return this;
    }

    public <T> NettyUdpServer childOption(ChannelOption<T> childKey, T value) {
        childOptions.put(childKey, value);
        return this;
    }
}
