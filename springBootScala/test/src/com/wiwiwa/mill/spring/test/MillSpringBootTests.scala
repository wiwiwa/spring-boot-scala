package com.wiwiwa.mill.spring.test

import com.fasterxml.jackson.databind.json.JsonMapper
import org.springframework.boot.SpringApplication
import utest.*

object MillSpringBootTests extends TestSuite:
  override def tests = Tests {
    val app = new SpringApplication(classOf[DemoSpringBootApplication]).run()
    val mapper = app.getBean(classOf[JsonMapper])
    val repo = app.getBean(classOf[DemoBeanRepository])
    val bean = repo.getById(1)
    assert{ mapper.writeValueAsString(bean).contains("msg") }
  }
