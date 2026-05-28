package cn.idealer01.test.infrastructure;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class SkuImageDaoSqlTest {

    @Test
    public void mapper_containsSkuImageQueriesOrderedBySortOrder() throws Exception {
        String mapper = new String(Files.readAllBytes(Paths.get("src/main/resources/mybatis/mapper/ISkuImageDao.xml")), StandardCharsets.UTF_8);

        assertTrue(mapper.contains("<mapper namespace=\"cn.idealer01.infrastructure.dao.ISkuImageDao\">"));
        assertTrue(mapper.contains("select id, goods_id, image_url, oss_object_key, sort_order"));
        assertTrue(mapper.contains("order by sort_order asc, id asc"));
        assertTrue(mapper.contains("max(sort_order)"));
        assertTrue(mapper.contains("delete from sku_image"));
    }

    @Test
    public void bootstrapSql_containsSkuImageTable() throws Exception {
        String sql = new String(Files.readAllBytes(Paths.get("../docs/dev-ops/mysql/sql/01-group_buy_pay_market.sql")), StandardCharsets.UTF_8);

        assertTrue(sql.contains("DROP TABLE IF EXISTS `sku_image`"));
        assertTrue(sql.contains("CREATE TABLE `sku_image`"));
        assertTrue(sql.contains("`goods_id` varchar(16) NOT NULL"));
        assertTrue(sql.contains("KEY `idx_goods_sort` (`goods_id`,`sort_order`,`id`)"));
    }
}
