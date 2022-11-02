package maks.molch.dmitr.firstrightvkbot.vk;

import com.google.gson.Gson;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.callback.Base;
import com.vk.api.sdk.objects.messages.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class VkController {

    @Autowired
    VkBotConfig vkBotConfig;

    private File uploadedFilesFolder = new File("src/main/resources/uploadedFiles");
    private List<String> photos = Arrays.asList(
            "pain.jpg",
            "Russia.png"
    );


    @PostMapping()
    public String requestHandle(@RequestBody String str){
        System.out.println("JSON: "+str);
        JSONObject jsonObj = new JSONObject(str);
        Gson gson = new Gson();
        Base base = gson.fromJson(str, Base.class);
        if(base.getSecret().equals(vkBotConfig.getSecretPassword())) {
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
                case MESSAGE_TYPING_STATE:
                    int peerId = jsonObj.getJSONObject("object").getInt("from_id");
                    typingUpdate(peerId);
                    return "ok";
                default:
                    System.out.println("Unknown type: " + base.getType());
                    return "ok";
            }
        }else {
            return "no";
        }
    }

    private void typingUpdate(int peerId){
        try {
            vkBotConfig.getMessagesSendQuery(peerId).message("I'm all ears...").execute();
        } catch (ApiException|ClientException e) {
            throw new RuntimeException(e);
        }
    }

    private void onUpdate(Message message){
//        System.out.println("Text:" + message.getText());
        int peerId = message.getPeerId();
        Matcher matcherDoc = Pattern.compile("/sendMeDocument").matcher(message.getText());
        Matcher matcherPhoto = Pattern.compile("/sendMePhoto").matcher(message.getText());
        Matcher matcherHelp = Pattern.compile("/help").matcher(message.getText());
        Random r = new Random();
        try {
            if (matcherDoc.find()) {
                vkBotConfig.getMessagesSendQuery(peerId).message("I'm sending doc").execute();
                vkBotConfig.getMessagesSendQuery(peerId).message(".").execute();
                vkBotConfig.getMessagesSendQuery(peerId).message("..").execute();
                vkBotConfig.getMessagesSendQuery(peerId).message("...").execute();
                vkBotConfig.getMessagesSendQuery(peerId).message("Your doc:").
                        attachment(vkBotConfig.getUploadDocAttachId(
                                Objects.requireNonNull(uploadedFilesFolder.listFiles())
                                        [r.nextInt(1000)%uploadedFilesFolder.listFiles().length],
                                        peerId)).execute();
            } else if(matcherPhoto.find()) {
                vkBotConfig.getMessagesSendQuery(peerId).message("I'm sending photo").execute();
                vkBotConfig.getMessagesSendQuery(peerId).message(".").execute();
                vkBotConfig.getMessagesSendQuery(peerId).message("..").execute();
                vkBotConfig.getMessagesSendQuery(peerId).message("...").execute();
                vkBotConfig.getMessagesSendQuery(peerId).message("Your photo:").attachment(
                                vkBotConfig.getUploadPhotoAttachId(new File("src/main/resources/uploadedFiles/"+
                                                photos.get(r.nextInt(1000)%photos.size())),
                                        peerId)).execute();
            } else if (matcherHelp.find()) {
                vkBotConfig.getMessagesSendQuery(peerId).
                        message("/help").execute();
                vkBotConfig.getMessagesSendQuery(peerId).
                        message("- List of all commands").execute();
                vkBotConfig.getMessagesSendQuery(peerId).
                        message("/sendMeDocument").execute();
                vkBotConfig.getMessagesSendQuery(peerId).
                        message("- sending document for you").execute();
                vkBotConfig.getMessagesSendQuery(peerId).
                        message("/sendMePhoto").execute();
                vkBotConfig.getMessagesSendQuery(peerId).
                        message(" - sending photo for you").execute();
            }else{
                vkBotConfig.getMessagesSendQuery(peerId).message("This command doesn't exist\n" +
                        "You can send /help to help").execute();
            }
        }catch (IOException|ClientException|ApiException|JSONException e) {
//            try {
//                vkBotConfig.getMessagesSendQuery(peerId).
//                        message("Sorry for happening error...").execute();
//            } catch (ApiException|ClientException ex) {
//                throw new RuntimeException(ex);
//            }
            throw new RuntimeException(e);
        }
    }

}
