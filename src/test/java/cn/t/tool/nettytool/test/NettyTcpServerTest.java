package cn.t.tool.nettytool.test;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;
import cn.t.tool.nettytool.test.handler.TcpServerHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * NettyTcpClientTest
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-02-28 15:28
 **/
public class NettyTcpServerTest {
    public static void main(String[] args) {
        int[] serverPorts = new int[]{18080};
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(LogLevel.DEBUG);
        //idle
        daemonConfigBuilder.configIdleHandler(0, 0, 10);
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(ch -> new TcpServerHandler()));
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        NettyTcpChannelInitializer nettyChannelInitializer = new NettyTcpChannelInitializer(daemonConfig);
        List<DaemonService> daemonServerList = new ArrayList<>();
        NettyTcpServer proxyServer = new NettyTcpServer(String.format("socks5-proxy-server(%s:%s)", "127.0.0.1", Arrays.toString(serverPorts)), serverPorts, nettyChannelInitializer, new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()), false, true);
        daemonServerList.add(proxyServer);
        DefaultLauncher defaultLauncher = new DefaultLauncher();
        defaultLauncher.setDaemonServiceList(daemonServerList);
        defaultLauncher.startup();
    }
}
