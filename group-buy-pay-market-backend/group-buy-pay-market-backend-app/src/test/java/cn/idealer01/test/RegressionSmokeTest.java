package cn.idealer01.test;

import cn.idealer01.test.domain.order.LocalGroupBuyMarketPortTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ApplicationSmokeTest.class,
        BaseContractMergeTest.class,
        LocalGroupBuyMarketPortTest.class
})
public class RegressionSmokeTest {
}
