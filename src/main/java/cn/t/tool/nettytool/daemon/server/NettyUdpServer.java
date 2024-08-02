package cn.t.tool.nettytool.daemon.server;

import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.tool.nettytool.initializer.NettyUdpChannelInitializer;
import cn.t.util.common.CollectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyUdpServer extends AbstractDaemonServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyUdpServer.class);

    private final NettyUdpChannelInitializer channelInitializer;
    private final EventLoopGroup workerGroup;
    private final boolean syncBind;
    private final boolean syncClose;
    private final List<Channel> serverChannelList = new ArrayList<>();
    private final Map<AttributeKey<?>, Object> attrs = new ConcurrentHashMap<>();

    public void doStart() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                 .channel(NioDatagramChannel.class)
                 .option(ChannelOption.SO_BROADCAST, true)
            .option(ChannelOption.SO_REUSEADDR, true);
        if(!CollectionUtil.isEmpty(attrs)) {
            for(Map.Entry<AttributeKey<?>, Object> entry: attrs.entrySet()) {
                @SuppressWarnings("unchecked")
                AttributeKey<Object> key = (AttributeKey<Object>) entry.getKey();
                bootstrap.attr(key, entry.getValue());
            }
        }
        bootstrap.handler(channelInitializer);
        try {
            logger.info("UDP Server: [{}] is going to start", name);
            for (int i=0; i<ports.length; i++) {
                int index = i;
                ChannelFuture bindFuture = bootstrap.bind(ports[index]).addListener((ChannelFutureListener) bindAsyncFuture -> {
                    if(bindAsyncFuture.isSuccess()) {
                        if(ports[index] == 0) {
                            int actualBindPort = ((InetSocketAddress)bindAsyncFuture.channel().localAddress()).getPort();
                            actualBindPorts[index] = actualBindPort;
                            logger.info("UDP Server: [{}] has bound successfully, port: {}", name, actualBindPort);
                        } else {
                            logger.info("UDP Server: [{}] has bound successfully, port: {}", name, ports[index]);
                        }
                        if (!CollectionUtil.isEmpty(daemonListenerList)) {
                            for (DaemonListener listener: daemonListenerList) {
                                listener.startup(this, bindAsyncFuture.channel());
                            }
                        }
                    } else {
                        logger.error(String.format("UDP Server: [%s] failed to start, port: %d", name, ports[index]), bindAsyncFuture.cause());
                    }
                });
                if(syncBind) {
                    bindFuture.sync();
                }
                serverChannelList.add(bindFuture.channel());
            }
            List<ChannelFuture> closeFutureList = new ArrayList<>(serverChannelList.size());
            for (Channel serverChannel : serverChannelList) {
                ChannelFuture closeFuture = serverChannel.closeFuture().addListener((ChannelFutureListener)f -> {
                    logger.info(String.format("UDP Server: [%s] is closed ", name));
                    if (!CollectionUtil.isEmpty(daemonListenerList)) {
                        Throwable throwable = f.cause();
                        if(throwable == null) {
                            for (DaemonListener listener: daemonListenerList) {
                                listener.close(this, f.channel());
                            }
                        } else {
                            for (DaemonListener listener: daemonListenerList) {
                                listener.close(this, f.channel(), throwable);
                            }
                        }
                    }
                });
                closeFutureList.add(closeFuture);
            }
            for (ChannelFuture closeFuture : closeFutureList) {
                if(syncClose) {
                    closeFuture.sync();
                }
            }
        } catch (Exception e) {
            logger.error(String.format("UDP Server: [%s] is Down", name), e);
        }
    }

    @Override
    public void doClose() {
        for (Channel serverChannel : serverChannelList) {
            if(serverChannel != null && serverChannel.isOpen()) {
                serverChannel.close();
            }
        }
    }

    public NettyUdpServer(String name, int[] ports, NettyUdpChannelInitializer channelInitializer, EventLoopGroup workerGroup, boolean syncBind, boolean syncClose) {
        super(name, ports);
        this.channelInitializer = channelInitializer;
        this.workerGroup = workerGroup;
        this.syncBind = syncBind;
        this.syncClose = syncClose;
    }

    public <T> NettyUdpServer attr(AttributeKey<T> childKey, T value) {
        attrs.put(childKey, value);
        return this;
    }
}
