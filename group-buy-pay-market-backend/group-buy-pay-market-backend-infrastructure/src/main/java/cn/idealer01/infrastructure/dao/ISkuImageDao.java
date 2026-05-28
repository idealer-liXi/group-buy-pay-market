package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.SkuImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ISkuImageDao {
    List<SkuImage> querySkuImagesByGoodsId(String goodsId);

    List<SkuImage> querySkuImagesByGoodsIds(@Param("goodsIds") Collection<String> goodsIds);

    SkuImage querySkuImageById(@Param("goodsId") String goodsId, @Param("id") Long id);

    Integer queryMaxSortOrder(String goodsId);

    int insertSkuImage(SkuImage skuImage);

    int deleteSkuImage(@Param("goodsId") String goodsId, @Param("id") Long id);
}
