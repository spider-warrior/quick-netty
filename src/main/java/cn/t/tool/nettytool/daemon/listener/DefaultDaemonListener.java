package cn.t.tool.nettytool.daemon.listener;


import cn.t.tool.nettytool.daemon.DaemonService;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultDaemonListener implements DaemonListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultDaemonListener.class);

    @Override
    public void startup(DaemonService server, Channel channel) {
        logger.info(server.getClass() + " start....");
    }

    @Override
    public void close(DaemonService server, Channel channel) {
        logger.info(server.getClass() + " stop....");
    }

    @Override
    public void close(DaemonService server, Channel channel, Throwable t) {
        logger.error(server.getClass() + " stop....", t);
    }
}
