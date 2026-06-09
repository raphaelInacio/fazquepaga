package com.fazquepaga.taskandpay.shared.stats;

import com.fazquepaga.taskandpay.identity.User;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller responsável por expor as estatísticas de negócio e uso consolidadas da família. */
@RestController
@RequestMapping("/api/v1/families")
public class FamilyStatsController {

    private final StatsService statsService;

    public FamilyStatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/{familyId}/stats")
    public ResponseEntity<Map<String, Object>> getFamilyStats(
            @PathVariable String familyId, @AuthenticationPrincipal User authenticatedUser)
            throws ExecutionException, InterruptedException {

        if (authenticatedUser == null) {
            return ResponseEntity.status(401).build();
        }

        // Validação de acesso: apenas o próprio pai/mãe da família ou um filho daquela família
        // podem ler os dados
        boolean isParentOfFamily =
                authenticatedUser.getRole() == User.Role.PARENT
                        && authenticatedUser.getId().equals(familyId);
        boolean isChildOfFamily =
                authenticatedUser.getRole() == User.Role.CHILD
                        && familyId.equals(authenticatedUser.getParentId());

        if (!isParentOfFamily && !isChildOfFamily) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> stats = statsService.getFamilyStats(familyId).get();
        return ResponseEntity.ok(stats);
    }
}
