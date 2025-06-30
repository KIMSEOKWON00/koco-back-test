package icet.koco.util.s3uploader;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    public String generatePresignedUrl(String uuidFilName) {
        String key = "profile/" + uuidFilName;                              // s3의 profile 객체에 저장

        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + 1000 * 60 * 3);           // presignedURl 유효시간: 3분

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, key)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    // fileUrl 생성
    public String getFileUrl(String fileName) {
        String key = "profile/" + fileName;
        return amazonS3.getUrl(bucket, key).toString();
    }
}