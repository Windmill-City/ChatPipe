package cappcraft.chat.network.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.FMLCommonHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

public class ChatMessage implements IMessage {
    public final MessageType type = MessageType.CHAT;
    public String sender = null;
    public String msg = null;

    public ChatMessage(String username, String message) {
        sender = username;
        msg = message;
    }

    public ChatMessage(){}

    @Override
    public IMessage onDeseralize(JsonObject in) {
        sender = in.get("sender").getAsString();
        msg = in.get("msg").getAsString();
        return this;
    }

    @Override
    public void onSeralize(JsonObject in) {
        in.addProperty("type",type.name());
        in.addProperty("sender",sender);
        in.addProperty("msg",msg);
    }

    @Override
    public void onReceived(ChannelHandlerContext ctx, Gson gson) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendChatMsgImpl(new ChatComponentTranslation("chat.type.announcement"
                        , new Object[] {sender, new ChatComponentText(msg)}),false);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS))));
    }
}
