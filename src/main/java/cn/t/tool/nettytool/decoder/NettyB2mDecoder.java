package cn.t.tool.nettytool.decoder;

import cn.t.tool.nettytool.analyser.ByteBufAnalyser;
import cn.t.tool.nettytool.util.NullMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NettyB2mDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(NettyB2mDecoder.class);

    private ByteBufAnalyser byteBufAnalyser;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.isReadable()) {
            int readerIndex = in.readerIndex();
            Object msg = byteBufAnalyser.analyse(in, ctx);
            if(msg == null) {
                logger.debug("[{}: message is incomplete, reader index reset", ctx.channel());
                in.readerIndex(readerIndex);
                break;
            } else {
                if(NullMessage.getNullMessage() == msg) {
                    logger.debug("[{}]: read a null message，reader index will not reset", ctx.channel());
                } else {
                    logger.debug("[{}]: decode success, type: {}", ctx.channel(), msg.getClass().getSimpleName());
                    out.add(msg);
                }
            }
        }
    }

    public NettyB2mDecoder(ByteBufAnalyser byteBufAnalyser) {
        this.byteBufAnalyser = byteBufAnalyser;
    }

    public void setByteBufAnalyser(ByteBufAnalyser byteBufAnalyser) {
        this.byteBufAnalyser = byteBufAnalyser;
    }
}
