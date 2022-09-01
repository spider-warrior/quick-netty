package cn.t.tool.nettytool.test.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * UdpClientTest
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-20 16:07
 **/
public class UdpClientTest {
    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new UdpClientHandler());
        Channel channel = bootstrap.bind(15566).sync().channel();
        InetSocketAddress address = new InetSocketAddress("192.168.1.125", 88);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            for (int i = 0; i < 10; i++) {
                ByteBuf byteBuf = Unpooled.wrappedBuffer(("count: " + i + "\n").getBytes(StandardCharsets.UTF_8));
                channel.writeAndFlush(new DatagramPacket(byteBuf, address));
            }
            ByteBuf byteBuf = Unpooled.wrappedBuffer(("===========================\n").getBytes(StandardCharsets.UTF_8));
            channel.writeAndFlush(new DatagramPacket(byteBuf, address));
            scanner.nextLine();
        }

//        channel.closeFuture().sync();
//        group.shutdownGracefully();
    }

    public static class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
            ByteBuf buf = msg.content();
            System.out.println("receive msg: " + buf.toString(Charset.defaultCharset()));
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("========================================= channelActive =========================================");
        }
    }
}
