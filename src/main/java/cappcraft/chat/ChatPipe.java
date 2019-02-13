package cappcraft.chat;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ChatPipe.MODID, name = ChatPipe.NAME, version = ChatPipe.VERSION, useMetadata = true,acceptableRemoteVersions = "*")
public class ChatPipe
{
    public static final String MODID = "chatpipe";
    public static final String NAME = "ChatPipe";
    public static final String VERSION = "1.5.1";

    public static Logger logger;
    @SidedProxy(clientSide = "cappcraft.chat.ClientProxy",serverSide = "cappcraft.chat.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preinit(event);
        logger = event.getModLog();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event){
        event.registerServerCommand(new ChatCommand());
    }

    @EventHandler
    public void serverStarted(FMLServerStartedEvent event){
        proxy.initCoolQ();
    }
}
