package cappcraft.chat.coolq;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupMessage {
    public final String post_type = "message";
    public final String message_type = "group";
    public Anonymous anonymous;
    public String font;
    public String group_id;
    public String message;
    public String message_id;
    public String raw_message;
    public Sender sender;
    public String sub_type;
    public String user_id;

    static Pattern CQPattern = Pattern.compile("\\[CQ:[a-z]*(?:,[a-z]*=[^\\[\\],]*)*\\]");
    //            & -> &amp;
    static Pattern Escape_amp = Pattern.compile("(?:&amp;)+?");
    //            [ -> &#91;
    static Pattern Escape_91 = Pattern.compile("(?:&#91;)+?");
    //            ] -> &#93;
    static Pattern Escape_93 = Pattern.compile("(?:&#93;)+?");
    //            , -> &#94;
    static Pattern Escape_44 = Pattern.compile("(?:&#94;)+?");
    //remove \r
    static Pattern Escape_r = Pattern.compile("(?:(?:\n)?\r)+?");

    public static String getWithNoCQCode(String s, boolean escape){
        String noCQCodeString = getCQCodes(s).replaceAll("");
        if(escape){
            noCQCodeString = Escape_amp.matcher(noCQCodeString).replaceAll("&");
            noCQCodeString = Escape_91.matcher(noCQCodeString).replaceAll("[");
            noCQCodeString = Escape_93.matcher(noCQCodeString).replaceAll("]");
            noCQCodeString = Escape_44.matcher(noCQCodeString).replaceAll(",");
            noCQCodeString = Escape_r.matcher(noCQCodeString).replaceAll("\n");
        }
        return noCQCodeString;
    }

    public static Matcher getCQCodes(String s){
        Matcher m = CQPattern.matcher(s);
        return  m;
    }


    public JsonObject reply(String reply, boolean auto_escape, boolean at_sender, boolean delete){
        JsonObject json = new JsonObject();
        return json;
    }

    public static class Anonymous {
        public String flag;
        public String id;
        public String name;
    }

    public static class Sender {
        public String age;
        public String nickname;
        public String sex;
        public String user_id;
        public String card;
        public String area;
        public String level;
        public String role;
        public String title;
    }

    public static class GroupMessageDeserializer implements JsonDeserializer<GroupMessage> {

        @Override
        public GroupMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            GroupMessage groupMessage = new GroupMessage();
            Sender sender = new Sender();
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            groupMessage.font = jsonObject.get("font").getAsString();
            groupMessage.group_id = jsonObject.get("group_id").getAsString();
            groupMessage.message = jsonObject.get("message").getAsString();
            groupMessage.raw_message = jsonObject.get("raw_message").getAsString();
            groupMessage.message_id = jsonObject.get("message_id").getAsString();
            groupMessage.sub_type = jsonObject.get("sub_type").getAsString();
            groupMessage.user_id = jsonObject.get("user_id").getAsString();

            JsonObject senderObject = jsonObject.getAsJsonObject("sender");
//            sender.age = senderObject.get("age").getAsString();
            sender.nickname = senderObject.get("nickname").getAsString();
//            sender.area = senderObject.get("area").getAsString();
//            sender.level = senderObject.get("level").getAsString();
            sender.role = senderObject.get("role").getAsString();
//            sender.title = senderObject.get("title").getAsString();
            sender.card = senderObject.get("card").getAsString();
//            sender.user_id = senderObject.get("user_id").getAsString();
//            sender.sex = senderObject.get("sex").getAsString();
            groupMessage.sender = sender;

            if(!jsonObject.get("anonymous").isJsonNull()){
                Anonymous anonymous = new Anonymous();
                JsonObject anonymousObject = jsonObject.getAsJsonObject("anonymous");
                anonymous.flag = anonymousObject.get("flag").getAsString();
                anonymous.id = anonymousObject.get("id").getAsString();
                anonymous.name = anonymousObject.get("name").getAsString();
                groupMessage.anonymous = anonymous;
            }
            return groupMessage;
        }
    }
}
