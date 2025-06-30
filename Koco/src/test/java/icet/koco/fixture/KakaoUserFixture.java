package icet.koco.fixture;

import icet.koco.auth.dto.KakaoUserResponse;

public class KakaoUserFixture {
	public static KakaoUserResponse existingKakaoUser() {
		return KakaoUserResponse.builder()
			.id(1234L)
			.kakao_account(KakaoUserResponse.KakaoAccount.builder()
				.email("existUser@example.com")
				.profile(KakaoUserResponse.KakaoAccount.Profile.builder()
					.nickname("existUser")
					.profile_image_url("http://example.com/existUser.jpg")
					.build())
				.build())
			.build();
	}

	public static KakaoUserResponse newKakaoUser() {
		return KakaoUserResponse.builder()
			.id(5678L)
			.kakao_account(KakaoUserResponse.KakaoAccount.builder()
				.email("newUser@example.com")
				.profile(KakaoUserResponse.KakaoAccount.Profile.builder()
					.nickname("newUser")
					.profile_image_url("http://example.com/newUser.jpg")
					.build())
				.build())
			.build();
	}
}
