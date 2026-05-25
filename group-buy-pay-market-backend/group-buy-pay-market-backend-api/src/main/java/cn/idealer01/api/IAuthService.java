package cn.idealer01.api;

import cn.idealer01.api.response.Response;

public interface IAuthService {

    Response<String> weixinQrCodeTicket();

    Response<String> checkLogin(String ticket);
    Response<String> weixinQrCodeTicket(String sceneStr);
    Response<String> checkLogin(String ticket, String sceneStr);

}
