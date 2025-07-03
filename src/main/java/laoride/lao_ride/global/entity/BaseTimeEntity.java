package laoride.lao_ride.global.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통으로 DB 저장 시 사용될 생성일/수정일 필드
 * 이 클래스를 상속받는 엔티티들은 createdAt, updatedAt 필드를 자동으로 관리함
 * 적용 테이블 : Product, Member, Reservation
 * 
 * 이 기능 전체를 활성화하기 위해 메인 애플리케이션 클래스에 @EnableJpaAuditing을 붙여줘야 함 
*/

@Getter
@MappedSuperclass // JAP가 이 클래스를 직접 테이블로 만들지 않고 상속받는 엔티티들에게만 아래 필드들을 컬럼으로 인식하게 설정
@EntityListeners(AuditingEntityListener.class) // Auditing(자동 시간 기록) 기능 추가
public class BaseTimeEntity {

    @CreatedDate // JPA가 엔티티를 최초로 저장(INSERT)할 때 현재 시간을 자동으로 기록
    private LocalDateTime createdAt;

    @LastModifiedDate // JPA가 엔티티의 값이 변경(UPDATE)될 때마다 현재 시간을 자동으로 기록
    private LocalDateTime updatedAt;

}
