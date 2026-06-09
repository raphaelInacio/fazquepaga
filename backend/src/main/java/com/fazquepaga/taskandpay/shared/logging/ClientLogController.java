package com.fazquepaga.taskandpay.shared.logging;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
public class ClientLogController {

    private static final Logger log = LoggerFactory.getLogger(ClientLogController.class);

    @PostMapping("/client")
    public ResponseEntity<Void> logClientError(@Valid @RequestBody ClientLogRequest request) {
        log.error(
                "Client-side error in component: {}, URI: {}, metadata: {}\n"
                        + "Message: {}\n"
                        + "Stacktrace:\n"
                        + "{}",
                request.component(),
                request.requestUri(),
                request.metadata(),
                request.message(),
                request.stack());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
