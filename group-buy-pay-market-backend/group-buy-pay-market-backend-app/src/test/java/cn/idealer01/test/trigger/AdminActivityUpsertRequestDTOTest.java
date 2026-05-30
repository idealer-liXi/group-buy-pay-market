package cn.idealer01.test.trigger;

import cn.idealer01.api.dto.AdminActivityUpsertRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AdminActivityUpsertRequestDTOTest {

    @Test
    public void deserializesDatetimeLocalActivityTimes() throws Exception {
        String json = "{\"startTime\":\"2026-05-29T21:30\",\"endTime\":\"2026-05-30T21:30\"}";

        AdminActivityUpsertRequestDTO request = new ObjectMapper().readValue(json, AdminActivityUpsertRequestDTO.class);

        assertNotNull(request.getStartTime());
        assertNotNull(request.getEndTime());

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        assertEquals(format.parse("2026-05-29 21:30"), request.getStartTime());
        assertEquals(format.parse("2026-05-30 21:30"), request.getEndTime());
    }
}
