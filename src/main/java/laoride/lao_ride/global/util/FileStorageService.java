package laoride.lao_ride.global.util;

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

@Service
public class FileStorageService {

    // application.yml에 파일 업로드 경로를 설정합니다.
    @Value("${file.upload-dir}")
    private String uploadDirRoot;

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

        // 1. 날짜 기반으로 폴더 경로 생성 (예: /product/2025/07/09)
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDirRoot, domain, datePath);

        // 원본 파일명과 파일명에서 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        try {
            // 2. 실제 저장 경로가 존재하지 않으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 3. 파일명 생성: {도메인ID}-{UUID}.{확장자}
            String storedFileName = domainId + "-" + UUID.randomUUID().toString() + extension;

            // 4. 파일 저장
            Path filePath = uploadPath.resolve(storedFileName);
            file.transferTo(filePath.toFile());

            // 5. 웹에서 접근 가능한 최종 경로 반환
            // 예: /uploads/product/2025/07/09/1-xxxxxxxx-xxxx.jpg
            return "/uploads/" + domain + "/" + datePath + "/" + storedFileName;

        } catch (IOException ex) {
            throw new RuntimeException("파일을 저장할 수 없습니다. 파일명 : " + originalFilename, ex);
        }
    }

}
