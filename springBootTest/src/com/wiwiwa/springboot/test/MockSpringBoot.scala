package com.wiwiwa.springboot.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.{JsonPath, PathNotFoundException}
import net.minidev.json.JSONArray
import org.springframework.beans.factory.support.{DefaultListableBeanFactory, RootBeanDefinition}
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.{SpringApplication, SpringBootConfiguration}
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.TestContextManager
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.{MockHttpServletRequestBuilder, MockMvcRequestBuilders}

import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.util
import jakarta.servlet.http.{Cookie, HttpSession}
import org.springframework.core.env.Environment

import scala.jdk.CollectionConverters.*
import scala.reflect.{ClassTag, classTag}

val SPRINT_CURRENT_SESSION = "org.springframework.session.SessionRepository.CURRENT_SESSION"

trait MockSpringBoot:
  var jsonRequestBody = true
  private var mockSpringMvc: MockMvc = null
  private var mockCookies: Array[Cookie] = Array.empty
  private var mockSpringSession: HttpSession = null
  private var objectMapper: ObjectMapper = null
  private var beanFactory: DefaultListableBeanFactory = createBeanFactory()

  def bean[T:ClassTag]: T = beanFactory.getBean(classTag[T].runtimeClass.asInstanceOf[Class[T]], null.asInstanceOf[Array[AnyRef]]:_*)
  def bean[T:ClassTag](beanObj: =>T): T =
    val requiredType = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    val bd = new RootBeanDefinition(requiredType)
    bd.setInstanceSupplier(()=>beanObj)
    beanFactory.registerBeanDefinition(requiredType.getName, bd)
    beanObj
  def property(key:String): String = bean[Environment].getProperty(key, classOf[String])
  def createBeanFactory() =
    if !this.getClass.isAnnotationPresent(classOf[SpringBootTest]) || !this.getClass.isAnnotationPresent(classOf[AutoConfigureMockMvc]) then
      throw new RuntimeException("Test class should be marked with @SpringBootTest and @AutoConfigureMockMvc")
    beanFactory =
      val testContextManager = new TestContextManager(this.getClass)
      testContextManager.prepareTestInstance(this)
      testContextManager.getTestContext.getApplicationContext
        .getAutowireCapableBeanFactory.asInstanceOf[DefaultListableBeanFactory]
    mockSpringMvc = beanFactory.getBean(classOf[MockMvc])
    if jsonRequestBody then
      objectMapper = beanFactory.getBean(classOf[ObjectMapper])
    mockSpringSession = null
    mockCookies = Array.empty
    beanFactory

  def get(uri:String, params:Map[String,String]=Map.empty): JsonResponse =
    val req = MockMvcRequestBuilders.get(uri)
    params.foreach { (k, v) =>
      req.param(k, v.toString)
    }
    sendRequest(uri, req)
  def post(uri:String, data:Array[Byte]): JsonResponse =
    val req = MockMvcRequestBuilders.post(uri).content(data)
    sendRequest(uri, req)
  def post(uri:String, data:Map[String,Any]=Map.empty): JsonResponse =
    val req = MockMvcRequestBuilders.post(uri)
    if jsonRequestBody then
      val json = objectMapper.writeValueAsString(data)
      req.contentType(MediaType.APPLICATION_JSON)
      req.content(json)
    else
      data.foreach { (k, v)=>
        req.param(k, v.toString)
      }
    sendRequest(uri,req)
  def sendRequest(uri:String, reqBuilder:MockHttpServletRequestBuilder): JsonResponse =
    if mockSpringSession != null then //save current session
      reqBuilder.requestAttr(SPRINT_CURRENT_SESSION, mockSpringSession)
    if mockCookies.nonEmpty then
      reqBuilder.cookie( mockCookies:_* )
    //send
    val result = mockSpringMvc.perform(reqBuilder).andReturn()
    val response = result.getResponse
    mockSpringSession = result.getRequest //load current session
      .getAttribute(SPRINT_CURRENT_SESSION).asInstanceOf[HttpSession]
    if response.getCookies.nonEmpty then
      mockCookies = response.getCookies
    if response.getStatus >= 400 then
      val msg = result.getRequest.getAttribute(classOf[DefaultErrorAttributes].getName+".ERROR") match
        case ex:Exception=> ex.getMessage
        case _ => response.getErrorMessage match
          case null => response.getContentAsString
          case s => s
      throw HttpStatusException(response.getStatus, uri, msg)
    new JsonResponse(uri, response)

class JsonResponse(url:String, response: MockHttpServletResponse):
  lazy val inputStream = new ByteArrayInputStream( response.getContentAsByteArray )
  lazy val text = response.getContentAsString(Charset.forName("utf-8"))

  private val jsonDocument = text match
    case null | "" | "null" => null
    case _ => JsonPath.parse(text)
  def status: Int = response.getStatus
  /** Assert json response with (JsonPath)[https://github.com/json-path/JsonPath] */
  def assertJson(path:String): JsonResponse = assertJson(path,None)
  def assertJson(path:String, expected:Any): JsonResponse = assertJson(path,Some(expected))
  def assertJson(path:String, expected:Option[Any]): JsonResponse =
    val jsonObj: AnyRef = json(path)
    val passed = (jsonObj, expected) match
      case (null, None) => false
      case (null, null) => true
      case (a:JSONArray, None) if a.isEmpty => false
      case (_, None) => true
      case (null, Some(v)) => v==null
      case (a:JSONArray, Some(v:Array[_])) => a.size==v.size && a.toArray.zip(v).forall(_ == _)
      case (a:JSONArray, Some(v:Any)) => a.asScala.contains(v)
      case (_, Some(v)) => v == jsonObj
    if !passed then
      throw new JsonAssertException(url, path, text, jsonObj, expected)
    this
  def json[T:ClassTag]: T = json("$")
  def json[T:ClassTag](path:String): T =
    val ct = classTag[T]
    if classOf[Map[_,_]].isAssignableFrom(ct.runtimeClass) then
      val value:util.Map[Any,Any] = json(path)
      value.asScala.toMap.asInstanceOf[T]
    else if classOf[List[_]].isAssignableFrom(ct.runtimeClass) then
      val value: JSONArray = json(path)
      value.asScala.toList.asInstanceOf[T]
    else jsonDocument match
      case null => null.asInstanceOf[T]
      case _ =>
        try jsonDocument.read(path, ct.runtimeClass.asInstanceOf[Class[T]])
        catch case _:PathNotFoundException => null.asInstanceOf[T]
  def assertStatus(expected:Int) = if status!=expected then
    throw AssertionError(s"Expected status: $expected; actual value: $status")
  def assertHeader(header:String, expected:String) = response.getHeader(header) match
    case null => throw AssertionError(s"Header '$header' not exists")
    case value => if value != expected then
      throw AssertionError(s"Expected header '$header' value: $expected; actual value: $value")

class JsonAssertException(url:String, jsonPath:String, json:String, valueAtPath:Any, expected:Option[Any]) extends Exception({
  val expectMsg = expected match
    case None => ""
    case Some(v)=>
      val msg = v match
        case a:Array[_] => "[" + a.mkString(",") + "]"
        case _ => v.toString
      s"\nExpected: $msg"
  s"""Response from $url failed to match json path '$jsonPath'$expectMsg
     |Value at path: $valueAtPath
     |Response: $json""".stripMargin
})

case class HttpStatusException(status:Int, url:String, text:String)
  extends Exception:
  override def getMessage = s"Http status $status at $url: $text"
