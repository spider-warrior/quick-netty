package cn.t.tool.nettytool.test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

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

//    public static void main(String[] args) {
//        String host = "127.0.01";
//        int port = 18080;
//        String requestUri = "/";
//        DaemonConfigBuilder daemonConfigBuilder = DaemonConfigBuilder.newInstance();
//        //logging
//        daemonConfigBuilder.configLogLevel(LogLevel.DEBUG);
//        //idle
//        daemonConfigBuilder.configIdleHandler(0, 0, 60);
//        //handler
//        List<Supplier<? extends ChannelHandler>> supplierList = new ArrayList<>();
//        //http request decoder
//        supplierList.add(HttpResponseDecoder::new);
//        //http response encoder
//        supplierList.add(HttpRequestEncoder::new);
//        //http message aggregate
//        supplierList.add(() -> new HttpObjectAggregator(1024 * 1024));
//        //log event
//        supplierList.add(EventLogHandler::new);
//        //response message handler
//        supplierList.add(() -> new PrintResponseHandler(requestUri, host));
//        daemonConfigBuilder.configHandler(supplierList);
//        DaemonConfig daemonConfig = daemonConfigBuilder.build();
//        NettyTcpChannelInitializer channelInitializer = new NettyTcpChannelInitializer(daemonConfig);
//        NettyTcpClient nettyTcpClient = new NettyTcpClient("http-client", host, port, channelInitializer, new NioEventLoopGroup(1), false, true);
//        nettyTcpClient.start();
//    }
//
//    private static class PrintResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
//
//        private final String host;
//        private final String uri;
//
//        @Override
//        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
//            System.out.println(response);
//            System.out.println(response.content().toString(Charset.defaultCharset()));
//        }
//
//        @Override
//        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, new URI(uri).toASCIIString());
//            request.headers().set(HttpHeaderNames.HOST, host);
//            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
//            ctx.writeAndFlush(request);
//        }
//
//        public PrintResponseHandler(String host, String uri) {
//            this.host = host;
//            this.uri = uri;
//        }
//    }

    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup(1))
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {

                }
            });
        bootstrap.attr(attrA, "valueA");
        ChannelFuture bindFuture = bootstrap.connect("www.baidu.com", 443);
        bindFuture.sync();
        Channel clientChannel = bindFuture.channel();
        ChannelFuture closeFuture = clientChannel.closeFuture();
        closeFuture.sync();
    }
}
