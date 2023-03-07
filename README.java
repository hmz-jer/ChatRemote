import org.junit.jupiter.api.Test;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import static org.mockito.Mockito.*;

class CertificateCheckerTest {

    @Test
    void checkDate_shouldLogMessage1_whenCertificateNotYetValid() throws Exception {
        // Arrange
        CertificateInfo certificateInfo = mock(CertificateInfo.class);
        X509Certificate cert = mock(X509Certificate.class);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime notBefore = now.plusDays(1);
        when(certificateInfo.getCertificate()).thenReturn(cert);
        when(cert.getNotBefore()).thenReturn(notBefore);
        when(certificateInfo.getThumbprint()).thenReturn("thumbprint");
        when(certificateInfo.getOriginFilePath()).thenReturn("/path/to/cert.pem");
        int deadLineWarn = 30;
        int deadLineError = 7;

        // Act
        CertificateChecker.checkDate(certificateInfo, now, deadLineWarn, deadLineError);

        // Assert
        verify(CertificateChecker.LOGGER).debug("message 1");
    }

    @Test
    void checkDate_shouldLogMessage2_whenCertificateExpired() throws Exception {
        // Arrange
        CertificateInfo certificateInfo = mock(CertificateInfo.class);
        X509Certificate cert = mock(X509Certificate.class);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime notAfter = now.minusDays(1);
        when(certificateInfo.getCertificate()).thenReturn(cert);
        when(cert.getNotAfter()).thenReturn(notAfter);
        when(certificateInfo.getThumbprint()).thenReturn("thumbprint");
        when(certificateInfo.getOriginFilePath()).thenReturn("/path/to/cert.pem");
        int deadLineWarn = 30;
        int deadLineError = 7;

        // Act
        CertificateChecker.checkDate(certificateInfo, now, deadLineWarn, deadLineError);

        // Assert
        verify(CertificateChecker.LOGGER).debug("message 2");
    }

    @Test
    void checkDate_shouldLogMessage3_whenCertificateExpiresSoon() throws Exception {
        // Arrange
        CertificateInfo certificateInfo = mock(CertificateInfo.class);
        X509Certificate cert = mock(X509Certificate.class);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime notAfter = now.plusDays(5);
        when(certificateInfo.getCertificate()).thenReturn(cert);
        when(cert.getNotAfter()).thenReturn(notAfter);
        when(certificateInfo.getThumbprint()).thenReturn("thumbprint");
        when(certificateInfo.getOriginFilePath()).thenReturn("/path/to/cert.pem");
        int deadLineWarn = 30;
        int deadLineError = 7;

        // Act
        CertificateChecker.checkDate(certificateInfo, now, deadLineWarn, deadLineError);

        // Assert
        verify(CertificateChecker.LOGGER).debug("message 3");
    }

    @Test
    void checkDate_shouldLogMessage4_whenCertificateExpiresSoonButNotInErrorRange() throws Exception {
        // Arrange
        CertificateInfo certificateInfo = mock(CertificateInfo.class);
        X509Certificate cert = mock(X509Certificate.class);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime notAfter = now.plusDays(20);
        when(certificateInfo.getCertificate()).thenReturn(cert);
        when(cert.getNotAfter()).thenReturn(notAfter);
        when(certificateInfo.getThumbprint()).thenReturn("thumbprint");
        when(certificateInfo.getOriginFilePath()).thenReturn("/path/to/cert.pem");
        int deadLineWarn = 30;
        int deadLineError = 7;

        // Act
        CertificateChecker.checkDate(certificateInfo, now, deadLineWarn, deadLineError);

        // Assert
        verify(CertificateChecker.LOGGER).debug("
