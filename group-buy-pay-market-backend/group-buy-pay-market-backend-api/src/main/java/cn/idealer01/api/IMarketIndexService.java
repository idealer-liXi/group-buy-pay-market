package cn.idealer01.api;

import cn.idealer01.api.dto.GoodsMarketRequestDTO;
import cn.idealer01.api.dto.GoodsMarketResponseDTO;
import cn.idealer01.api.response.Response;

public interface IMarketIndexService {
    /**
     * 查询拼团营销配置展示到首页
     * @param goodsMarketRequestDTO 营销商品和用户信息
     * @return 营销配置信息
     */
    Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(GoodsMarketRequestDTO goodsMarketRequestDTO);
}
