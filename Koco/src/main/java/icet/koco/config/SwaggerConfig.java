package icet.koco.config;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Koco API")
                .description("Koco API 문서 - 쿠키 기반 인증 사용")
                .version("1.0.0"))

            // 전역 보안 요구사항 추가
            .addSecurityItem(new SecurityRequirement()
                .addList("cookieAuth"))

            // 보안 스키마 정의
            .components(new Components()
                // 쿠키 기반 인증 (access_token)
                .addSecuritySchemes("cookieAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("access_token")
                        .description("Access Token을 쿠키로 전달")));
    }
}
