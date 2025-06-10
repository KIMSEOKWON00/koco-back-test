package icet.koco.util.s3uploader;

import icet.koco.enums.ApiResponseCode;
import icet.koco.global.dto.ApiResponse;
import icet.koco.util.s3uploader.dto.S3UrlResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backend/v1/upload")
@Tag(name = "S3 Presigned URL", description = "Presigned URL 관련 API")
public class S3UploadController {

    private final S3PresignedUrlService s3Service;

    /**
     * PresignedUrl 조회 API
     * @param fileName
     * @return
     */
    @Operation(summary = "presigned-url 제공")
    @GetMapping("/presigned-url")
    public ResponseEntity<?> getPresignedUrl(@RequestParam String fileName) {
        String uuidFileName = UUID.randomUUID() + "_" + fileName;

        String presignedUrl = s3Service.generatePresignedUrl(uuidFileName);
        String fileUrl = s3Service.getFileUrl(uuidFileName);

        S3UrlResponseDto responseDto = S3UrlResponseDto.builder()
                .presignedUrl(presignedUrl)
                .fileUrl(fileUrl)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success(ApiResponseCode.SUCCESS,"AWS_S3 presigned url 조회 성공", responseDto)
        );
    }

}

