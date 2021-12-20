package com.wiwiwa.mill.spring.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

@SpringBootApplication
@RestController
@RequestMapping(path = Array("/"))
class DemoSpringBootApplication:
  @Autowired var bean: DemoBean = null

  @RequestMapping(path = Array("/"))
  def home = bean

@main
def main(): Unit =
  new SpringApplication(classOf[DemoSpringBootApplication]).run()
