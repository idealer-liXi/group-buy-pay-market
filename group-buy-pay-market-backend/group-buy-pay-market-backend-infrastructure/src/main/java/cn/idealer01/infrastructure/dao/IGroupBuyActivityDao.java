package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.GroupBuyActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IGroupBuyActivityDao {

    List<GroupBuyActivity> queryGroupBuyActivityList();

    GroupBuyActivity queryValidGroupBuyActivity(GroupBuyActivity groupBuyActivity);

    GroupBuyActivity queryValidGroupBuyActivityId(Long activityId);

    GroupBuyActivity queryGroupBuyActivityByActivityId(Long activityId);

    int insertGroupBuyActivity(GroupBuyActivity activity);

    int updateGroupBuyActivity(GroupBuyActivity activity);

    int updateGroupBuyActivityStatus(@Param("activityId") Long activityId, @Param("status") Integer status);
}
