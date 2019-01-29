package cappcraft.chat;

import cappcraft.chat.network.ExternelChatHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.ServerChatEvent;

public class ChatHandler {
    @SubscribeEvent
    public void onClientChatReceived(ServerChatEvent chat){
        if(!chat.message.startsWith("/")){
            ExternelChatHandler.INSTANCE.sendChatOutbound("[" + chat.username + "]: " + chat.message);
        }
    }
}
