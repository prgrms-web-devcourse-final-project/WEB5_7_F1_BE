package io.f1.backend.domain.user.dto;

import io.f1.backend.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDto {

    private Long id;
    private String nickname;

    public static SignupResponseDto toDto(User user) {
        return SignupResponseDto.builder().id(user.getId()).nickname(user.getNickname()).build();
    }
}
