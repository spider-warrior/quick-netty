package cn.t.tool.nettytool.handler;

import cn.t.util.common.ExceptionUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

@ChannelHandler.Sharable
public class NettyExceptionHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyExceptionHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Uncaught exceptions from inbound handlers will propagate up to this handler
        logger.error("[{} -> {}]: 读取消息异常, 即将关闭连接, 异常: {}, 异常消息: {}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), cause.getClass().getSimpleName(), ExceptionUtil.getErrorMessage(cause));
        if (ctx.channel().isOpen()) {
            ctx.flush();
        }
        ctx.close();
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        ctx.connect(remoteAddress, localAddress, promise.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                // Handle connect exception here...
                logger.error("连接失败: [{}], {}", remoteAddress, ExceptionUtil.getErrorMessage(future.cause()));
            }
        }));
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.write(msg, promise.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                // Handle write exception here...
                logger.error("[{}] -> [{}]: 写出消息异常, {}", ctx.channel().localAddress(), ctx.channel().remoteAddress(), ExceptionUtil.getErrorMessage(future.cause()));
            }
        }));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.warn("通道: {}, 未处理的消息: {}", ctx.channel(), msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    logger.warn("未处理的Idle事件类型: {}", e);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        logger.warn("[{} -> {}]: 读取超时,断开连接", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        if (ctx.channel().isOpen()) {
            ctx.flush();
        }
        ctx.close();
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        logger.warn("[{} -> {}]: 写出超时,断开连接", ctx.channel().localAddress(), ctx.channel().remoteAddress());
        if (ctx.channel().isOpen()) {
            ctx.flush();
        }
        ctx.close();
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        logger.warn("[{} <-> {}]: 读取或写出超时,断开连接", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        if (ctx.channel().isOpen()) {
            ctx.flush();
        }
        ctx.close();
    }

    // ... override more outbound methods to handle their exceptions as well
}
