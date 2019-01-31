package cappcraft.chat.network.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;

public interface IMessage {
    public abstract IMessage onDeseralize(JsonObject in);
    public abstract void onSeralize(JsonObject in);
    public abstract void onReceived(ChannelHandlerContext ctx, Gson gson);
}
