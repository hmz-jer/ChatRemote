import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.mockito.*;

public class IcoStateChekerServiceTest {

    private SocketController socketController;
    private IcoStateChekerService service;

    @Before
    public void setup() {
        socketController = mock(SocketController.class);
        service = IcoStateChekerService.getInstance();
        Whitebox.setInternalState(service, "socketController", socketController);
    }

    @Test
    public void testCheck_withSameIcoStatus_shouldNotGenerateLog() {
        // Arrange
        IcoStatus status = new IcoStatus("UP");
        when(socketController.runningThreadStatus()).thenReturn(status);
        String icoStatus = Whitebox.getInternalState(service, "icoStatus");

        // Act
        service.check();

        // Assert
        assertEquals(icoStatus, Whitebox.getInternalState(service, "icoStatus"));
        verifyZeroInteractions(RsLogEnum.class);
    }

    @Test
    public void testCheck_withDifferentIcoStatus_shouldGenerateLog() {
        // Arrange
        IcoStatus status = new IcoStatus("DEGRADED");
        when(socketController.runningThreadStatus()).thenReturn(status);
        String icoStatus = Whitebox.getInternalState(service, "icoStatus");

        // Act
        service.check();

        // Assert
        assertNotEquals(icoStatus, Whitebox.getInternalState(service, "icoStatus"));
        verify(RsLogEnum.ICON_STATE_TRANSITION_DEGRADED).generateLog(icoStatus, "DEGRADED");
    }
}
