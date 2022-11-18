package cn.t.tool.nettytool.initializer;

import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import cn.t.tool.nettytool.aware.NettyB2mDecoderAware;
import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import cn.t.util.common.CollectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class DaemonConfigBuilder<C extends Channel> {

    private final DaemonConfig<C> daemonConfig = new DaemonConfig<>();

    public DaemonConfigBuilder<C> configByteBufAnalyser(Function<C, ? extends ByteBufAnalyser> byteBufAnalyserFactory) {
        if(byteBufAnalyserFactory != null) {
            daemonConfig.setNettyB2mDecoderFactory(ch -> {
                ByteBufAnalyser byteBufAnalyser = byteBufAnalyserFactory.apply(ch);
                NettyB2mDecoder nettyB2mDecoder = new NettyB2mDecoder(byteBufAnalyser);
                if(byteBufAnalyser instanceof NettyB2mDecoderAware) {
                    ((NettyB2mDecoderAware)byteBufAnalyser).setNettyB2mDecoder(nettyB2mDecoder);
                }
                return nettyB2mDecoder;
            });
        }
        return this;
    }

    public DaemonConfigBuilder<C> configLogLevel(LogLevel logLevel) {
        if(logLevel != null) {
            daemonConfig.setLoggingHandlerLogLevel(logLevel);
        }
        return this;
    }

    public DaemonConfigBuilder<C> configLogFactory(InternalLoggerFactory loggerFactory) {
        if(loggerFactory != null) {
            daemonConfig.setInternalLoggerFactory(loggerFactory);
        }
        return this;
    }

    public DaemonConfigBuilder<C> configIdleHandler(long readerIdleTime, long writerIdleTime, long allIdleTime) {
        daemonConfig.setIdleStateHandlerFactory(ch -> new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS));
        return this;
    }

    public DaemonConfigBuilder<C> configM2mEncoder(List<Function<C, MessageToMessageEncoder<?>>> factoriesList) {
        if(!CollectionUtil.isEmpty(factoriesList)) {
            daemonConfig.setNettyM2mEncoderListFactory(ch -> {
                List<MessageToMessageEncoder<?>> nettyTcpEncoderList = new ArrayList<>();
                factoriesList.forEach(factory -> nettyTcpEncoderList.add(factory.apply(ch)));
                return nettyTcpEncoderList;
            });
        }
        return this;
    }

    public DaemonConfigBuilder<C> configM2bEncoder(List<Function<C, ? extends MessageToByteEncoder<?>>> factoriesList) {
        if(!CollectionUtil.isEmpty(factoriesList)) {
            daemonConfig.setNettyM2bEncoderListFactory(ch -> {
                List<MessageToByteEncoder<?>> nettyTcpEncoderList = new ArrayList<>();
                factoriesList.forEach(factory -> nettyTcpEncoderList.add(factory.apply(ch)));
                return nettyTcpEncoderList;
            });
        }
        return this;
    }

    public DaemonConfigBuilder<C> configHandler(List<Function<C, ? extends ChannelHandler>> factoriesList) {
        if(!CollectionUtil.isEmpty(factoriesList)) {
            daemonConfig.setChannelHandlerListFactory(ch -> {
                List<ChannelHandler> handlerList = new ArrayList<>();
                factoriesList.forEach(factory -> handlerList.add(factory.apply(ch)));
                return handlerList;
            });
        }
        return this;
    }

    public static <C extends Channel> DaemonConfigBuilder<C> newInstance() {
        return new DaemonConfigBuilder<>();
    }

    public DaemonConfig<C> build() {
        return daemonConfig;
    }

    private DaemonConfigBuilder() {}

}
