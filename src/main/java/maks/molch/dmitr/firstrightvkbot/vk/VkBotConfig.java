package maks.molch.dmitr.firstrightvkbot.vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.queries.messages.MessagesSendQuery;
import com.vk.api.sdk.queries.upload.UploadDocQuery;
import com.vk.api.sdk.queries.upload.UploadPhotoMessageQuery;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Component
@PropertySource("application.properties")
public class VkBotConfig {
    @Value("${vk.bot.response}")
    private String response;
    private int groupId;
    private String accessToken;
    private TransportClient transportClient;
    private VkApiClient vk;
    private GroupActor actor;
    private Random random = new Random();
    private static final int GROUP_PEER_ID = 2000000000;
    private static final Set<String> fileExtensionsDenied = new HashSet<>(Arrays.asList(
            "exe",
            "mp3"
    ));

    public MessagesSendQuery getMessagesSendQuery(int peerId){
        if(peerId>=GROUP_PEER_ID){
            return this.vk.messages().send(this.actor).chatId(peerId-GROUP_PEER_ID).randomId(random.nextInt(10000));
        }else{
            return this.vk.messages().send(this.actor).userId(peerId).randomId(random.nextInt(10000));
        }
    }
    public int getGroupId() {
        return groupId;
    }
    public String getResponse() {
        return response;
    }

    public VkBotConfig(@Value("${vk.bot.groupId}") int groupId, @Value("${vk.bot.accessToken}") String accessToken){
        this.groupId = groupId;
        this.accessToken = accessToken;
        this.transportClient = new HttpTransportClient();
        this.vk = new VkApiClient(this.transportClient);
        this.actor = new GroupActor(this.groupId, this.accessToken);
    }
    public String getUploadDocAttachId(File file, int peerId) throws IOException, ClientException, ApiException, JSONException {
        String[] fileParts = file.getName().split("\\.");
        String fileExtension = fileParts[fileParts.length-1];
        if(fileExtensionsDenied.contains(fileExtension)){
            File tempFile = new File(file.getAbsolutePath()+".delete.me");
            if(!file.renameTo(tempFile)){
                throw new FileNotFoundException("Not successful renaming file...");
            }
            file = tempFile;
        }
        UploadDocQuery uploadDocQuery = vk.upload().doc(vk.docs().getMessagesUploadServer(actor).peerId(peerId).execute().
                getUploadUrl().toString(), file);
        JSONObject jsonObject = new JSONObject(uploadDocQuery.executeAsString());
        try{
            JSONObject json = new JSONObject(this.vk.docs().save(this.actor, jsonObject.getString("file")).executeAsString());
            JSONObject jsonObj = json.getJSONObject("response").getJSONObject("doc");
            return "doc"+jsonObj.getLong("owner_id")+"_"+jsonObj.getLong("id");
        }catch (JSONException e){
            System.out.println("Add extension '"+fileExtension+"' in DENIED EXTENSIONS!!!");
            return "";
        }
    }
    public String getUploadPhotoAttachId(File file, int peerId) throws IOException, ClientException, ApiException, JSONException {
        URI url = vk.photos().getMessagesUploadServer(actor).peerId(peerId).execute().getUploadUrl();
        UploadPhotoMessageQuery uploadPhotoMessageQuery = vk.upload().photoMessage(url.toString(), file);
        JSONObject photoJsonObject = new JSONObject(uploadPhotoMessageQuery.executeAsString());
        JSONObject responseJsonObject = new JSONObject(vk.photos().
                saveMessagesPhoto(actor, photoJsonObject.getString("photo")).
                server(photoJsonObject.getInt("server")).
                hash(photoJsonObject.getString("hash"))
                .executeAsString());
        JSONObject content = responseJsonObject.
                getJSONArray("response").getJSONObject(0);
        int ownerId = content.getInt("owner_id");
        int id = content.getInt("id");
        return "photo"+ownerId+"_"+id;
    }
}
