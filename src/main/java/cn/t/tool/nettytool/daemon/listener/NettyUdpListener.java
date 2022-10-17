package cn.t.tool.nettytool.daemon.listener;

import cn.t.tool.nettytool.daemon.DaemonService;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyUdpListener implements DaemonListener {

    private static final Logger logger = LoggerFactory.getLogger(NettyUdpListener.class);

    @Override
    public void startup(DaemonService server, Channel channel) {
        logger.info(server.getClass() + " start....");
    }

    @Override
    public void close(DaemonService server, Channel channel) {
        logger.info(server.getClass() + " stop....");
    }

    @Override
    public void close(DaemonService server, Channel channel, Exception e) {
        logger.error(server.getClass() + " stop....", e);
    }
}
