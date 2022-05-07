package com.wiwiwa.mill.spring.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class DemoSpringBootApplication

@main def main(): Unit =
  new SpringApplication(classOf[DemoSpringBootApplication]).run()
