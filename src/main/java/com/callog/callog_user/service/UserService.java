package com.callog.callog_user.service;

import com.callog.callog_user.common.exception.BadParameter;
import com.callog.callog_user.common.exception.NotFound;
import com.callog.callog_user.dto.UserLoginDto;
import com.callog.callog_user.dto.UserRegisterDto;
import com.callog.callog_user.entity.User;
import com.callog.callog_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public void registerUser(UserRegisterDto dto) {
        User user = dto.toEntity();
        userRepository.save(user);
    }

    @Transactional
    public boolean login(UserLoginDto dto) {
        User user = userRepository.findByUserId(dto.getUserId());

        if(user == null) {
            throw new NotFound("존재하지 않는 사용자입니다.");
        }
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new BadParameter("비밀번호가 일치하지 않습니다.");
        }
        return true; // 로그인 성공 -> 유저페이지 창으로 이동?
    }
}
