package com.wiwiwa.mill.spring.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{GetMapping, PostMapping, RequestBody, RequestMapping, RestController}

import java.util.Date

@RestController
@RequestMapping(path = Array("/demo"))
class DemoController:
  @Autowired var repo: DemoBeanRepository = null

  @GetMapping(path = Array("/"))
  def listDemo = repo.findAll()
  @GetMapping(path = Array("/{obj}"))
  def showDemo(obj:DemoBean) = obj

  @PostMapping(path = Array("/"))
  def newDemo(@RequestBody demo:DemoBean) =
    demo.id = new Date().getTime
    repo.save(demo)
