package laoride.lao_ride.global.security;

import laoride.lao_ride.member.domain.Member;
import laoride.lao_ride.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * Spring Security가 로그인 요청을 가로챌 때, username을 이 메서드로 넘겨줍니다.
     * DB에서 해당 사용자를 찾아 UserDetails 형태로 반환합니다.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 찾은 사용자 정보를 PrincipalDetails로 감싸서 반환하면,
        // Spring Security가 알아서 비밀번호를 비교하고 인증을 처리해줍니다.
        return new PrincipalDetails(member);
    }

}
