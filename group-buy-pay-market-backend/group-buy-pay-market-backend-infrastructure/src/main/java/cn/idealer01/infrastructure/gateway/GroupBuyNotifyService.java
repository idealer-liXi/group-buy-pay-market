package cn.idealer01.infrastructure.gateway;

import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class GroupBuyNotifyService {

    @Resource
    private OkHttpClient okHttpClient;

    public String groupBuyNotify(String apiUrl, String notifyRequestDTOJSON) throws Exception{
        try{
            //构建请求参数
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody requestBody = RequestBody.create(mediaType, notifyRequestDTOJSON);
            Request request = new Request.Builder()
                    .addHeader("content-type", "application/json")
                    .post(requestBody)
                    .url(apiUrl)
                    .build();

            //调用接口
            Response response = okHttpClient.newCall(request).execute();

            return response.body().string();
        }catch (Exception e){
            log.error("拼团回调 http 请求接口失败");
            throw new AppException(ResponseCode.HTTP_EXCEPTION);
        }
    }

}
