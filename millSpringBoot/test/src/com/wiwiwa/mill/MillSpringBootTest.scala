package com.wiwiwa.mill

import com.wiwiwa.springboot.SpringBootScalaModule
import mill.api.Result
import mill.define.Segment.Label
import mill.define.{Segments, Target}
import mill.main.MainModule
import mill.scalalib.DepSyntax
import mill.testkit.MillTestKit
import mill.{Agg, T}
import org.springframework.boot.loader.tools.Repackager
import sourcecode.{Enclosing, Line}
import utest._

object MillSpringBootTest extends TestSuite with MillTestKit {
  override def tests = Tests {
    val retVal = runTask(springBootScalaTestModule.applicationVersion)
    assert(retVal != "")
  }

  val evaluator = createEvaluator()
  object springBootScalaTestModule extends BaseModule()(Enclosing("test"), Line(0))
    with SpringBootScalaModule with MainModule {
    override def scalaVersion = T {"3.2.1"}
    override def ivyDeps = T {
      val springBootVersion = classOf[Repackager].getPackage.getImplementationVersion
      super.ivyDeps() ++ Seq(
        ivy"com.wiwiwa::spring-boot-test:SNAPSHOT",
        ivy"com.lihaoyi::utest:0.8.1",
        ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
        ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
        ivy"com.h2database:h2:2.1.212",
        ivy"javax.xml.bind:jaxb-api:2.3.1",
      )
    }
    override implicit def millModuleSegments = Segments(Seq(Label("test")))
  }
  override def getSrcPathBase() = os.pwd / "springBootScala"

  def createEvaluator() = new TestEvaluator(springBootScalaTestModule,
      s"${os.pwd}/springBootScala/test".split('/').drop(1),
      true, Some(1),
      System.out, System.in, false,
      Seq.empty
    ).evaluator
  def runTask[T](task:Target[T]) = {
    val res = evaluator.evaluate(Agg(task))
    res.failing.values.headOption
      .map(_.head)
      .collectFirst { case Result.Exception(e, _) => throw e }
    res.rawValues.head.asSuccess.get.value.value
  }
}
