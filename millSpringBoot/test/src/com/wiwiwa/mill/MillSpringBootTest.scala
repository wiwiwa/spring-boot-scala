package com.wiwiwa.mill

import ammonite.main.Defaults
import ammonite.util.Colors
import com.wiwiwa.springboot.SpringBootScalaModule
import mill.api.Result.{Exception, Failure, Success}
import mill.define.Segment.Label
import mill.define._
import mill.eval.{Evaluator, EvaluatorPaths}
import mill.moduledefs.Cacher
import mill.scalalib.DepSyntax
import mill.util.PrintLogger
import mill.{Agg, T}
import org.springframework.boot.loader.tools.Repackager
import sourcecode.Name
import utest._

object MillSpringBootTest extends TestSuite with Cacher {
  val testApp = createModule()
  val evaluator = createEvaluator()

  override def tests = Tests{
    val ret = testApp.assembly.value
    assert( ret != null )
  }

  def createModule() = {
    val name = "springBootScalaTest"
    val projectPath = os.pwd/"springBootScala"/"test"
    implicit val ctx = Ctx.make(
      implicitly, implicitly, Name(name), BasePath(projectPath), Segments(),
      Ctx.External(false), Ctx.Foreign(None), sourcecode.File(""), Caller("")
    )
    new TestSpringBootModule with SpringBootScalaModule {
      override def scalaVersion = "3.1.0"
      override def ivyDeps = T {
        val springBootVersion = classOf[Repackager].getPackage.getImplementationVersion
        super.ivyDeps() ++ Seq(
          ivy"com.wiwiwa::spring-boot-test:1.6",
          ivy"com.lihaoyi::utest:0.7.10",
          ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
          ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
          ivy"com.h2database:h2:1.4.200",
          ivy"javax.xml.bind:jaxb-api:2.3.1",
        )
      }
      override def millSourcePath = projectPath
      implicit override def millModuleSegments = Segments(Label(name))
    }
  }

  def createEvaluator() = {
    val colors = Colors.Default
    val logger = PrintLogger(
      true, false,
      colors.info(), colors.error(),
      System.out, System.out, System.err, inStream = System.in,
      false,
      context = ""
    )
    val outDir = testApp.millSourcePath/"out"
    Evaluator( Defaults.ammoniteHome, outDir, outDir, testApp, logger )
  }

  implicit class MillTask(task:Task[_]) {
    def value = {
      clearCache()
      evaluator.evaluate(Agg(task))
        .results
        .collect {
          case (_, Success(v)) => v
          case (t, Exception(e, _)) => throw new RuntimeException(s"$t failed", e)
          case (t, Failure(msg, _)) => throw new RuntimeException(s"$t failed: $msg")
        }.last
    }
    def clearCache() = {
      val (map, _) = Evaluator.plan(Agg(task))
      val Right(label) = map.keys().toSeq.last
      val meta = EvaluatorPaths.resolveDestPaths(evaluator.outPath, evaluator.destSegments(label)).meta
      os.remove(meta)
    }
  }
}

class RootModule extends ExternalModule {
  override def millDiscover = Discover[this.type]
}
abstract class TestSpringBootModule extends RootModule with ScalaLibraryModule {
  object test extends Tests
  override def organization = "org"
}
