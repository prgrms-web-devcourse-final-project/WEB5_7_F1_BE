package io.f1.backend.domain.stat.api;

import io.f1.backend.domain.stat.app.StatService;
import io.f1.backend.domain.stat.dto.StatPageResponse;
import io.f1.backend.global.validation.LimitPageSize;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
public class StatController {

    private final StatService statService;

    @LimitPageSize
    @GetMapping("/rankings")
    public ResponseEntity<StatPageResponse> getRankings(
            @PageableDefault(sort = "score", direction = Direction.DESC) Pageable pageable) {
        StatPageResponse response = statService.getRanks(pageable);

        return ResponseEntity.ok().body(response);
    }
}
