package cn.t.tool.nettytool.daemon.listener;

import cn.t.tool.nettytool.daemon.DaemonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyUdpListener implements DaemonListener {

    private static final Logger logger = LoggerFactory.getLogger(NettyUdpListener.class);

    @Override
    public void startup(DaemonService server) {
        logger.info(server.getClass() + " start....");
    }

    @Override
    public void close(DaemonService server) {
        logger.info(server.getClass() + " stop....");
    }

}
