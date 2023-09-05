package cn.t.tool.nettytool.test;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.daemon.listener.DaemonListener;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.test.handler.TcpClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
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
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(LogLevel.DEBUG);
        //idle
        //fetch message handler
        daemonConfigBuilder.configHandler(Collections.singletonList(ch -> new TcpClientHandler()));
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        NettyTcpChannelInitializer channelInitializer = new NettyTcpChannelInitializer(daemonConfig);
        NettyTcpClient nettyTcpClient = new NettyTcpClient("test-client", "123", 80, channelInitializer, new NioEventLoopGroup(1), true, true);
        nettyTcpClient.addListenerList(Collections.singletonList(new DaemonListenerDemo()));
        nettyTcpClient.start();
        Thread.sleep(10000);
    }

    private static class DaemonListenerDemo implements DaemonListener {
        @Override
        public void startup(DaemonService server, Channel channel) {
            System.out.println("DaemonListenerDemo#startup(DaemonService server, Channel channel)");
        }

        @Override
        public void close(DaemonService server, Channel channel) {
            System.out.println("DaemonListenerDemo#close(DaemonService server, Channel channel)");
        }

        @Override
        public void close(DaemonService server, Channel channel, Throwable t) {
            System.out.println("DaemonListenerDemo#close(DaemonService server, Channel channel, Throwable t): " + Thread.currentThread());
        }
    }
}
