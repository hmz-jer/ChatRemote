import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import static org.mockito.Mockito.*;

class IcoStateChekerJobTest {

    @Test
    void execute_shouldCallIcoStateCheckerServiceCheckMethod() throws JobExecutionException {
        // Arrange
        IcoStateChekerService icoStateChekerService = mock(IcoStateChekerService.class);
        IcoStateChekerJob icoStateChekerJob = new IcoStateChekerJob();
        JobExecutionContext context = mock(JobExecutionContext.class);
        when(context.getJobDetail().getJobClass()).thenReturn(IcoStateChekerJob.class);
        when(context.getJobDetail().getJobDataMap()).thenReturn(new JobDataMap());

        // Act
        icoStateChekerJob.execute(context);

        // Assert
        verify(icoStateChekerService).check();
    }
}
