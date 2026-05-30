package cn.idealer01.api;

import cn.idealer01.api.dto.CreatePayRequestDTO;
import cn.idealer01.api.dto.CreatePayResponseDTO;
import cn.idealer01.api.dto.NotifyRequestDTO;
import cn.idealer01.api.response.Response;

public interface IPayService {

    Response<CreatePayResponseDTO> createPayOrder(CreatePayRequestDTO createPayRequestDTO);
    /**
     * 拼团结算回调
     *
     * @param requestDTO 请求对象
     * @return 返参，success 成功
     */
    String groupBuyNotify(NotifyRequestDTO requestDTO);

}
