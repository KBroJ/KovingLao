package laoride.lao_ride.global.init;

import laoride.lao_ride.member.repository.MemberRepository;
import laoride.lao_ride.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component // Spring이 이 클래스를 찾아서 Bean으로 등록하도록 함
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     *  프로젝트 실행 시 Member 테이블에 아무 데이터가 들어있지 않다면 
     *  아래 설정된 값으로 기본 관리자 계정 생성을 한다
     */
    @Override
    public void run(String... args) throws Exception {
        if (memberRepository.findByUsername("admin").isEmpty()) {
            Member admin = Member.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("qwer1234")) // 비밀번호 암호화
                    .name("총괄 관리자")
                    .role("ROLE_SUPER_ADMIN")
                    .build();

            memberRepository.save(admin);
        }
    }

}
