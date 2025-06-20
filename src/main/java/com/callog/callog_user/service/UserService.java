package com.callog.callog_user.service;

import com.callog.callog_user.common.exception.BadParameter;
import com.callog.callog_user.common.exception.NotFound;
import com.callog.callog_user.config.jwt.JwtUtil;
import com.callog.callog_user.dto.UserLoginDto;
import com.callog.callog_user.dto.UserRegisterDto;
import com.callog.callog_user.dto.UserUpdateDto;
import com.callog.callog_user.entity.User;
import com.callog.callog_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void register(UserRegisterDto dto) {
        //중복 아이디 체크
        User existingUser = userRepository.findByUserId(dto.getUserId());
        // 똑같은 아이디가 있으면 예외 발생
        if(existingUser != null) {
            throw new BadParameter("이미 사용중인 아이디 입니다.");
        }
        User user = dto.toEntity();
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String login(UserLoginDto dto) {
        User user = userRepository.findByUserId(dto.getUserId());
        if(user == null) {
            throw new NotFound("존재하지 않는 사용자입니다.");
        }

        //  matches : 입력받은 평문과 저장된 암호화 비밀번호를 비교하기위해 평문을 암호화
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadParameter("비밀번호가 일치하지 않습니다.");
        }
        String jwtToken = jwtUtil.generateToken(user.getUserId());
        return jwtToken; // 로그인 성공 -> 유저페이지 창으로 이동?
    }

    @Transactional
    public void updateUser(String currentUserId, UserUpdateDto dto) {
        User user = userRepository.findByUserId(currentUserId);
        if (user == null) {
            throw new NotFound("존재하지 않는 사용자입니다.");
        }
        user.setUserName(dto.getUserName()); //닉네임 수정 (무조건 업데이트)

        if (dto.hasNewPassword()) {
            // 새로운 비밀번호를 암호화해서 저장
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            user.setPassword(encodedPassword);
        }
        userRepository.save(user);
    }
}
