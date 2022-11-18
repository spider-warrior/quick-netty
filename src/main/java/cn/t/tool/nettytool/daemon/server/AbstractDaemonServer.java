package cn.t.tool.nettytool.daemon.server;

import cn.t.tool.nettytool.daemon.ListenableDaemonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDaemonServer extends ListenableDaemonService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractDaemonServer.class);
    protected String name;
    protected int[] ports;

    @Override
    public final void start() {
        logger.debug("server: {} is going to start", name);
        doStart();
        logger.debug("server: {} is started", name);
    }

    @Override
    public final void close() {
        logger.debug("server: {} is going to stop", name);
        doClose();
        logger.debug("server: {} is stopped", name);
    }

    public abstract void doStart();

    public abstract void doClose();

    public AbstractDaemonServer() {}

    public AbstractDaemonServer(int[] ports) {
        this.ports = ports;
    }

    public AbstractDaemonServer(String name, int[] ports) {
        this.name = name;
        this.ports = ports;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getPorts() {
        return ports;
    }

    public void setPorts(int[] ports) {
        this.ports = ports;
    }
}
