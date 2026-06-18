package com.likelion

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class LikeLionApplication

fun main(args: Array<String>) {
    runApplication<LikeLionApplication>(*args)
}
