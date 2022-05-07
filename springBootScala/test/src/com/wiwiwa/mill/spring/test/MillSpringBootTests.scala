package com.wiwiwa.mill.spring.test

import com.wiwiwa.springboot.test.MockSpringBoot
import utest.*

import java.util.Date

object MillSpringBootTests extends TestSuite with MockSpringBoot:
  override def tests = Tests {
    val ret: Map[String,Any] = post("/demo/", Map("msg"->s"hello at ${new Date()}"))
      .assertJson("$.id")
      .json("$")
    ret
  }
