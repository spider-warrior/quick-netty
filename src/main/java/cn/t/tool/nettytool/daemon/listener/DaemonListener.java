package cn.t.tool.nettytool.daemon.listener;


import cn.t.tool.nettytool.daemon.DaemonService;
import io.netty.channel.Channel;

public interface DaemonListener {
    void startup(DaemonService server, Channel channel);
    void close(DaemonService server, Channel channel);
    void close(DaemonService server, Channel channel, Throwable t);
}
