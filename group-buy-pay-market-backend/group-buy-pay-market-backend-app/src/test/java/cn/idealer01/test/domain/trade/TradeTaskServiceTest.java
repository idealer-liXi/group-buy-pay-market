package cn.idealer01.test.domain.trade;

import cn.idealer01.domain.trade.adapter.port.ITradePort;
import cn.idealer01.domain.trade.adapter.respository.ITradeRepository;
import cn.idealer01.domain.trade.model.entity.NotifyTaskEntity;
import cn.idealer01.domain.trade.service.task.TradeTaskService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static cn.idealer01.types.enums.NotifyTaskHTTPEnumVO.ERROR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TradeTaskServiceTest {

    @Test
    public void execNotifyJob_marksRetryWhenPortReturnsError() throws Exception {
        ITradeRepository repository = mock(ITradeRepository.class);
        ITradePort port = mock(ITradePort.class);
        NotifyTaskEntity task = notifyTask(1);
        when(port.groupBuyNotify(task)).thenReturn(ERROR.getCode());
        when(repository.updateNotifyTaskStatusRetry(task)).thenReturn(1);
        TradeTaskService service = service(repository, port);

        Map<String, Integer> result = service.execNotifyJob(task);

        assertEquals(Integer.valueOf(1), result.get("retryCount"));
        assertEquals(Integer.valueOf(0), result.get("errorCount"));
        verify(repository).updateNotifyTaskStatusRetry(task);
    }

    @Test
    public void execNotifyJob_marksRetryWhenPortThrowsException() throws Exception {
        ITradeRepository repository = mock(ITradeRepository.class);
        ITradePort port = mock(ITradePort.class);
        NotifyTaskEntity task = notifyTask(1);
        when(port.groupBuyNotify(task)).thenThrow(new RuntimeException("mq down"));
        when(repository.updateNotifyTaskStatusRetry(task)).thenReturn(1);
        TradeTaskService service = service(repository, port);

        Map<String, Integer> result = service.execNotifyJob(task);

        assertEquals(Integer.valueOf(1), result.get("retryCount"));
        assertEquals(Integer.valueOf(0), result.get("errorCount"));
        verify(repository).updateNotifyTaskStatusRetry(task);
    }

    @Test
    public void execNotifyJob_marksErrorWhenRetryLimitExceeded() throws Exception {
        ITradeRepository repository = mock(ITradeRepository.class);
        ITradePort port = mock(ITradePort.class);
        NotifyTaskEntity task = notifyTask(6);
        when(port.groupBuyNotify(task)).thenThrow(new RuntimeException("mq down"));
        when(repository.updateNotifyTaskStatusError(task)).thenReturn(1);
        TradeTaskService service = service(repository, port);

        Map<String, Integer> result = service.execNotifyJob(task);

        assertEquals(Integer.valueOf(0), result.get("retryCount"));
        assertEquals(Integer.valueOf(1), result.get("errorCount"));
        verify(repository).updateNotifyTaskStatusError(task);
        verify(repository, never()).updateNotifyTaskStatusRetry(task);
    }

    private NotifyTaskEntity notifyTask(int notifyCount) {
        return NotifyTaskEntity.builder()
                .uuid("task-1")
                .teamId("team-1")
                .notifyType("MQ")
                .notifyMQ("topic.team_success")
                .parameterJson("{}")
                .notifyCount(notifyCount)
                .build();
    }

    private TradeTaskService service(ITradeRepository repository, ITradePort port) throws Exception {
        TradeTaskService service = new TradeTaskService();
        setField(service, "repository", repository);
        setField(service, "port", port);
        return service;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = TradeTaskService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
