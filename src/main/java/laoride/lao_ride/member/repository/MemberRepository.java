package laoride.lao_ride.member.repository;

import laoride.lao_ride.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 사용자 이름으로 회원을 찾는 메서드
    Optional<Member> findByUsername(String username);

}
