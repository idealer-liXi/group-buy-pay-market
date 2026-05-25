package cn.idealer01.config;

import cn.idealer01.infrastructure.gateway.IWeixinApiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Configuration
public class WeixinApiConfig {

    private static final String WEIXIN_BASE_URL = "https://api.weixin.qq.com/";

    @Bean
    public IWeixinApiService weixinApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WEIXIN_BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(IWeixinApiService.class);
    }
}
