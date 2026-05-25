package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ISkuDao {

    Sku querySkuByGoodsId(String goodsId);
}
