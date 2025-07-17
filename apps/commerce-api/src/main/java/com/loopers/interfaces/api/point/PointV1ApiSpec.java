package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Point V1 API", description = "포인트 관련 API 입니다.")
public interface PointV1ApiSpec {

    @Operation(
        summary = "포인트 조회",
        description = "X-USER-ID 헤더를 통해 포인트를 조회합니다."
    )
    @GetMapping("")
    ApiResponse<Long> getPoints(
        @Schema(name = "X-USER-ID", description = "조회할 회원의 ID", example = "gdh5866")
        String userId
    );

    /*@Operation(
            summary = "회원 가입",
            description = "회원 가입 정보를 입력하여 회원을 등록합니다."
    )
    @PostMapping("/")
    ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.UserRequest userRequest
    );*/
}
