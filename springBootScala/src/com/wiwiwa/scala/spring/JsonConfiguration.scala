package com.wiwiwa.scala.spring

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext

import java.util.function.Supplier

class JsonConfiguration extends ApplicationContextInitializer[GenericApplicationContext]:
  override def initialize(context: GenericApplicationContext): Unit =
    val supplier: Supplier[JsonMapper] = () => JsonMapper.builder()
      .addModule(DefaultScalaModule)
      .serializationInclusion(JsonInclude.Include.NON_NULL)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build()
    context.registerBean(classOf[JsonMapper], supplier)
