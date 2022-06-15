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
    val ret: Map[String, Any] = post("/demo/", Map("msg" -> s"hello at ${new Date()}"))
      .assertJson("$.id")
      .json("$")
    println(ret)
  }
