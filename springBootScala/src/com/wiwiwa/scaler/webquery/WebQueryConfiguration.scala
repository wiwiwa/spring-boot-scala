package com.wiwiwa.scaler.webquery

import org.apache.coyote.http11.AbstractHttp11Protocol
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

import scala.jdk.CollectionConverters.*

class WebQueryConfiguration:
  @Bean def dbQueryResolver(adapter:RequestMappingHandlerAdapter) =
    val resolver = new WebQueryResolver
    val resolvers = resolver +: adapter.getArgumentResolvers.asScala
    adapter.setArgumentResolvers(resolvers.asJava)
    resolver

  @Bean def tomcatConnectorCustomizer: TomcatConnectorCustomizer = connector =>
    connector.getProtocolHandler match
      case h: AbstractHttp11Protocol[_] => h.setRelaxedQueryChars("<>")
      case _ => ()