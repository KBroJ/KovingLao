package laoride.lao_ride.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    // application.yml에 파일 업로드 경로를 설정합니다.
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 파일을 도메인별/날짜별 폴더에 저장하고, 의미있는 파일명을 생성하여 웹 경로를 반환합니다.
     * @param file 업로드된 파일
     * @param domain 파일의 종류 (예: "product", "banner")
     * @param domainId 파일이 속한 객체의 ID (예: 상품모델 ID)
     * @return 저장된 파일의 웹 경로
     */
    public String storeFile(MultipartFile file, String domain, String domainId) {

        if (file.isEmpty()) {
            return null;
        }

        // 원본 파일명.확장자 추출
        String originalFilename = file.getOriginalFilename();
        log.info("FileStorageService|파일명.확장자 추출 : {}", originalFilename);

        try {

            // 1. 도메인과 ID를 기반으로 폴더 경로 생성 (예: ./uploads/product/5)
            // System.getProperty("user.dir")는 프로젝트의 루트 폴더 경로를 가져옵니다. (예: C:\Devel\KovingLao)
            Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir, domain, domainId);

            // 2. 실제 저장 경로가 존재하지 않으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("저장 경로가 존재하지 않으면 생성|생성경로 : {}", uploadPath);
            }

            // 3. 파일명 생성: {타임스탬프}-{랜덤문자8자리}.{확장자}
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String randomChars = UUID.randomUUID().toString().substring(0, 8);
            String storedFileName = System.currentTimeMillis() + "-" + randomChars + extension;

            // 4. 파일 저장
            Path filePath = uploadPath.resolve(storedFileName);
            file.transferTo(filePath.toFile());

            // 5. 웹에서 접근 가능한 최종 경로 반환
            // 예: /uploads/product/2025/07/09/1-xxxxxxxx-xxxx.jpg
            return "/uploads/" + domain + "/" + domainId + "/" + storedFileName;

        } catch (IOException ex) {
            throw new RuntimeException("파일을 저장할 수 없습니다. 파일명 : " + originalFilename, ex);
        }
    }

}
