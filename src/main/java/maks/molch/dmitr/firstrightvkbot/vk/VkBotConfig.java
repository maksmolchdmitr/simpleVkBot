package maks.molch.dmitr.firstrightvkbot.vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@PropertySource("application.properties")
public class VkBotConfig {
    private int groupId;
    private String accessToken;
    @Value("${vk.bot.response}")
    private String response;


    public int getGroupId() {
        return groupId;
    }

    public String getResponse() {
        return response;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public VkApiClient getVk() {
        return vk;
    }

    private TransportClient transportClient;
    private VkApiClient vk;
    private GroupActor actor;

    public VkBotConfig(@Value("${vk.bot.groupId}") int groupId, @Value("${vk.bot.accessToken}") String accessToken){
        this.groupId = groupId;
        this.accessToken = accessToken;
        this.transportClient = new HttpTransportClient();
        this.vk = new VkApiClient(this.transportClient);
        this.actor = new GroupActor(this.groupId, this.accessToken);
    }

    public void sendMessage(Message message){
        Integer peerId = message.getPeerId();
        Random random = new Random();
        if(peerId>=2000000000){
            try {
                Integer ts = vk.messages().getLongPollServer(actor).execute().getTs();
                vk.messages().send(actor).message("Haha:)").chatId(peerId-2000000000).randomId(random.nextInt(10000)).execute();
            } catch (ApiException | ClientException e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                Integer ts = vk.messages().getLongPollServer(actor).execute().getTs();
                vk.messages().send(actor).message("Haha:)").userId(peerId).randomId(random.nextInt(10000)).execute();
            } catch (ApiException | ClientException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
