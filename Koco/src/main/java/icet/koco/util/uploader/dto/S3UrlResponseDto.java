package icet.koco.util.uploader.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class S3UrlResponseDto {
    private String fileUrl;
    private String presignedUrl;
}
