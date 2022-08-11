package cn.t.tool.nettytool.initializer;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import io.netty.channel.socket.SocketChannel;

public class NettyTcpChannelInitializer extends NettyChannelInitializer<SocketChannel> {
    public NettyTcpChannelInitializer(DaemonConfig daemonConfig) {
        super(daemonConfig);
    }
}
