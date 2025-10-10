package co.edu.uniquindio.alojamientos.alojamientos_app;

import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.dto.externalServiceDto.SendEmailDto;
import co.edu.uniquindio.alojamientos.alojamientos_app.businessLayer.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simplejavamail.api.mailer.Mailer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private Mailer mailerMock;  // Mock del Mailer para evitar conexión real

    @InjectMocks
    private EmailServiceImpl emailService; // Inyecta el mock

    private SendEmailDto emailDto;

    @BeforeEach
    void setUp() {
        emailDto = SendEmailDto.builder()
                .recipient("usuario@correo.com")
                .subject("Asunto de prueba")
                .body("Cuerpo del correo")
                .build();
    }

    @Test
    @DisplayName("SEND MAIL - Llama correctamente a Mailer.sendMail")
    void sendMail_Success() throws Exception {
        emailService.sendMail(emailDto);

        // Verifica que el método sendMail del Mailer se llame exactamente una vez
        verify(mailerMock, times(1)).sendMail(any());
    }

    @Test
    @DisplayName("SEND MAIL - DTO con datos vacíos no lanza excepción")
    void sendMail_DtoEmpty_DoesNotThrow() {
        SendEmailDto invalidDto = SendEmailDto.builder()
                .recipient("usuario@correo.com")
                .subject("")
                .body("")
                .build();

        // Asegura que no se lance excepción al enviar DTO inválido
        assertThatCode(() -> emailService.sendMail(invalidDto)).doesNotThrowAnyException();

        // También podemos verificar que sendMail se llama, aunque con DTO vacío
        try {
            emailService.sendMail(invalidDto);
        } catch (Exception ignored) {}
        verify(mailerMock, atLeastOnce()).sendMail(any());
    }

    @Test
    @DisplayName("SEND MAIL - Simula excepción de Mailer.sendMail")
    void sendMail_MailerThrows_Exception() throws Exception {
        // Forzamos que el Mailer lance una excepción
        doThrow(new RuntimeException("Falla simulada")).when(mailerMock).sendMail(any());

        SendEmailDto dto = SendEmailDto.builder()
                .recipient("usuario@correo.com")
                .subject("Test")
                .body("Test")
                .build();

        // Verificamos que se lance la excepción correctamente
        assertThatCode(() -> emailService.sendMail(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falla simulada");
    }
}
