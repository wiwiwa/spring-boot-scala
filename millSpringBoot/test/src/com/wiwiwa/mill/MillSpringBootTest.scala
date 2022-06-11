package com.wiwiwa.mill

import ammonite.main.Defaults
import ammonite.util.Colors
import com.wiwiwa.springboot.SpringBootScalaModule
import mill.{Agg, T}
import mill.api.Result.{Exception, Failure, Success}
import mill.define.{BasePath, Caller, Ctx, ExternalModule, Segments, Target, Task}
import mill.eval.{Evaluator, EvaluatorPaths}
import mill.moduledefs.Cacher
import mill.scalalib.DepSyntax
import mill.util.PrintLogger
import org.springframework.boot.SpringApplication
import sourcecode.Name
import utest._

object MillSpringBootTest extends TestSuite with Cacher {
  val evaluator = createEvaluator()
  val testApp = createModule()

  override def tests = Tests{
    val ret = testApp.assembly.value
    assert( ret != null )
  }

  def createModule() = {
    val name = getClass.getSimpleName.replace("$","")
    implicit val ctx = Ctx.make(
      implicitly, implicitly, Name(name), BasePath(os.pwd), Segments(),
      Ctx.External(false), Ctx.Foreign(None), sourcecode.File(""), Caller("")
    )
    new SpringBootScalaModule {
      override def organization = this.getClass.getPackageName
      override def mainClass = Some("DummyMain")
      override def ivyDeps = T{
        val springBootVersion = classOf[SpringApplication].getPackage.getImplementationVersion
        Agg(ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion")
      }
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
    val wd = os.pwd
    Evaluator(
      Defaults.ammoniteHome, wd / "out", wd / "out",
      rootModule, logger
    )
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

object rootModule extends ExternalModule {
  override def millDiscover = ???
}
