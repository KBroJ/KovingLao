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
    // 실제 파일이 저장될 물리적인 폴더 경로
    @Value("${file.upload-path}")
    private String uploadPath;      // ../kovinglao-uploads

    // 웹 요청 URL 경로
    @Value("${file.resource-handler}")
    private String resourceHandler; // /media/**

    /**
     * 파일을 도메인별/날짜별 폴더에 저장하고, 의미있는 파일명을 생성하여 웹 경로를 반환합니다.
     * @param file 업로드된 파일
     * @param domain 파일의 종류 (예: "product", "banner")
     * @param domainId 파일이 속한 객체의 ID (예: 상품모델 ID)
     * @return 저장된 파일의 웹 경로
     */
    public String storeFile(MultipartFile file, String domain, String domainId) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        // 원본 파일명.확장자 추출
        String originalFilename = file.getOriginalFilename();
        log.info("FileStorageService|파일명.확장자 추출 : {}", originalFilename);

        try {
            log.info("FileStorageService|storeFile|uploadPath : {}", uploadPath);                   // ../kovinglao-uploads

            // 실제 저장될 물리적 경로 생성
            // System.getProperty("user.dir") : 현재 프로젝트 루트 디렉토리 (예: C:\Devel\KovingLao)
            Path physicalUploadPath = Paths.get(System.getProperty("user.dir"), uploadPath, domain, domainId); // ex) 예: C:\Devel\KovingLao../kovinglao-uploads/product/5
            log.info("FileStorageService|storeFile|physicalUploadPath : {}", physicalUploadPath);

            // 2. 실제 저장 경로가 존재하지 않으면 생성
            if (!Files.exists(physicalUploadPath)) {
                Files.createDirectories(physicalUploadPath);
                log.info("저장 경로가 존재하지 않으면 생성|폴더 생성 =====> {}", physicalUploadPath);
            }

            // 3. 파일명 생성: {타임스탬프}-{랜덤문자8자리}.{확장자}
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String randomChars = UUID.randomUUID().toString().substring(0, 8);
            String storedFileName = System.currentTimeMillis() + "-" + randomChars + extension;
            log.info("FileStorageService|storeFile|storedFileName : {}", storedFileName);   // ex. 1752128745500-869f8dd5.jpg

            // 4. 파일 저장
            Path filePath = physicalUploadPath.resolve(storedFileName);                     // 예: C:\Devel\KovingLao../kovinglao-uploads/product/5/1xxxxxxxx-xxxx.jpg
            log.info("FileStorageService|storeFile|filePath : {}", filePath);

            // 파일을 우리가 지정한 실제 저장 위치로 옮기는(전송하는) 작업
            file.transferTo(filePath.toFile());

            // 5. 웹에서 접근 가능한 URL 반환 (예: /media/product/x/xxxxx.jpg)
            String baseUrl = resourceHandler.replace("/**", "");            // resourceHandler : /media/** → /media

            // ex. /media/product/x/1xxxxxxxx-xxxx.jpg
            return Paths.get(baseUrl, domain, domainId, storedFileName).toString().replace("\\", "/");

        } catch (IOException ex) {
            throw new RuntimeException("파일을 저장할 수 없습니다. 파일명 : " + originalFilename, ex);
        }
    }

    /**
     * 서버에 저장된 실제 파일을 삭제하는 메서드
     * @param fileUrl DB에 저장된 웹 경로 (예: /uploads/product/5/image.jpg)
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // 웹 URL을 실제 파일 시스템의 물리적 경로로 변환
            String baseUrl = resourceHandler.replace("/**", "");                // resourceHandler : /media/** → /media
            String relativePath = fileUrl.substring(baseUrl.length());                          // /media/product/5/image.jpg → /product/5/image.jpg
            Path filePath = Paths.get(System.getProperty("user.dir"), uploadPath, relativePath).toAbsolutePath().normalize();   // ../kovinglao-uploads/product/5/image.jpg
            log.info("deleteFile|baseUrl : {}, relativePath : {}", baseUrl, relativePath);
            log.info("deleteFile|filePath : {}", filePath);

            Files.deleteIfExists(filePath);
            log.info("파일 삭제 성공: {}", filePath);
        } catch (IOException e) {
            log.error("파일 삭제 실패: " + fileUrl, e);
        }
    }

}
