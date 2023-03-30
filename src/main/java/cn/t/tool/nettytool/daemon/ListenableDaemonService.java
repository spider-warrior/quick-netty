package cn.t.tool.nettytool.daemon;

import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.util.common.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class ListenableDaemonService implements DaemonService {
    protected final List<DaemonListener> daemonListenerList = new ArrayList<>();
    public void addListener(DaemonListener daemonListener) {
        if(daemonListener != null) {
            this.daemonListenerList.add(daemonListener);
        }
    }
    public void addListenerList(List<DaemonListener> daemonListenerList) {
        if(!CollectionUtil.isEmpty(daemonListenerList)) {
            this.daemonListenerList.addAll(daemonListenerList);
        }
    }
}
