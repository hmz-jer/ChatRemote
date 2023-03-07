import org.junit.BeforeClass;
import org.junit.Test;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import org.slf4j.Logger;

public class CertificateCheckerTest {

    private static CertificateInfo certificateInfoMock;
    private static X509Certificate x509CertificateMock;
    private static LocalDateTime dateTime;

    @BeforeClass
    public static void setUp() {
        certificateInfoMock = mock(CertificateInfo.class);
        x509CertificateMock = mock(X509Certificate.class);
        dateTime = LocalDateTime.now();
    }

    @Test
    public void checkDate_withValidCertificateInfoAndDateTime_logsMessages() {
        // GIVEN
        int deadLineWarn = 30;
        int deadLineError = 7;
        long daysBeforeNotBefore = -10;
        long daysBeforeNotAfterError = 2;
        long daysBeforeNotAfterWarn = 20;
        when(certificateInfoMock.getCertificate()).thenReturn(x509CertificateMock);
        when(certificateInfoMock.getOriginFilePath()).thenReturn("file/path");
        try {
            when(certificateInfoMock.getThumbprint()).thenReturn("thumbprint");
        } catch (Exception e) {
            fail("An exception occurred while mocking the CertificateInfo object: " + e.getMessage());
        }
        when(x509CertificateMock.getNotBefore()).thenReturn(dateTime.plusDays(daysBeforeNotBefore).atZone(ZoneId.systemDefault()).toInstant());
        when(x509CertificateMock.getNotAfter()).thenReturn(dateTime.plusDays(daysBeforeNotAfterWarn).atZone(ZoneId.systemDefault()).toInstant());

        // WHEN
        CertificateChecker.checkDate(certificateInfoMock, dateTime, deadLineWarn, deadLineError);

        // THEN
        verify(x509CertificateMock, times(1)).getNotBefore();
        verify(x509CertificateMock, times(2)).getNotAfter();
        Logger logger = LoggerFactory.getLogger(CertificateChecker.class);
        verify(logger, times(1)).debug("message 1");
        verify(logger, times(1)).debug("message 4");
    }

    @Test
    public void checkDate_withExpiredCertificateInfoAndDateTime_logsMessages() {
        // GIVEN
        int deadLineWarn = 30;
        int deadLineError = 7;
        long daysBeforeNotBefore = -10;
        long daysBeforeNotAfterError = 2;
        long daysBeforeNotAfterWarn = -2;
        when(certificateInfoMock.getCertificate()).thenReturn(x509CertificateMock);
        when(certificateInfoMock.getOriginFilePath()).thenReturn("file/path");
        try {
            when(certificateInfoMock.getThumbprint()).thenReturn("thumbprint");
        } catch (Exception e) {
            fail("An exception occurred while mocking the CertificateInfo object: " + e.getMessage());
        }
        when(x509CertificateMock.getNotBefore()).thenReturn(dateTime.plusDays(daysBeforeNotBefore).atZone(ZoneId.systemDefault()).toInstant());
        when(x509CertificateMock.getNotAfter()).thenReturn(dateTime.plusDays(daysBeforeNotAfterWarn).atZone(ZoneId.systemDefault()).toInstant());

        // WHEN
        CertificateChecker.checkDate(certificateInfoMock, dateTime, deadLineWarn, deadLineError);

        // THEN
        verify(x509CertificateMock, times(1)).getNotBefore();
        verify(x509CertificateMock, times(2)).getNotAfter();
        Logger logger = LoggerFactory.getLogger(CertificateChecker.class);
        verify(logger, times(1)).debug("message 1");
        verify(logger, times(1
