package maks.molch.dmitr.firstrightvkbot.vk;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vk.api.sdk.objects.callback.Base;
import com.vk.api.sdk.objects.messages.Message;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class VkController {

    @Autowired
    VkBotConfig vkBotConfig;

    @PostMapping()
    public String requestHandle(@RequestBody String str){
        System.out.println("JSON: "+str);
        JSONObject jsonObj = new JSONObject(str);
        Gson gson = new Gson();
        Base base = gson.fromJson(str, Base.class);
        switch (base.getType()) {
            case CONFIRMATION:
                if (base.getGroupId() == vkBotConfig.getGroupId()) {
                    return vkBotConfig.getResponse().toString();
                } else {
                    return "Error";
                }
            case MESSAGE_NEW:
                Message message = gson.fromJson(jsonObj.getJSONObject("object").
                        getJSONObject("message").toString(), Message.class);
                onUpdate(message);
                return "ok";
            case MESSAGE_REPLY:
                return "ok";
            default:
                System.out.println("Unknown type: " + base.getType());
                return "ok";
        }
    }

    private void onUpdate(Message message){
        System.out.println("Message:" + message.toString());
        System.out.println("Text:" + message.getText());
        vkBotConfig.sendMessage(message);
    }

}
