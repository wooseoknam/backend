package tourapi24.backend.member.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tourapi24.backend.member.domain.Member;
import tourapi24.backend.member.dto.auth.LoginRequest;
import tourapi24.backend.member.dto.auth.LoginResponse;
import tourapi24.backend.member.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthClient oAuthClient;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    public LoginResponse auth(LoginRequest request) {
        UserInfo userInfo = oAuthClient.fetchUserInfo(
                request.getProvider(),
                request.getAccessToken()
        );

        return memberRepository.findOneBySocialId(userInfo.getSocialId())
                .map(this::login)
                .orElseGet(() -> temporary(userInfo));
    }

    // DB에 회원이 존재하므로 정식 JWT 발급
    private LoginResponse login(Member member) {
        return LoginResponse.builder()
                .token(jwtService.generateToken(member))
                .build();
    }

    // DB에 회원이 존재하지 않으므로 임시 JWT 발급
    private LoginResponse temporary(UserInfo userInfo) {
        return LoginResponse.builder()
                .token(jwtService.generateTempToken(userInfo))
                .build();
    }
}
