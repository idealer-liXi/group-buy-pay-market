package cn.idealer01.trigger.http;

import cn.idealer01.api.dto.FingerprintLoginRequestDTO;
import cn.idealer01.api.dto.FingerprintLoginResponseDTO;
import cn.idealer01.api.response.Response;
import cn.idealer01.infrastructure.dao.IMarketUserDao;
import cn.idealer01.infrastructure.dao.po.MarketUser;
import cn.idealer01.types.enums.ResponseCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/login")
public class FingerprintLoginController {

    private final IMarketUserDao marketUserDao;

    public FingerprintLoginController(IMarketUserDao marketUserDao) {
        this.marketUserDao = marketUserDao;
    }

    @PostMapping("/fingerprint")
    public Response<FingerprintLoginResponseDTO> loginByFingerprint(@RequestBody FingerprintLoginRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getVisitorId())) {
            return Response.<FingerprintLoginResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                    .build();
        }

        String userId = request.getVisitorId().trim();
        MarketUser marketUser = marketUserDao.queryMarketUserByUserId(userId);
        String displayName;
        if (marketUser == null) {
            displayName = generateDisplayName(userId);
            marketUserDao.insertMarketUser(MarketUser.builder()
                    .userId(userId)
                    .loginType("FINGERPRINT")
                    .displayName(displayName)
                    .status(0)
                    .build());
        } else {
            displayName = marketUser.getDisplayName();
            marketUserDao.updateMarketUserLastLoginTime(userId);
        }

        return Response.<FingerprintLoginResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(FingerprintLoginResponseDTO.builder().userId(userId).displayName(displayName).build())
                .build();
    }

    private String generateDisplayName(String userId) {
        String suffix = userId.length() <= 6 ? userId : userId.substring(userId.length() - 6);
        return "指纹用户-" + suffix;
    }
}
