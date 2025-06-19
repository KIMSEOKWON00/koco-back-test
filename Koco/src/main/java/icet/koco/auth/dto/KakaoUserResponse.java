package icet.koco.auth.dto;

import lombok.*;
import java.util.Optional;

//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class KakaoUserResponse {
//    private Long id;
//    private KakaoAccount kakao_account;
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class KakaoAccount {
//        private String email;
//        private String nickname;
////        private String name;
//    }
//
//    public String getEmail() {
//        return kakao_account.getEmail();
//    }
//
////    public String getName() {
////        return kakao_account.getName();
////    }
//
//    public String getName() {
//        return kakao_account.getNickname();
//    }
//
//    public String getProviderId() {
//        return String.valueOf(id);
//    }
//}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoUserResponse {
    private Long id;
    private KakaoAccount kakao_account;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class KakaoAccount {
        private String email;
        private Profile profile;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class Profile {
            private String nickname;
            private String profile_image_url;
        }
    }

    public String getEmail() {
        return kakao_account.getEmail();
    }

    public String getName() {
        // nickname이 null일 경우 "카카오유저" 등 fallback 처리
        if (kakao_account != null && kakao_account.getProfile() != null) {
            return Optional.ofNullable(kakao_account.getProfile().getNickname()).orElse("카카오유저");
        }
        return "카카오유저";
    }

    public String getProviderId() {
        return String.valueOf(id);
    }
}
