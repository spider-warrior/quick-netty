package cn.t.tool.nettytool.daemon.client;

import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.util.common.CollectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyTcpClient extends AbstractDaemonClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyTcpClient.class);

    private final ChannelInitializer<SocketChannel> channelInitializer;
    private final EventLoopGroup workerGroup;
    private final boolean shutdownWorkerGroup;
    private Channel clientChannel;
    private final Map<AttributeKey<?>, Object> childAttrs = new ConcurrentHashMap<>();

    @Override
    public void doStart() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1500)
            .handler(channelInitializer);
        if (!CollectionUtil.isEmpty(childAttrs)) {
            for (Map.Entry<AttributeKey<?>, Object> entry : childAttrs.entrySet()) {
                @SuppressWarnings("unchecked")
                AttributeKey<Object> key = (AttributeKey<Object>) entry.getKey();
                bootstrap.attr(key, entry.getValue());
            }
        }
        try {
            ChannelFuture openFuture = bootstrap.connect(host, port);
            clientChannel = openFuture.channel();
            ChannelFuture closeFuture = clientChannel.closeFuture();
            closeFuture.addListener(f -> {
                    if (!CollectionUtil.isEmpty(daemonListenerList)) {
                        for (DaemonListener listener : daemonListenerList) {
                            listener.close(this);
                        }
                    }
                }
            );
            openFuture.sync();
            if (!CollectionUtil.isEmpty(daemonListenerList)) {
                for (DaemonListener listener : daemonListenerList) {
                    listener.startup(this);
                }
            }
            closeFuture.sync();
        } catch (Exception e) {
            logger.error(String.format("TCP Client: [%s] is Down", name), e);
        } finally {
            logger.info("TCP Client: [{}] is closed", name);
            if(shutdownWorkerGroup) {
                workerGroup.shutdownGracefully();
            }
        }
    }

    @Override
    public void doClose() {
        if (clientChannel != null) {
            clientChannel.close();
        }
    }

    public NettyTcpClient(String name, String host, int port, ChannelInitializer<SocketChannel> channelInitializer) {
        super(name, host, port);
        this.channelInitializer = channelInitializer;
        this.workerGroup = new NioEventLoopGroup(1);
        this.shutdownWorkerGroup = true;
    }
    public NettyTcpClient(String name, String host, int port, ChannelInitializer<SocketChannel> channelInitializer, EventLoopGroup workerGroup) {
        super(name, host, port);
        this.channelInitializer = channelInitializer;
        this.workerGroup = workerGroup;
        this.shutdownWorkerGroup = false;
    }

    public void setDaemonListenerList(List<DaemonListener> daemonListenerList) {
        this.daemonListenerList = daemonListenerList;
    }

    public void sendMsg(Object msg) {
        if (clientChannel != null && clientChannel.isOpen()) {
            clientChannel.writeAndFlush(msg);
        } else {
            logger.warn("[{}], channel is not available, msg ignored, detail: {}", name, msg);
        }
    }

    public <T> NettyTcpClient childAttr(AttributeKey<T> childKey, T value) {
        childAttrs.put(childKey, value);
        return this;
    }
}
