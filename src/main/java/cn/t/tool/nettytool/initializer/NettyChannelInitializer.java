package cn.t.tool.nettytool.initializer;

import cn.t.tool.nettytool.aware.NettyB2mDecoderAware;
import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import cn.t.tool.nettytool.handler.EventLoggingHandler;
import cn.t.tool.nettytool.handler.NettyExceptionHandler;
import cn.t.util.common.CollectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.List;

import static cn.t.tool.nettytool.constants.HandlerNames.*;

public class NettyChannelInitializer<C extends Channel> extends ChannelInitializer<C> {

    private final DaemonConfig<C> daemonConfig;

    @Override
    protected void initChannel(C ch) {
        ChannelPipeline channelPipeline = ch.pipeline();
        //切换日志实现，防止有人篡改LoggerFactory，强制使用Slf4JLoggerFactory
        InternalLoggerFactory originalInternalLoggerFactory = InternalLoggerFactory.getDefaultFactory();
        InternalLoggerFactory.setDefaultFactory(daemonConfig.getInternalLoggerFactory());
        try {
            //logging
            if(daemonConfig.getLoggingHandlerLogLevel() != null) {
                channelPipeline.addLast(LOGGING_HANDLER, new EventLoggingHandler(daemonConfig.getLoggingHandlerLogLevel()));
            }
            //idle
            if(daemonConfig.getIdleStateHandlerFactory() != null) {
                channelPipeline.addLast(IDLE_HANDLER, daemonConfig.getIdleStateHandlerFactory().apply(ch));
            }
            // b2m decoder
            if(daemonConfig.getNettyB2mDecoderFactory() != null) {
                channelPipeline.addLast(MSG_DECODER, daemonConfig.getNettyB2mDecoderFactory().apply(ch));
            }
            // m2b encoder
            if(daemonConfig.getNettyM2bEncoderListFactory() != null) {
                List<MessageToByteEncoder<?>> nettyTcpEncoderList = daemonConfig.getNettyM2bEncoderListFactory().apply(ch);
                if(!CollectionUtil.isEmpty(nettyTcpEncoderList)) {
                    nettyTcpEncoderList.forEach(encoder -> channelPipeline.addLast(ENCODER_PREFIX + encoder.getClass().getName(), encoder));
                }
            }
            // m2m decoder
            if(daemonConfig.getNettyM2mDecoderFactory() != null) {
                List<MessageToMessageDecoder<?>> nettyM2mDecoderList = daemonConfig.getNettyM2mDecoderFactory().apply(ch);
                if(!CollectionUtil.isEmpty(nettyM2mDecoderList)) {
                    nettyM2mDecoderList.forEach(decoder -> channelPipeline.addLast(DECODER_PREFIX + decoder.getClass().getName(), decoder));
                }
            }
            // m2m encoder
            if(daemonConfig.getNettyM2mEncoderListFactory() != null) {
                List<MessageToMessageEncoder<?>> nettyTcpEncoderList = daemonConfig.getNettyM2mEncoderListFactory().apply(ch);
                if(!CollectionUtil.isEmpty(nettyTcpEncoderList)) {
                    nettyTcpEncoderList.forEach(encoder -> channelPipeline.addLast(ENCODER_PREFIX + encoder.getClass().getName(), encoder));
                }
            }
            // handler
            if(daemonConfig.getChannelHandlerListFactory() != null) {
                List<ChannelHandler> channelHandlerList = daemonConfig.getChannelHandlerListFactory().apply(ch);
                if(!CollectionUtil.isEmpty(channelHandlerList)) {
                    NettyB2mDecoder nettyB2mDecoder = (NettyB2mDecoder)channelPipeline.get(MSG_DECODER);
                    channelHandlerList.forEach(handler -> {
                        if(handler instanceof NettyB2mDecoderAware) {
                            ((NettyB2mDecoderAware)handler).setNettyB2mDecoder(nettyB2mDecoder);
                        }
                        channelPipeline.addLast(HANDLER_PREFIX + handler.getClass().getName(), handler);
                    });
                }
            }
            channelPipeline.addLast(EXCEPTION_HANDLER, new NettyExceptionHandler());
        } finally {
            InternalLoggerFactory.setDefaultFactory(originalInternalLoggerFactory);
        }
    }

    public NettyChannelInitializer(DaemonConfig<C> daemonConfig) {
        this.daemonConfig = daemonConfig;
    }
}
