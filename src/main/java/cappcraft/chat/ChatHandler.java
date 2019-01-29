package cappcraft.chat;

import cappcraft.chat.network.ExternelChatHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatHandler {
    @SubscribeEvent
    public static void onClientChatReceived(ServerChatEvent chat){
        if(!chat.getMessage().startsWith("/")){
            ExternelChatHandler.INSTANCE.sendChatOutbound(new TextWebSocketFrame("[" + chat.getUsername() + "]: " + chat.getMessage()));
        }
    }
}
