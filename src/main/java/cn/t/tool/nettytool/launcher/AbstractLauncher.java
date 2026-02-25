package cn.t.tool.nettytool.launcher;

import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.tool.nettytool.launcher.listener.LauncherListener;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractLauncher implements Launcher, DaemonListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLauncher.class);

    protected volatile boolean stop = false;
    protected List<DaemonService> daemonServiceList;
    protected Set<DaemonService> startedDaemonService = Collections.synchronizedSet(new HashSet<>());
    protected Set<DaemonService> downedDaemonService = Collections.synchronizedSet(new HashSet<>());
    protected List<LauncherListener> launcherListenerList;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * 启动
     * */
    public void startup() {
        logger.info("launcher begin to startup");
        doStart();
        //回调监听器
        if (launcherListenerList != null && !launcherListenerList.isEmpty()) {
            logger.info("launcher listener size: {}, begin to call launcher listeners", launcherListenerList.size());
            for (LauncherListener listener: launcherListenerList) {
                listener.startup(this);
            }
        }
        if(!stop) {
            logger.info("launcher startup successfully");
        }
    }

    public abstract void doStart();

    /**
     * 关闭
     * */
    public void close() {
        logger.info("launcher begin to shutdown");
        stop = true;
        doClose();
        executorService.shutdown();
        try {
            boolean success = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if(!success) {
                executorService.shutdownNow();
            }
        } catch (Exception e) {
            logger.error("Executor Service shutdown error", e);
            executorService.shutdownNow();
        }
        //回调监听器
        if (launcherListenerList != null && !launcherListenerList.isEmpty()) {
            for (LauncherListener listener: launcherListenerList) {
                try { listener.close(this); } catch (Exception e) { logger.error("", e); }
            }
        }
        logger.info("launcher shutdown successfully");
    }

    //异步启动服务
    public void startServer(DaemonService server) {
        if (!executorService.isShutdown()) {
            executorService.submit(server::start);
        }
    }

    public abstract void doClose();

    @Override
    public void startup(DaemonService server, Channel channel) {
        boolean newServer = startedDaemonService.add(server);
        if(!newServer) {
            logger.warn("duplicated server started: {}", server);
        }
        logger.info("server alive count: {}", downedDaemonService.size());
    }

    @Override
    public void close(DaemonService server, Channel channel) {
        boolean contains = startedDaemonService.remove(server);
        if(!contains) {
            logger.warn("closed server not found in stopped server list: {}", server);
        }
        boolean newServer = downedDaemonService.add(server);
        if(!newServer) {
            logger.warn("duplicated server downed: {}", server);
        }
    }

    @Override
    public void close(DaemonService server, Channel channel, Throwable t) {
        logger.error("server start failed", t);
    }

    public List<DaemonService> getDaemonServiceList() {
        return daemonServiceList;
    }

    public AbstractLauncher setDaemonServiceList(List<DaemonService> daemonServiceList) {
        this.daemonServiceList = daemonServiceList;
        return this;
    }

    public List<LauncherListener> getLauncherListenerList() {
        return launcherListenerList;
    }

    public AbstractLauncher setLauncherListenerList(List<LauncherListener> launcherListenerList) {
        this.launcherListenerList = launcherListenerList;
        return this;
    }
}
