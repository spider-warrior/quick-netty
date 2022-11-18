package cn.t.tool.nettytool.test;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;
import cn.t.tool.nettytool.test.handler.EventLogHandler;
import cn.t.util.common.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * NettyHttpServerTest
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-03 10:40
 **/
public class NettyHttpServerTest {
    public static void main(String[] args) {
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging handler
        daemonConfigBuilder.configLogLevel(LogLevel.DEBUG);
        //idle handler
        daemonConfigBuilder.configIdleHandler(0, 0, 60);
        List<Function<SocketChannel, ? extends ChannelHandler>> factoriesList = new ArrayList<>();
        //http request decoder
        factoriesList.add(ch -> new HttpRequestDecoder());
        //http response encoder
        factoriesList.add(ch -> new HttpResponseEncoder());
        //http message aggregate
        factoriesList.add(ch -> new HttpObjectAggregator(1024 * 1024));
        //log event
        factoriesList.add(ch -> new EventLogHandler());
        //http proxy handler
        factoriesList.add(ch -> new EchoTimeRequestHandler());
        daemonConfigBuilder.configHandler(factoriesList);
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        NettyTcpChannelInitializer channelInitializer = new NettyTcpChannelInitializer(daemonConfig);
        NettyTcpServer httpServer = new NettyTcpServer("http-server", new int[]{5566}, channelInitializer, new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()), false, true);
        List<DaemonService> daemonServerList = new ArrayList<>();
        daemonServerList.add(httpServer);
        DefaultLauncher defaultLauncher = new DefaultLauncher();
        defaultLauncher.setDaemonServiceList(daemonServerList);
        defaultLauncher.startup();
    }

    private static class EchoTimeRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            byte[] timeStringBytes = DateUtil.formatLocalDateTime(LocalDateTime.now()).getBytes();
            ByteBuf content = ByteBufAllocator.DEFAULT.buffer(timeStringBytes.length).writeBytes(timeStringBytes);
            ctx.writeAndFlush(new DefaultFullHttpResponse(request.protocolVersion(), OK, content)).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
