package cn.idealer01.test.infrastructure;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class UserTagMapperSqlTest {

    @Test
    public void marketUserMapperContainsUpsertSupportQueries() throws IOException {
        String mapper = read("src/main/resources/mybatis/mapper/IMarketUserDao.xml");

        assertTrue(mapper.contains("<select id=\"queryMarketUserByUserId\""));
        assertTrue(mapper.contains("<select id=\"queryMarketUserList\""));
        assertTrue(mapper.contains("<insert id=\"insertMarketUser\""));
        assertTrue(mapper.contains("<update id=\"updateMarketUserLastLoginTime\""));
    }

    @Test
    public void crowdTagMappersContainAdminMemberQueries() throws IOException {
        String tagsMapper = read("src/main/resources/mybatis/mapper/ICrowdTagsDao.xml");
        String detailMapper = read("src/main/resources/mybatis/mapper/ICrowdTagsDetailDao.xml");

        assertTrue(tagsMapper.contains("<select id=\"queryCrowdTagsList\""));
        assertTrue(tagsMapper.contains("<insert id=\"insertCrowdTags\""));
        assertTrue(tagsMapper.contains("<update id=\"updateCrowdTagsStatisticsTo\""));
        assertTrue(detailMapper.contains("<select id=\"queryCrowdTagsDetailListByTagId\""));
        assertTrue(detailMapper.contains("<select id=\"queryCrowdTagsDetailListByUserId\""));
        assertTrue(detailMapper.contains("<delete id=\"deleteCrowdTagsUserId\""));
    }

    private String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
