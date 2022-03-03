package cn.t.tool.nettytool.test.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * TcpClientHandler
 *
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2022-02-28 15:40
 **/
public class TcpClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        byte[] content = "GET / HTTP/1.1\r\n\r\n".getBytes();
//        byte[] content = "GET / HTTP/1.1\r\nconnection:keep-alive\r\n\r\n".getBytes();
        if(true) {
            throw new RuntimeException("on purpose");
        }
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(content.length);
        buf.writeBytes(content);
        ctx.writeAndFlush(buf);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        int size = msg.readableBytes();
        byte[] content = new byte[size];
        msg.readBytes(content);
        System.out.println(new String(content));
    }
}
