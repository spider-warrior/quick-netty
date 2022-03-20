package cn.t.tool.nettytool.test.udp;

import cn.t.util.common.DateUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.nio.charset.Charset;
import java.time.LocalDateTime;

/**
 * UdpServerTest
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-03-20 16:07
 **/
public class UdpServerTest {
    public static void main(String[] args) throws Exception {
        Bootstrap serverBootstrap = new Bootstrap();
        EventLoopGroup group = new NioEventLoopGroup();
        serverBootstrap.group(group)
            .channel(NioDatagramChannel.class)
            .option(ChannelOption.SO_BROADCAST, true)
            .handler(new UdpServerHandler());
        serverBootstrap.bind(5566).sync().channel().closeFuture().sync();
        group.shutdownGracefully();
    }

    public static class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
            ByteBuf buf = msg.content();
            System.out.println("receive msg: " + buf.toString(Charset.defaultCharset()));
            ByteBuf response = Unpooled.wrappedBuffer(DateUtil.formatLocalDateTime(LocalDateTime.now()).getBytes());
            ctx.writeAndFlush(new DatagramPacket(response, msg.sender()));
        }
    }
}
