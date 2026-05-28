package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ISkuDao {

    Sku querySkuByGoodsId(String goodsId);

    List<Sku> querySkuList();

    int insertSku(Sku sku);

    int updateSku(Sku sku);

    int updateSkuStatus(@Param("goodsId") String goodsId, @Param("status") Integer status);
}
