package cappcraft.chat;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import org.apache.logging.log4j.Logger;

@Mod(modid = ChatPipe.MODID, name = ChatPipe.NAME, version = ChatPipe.VERSION, useMetadata = true,acceptableRemoteVersions = "*")
public class ChatPipe
{
    public static final String MODID = "chatpipe";
    public static final String NAME = "ChatPipe";
    public static final String VERSION = "1.4";

    public static Logger logger;
    @SidedProxy(clientSide = "cappcraft.chat.ClientProxy",serverSide = "cappcraft.chat.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preinit();
        Config.initConfig(event.getSuggestedConfigurationFile());
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event){
        event.registerServerCommand(new ChatCommand());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event){
        proxy.finished();
    }
}
