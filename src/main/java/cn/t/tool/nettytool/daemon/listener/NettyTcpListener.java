package cn.t.tool.nettytool.daemon.listener;

import cn.t.tool.nettytool.daemon.DaemonService;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyTcpListener implements DaemonListener {

    private static final Logger logger = LoggerFactory.getLogger(NettyTcpListener.class);

    @Override
    public void startup(DaemonService server, Channel channel) {
        logger.info(server.getClass() + " start....");
    }

    @Override
    public void close(DaemonService server, Channel channel) {
        logger.info(server.getClass() + " stop....");
    }
}
