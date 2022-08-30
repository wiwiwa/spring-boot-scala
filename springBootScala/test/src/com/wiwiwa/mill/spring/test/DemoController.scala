package com.wiwiwa.mill.spring.test

import com.wiwiwa.scaler.webquery.WebQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, PostMapping, RequestBody, RequestMapping, RestController}

import java.util.Date

@RestController
@RequestMapping(path = Array("/demo"))
class DemoController:
  @Autowired var repo: DemoBeanRepository = null

  @GetMapping(path = Array("/"))
  def listDemo(query:WebQuery[DemoEntiy]) = query.execute()
  @GetMapping(path = Array("/{obj}"))
  def showDemo(obj:DemoEntiy) = obj

  @PostMapping(path = Array("/"))
  def newDemo(@RequestBody demo:DemoEntiy) =
    demo.id = new Date().getTime
    demo.lastModified = new Date()
    repo.save(demo)
