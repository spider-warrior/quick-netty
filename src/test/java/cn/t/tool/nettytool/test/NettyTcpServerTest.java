package cn.t.tool.nettytool.test;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;
import cn.t.tool.nettytool.test.handler.TcpServerHandler;
import io.netty.handler.logging.LogLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NettyTcpClientTest
 *
 * @author <a href="mailto:yangjian@ifenxi.com">研发部-杨建</a>
 * @version V1.0
 * @since 2022-02-28 15:28
 **/
public class NettyTcpServerTest {
    public static void main(String[] args) {
        int serverPort = 18080;
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(LogLevel.DEBUG);
        //idle
        daemonConfigBuilder.configIdleHandler(0, 0, 10);
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(TcpServerHandler::new));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        NettyChannelInitializer nettyChannelInitializer = new NettyChannelInitializer(daemonConfig);
        List<DaemonService> daemonServerList = new ArrayList<>();
        NettyTcpServer proxyServer = new NettyTcpServer(String.format("socks5-proxy-server(%s:%s)", "127.0.0.1", serverPort), serverPort, nettyChannelInitializer);
        daemonServerList.add(proxyServer);
        DefaultLauncher defaultLauncher = new DefaultLauncher();
        defaultLauncher.setDaemonServiceList(daemonServerList);
        defaultLauncher.startup();
    }
}
