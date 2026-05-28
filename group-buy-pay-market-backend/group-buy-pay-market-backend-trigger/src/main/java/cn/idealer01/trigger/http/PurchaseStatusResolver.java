package cn.idealer01.trigger.http;

import cn.idealer01.infrastructure.dao.po.PayOrder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PurchaseStatusResolver {

    public String resolve(PayOrder payOrder) {
        if (payOrder == null) {
            return null;
        }

        String status = payOrder.getStatus();
        Integer marketType = payOrder.getMarketType();

        if (isGroupOrder(marketType) && isRefundedGroupDetail(payOrder)) {
            return "CLOSED";
        }
        if ("CLOSE".equals(status)) {
            return "CLOSED";
        }
        if ("CREATE".equals(status) && isGroupOrder(marketType)) {
            return "WAIT_PAY";
        }
        if ("PAY_WAIT".equals(status) && StringUtils.isNotBlank(payOrder.getPayUrl())) {
            return "WAIT_PAY";
        }
        if ("PAY_SUCCESS".equals(status)) {
            if (isGroupOrder(marketType)) {
                return resolveGroupPaidStatus(payOrder);
            }
            return "GROUP_SUCCESS";
        }
        if ("MARKET".equals(status) || "DEAL_DONE".equals(status)) {
            return "GROUP_SUCCESS";
        }

        return null;
    }

    private boolean isGroupOrder(Integer marketType) {
        return marketType != null && marketType == 1;
    }

    private boolean isRefundedGroupDetail(PayOrder payOrder) {
        return payOrder.getGroupOrderListStatus() != null && payOrder.getGroupOrderListStatus() == 2;
    }

    private String resolveGroupPaidStatus(PayOrder payOrder) {
        Integer groupOrderStatus = payOrder.getGroupOrderStatus();
        if (groupOrderStatus != null) {
            if (groupOrderStatus == 1 || groupOrderStatus == 3) {
                return "GROUP_SUCCESS";
            }
            if (groupOrderStatus == 2) {
                return "CLOSED";
            }
        }

        Date groupValidEndTime = payOrder.getGroupValidEndTime();
        if (groupValidEndTime != null && !groupValidEndTime.after(new Date())) {
            return "CLOSED";
        }

        return "GROUP_WAIT";
    }
}
