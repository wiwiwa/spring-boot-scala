package com.wiwiwa.mill.spring.test

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class TestSpringBootJar

object TestSpringBootJar:
  def main(args:Array[String]): Unit =
    SpringApplication.run(classOf[TestSpringBootJar], args:_*)
