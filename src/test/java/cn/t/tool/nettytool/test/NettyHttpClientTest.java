package cn.t.tool.nettytool.test;

import cn.t.tool.nettytool.daemon.DaemonConfig;
import cn.t.tool.nettytool.daemon.client.NettyTcpClient;
import cn.t.tool.nettytool.initializer.DaemonConfigBuilder;
import cn.t.tool.nettytool.initializer.NettyChannelInitializer;
import cn.t.tool.nettytool.test.handler.EventLogHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * NettyHttpClientTest
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-03 14:40
 **/
public class NettyHttpClientTest {
    public static void main(String[] args) throws Exception {
        String host = "127.0.01";
        int port = 18080;
        String requestUri = "/";
        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
        //logging
        daemonConfigBuilder.configLogLevel(LogLevel.DEBUG);
        //idle
        daemonConfigBuilder.configIdleHandler(0, 0, 60);
        //handler
        List<Supplier<? extends ChannelHandler>> supplierList = new ArrayList<>();
        //http request decoder
        supplierList.add(HttpResponseDecoder::new);
        //http response encoder
        supplierList.add(HttpRequestEncoder::new);
        //http message aggregate
        supplierList.add(() -> new HttpObjectAggregator(1024 * 1024));
        //log event
        supplierList.add(EventLogHandler::new);
        //response message handler
        supplierList.add(() -> new PrintResponseHandler(requestUri, host));
        daemonConfigBuilder.configHandler(supplierList);
        DaemonConfig daemonConfig = daemonConfigBuilder.build();
        NettyChannelInitializer channelInitializer = new NettyChannelInitializer(daemonConfig);
        NettyTcpClient nettyTcpClient = new NettyTcpClient("http-client", host, port, channelInitializer);
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
