package com.wiwiwa.mill.spring.test

import com.wiwiwa.spring.test.MockSpringBoot
import utest.*

object MillSpringBootTests extends TestSuite with MockSpringBoot:
  override def tests = Tests {
    val ret = get("/").text
    ret
  }
