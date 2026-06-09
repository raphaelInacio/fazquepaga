package com.fazquepaga.taskandpay.shared.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fazquepaga.taskandpay.shared.LocaleConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = ExceptionTestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, LocaleConfig.class})
class GlobalExceptionHandlerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    // Mocks de segurança necessários para carregar o contexto do WebMvcTest sem falhas
    @MockBean private com.fazquepaga.taskandpay.identity.UserRepository userRepository;

    @MockBean private com.fazquepaga.taskandpay.security.JwtService jwtService;

    @MockBean private com.fazquepaga.taskandpay.security.RateLimitService rateLimitService;

    @MockBean private com.fazquepaga.taskandpay.security.RateLimitConfig rateLimitConfig;

    @Test
    void shouldReturn500AndStructuredJsonForRuntimeException() throws Exception {
        mockMvc.perform(get("/test/runtime-exception").header("Accept-Language", "pt-BR"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Ocorreu um erro interno. Por favor, tente novamente mais"
                                                + " tarde."))
                .andExpect(jsonPath("$.path").value("/test/runtime-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn500AndStructuredJsonForGenericCheckedException() throws Exception {
        mockMvc.perform(get("/test/checked-exception").header("Accept-Language", "pt-BR"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Ocorreu um erro interno. Por favor, tente novamente mais"
                                                + " tarde."))
                .andExpect(jsonPath("$.path").value("/test/checked-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn500AndStructuredJsonForAsaasIntegrationException() throws Exception {
        mockMvc.perform(get("/test/asaas-exception").header("Accept-Language", "pt-BR"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Ocorreu um erro interno. Por favor, tente novamente mais"
                                                + " tarde."))
                .andExpect(jsonPath("$.path").value("/test/asaas-exception"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}

@RestController
class ExceptionTestController {

    @GetMapping("/test/runtime-exception")
    public void throwRuntimeException() {
        throw new RuntimeException("Simulated runtime error");
    }

    @GetMapping("/test/checked-exception")
    public void throwCheckedException() throws Exception {
        throw new Exception("Simulated checked error");
    }

    @GetMapping("/test/asaas-exception")
    public void throwAsaasException() {
        throw new com.fazquepaga.taskandpay.payment.AsaasIntegrationException(
                "Simulated Asaas error", HttpStatus.BAD_GATEWAY, "{\"error\":\"bad gateway\"}");
    }
}
