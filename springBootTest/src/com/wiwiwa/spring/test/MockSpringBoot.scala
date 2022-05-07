package com.wiwiwa.spring.test

import com.jayway.jsonpath.{JsonPath, PathNotFoundException}
import net.minidev.json.JSONArray
import org.springframework.beans.factory.support.{DefaultListableBeanFactory, RootBeanDefinition}
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.{AnnotatedClassFinder, SpringBootContextLoader, SpringBootTest, SpringBootTestContextBootstrapper}
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.test.context.MergedContextConfiguration
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate
import org.springframework.test.context.support.DefaultBootstrapContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.{MockHttpServletRequestBuilder, MockMvcRequestBuilders}

import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.util
import javax.servlet.http.HttpSession
import scala.jdk.CollectionConverters.*
import scala.reflect.{ClassTag, classTag}

trait MockSpringBoot:
  private var mockSpringMvc: MockMvc = null
  private var mockSpringSession: HttpSession = null
  private var beanFactory: DefaultListableBeanFactory = createBeanFactory()

  def bean[T:ClassTag]: T = beanFactory.getBean(classTag[T].runtimeClass.asInstanceOf[Class[T]], null.asInstanceOf[Array[AnyRef]]:_*)
  def bean[T:ClassTag](beanObj: =>T): T =
    val requiredType = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    val bd = new RootBeanDefinition(requiredType)
    bd.setInstanceSupplier(()=>beanObj)
    beanFactory.registerBeanDefinition(requiredType.getName, bd)
    beanObj
  def createBeanFactory() =
    System.setProperty("spring.profiles.active", "test")
    val mainClass = new AnnotatedClassFinder(classOf[SpringBootConfiguration])
      .findFromClass(this.getClass)
    val configuration =
      val contextLoader = new DefaultCacheAwareContextLoaderDelegate()
      val bootstrapper = new SpringBootTestContextBootstrapper :
        /** return mainClass to Spring */
        override def getOrFindConfigurationClasses(config: MergedContextConfiguration) = Array(mainClass)
      bootstrapper.setBootstrapContext(new DefaultBootstrapContext(classOf[MockAllBeans], contextLoader))
      bootstrapper.buildMergedContextConfiguration()
    beanFactory = new SpringBootContextLoader{
        override def getSpringApplication = //override to lazy init beans
          val app = super.getSpringApplication
          app.setLazyInitialization(true)
          app
      }.loadContext(configuration)
      .getAutowireCapableBeanFactory.asInstanceOf[DefaultListableBeanFactory]
    mockSpringMvc = beanFactory.getBean(classOf[MockMvc])
    mockSpringSession = null
    beanFactory

  def get(uri:String) = sendRequest(uri, MockMvcRequestBuilders.get(uri))
  def get(uri:String, params:Map[String,String]): JsonResponse =
    val req = MockMvcRequestBuilders.get(uri)
    sendRequest(uri, req, params)
  def post(uri:String, data:Array[Byte]): JsonResponse =
    val req = MockMvcRequestBuilders.post(uri).content(data)
    sendRequest(uri, req)
  def post(uri:String, data:Map[String,Any]=Map.empty): JsonResponse =
    val req = MockMvcRequestBuilders.post(uri)
    sendRequest(uri,req,data)
  private def sendRequest(uri:String, req:MockHttpServletRequestBuilder, data:Map[String,Any]=Map.empty): JsonResponse =
    def addParam(key:String,value:Any): Unit = value match
      case null => ()
      case m:Map[_,_] =>
        val sep = if key.isEmpty then "" else "."
        m.foreach{(k,v)=>addParam(key+sep+k, v)}
      case m:util.LinkedHashMap[_,_] => addParam(key,m.asScala.toMap)
      case l:util.List[_] => l.asScala.foreach{v=> addParam(key,v)}
      case l:Array[_] => l.foreach{v=> addParam(key,v)}
      case _ => req.param(key,value.toString)
    addParam("", data)
    sendRequest(uri, req)
  private def sendRequest(url:String, reqBuider:MockHttpServletRequestBuilder): JsonResponse =
    if mockSpringSession!=null then reqBuider.session(mockSpringSession.asInstanceOf)
    val result = mockSpringMvc.perform(reqBuider).andReturn()
    if mockSpringSession==null then
      mockSpringSession = result.getRequest.getSession(false)
    val response = result.getResponse
    if response.getStatus >= 400 then
      val msg = result.getRequest.getAttribute(classOf[DefaultErrorAttributes].getName+".ERROR") match
        case ex:Exception=> ex.getMessage
        case _ => response.getContentAsString
      throw HttpStatusException(response.getStatus, url, msg)
    new JsonResponse(url, response)

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
      case (a:JSONArray, None) if a.isEmpty => false
      case (_, None) => true
      case (null, Some(v)) => v==null
      case (a:JSONArray, Some(v:Array[_])) => a.size==v.size && a.toArray.zip(v).forall(_ == _)
      case (a:JSONArray, Some(v:Any)) => a.asScala.contains(v)
      case (_, Some(v)) => v == jsonObj
    if !passed then
      throw new JsonAssertException(url, path, text, jsonObj, expected)
    this
  def json[T>:Null](implicit ct:ClassTag[T]): T = json("$")
  def json[T>:Null](path:String)(implicit ct:ClassTag[T]): T =
    if classOf[Map[_,_]].isAssignableFrom(ct.runtimeClass) then
      val value:util.Map[Any,Any] = json(path)
      value.asScala.toMap.asInstanceOf[T]
    else if classOf[List[_]].isAssignableFrom(ct.runtimeClass) then
      val value: JSONArray = json(path)
      value.asScala.toList.asInstanceOf[T]
    else jsonDocument match
      case null => null
      case _ =>
        try jsonDocument.read(path, ct.runtimeClass.asInstanceOf[Class[T]])
        catch case _:PathNotFoundException => null
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

@SpringBootTest
@AutoConfigureMockMvc
@EnableScheduling
class MockAllBeans
