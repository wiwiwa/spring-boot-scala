package com.wiwiwa.mill.spring.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

@RestController
@RequestMapping(path = Array("/"))
class TestController{
  @Autowired var bean: TestBean = null

  @RequestMapping(path = Array("/"))
  def home = bean
}
