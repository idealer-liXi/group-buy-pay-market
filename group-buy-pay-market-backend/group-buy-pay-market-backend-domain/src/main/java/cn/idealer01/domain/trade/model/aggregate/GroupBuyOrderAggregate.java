package cn.idealer01.domain.trade.model.aggregate;

import cn.idealer01.domain.trade.model.entity.PayActivityEntity;
import cn.idealer01.domain.trade.model.entity.PayDiscountEntity;
import cn.idealer01.domain.trade.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupBuyOrderAggregate {
    /** 用户实体对象 */
    private UserEntity userEntity;
    /** 支付活动实体对象 */
    private PayActivityEntity payActivityEntity;
    /** 支付优惠实体对象 */
    private PayDiscountEntity payDiscountEntity;
    /** 已参与拼团量 */
    private Integer userTakeOrderCount;

}
