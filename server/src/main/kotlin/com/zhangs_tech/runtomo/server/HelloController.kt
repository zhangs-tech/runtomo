package com.zhangs_tech.runtomo.server

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/hello")
class HelloController {
    @GetMapping
    fun index(): String = "Greetings from Kotlin Spring Boot!"

    @GetMapping("/{name}")
    fun helloName(@PathVariable name: String) = "Hello, $name!"
}