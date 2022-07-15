package cn.t.tool.nettytool.launcher;

import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.tool.nettytool.launcher.listener.LauncherListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractLauncher implements Launcher, DaemonListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLauncher.class);

    protected volatile boolean stop = false;
    protected List<DaemonService> daemonServiceList;
    protected List<DaemonService> startedDaemonService = new ArrayList<>();
    protected List<DaemonService> downDaemonService = new ArrayList<>();
    protected List<LauncherListener> launcherListenerList;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 启动
     * */
    public void startup() {
        logger.info("launcher begin to startup");
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        doStart();
        //回调监听器
        if (launcherListenerList != null && !launcherListenerList.isEmpty()) {
            logger.info(String.format("launcher listener size: %d, begin to call launcher listeners", launcherListenerList.size()));
            for (LauncherListener listener: launcherListenerList) {
                listener.startup(this);
            }
        }
        logger.info("launcher startup successfully");
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
    public void startup(DaemonService server) {
        downDaemonService.remove(server);
        if(!downDaemonService.contains(server)) {
            startedDaemonService.add(server);
        }
        logger.info("server alive count: " + startedDaemonService.size());
    }

    @Override
    public void close(DaemonService server) {
        startedDaemonService.remove(server);
        if(!downDaemonService.contains(server)) {
            downDaemonService.add(server);
        }
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
