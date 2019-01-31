#ChatPipe

Chat between minecraft and external programs

#Usage
Put this mod in your server's mods  folder
Restart your server
Set server port in chatpipe.cfg

**You can use minecraft server's port**

use /chatpipe reload to reload the config
then you can use your websocketClient to connect to the server

**This mod use json to handle messages and commands**


----------
#Available Request
##Chat Message

    {
       "type":"CHAT"
       "sender":"PlayerName"
       "msg":"%^$%#&#&"
    }

|Name|Required Field?|Value|Value Desc|
|----|-----|-----|---------|
|type|✔|CHAT|Message type|
|sender|✔|String|chat sender|
|msg|✔|String|what you want to say|
##Command Message
    {
       "type":"COMMAND"
       "command":"RESTART"
    }



----------

    {
       "type":"COMMAND"
       "command":"MCCOMMAND"
       "sender":"Server"
       "value":"/nuke xxx"
    }

|Name|Required Field?|Value|Value Desc|
|----|-----|-----|---------|
|type|✔|COMMAND|Message type|
|command|✔|RESTART|Restart chatpipe's websocket server|
|||STOPSYNC|Stop listening Forge's chatevent,but you can send chat to server|
|||STARTSYNC|Reregister chatevent listener,if you use the STOPSYNC command|
|||MCCOMMAND|run minecraft's command|
|sender|MCCOMMAND Require|PlayerName or "Server"|command sender|
|value|MCCOMMAND Require|Command string|/nuke xxx|



