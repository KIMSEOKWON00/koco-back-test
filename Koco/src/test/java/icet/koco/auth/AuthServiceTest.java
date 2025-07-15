package icet.koco.auth;

import icet.koco.auth.dto.AuthResponse;
import icet.koco.auth.dto.LogoutResponse;
import icet.koco.auth.entity.OAuth;
import icet.koco.auth.service.AuthService;
import icet.koco.auth.service.KakaoOAuthClient;
import icet.koco.auth.repository.OAuthRepository;
import icet.koco.fixture.KakaoUserFixture;
import icet.koco.fixture.UserFixture;
import icet.koco.user.entity.User;
import icet.koco.user.repository.UserRepository;
import icet.koco.util.CookieUtil;
import icet.koco.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private KakaoOAuthClient kakaoOAuthClient;

	@Mock
	private UserRepository userRepository;

	@Mock
	private OAuthRepository oauthRepository;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private CookieUtil cookieUtil;

	@Mock
	private HttpServletResponse response;

	@Mock
	private HttpServletRequest request;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Captor
	private ArgumentCaptor<OAuth> oAuthCaptor;

	private static final String TEST_CODE = "testCode";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
	private static final String LOGOUT_SUCCESS = "LOGOUT_SUCCESS";
	private static final Long USER_ID = 1L;

    @Test
	void loginWithKakao_기가입자_성공() {
		// Given
		var kakaoUser = KakaoUserFixture.existingKakaoUser();
		var existingUser = UserFixture.userWithIdAndEmail(USER_ID, kakaoUser.getKakao_account().getEmail());

		given(kakaoOAuthClient.getUserInfo(TEST_CODE)).willReturn(kakaoUser);
		given(userRepository.findByEmail(existingUser.getEmail())).willReturn(Optional.of(existingUser));
		given(jwtTokenProvider.createAccessToken(existingUser)).willReturn(ACCESS_TOKEN);
		given(jwtTokenProvider.createRefreshToken(existingUser)).willReturn(REFRESH_TOKEN);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		var mockResponse = new MockHttpServletResponse();

		// When
		AuthResponse authResponse = authService.loginWithKakao(TEST_CODE, mockResponse);

		// Then
		assertThat(authResponse.getCode()).isEqualTo(LOGIN_SUCCESS);
		assertThat(authResponse.getData().getEmail()).isEqualTo(existingUser.getEmail());

		Cookie accessCookie = mockResponse.getCookie("access_token");
		Cookie refreshCookie = mockResponse.getCookie("refresh_token");

		assertThat(accessCookie).isNotNull();
		assertThat(accessCookie.getValue()).isEqualTo(ACCESS_TOKEN);
		assertThat(refreshCookie).isNotNull();
		assertThat(refreshCookie.getValue()).isEqualTo(REFRESH_TOKEN);

		verify(valueOperations).set(eq("1"), eq(REFRESH_TOKEN));
		verify(oauthRepository).updateRefreshToken(eq(1L), eq(REFRESH_TOKEN));
	}

	@Test
	void loginWithKakao_신규가입자_성공() {
		// Given
		var kakaoUser = KakaoUserFixture.newKakaoUser();
		var newUser = UserFixture.userWithId(2L);

		given(kakaoOAuthClient.getUserInfo(TEST_CODE)).willReturn(kakaoUser);
		given(userRepository.findByEmail(newUser.getEmail())).willReturn(Optional.empty());
		given(userRepository.save(any(User.class))).willReturn(newUser);
		given(jwtTokenProvider.createAccessToken(newUser)).willReturn(ACCESS_TOKEN);
		given(jwtTokenProvider.createRefreshToken(newUser)).willReturn(REFRESH_TOKEN);
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		var mockResponse = new MockHttpServletResponse();

		// When
		AuthResponse authResponse = authService.loginWithKakao(TEST_CODE, mockResponse);

		// Then
		assertThat(authResponse.getCode()).isEqualTo(LOGIN_SUCCESS);
		assertThat(authResponse.getData().getEmail()).isEqualTo(newUser.getEmail());

		Cookie accessCookie = mockResponse.getCookie("access_token");
		Cookie refreshCookie = mockResponse.getCookie("refresh_token");

		assertThat(accessCookie).isNotNull();
		assertThat(refreshCookie).isNotNull();

		verify(valueOperations).set(eq("2"), eq(REFRESH_TOKEN));
		verify(oauthRepository).save(oAuthCaptor.capture());

		OAuth savedOAuth = oAuthCaptor.getValue();
		assertThat(savedOAuth.getUser().getId()).isEqualTo(2L);
		assertThat(savedOAuth.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
	}


	@Test
	void logout_성공() {
		// Given
		long expiration = 1000L * 60 * 10;

		Cookie accessTokenCookie = new Cookie("access_token", ACCESS_TOKEN);
		HttpServletRequest mockRequest = mock(HttpServletRequest.class);
		HttpServletResponse mockResponse = mock(HttpServletResponse.class);
		when(mockRequest.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});

		given(jwtTokenProvider.isInvalidToken(ACCESS_TOKEN)).willReturn(true);
		given(jwtTokenProvider.getUserIdFromToken(ACCESS_TOKEN)).willReturn(USER_ID);
		given(jwtTokenProvider.getExpiration(ACCESS_TOKEN)).willReturn(expiration);

		OAuth oAuth = OAuth.builder()
			.user(User.builder().id(USER_ID).build())
			.refreshToken(REFRESH_TOKEN)
			.build();
		given(oauthRepository.findByUserId(USER_ID)).willReturn(Optional.of(oAuth));
		given(redisTemplate.opsForValue()).willReturn(valueOperations);

		// When
		LogoutResponse logoutResponse = authService.logout(mockRequest, mockResponse);

		// Then
		assertThat(logoutResponse.getCode()).isEqualTo(LOGOUT_SUCCESS);
		assertThat(logoutResponse.getMessage()).isEqualTo("로그아웃 성공.");

		verify(oauthRepository).save(oAuthCaptor.capture());
		assertThat(oAuthCaptor.getValue().getRefreshToken()).isNull();

		verify(redisTemplate).delete(USER_ID.toString());
		verify(redisTemplate.opsForValue()).set(eq("BL:" + ACCESS_TOKEN), eq("logout"), eq(expiration), eq(TimeUnit.MILLISECONDS));
		verify(cookieUtil).invalidateCookie(mockResponse, "access_token");
		verify(cookieUtil).invalidateCookie(mockResponse, "refresh_token");
	}

}

