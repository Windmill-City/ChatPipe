package cappcraft.chat.network.message;

import cappcraft.chat.ChatPipe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelHandlerContext;

public class ResultMessage implements IMessage {

    public final MessageType type = MessageType.RESULT;
    public final RequestResult result;
    public final String extraMessage;
    public ResultMessage(RequestResult result) {
        this.result = result;
        extraMessage = null;
    }
    public ResultMessage(RequestResult result, String extraMessage) {
        this.result = result;
        this.extraMessage = extraMessage;
    }

    public ResultMessage(){
        result = null;
        extraMessage = null;
    }

    @Override
    public IMessage onDeseralize(JsonObject reader) {
        return null;
    }

    @Override
    public void onSeralize(JsonObject in) {
        in.addProperty("type",type.name());
        in.addProperty("result",result.name());
        in.addProperty("extraMessage",extraMessage);
    }

    @Override
    public void onReceived(ChannelHandlerContext ctx, Gson gson) {
        ChatPipe.logger.warn("Server received ResultMessage,this should not happen");
    }
}
