package com.fazquepaga.taskandpay.shared.stats;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fazquepaga.taskandpay.identity.User;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class FamilyStatsControllerTest {

    private MockMvc mockMvc;

    private FamilyStatsController familyStatsController;

    @Mock private StatsService statsService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        familyStatsController = new FamilyStatsController(statsService);
        mockMvc =
                MockMvcBuilders.standaloneSetup(familyStatsController)
                        .setCustomArgumentResolvers(
                                new HandlerMethodArgumentResolver() {
                                    @Override
                                    public boolean supportsParameter(MethodParameter parameter) {
                                        return parameter.hasParameterAnnotation(
                                                AuthenticationPrincipal.class);
                                    }

                                    @Override
                                    public Object resolveArgument(
                                            MethodParameter parameter,
                                            ModelAndViewContainer mavContainer,
                                            NativeWebRequest webRequest,
                                            WebDataBinderFactory binderFactory)
                                            throws Exception {
                                        return currentUser;
                                    }
                                })
                        .setMessageConverters(new MappingJackson2HttpMessageConverter())
                        .build();
    }

    @Test
    void getFamilyStats_whenUserNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        currentUser = null;

        mockMvc.perform(get("/api/v1/families/family-123/stats"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getFamilyStats_whenUserIsParentOfDifferentFamily_shouldReturnForbidden() throws Exception {
        currentUser = User.builder().id("parent-456").role(User.Role.PARENT).build();

        mockMvc.perform(get("/api/v1/families/family-123/stats")).andExpect(status().isForbidden());
    }

    @Test
    void getFamilyStats_whenUserIsChildOfDifferentFamily_shouldReturnForbidden() throws Exception {
        currentUser =
                User.builder().id("child-456").role(User.Role.CHILD).parentId("parent-456").build();

        mockMvc.perform(get("/api/v1/families/family-123/stats")).andExpect(status().isForbidden());
    }

    @Test
    void getFamilyStats_whenUserIsParentOfSameFamily_shouldReturnStats() throws Exception {
        String familyId = "family-123";
        currentUser = User.builder().id(familyId).role(User.Role.PARENT).build();

        Map<String, Object> mockStats =
                Map.of(
                        "totalTasksCreated", 5L,
                        "totalTasksCompleted", 3L,
                        "totalTasksApproved", 2L,
                        "totalAllowancePaid", 10.0,
                        "aiSuggestionsUsed", 1L);

        when(statsService.getFamilyStats(familyId))
                .thenReturn(CompletableFuture.completedFuture(mockStats));

        mockMvc.perform(get("/api/v1/families/" + familyId + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasksCreated").value(5))
                .andExpect(jsonPath("$.totalTasksCompleted").value(3))
                .andExpect(jsonPath("$.totalTasksApproved").value(2))
                .andExpect(jsonPath("$.totalAllowancePaid").value(10.0))
                .andExpect(jsonPath("$.aiSuggestionsUsed").value(1));
    }

    @Test
    void getFamilyStats_whenUserIsChildOfSameFamily_shouldReturnStats() throws Exception {
        String familyId = "family-123";
        currentUser =
                User.builder().id("child-789").role(User.Role.CHILD).parentId(familyId).build();

        Map<String, Object> mockStats =
                Map.of(
                        "totalTasksCreated", 5L,
                        "totalTasksCompleted", 3L,
                        "totalTasksApproved", 2L,
                        "totalAllowancePaid", 10.0,
                        "aiSuggestionsUsed", 1L);

        when(statsService.getFamilyStats(familyId))
                .thenReturn(CompletableFuture.completedFuture(mockStats));

        mockMvc.perform(get("/api/v1/families/" + familyId + "/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasksCreated").value(5))
                .andExpect(jsonPath("$.totalTasksCompleted").value(3))
                .andExpect(jsonPath("$.totalTasksApproved").value(2))
                .andExpect(jsonPath("$.totalAllowancePaid").value(10.0))
                .andExpect(jsonPath("$.aiSuggestionsUsed").value(1));
    }

    @Test
    void getFamilyStats_whenSyncIsTrue_shouldRecalculateStatsFirst() throws Exception {
        String familyId = "family-123";
        currentUser = User.builder().id(familyId).role(User.Role.PARENT).build();

        Map<String, Object> mockStats =
                Map.of(
                        "totalTasksCreated", 5L,
                        "totalTasksCompleted", 3L,
                        "totalTasksApproved", 2L,
                        "totalAllowancePaid", 10.0,
                        "aiSuggestionsUsed", 1L);

        when(statsService.recalculateFamilyStats(familyId))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(statsService.getFamilyStats(familyId))
                .thenReturn(CompletableFuture.completedFuture(mockStats));

        mockMvc.perform(get("/api/v1/families/" + familyId + "/stats").param("sync", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTasksCreated").value(5))
                .andExpect(jsonPath("$.totalTasksCompleted").value(3))
                .andExpect(jsonPath("$.totalTasksApproved").value(2))
                .andExpect(jsonPath("$.totalAllowancePaid").value(10.0))
                .andExpect(jsonPath("$.aiSuggestionsUsed").value(1));

        verify(statsService).recalculateFamilyStats(familyId);
    }
}
