package laoride.lao_ride.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 정적 리소스(이미지 등)를 위한 웹 설정
 * @Configuration : 이 클래스가 Spring의 설정 파일임을 선언
 * implements WebMvcConfigurer : Spring MVC의 웹 설정을 직접 커스터마이징할 수 있게 해주는 인터페이스
 *
 */
@Configuration // 이 클래스가 Spring의 설정 파일임을 선언
public class WebConfig implements WebMvcConfigurer {

    // application.yml에 설정한 경로를 주입받습니다.
    @Value("${file.resource-handler}")
    private String resourceHandler;     // ../kovinglao-uploads

    @Value("${file.resource-location}")
    private String resourceLocation;    // file:../kovinglao-uploads/

    /**
     * 특정 URL 경로 요청을 실제 파일 시스템의 경로와 매핑해주는 메서드입니다.
     * @param registry 리소스 핸들러를 등록하는 레지스트리
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /*
            "file:" 접두사는 실제 파일 시스템의 경로를 의미합니다.
            마지막에 "/"를 붙여주는 것이 중요합니다.
            System.getProperty("user.dir") : 현재 프로젝트 루트 디렉토리(ex. C:\Devel\KovingLao)
         */
//        String resourcePath = "file:///" + System.getProperty("user.dir") + "/" + uploadDir + "/";

        registry.addResourceHandler(resourceHandler)    // 웹 브라우저가 파일에 접근할 때 사용할 요청 URL 경로
                .addResourceLocations(resourceLocation);            // 위 resource-handler 경로를 어느 물리적 위치에 매핑할지 지정
    }

}
