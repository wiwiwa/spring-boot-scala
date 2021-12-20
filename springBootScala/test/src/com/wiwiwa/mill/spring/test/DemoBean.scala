package com.wiwiwa.mill.spring.test

import org.springframework.stereotype.Component

import java.util.Date

@Component
class DemoBean:
  val msg = s"hello at ${new Date()}"
