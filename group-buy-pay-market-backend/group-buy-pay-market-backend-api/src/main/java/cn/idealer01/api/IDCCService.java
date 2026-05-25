package cn.idealer01.api;

import cn.idealer01.api.response.Response;

public interface IDCCService {
    Response<Boolean> updateCofig(String key, String value);
}
