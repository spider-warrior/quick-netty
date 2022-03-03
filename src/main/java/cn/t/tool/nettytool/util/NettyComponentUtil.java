package cn.t.tool.nettytool.util;

import cn.t.tool.nettytool.constants.HandlerNames;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

/**
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2021-07-15 11:31
 **/
public class NettyComponentUtil {
    public static void removeAllHandler(ChannelPipeline channelPipeline, Class<? extends ChannelHandler> clazz) {
        while (true) {
            ChannelHandler handler = channelPipeline.get(clazz);
            if(handler == null) {
                break;
            }
            channelPipeline.remove(handler);
        }
    }
    public static <T extends ChannelHandler> void addLastHandler(ChannelPipeline channelPipeline, String handlerName, T handler) {
        channelPipeline.addBefore(HandlerNames.EXCEPTION_HANDLER, handlerName, handler);
    }
}
