package rackdon.kosic

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

class KosicApplicationTests: StringSpec() {

	init {
		"foo" {
			1 shouldBe 1
		}

		"asdf" {
			1 shouldBe 1
		}
	}

}
