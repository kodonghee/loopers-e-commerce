package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserCriteria;
import com.loopers.application.user.UserResult;
import com.loopers.application.user.UserFacade;
import com.loopers.domain.user.UserId;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.annotation.UserIdParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> getUser(
        @UserIdParam UserId userId
    ) {
        UserResult info = userFacade.getUserInfo(userId);
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(info);
        return ApiResponse.success(response);
    }

    @PostMapping("")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> signUp(
            @RequestBody UserV1Dto.UserRequest userRequest
    ) {
        UserCriteria.Create command = userRequest.toCommand();
        UserResult userResult = userFacade.signUp(command);
        UserV1Dto.UserResponse response = UserV1Dto.UserResponse.from(userResult);
        return ApiResponse.success(response);
    }
}
