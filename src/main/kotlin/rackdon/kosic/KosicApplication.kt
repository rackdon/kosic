package rackdon.kosic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KosicApplication

fun main(args: Array<String>) {
	runApplication<KosicApplication>(*args)
}
