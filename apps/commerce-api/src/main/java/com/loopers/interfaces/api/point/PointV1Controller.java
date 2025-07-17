package com.loopers.interfaces.api.point;

import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec {

    private final UserService userService;

    @GetMapping("")
    @Override
    public ApiResponse<Long> getPoints(
        @RequestHeader(value = "X-USER-ID", required = false) String userId
    ) {
        if (userId == null || userId.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "X-USER-ID 헤더가 누락되었습니다.");
        }
        Long points = userService.getPoints(userId);
        if (points == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 ID입니다.");
        }
        return ApiResponse.success(points);
    }

    /*@PostMapping("")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.UserRequest userRequest
    ) {
        UserCommand.Create command = userRequest.toCommand();
        UserInfo userInfo = userService.signUp(command);
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(userInfo);
        return ApiResponse.success(response);
    }*/
}
