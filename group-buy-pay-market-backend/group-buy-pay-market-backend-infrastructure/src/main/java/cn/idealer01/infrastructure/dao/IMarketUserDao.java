package cn.idealer01.infrastructure.dao;

import cn.idealer01.infrastructure.dao.po.MarketUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMarketUserDao {
    MarketUser queryMarketUserByUserId(@Param("userId") String userId);

    List<MarketUser> queryMarketUserList(@Param("keyword") String keyword);

    void insertMarketUser(MarketUser marketUser);

    void updateMarketUserLastLoginTime(@Param("userId") String userId);
}
