package com.zhangs_tech.runtomo.server

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = ["runtomo.google.web-client-id=test-client-id"])
class ApplicationTests {

	@Test
	fun contextLoads() {
	}

}
