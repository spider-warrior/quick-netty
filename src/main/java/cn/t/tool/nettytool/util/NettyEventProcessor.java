package cn.t.tool.nettytool.util;

import cn.t.tool.nettytool.constants.DaemonServiceChannelConstants;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.util.common.CollectionUtil;
import io.netty.channel.ChannelFuture;

import java.util.List;

public class NettyEventProcessor {
    public static void startFailed(ChannelFuture f) {
        f.channel().attr(DaemonServiceChannelConstants.STARTUP_ERROR).set(f.cause());
    }
    public static void daemonClose(List<DaemonListener> daemonListenerList, ChannelFuture f, DaemonService service) {
        if (!CollectionUtil.isEmpty(daemonListenerList)) {
            Throwable throwable = f.channel().attr(DaemonServiceChannelConstants.STARTUP_ERROR).get();
            if(throwable == null) {
                for (DaemonListener listener : daemonListenerList) {
                    listener.close(service, f.channel());
                }
            } else {
                for (DaemonListener listener : daemonListenerList) {
                    listener.close(service, f.channel(), throwable);
                }
            }
        }
    }
}
