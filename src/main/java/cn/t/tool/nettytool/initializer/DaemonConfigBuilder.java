package cn.t.tool.nettytool.initializer;

import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.util.common.CollectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
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

    public DaemonConfigBuilder<C> configByteBufAnalyser(Function<C, ? extends ByteBufAnalyser> byteBufAnalyserFactory) {
        if(byteBufAnalyserFactory != null) {
            daemonConfig.setNettyByteBufAnalyserFactory(byteBufAnalyserFactory::apply);
        }
        return this;
    }

    public DaemonConfigBuilder<C> configM2mDecoder(List<Function<C, MessageToMessageDecoder<?>>> factoriesList) {
        if(!CollectionUtil.isEmpty(factoriesList)) {
            daemonConfig.setNettyM2mDecoderFactory(ch -> {
                List<MessageToMessageDecoder<?>> nettyTcpDecoderList = new ArrayList<>(factoriesList.size());
                factoriesList.forEach(factory -> nettyTcpDecoderList.add(factory.apply(ch)));
                return nettyTcpDecoderList;
            });
        }
        return this;
    }

    public DaemonConfigBuilder<C> configM2bEncoder(List<Function<C, ? extends MessageToByteEncoder<?>>> factoriesList) {
        if(!CollectionUtil.isEmpty(factoriesList)) {
            daemonConfig.setNettyM2bEncoderListFactory(ch -> {
                List<MessageToByteEncoder<?>> nettyTcpEncoderList = new ArrayList<>(factoriesList.size());
                factoriesList.forEach(factory -> nettyTcpEncoderList.add(factory.apply(ch)));
                return nettyTcpEncoderList;
            });
        }
        return this;
    }

    public DaemonConfigBuilder<C> configM2mEncoder(List<Function<C, MessageToMessageEncoder<?>>> factoriesList) {
        if(!CollectionUtil.isEmpty(factoriesList)) {
            daemonConfig.setNettyM2mEncoderListFactory(ch -> {
                List<MessageToMessageEncoder<?>> nettyTcpEncoderList = new ArrayList<>(factoriesList.size());
                factoriesList.forEach(factory -> nettyTcpEncoderList.add(factory.apply(ch)));
                return nettyTcpEncoderList;
            });
        }
        return this;
    }

    public DaemonConfigBuilder<C> configHandler(List<Function<C, ? extends ChannelHandler>> factoriesList) {
        if(!CollectionUtil.isEmpty(factoriesList)) {
            daemonConfig.setChannelHandlerListFactory(ch -> {
                List<ChannelHandler> handlerList = new ArrayList<>(factoriesList.size());
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
