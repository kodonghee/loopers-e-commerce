package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserCommand;
import com.loopers.domain.user.UserInfo;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserService userService;

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> getUser(
        @RequestHeader(value = "X-USER-ID") String userId
    ) {
        UserInfo info = userService.getUserInfo(userId);
        if (info == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 ID입니다.");
        }
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(info);
        return ApiResponse.success(response);
    }

    @PostMapping("")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.UserRequest userRequest
    ) {
        UserCommand.Create command = userRequest.toCommand();
        UserInfo userInfo = userService.signUp(command);
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(userInfo);
        return ApiResponse.success(response);
    }
}
