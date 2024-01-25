package com.wiwiwa.mill.spring.test

import com.wiwiwa.springboot.test.MockSpringBoot
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import utest.*

import java.util.Date

@SpringBootTest
@AutoConfigureMockMvc
object SpringBootScalaTests extends TestSuite with MockSpringBoot :
  override def tests = Tests {
    val date = new Date()
    post("/demo/", Map("msg" -> s"hello at $date"))
    get("/demo/",Map("*baseValue*" -> "base"))
      .assertJson("$.content[0].baseValue")
      .assertJson("$.content[0].id")
    get("/demo/", Map("lastModified<" -> (date.getTime*2).toString) )
      .assertJson("$.content[0].id")

    post("/demo/session", Map())
    get("/demo/session").assertJson("$.Key")
  }
