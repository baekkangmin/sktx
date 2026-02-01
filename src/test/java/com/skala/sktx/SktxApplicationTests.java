package com.skala.sktx;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("통합 테스트 - PostgreSQL, Redis, Kafka가 모두 실행 중이어야 합니다")
@SpringBootTest
class SktxApplicationTests {

	@Test
	void contextLoads() {
	}

}
