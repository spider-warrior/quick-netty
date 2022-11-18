package cn.t.tool.nettytool.test;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.test.handler.EventLogHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.util.AttributeKey;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * NettyHttpClientTest
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-03 14:40
 **/
public class NettyHttpClientTest {

    private static final AttributeKey<String> attrA = AttributeKey.newInstance("A");
    private static final AttributeKey<String> attrB = AttributeKey.newInstance("B");

    public static void main(String[] args) {
        String host = "127.0.01";
        int port = 5566;
        String requestUri = "/";
        DaemonConfigBuilder<SocketChannel> daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(LogLevel.DEBUG);
        //idle
        daemonConfigBuilder.configIdleHandler(0, 0, 60);
        //handler
        List<Function<SocketChannel, ? extends ChannelHandler>> factoriesList = new ArrayList<>();
        //http request decoder
        factoriesList.add(ch -> new HttpResponseDecoder());
        //http response encoder
        factoriesList.add(ch -> new HttpRequestEncoder());
        //http message aggregate
        factoriesList.add(ch -> new HttpObjectAggregator(1024 * 1024));
        //log event
        factoriesList.add(ch -> new EventLogHandler());
        //response message handler
        factoriesList.add(ch -> new PrintResponseHandler(requestUri, host));
        daemonConfigBuilder.configHandler(factoriesList);
        DaemonConfig<SocketChannel> daemonConfig = daemonConfigBuilder.build();
        NettyTcpChannelInitializer channelInitializer = new NettyTcpChannelInitializer(daemonConfig);
        NettyTcpClient nettyTcpClient = new NettyTcpClient("http-client", host, port, channelInitializer, new NioEventLoopGroup(1), false, true);
        nettyTcpClient.start();
    }

    private static class PrintResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

        private final String host;
        private final String uri;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
            System.out.println(response);
            System.out.println(response.content().toString(Charset.defaultCharset()));
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, new URI(uri).toASCIIString());
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
            ctx.writeAndFlush(request);
        }

        public PrintResponseHandler(String host, String uri) {
            this.host = host;
            this.uri = uri;
        }
    }

}
