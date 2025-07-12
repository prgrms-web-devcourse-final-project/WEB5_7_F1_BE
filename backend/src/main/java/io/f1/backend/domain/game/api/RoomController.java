package io.f1.backend.domain.game.api;

import io.f1.backend.domain.game.app.RoomService;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import io.f1.backend.domain.game.dto.response.RoomCreateResponse;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomCreateResponse saveRoom(@RequestBody @Valid RoomCreateRequest request) {

        Map<String, Object> loginUser = new HashMap<>();
        loginUser.put("id", 1L);
        loginUser.put("nickname", "빵야빵야");

        return roomService.saveRoom(request, loginUser);
    }

}
