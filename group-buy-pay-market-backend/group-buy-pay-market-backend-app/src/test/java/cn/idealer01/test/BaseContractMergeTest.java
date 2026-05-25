package cn.idealer01.test;

import cn.idealer01.api.response.Response;
import cn.idealer01.types.enums.ResponseCode;
import cn.idealer01.types.exception.AppException;
import org.junit.Assert;
import org.junit.Test;

public class BaseContractMergeTest {

    @Test
    public void shouldExposeUnifiedBaseContracts() {
        Response<String> response = Response.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data("ok")
                .build();

        Assert.assertEquals("0000", response.getCode());
        Assert.assertEquals("ok", response.getData());
        Assert.assertEquals("x", new AppException("x", "x").getInfo());
    }
}
