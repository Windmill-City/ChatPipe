package cappcraft.chat.network.message;

import com.google.gson.JsonElement;

public interface IMessageType {
    //se JsonTypeName to get type value
    public String getJsonTypeName();
    //use type value to get class
    public Class<? extends IMessage> getJsonTypeClass(JsonElement typeValue);
}
