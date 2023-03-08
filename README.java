import org.junit.*;
import org.mockito.*;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IcoStateChekerServiceTest {

    private SocketController socketController;
    private IcoStateChekerService service;

    @Before
    public void setup() {
        socketController = mock(SocketController.class);
        service = IcoStateChekerService.getInstance();
        setPrivateField(service, "socketController", socketController);
    }

    @Test
    public void testCheck_withSameIcoStatus_shouldNotGenerateLog() {
        // Arrange
        IcoStatus status = new IcoStatus("UP");
        when(socketController.runningThreadStatus()).thenReturn(status);
        String icoStatus = getPrivateField(service, "icoStatus");

        // Act
        service.check();

        // Assert
        assertEquals(icoStatus, getPrivateField(service, "icoStatus"));
        verifyZeroInteractions(RsLogEnum.class);
    }

    @Test
    public void testCheck_withDifferentIcoStatus_shouldGenerateLog() {
        // Arrange
        IcoStatus status = new IcoStatus("DEGRADED");
        when(socketController.runningThreadStatus()).thenReturn(status);
        String icoStatus = getPrivateField(service, "icoStatus");

        // Act
        service.check();

        // Assert
        assertNotEquals(icoStatus, getPrivateField(service, "icoStatus"));
        verify(RsLogEnum.ICON_STATE_TRANSITION_DEGRADED).generateLog(icoStatus, "DEGRADED");
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to set private field " + fieldName + " in " + target.getClass().getName(), e);
        }
    }

    private Object getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to get private field " + fieldName + " in " + target.getClass().getName(), e);
        }
    }
}
