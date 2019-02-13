package cappcraft.chat.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class HeaderChecker extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.channel().pipeline().remove(this);
        if(msg instanceof ByteBuf){
            ByteBuf byteBuf = (ByteBuf) msg;
            byteBuf.markReaderIndex();
            //Wrong Mc_PacketSize            PacketID != Mc_Handshake_PacketID
            if(readVarInt(byteBuf) == -1 || readVarInt(byteBuf) != 0x00) {
                //Removes MC's handlers
                ctx.channel().pipeline().remove("timeout");
                ctx.channel().pipeline().remove("splitter");
                ctx.channel().pipeline().remove("decoder");
                ctx.channel().pipeline().remove("prepender");
                ctx.channel().pipeline().remove("encoder");
                ctx.channel().pipeline().remove("packet_handler");
                //Add custom messagehandler
                ctx.channel().pipeline().addFirst(CustomInitializer.INSTANCE);
                ctx.channel().pipeline().fireChannelRegistered().fireChannelActive();
            }
            byteBuf.resetReaderIndex();
        }
        ctx.fireChannelRead(msg);
    }

    private static int readVarInt(ByteBuf byteBuf) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = byteBuf.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                //varInt too big
                return -1;
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }
}
