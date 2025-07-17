package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User V1 API", description = "회원 관련 API 입니다.")
public interface UserV1ApiSpec {

    @Operation(
        summary = "회원 정보 조회",
        description = "X-USER-ID 헤더를 통해 회원 정보를 조회합니다."
    )
    @GetMapping("/me")
    ApiResponse<UserV1Dto.UserResponse> getUser(
        @Schema(name = "X-USER-ID", description = "조회할 회원의 ID", example = "gdh5866")
        String userId
    );

    @Operation(
            summary = "회원 가입",
            description = "회원 가입 정보를 입력하여 회원을 등록합니다."
    )
    @PostMapping("")
    ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.UserRequest userRequest
    );
}
