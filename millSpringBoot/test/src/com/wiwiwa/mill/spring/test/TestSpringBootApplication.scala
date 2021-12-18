package com.wiwiwa.mill.spring.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class TestSpringBootApplication{
}

object TestSpringBootApplication {
  def main(args:Array[String]): Unit =
    SpringApplication.run(classOf[TestSpringBootApplication], args:_*)
}

