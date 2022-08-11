package cn.t.tool.nettytool.initializer;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import io.netty.channel.socket.DatagramChannel;

public class NettyUdpChannelInitializer extends NettyChannelInitializer<DatagramChannel> {
    public NettyUdpChannelInitializer(DaemonConfig daemonConfig) {
        super(daemonConfig);
    }
}
