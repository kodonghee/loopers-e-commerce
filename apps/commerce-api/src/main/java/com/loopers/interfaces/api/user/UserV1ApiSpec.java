package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "User V1 API", description = "회원 관련 API 입니다.")
public interface UserV1ApiSpec {

    @Operation(
        summary = "회원 정보 조회",
        description = "userId를 이용하여 회원 정보를 조회합니다."
    )
    ApiResponse<UserV1Dto.UserResponse> getUser(
        @Schema(name = "userId", description = "조회할 회원의 ID", example = "gdh5866")
        String userId
    );

    @PostMapping("")
    ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.UserRequest userRequest
    );
}
