package cn.t.tool.nettytool.daemon.client;

import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.util.common.CollectionUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyTcpClient extends AbstractDaemonClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyTcpClient.class);

    private final ChannelInitializer<SocketChannel> channelInitializer;
    private final EventLoopGroup workerGroup;
    private final boolean syncBind;
    private final boolean syncClose;
    private Channel channel;
    private final Map<AttributeKey<?>, Object> childAttrs = new ConcurrentHashMap<>();

    @Override
    public void doStart() {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
            .handler(channelInitializer);
        if (!CollectionUtil.isEmpty(childAttrs)) {
            for (Map.Entry<AttributeKey<?>, Object> entry : childAttrs.entrySet()) {
                @SuppressWarnings("unchecked")
                AttributeKey<Object> key = (AttributeKey<Object>) entry.getKey();
                bootstrap.attr(key, entry.getValue());
            }
        }
        try {
            logger.info("TCP Client: [{}] is going to start, target: {}:{}", name, host, port);
            ChannelFuture connectFuture = bootstrap.connect(host, port).addListener((ChannelFutureListener)connectAsyncFuture -> {
                if(connectAsyncFuture.isSuccess()) {
                    logger.info("TCP Client: [{}] has been started successfully", name);
                    if (!CollectionUtil.isEmpty(daemonListenerList)) {
                        for (DaemonListener listener : daemonListenerList) {
                            listener.startup(this, connectAsyncFuture.channel());
                        }
                    }
                    connectAsyncFuture.channel().closeFuture().addListener((ChannelFutureListener)closeAsyncFuture -> {
                        logger.info("TCP Client: [{}] is closed", name);
                        callListenerClose(closeAsyncFuture.channel(), closeAsyncFuture.cause(), "close");
                    });
                } else {
                    callListenerClose(connectAsyncFuture.channel(), connectAsyncFuture.cause(), "connect");
                }
            });
            if(syncBind) {
                connectFuture.sync();
            }
            channel = connectFuture.channel();
            if(syncClose) {
                channel.closeFuture().sync();
            }
        } catch (Exception e) {
            logger.error(String.format("TCP Client: [%s] is Down", name), e);
        }
    }

    private void callListenerClose(Channel channel, Throwable cause, String stage) {
        if(CollectionUtil.isEmpty(daemonListenerList)) {
            if(cause == null) {
                logger.info(String.format("TCP Client: [%s] close, stage: %s target address: [%s:%d]", name, stage, host, port));
            } else {
                logger.error(String.format("TCP Client: [%s] close, stage: %s target address: [%s:%d]", name, stage, host, port), cause);
            }
        } else {
            if(cause == null) {
                for (DaemonListener listener: daemonListenerList) {
                    listener.close(this, channel);
                }
            } else {
                for (DaemonListener listener: daemonListenerList) {
                    listener.close(this, channel, cause);
                }
            }
        }
    }

    private boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    @Override
    public void doClose() {
        if (isOpen()) {
            channel.close();
        }
    }

    public NettyTcpClient(String name, String host, int port, ChannelInitializer<SocketChannel> channelInitializer, EventLoopGroup workerGroup, boolean syncBind, boolean syncClose) {
        super(name, host, port);
        this.channelInitializer = channelInitializer;
        this.workerGroup = workerGroup;
        this.syncBind = syncBind;
        this.syncClose = syncClose;
    }

    public boolean sendMsg(Object msg) {
        if (isOpen()) {
            channel.writeAndFlush(msg);
            return true;
        } else {
            logger.warn("[{}], channel is not available, msg ignored, detail: {}", name, msg);
            return false;
        }
    }

    public <T> NettyTcpClient childAttr(AttributeKey<T> childKey, T value) {
        childAttrs.put(childKey, value);
        return this;
    }
}
