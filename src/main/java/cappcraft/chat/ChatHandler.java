package cappcraft.chat;

import cappcraft.chat.network.ExternelChatHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.minecraftforge.event.ServerChatEvent;

public class ChatHandler {
    @SubscribeEvent
    public static void onClientChatReceived(ServerChatEvent chat){
        if(!chat.message.startsWith("/")){
            ExternelChatHandler.INSTANCE.sendChatOutbound(new TextWebSocketFrame("[" + chat.username + "]: " + chat.message));
        }
    }
}
