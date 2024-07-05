package cn.t.tool.nettytool.daemon;

import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.util.List;
import java.util.function.Function;

public class DaemonConfig<C extends Channel> {
    private LogLevel loggingHandlerLogLevel;
    private InternalLoggerFactory internalLoggerFactory = Slf4JLoggerFactory.INSTANCE;
    private Function<C, IdleStateHandler> idleStateHandlerFactory;
    private Function<C, ByteBufAnalyser> nettyByteBufAnalyserFactory;
    private Function<C, List<MessageToMessageDecoder<?>>> nettyM2mDecoderFactory;
    private Function<C, List<MessageToMessageEncoder<?>>> nettyM2mEncoderListFactory;
    private Function<C, List<MessageToByteEncoder<?>>> nettyM2bEncoderListFactory;
    private Function<C, List<ChannelHandler>> channelHandlerListFactory;

    public LogLevel getLoggingHandlerLogLevel() {
        return loggingHandlerLogLevel;
    }

    public void setLoggingHandlerLogLevel(LogLevel loggingHandlerLogLevel) {
        this.loggingHandlerLogLevel = loggingHandlerLogLevel;
    }

    public InternalLoggerFactory getInternalLoggerFactory() {
        return internalLoggerFactory;
    }

    public void setInternalLoggerFactory(InternalLoggerFactory internalLoggerFactory) {
        this.internalLoggerFactory = internalLoggerFactory;
    }

    public Function<C, IdleStateHandler> getIdleStateHandlerFactory() {
        return idleStateHandlerFactory;
    }

    public void setIdleStateHandlerFactory(Function<C, IdleStateHandler> idleStateHandlerFactory) {
        this.idleStateHandlerFactory = idleStateHandlerFactory;
    }

    public Function<C, ByteBufAnalyser> getNettyByteBufAnalyserFactory() {
        return nettyByteBufAnalyserFactory;
    }

    public void setNettyByteBufAnalyserFactory(Function<C, ByteBufAnalyser> nettyByteBufAnalyserFactory) {
        this.nettyByteBufAnalyserFactory = nettyByteBufAnalyserFactory;
    }

    public Function<C, List<MessageToMessageDecoder<?>>> getNettyM2mDecoderFactory() {
        return nettyM2mDecoderFactory;
    }

    public void setNettyM2mDecoderFactory(Function<C, List<MessageToMessageDecoder<?>>> nettyM2mDecoderFactory) {
        this.nettyM2mDecoderFactory = nettyM2mDecoderFactory;
    }

    public Function<C, List<MessageToMessageEncoder<?>>> getNettyM2mEncoderListFactory() {
        return nettyM2mEncoderListFactory;
    }

    public void setNettyM2mEncoderListFactory(Function<C, List<MessageToMessageEncoder<?>>> nettyM2mEncoderListFactory) {
        this.nettyM2mEncoderListFactory = nettyM2mEncoderListFactory;
    }

    public Function<C, List<MessageToByteEncoder<?>>> getNettyM2bEncoderListFactory() {
        return nettyM2bEncoderListFactory;
    }

    public void setNettyM2bEncoderListFactory(Function<C, List<MessageToByteEncoder<?>>> nettyM2bEncoderListFactory) {
        this.nettyM2bEncoderListFactory = nettyM2bEncoderListFactory;
    }

    public Function<C, List<ChannelHandler>> getChannelHandlerListFactory() {
        return channelHandlerListFactory;
    }

    public void setChannelHandlerListFactory(Function<C, List<ChannelHandler>> channelHandlerListFactory) {
        this.channelHandlerListFactory = channelHandlerListFactory;
    }
}
