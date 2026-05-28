package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.GroupBuyDiscount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IGroupBuyDiscountDao {
    List<GroupBuyDiscount> queryGroupBuyDiscountList();

    GroupBuyDiscount queryGroupBuyActivityDiscountByDiscountId(String discountId);

    int insertGroupBuyDiscount(GroupBuyDiscount discount);

    int updateGroupBuyDiscount(GroupBuyDiscount discount);

    int updateGroupBuyDiscountStatus(@Param("discountId") String discountId, @Param("status") Integer status);
}
