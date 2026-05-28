package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.SCSkuActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ISCSkuActivityDao {
    SCSkuActivity querySCSkuActivityBySCGoodsId(SCSkuActivity scSkuActivity);

    List<SCSkuActivity> querySCSkuActivityListByActivityId(Long activityId);

    int insertSCSkuActivity(SCSkuActivity scSkuActivity);

    int updateSCSkuActivity(SCSkuActivity scSkuActivity);

    int updateSCSkuActivityByActivityId(@Param("activityId") Long activityId, @Param("goodsId") String goodsId, @Param("source") String source, @Param("channel") String channel);
}
