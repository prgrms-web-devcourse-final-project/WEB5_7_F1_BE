package io.f1.backend.domain.game.app;

import io.f1.backend.domain.game.store.RoomRepository;
import io.f1.backend.domain.game.dto.request.RoomCreateRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public Long saveRoom(RoomCreateRequest request,Map<String, Object> loginUser) {
        return roomRepository.saveRoom(request,loginUser);
    }

}
