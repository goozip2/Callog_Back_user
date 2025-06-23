package com.callog.callog_user.service;

import com.callog.callog_user.common.exception.BadParameter;
import com.callog.callog_user.common.exception.NotFound;
import com.callog.callog_user.config.jwt.JwtUtil;
import com.callog.callog_user.config.jwt.TokenGenerator;
import com.callog.callog_user.dto.TokenDto;
import com.callog.callog_user.dto.UserLoginDto;
import com.callog.callog_user.dto.UserRegisterDto;
import com.callog.callog_user.dto.UserUpdateDto;
import com.callog.callog_user.entity.User;
import com.callog.callog_user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenGenerator tokenGenerator;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();


    @Transactional
    public void register(UserRegisterDto dto) {
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new BadParameter("비밀번호가 일치하지 않습니다.");
        }
        //중복 아이디 체크
        User existingUser = userRepository.findByUsername(dto.getUsername());
        // 똑같은 아이디가 있으면 예외 발생
        if (existingUser != null) {
            throw new BadParameter("이미 사용중인 아이디 입니다.");
        }
        User user = dto.toEntity();
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

    }

    @Transactional(readOnly = true)
    public TokenDto.AccessRefreshToken login(UserLoginDto dto) {
        User user = userRepository.findByUsername(dto.getUsername());
        if (user == null) {
            throw new NotFound("존재하지 않는 사용자입니다.");
        }

        //  matches : 입력받은 평문과 저장된 암호화 비밀번호를 비교하기위해 평문을 암호화
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadParameter("비밀번호가 일치하지 않습니다.");
        }

        TokenDto.AccessRefreshToken tokens = tokenGenerator.generateAccessRefreshToken(user.getUsername(),
                "WEB");

        return tokens; // 로그인 성공


    }

    @Transactional(readOnly = true)
    public TokenDto.AccessToken refresh(String refreshToken) {
        // 1️⃣ 리프레시 토큰 검증
        String username = tokenGenerator.validateJwtToken(refreshToken);
        if (username == null) {
            throw new BadParameter("토큰이 유효하지 않습니다.");
        }

        // 2️⃣ 사용자 존재 여부 확인
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new NotFound("사용자를 찾을 수 없습니다.");
        }

        TokenDto.AccessToken newAccessToken = tokenGenerator.generateAccessToken(username, "WEB");

        return newAccessToken;
    }

    @Transactional(readOnly = true)
    public TokenDto.LogoutToken logout(String currentUserId) {
        //  사용자 존재 확인
        User user = userRepository.findByUsername(currentUserId);
        if (user == null) {
            throw new NotFound("존재하지 않는 사용자입니다.");
        }

        // 로그아웃용 토큰 생성 (즉시 만료되는 토큰)
        TokenDto.LogoutToken logoutTokens = tokenGenerator.generateLogoutToken(
                user.getUsername(),
                "WEB"
        );

        log.info("사용자 {}가 로그아웃했습니다.", currentUserId);

        return logoutTokens;
    }

    @Transactional
    public void updateUser(String currentUserId, UserUpdateDto dto) {
        User user = userRepository.findByUsername(currentUserId);

        //사용자 정보 확인
        if (user == null) {
            throw new NotFound("존재하지 않는 사용자입니다.");
        }
        //수정한 정보있는지 확인
        if (!dto.hasAnyUpdate()) {
            throw new BadParameter("수정한 정보가 없습니다.");
        }
        // 키 수정
        if (dto.hasHeight()) {
            user.setHeight(dto.getHeight());
        }
        // 몸무게 수정
        if (dto.hasWeight()) {
            user.setWeight(dto.getWeightAsDouble());
        }
        userRepository.save(user);
    }


}
