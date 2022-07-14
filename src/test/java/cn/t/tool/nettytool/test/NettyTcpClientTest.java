package cn.t.tool.nettytool.test;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyChannelInitializer;
import cn.t.tool.nettytool.test.handler.TcpClientHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;

import java.util.Collections;

/**
 * NettyTcpClientTest
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-02-28 15:39
 **/
public class NettyTcpClientTest {

    public static void main(String[] args) throws Exception {
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(LogLevel.DEBUG);
        //idle
        daemonConfigBuilder.configIdleHandler(0, 0, 180);
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(TcpClientHandler::new));
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        NettyChannelInitializer channelInitializer = new NettyChannelInitializer(daemonConfig);
        NettyTcpClient nettyTcpClient = new NettyTcpClient("test-client", "www.shansong.com", 80, channelInitializer, new NioEventLoopGroup(1), true);
        nettyTcpClient.start();
        Thread.sleep(10000);
    }
}
