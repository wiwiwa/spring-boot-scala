package com.wiwiwa.scaler.webquery

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.core.MethodParameter
import org.springframework.data.domain.Pageable
import org.springframework.data.web.{PageableHandlerMethodArgumentResolver, SortHandlerMethodArgumentResolver}
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.{HandlerMethodArgumentResolver, ModelAndViewContainer}

import java.lang.reflect.ParameterizedType
import javax.persistence.{Entity, EntityManager}
import javax.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters.*

@ConditionalOnClass(Array(classOf[HandlerMethodArgumentResolver], classOf[Entity]))
class WebQueryResolver extends HandlerMethodArgumentResolver:
  @Autowired val entityManager: EntityManager = null
  @Autowired val pageableResolver: PageableHandlerMethodArgumentResolver = null

  @Autowired def init(sortResolver:SortHandlerMethodArgumentResolver) =
    pageableResolver.setPageParameterName("$page")
    pageableResolver.setSizeParameterName("$size")
    sortResolver.setSortParameter("$sort")

  override def supportsParameter(parameter:MethodParameter) = classOf[WebQuery[_]].isAssignableFrom(parameter.getParameterType)
  override def resolveArgument(parameter:MethodParameter, mavContainer:ModelAndViewContainer, webRequest:NativeWebRequest, binderFactory:WebDataBinderFactory) =
    val entityClass = parameter.getGenericParameterType.asInstanceOf[ParameterizedType]
      .getActualTypeArguments()(0).asInstanceOf[Class[_]]
    if !entityClass.isAnnotationPresent(classOf[Entity]) then
      throw new IllegalArgumentException(s"Class $entityClass is not an Entity class")
    val request = webRequest.getNativeRequest(classOf[HttpServletRequest])
    val pageable = pageableResolver.resolveArgument(parameter,mavContainer,webRequest,binderFactory)
    new WebQueryImpl(request, pageable, entityClass, entityManager)
