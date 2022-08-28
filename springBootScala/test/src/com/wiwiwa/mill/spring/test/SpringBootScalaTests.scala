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
    post("/demo/", Map("msg" -> s"hello at ${new Date()}"))
    get("/demo/", Map("*msg*" -> "hello"))
      .assertJson("$.content[0].id")
  }
