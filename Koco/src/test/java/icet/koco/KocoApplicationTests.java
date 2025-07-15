package icet.koco;

import icet.koco.auth.TestWebClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(TestWebClientConfig.class)
class KocoApplicationTests {

	@Test
	void contextLoads() {
	}
}
