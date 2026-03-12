package com.sogong.todak.user.service;

import com.sogong.todak.user.dto.request.UpdateBirthRequest;
import com.sogong.todak.user.dto.request.UpdateEmailRequest;
import com.sogong.todak.user.dto.request.UpdateGenderRequest;
import com.sogong.todak.user.dto.request.UpdateNicknameRequest;
import com.sogong.todak.user.dto.request.UpdateProfileImageRequest;
import com.sogong.todak.user.dto.response.UserMeProfileResponse;
import com.sogong.todak.user.dto.response.UserMeResponse;

public interface UserService {

    UserMeResponse getMyPage();

    UserMeProfileResponse getMyProfile();

    void updateProfileImage(UpdateProfileImageRequest request);

    void updateNickname(UpdateNicknameRequest request);

    void updateEmail(UpdateEmailRequest request);

    void updateBirthDate(UpdateBirthRequest request);

    void updateGender(UpdateGenderRequest request);
}
