package cappcraft.chat;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = ChatPipe.MODID, name = ChatPipe.NAME, version = ChatPipe.VERSION, useMetadata = true,acceptableRemoteVersions = "*")
public class ChatPipe
{
    public static final String MODID = "chatpipe";
    public static final String NAME = "ChatPipe";
    public static final String VERSION = "1.3";

    public static Logger logger;
    @SidedProxy(clientSide = "cappcraft.chat.CommonProxy",serverSide = "cappcraft.chat.ClientProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        CommonProxy.preinit();
        Config.initConfig(event.getSuggestedConfigurationFile());
        logger = event.getModLog();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        CommonProxy.init();
    }

    @Mod.EventHandler
    public void serverStarted(FMLLoadCompleteEvent event){
        CommonProxy.finished();
    }
}
