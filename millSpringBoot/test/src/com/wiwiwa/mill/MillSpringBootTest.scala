package com.wiwiwa.mill

import ammonite.main.Defaults
import ammonite.util.Colors
import com.wiwiwa.springboot.SpringBootScalaModule
import mill.api.Result.{Exception, Failure, Success}
import mill.Agg
import mill.define.{BasePath, Caller, Ctx, ExternalModule, Segments, Task}
import mill.eval.Evaluator
import mill.moduledefs.Cacher
import mill.util.PrintLogger
import sourcecode.Name
import utest._

object MillSpringBootTest extends TestSuite with Cacher {
  val evaluator = createEvaluator()
  val testApp = createModule()

  override def tests = Tests{
    val ret = testApp.applicationVersion.value
    assert( ret != null )
  }

  def createModule() = {
    val name = getClass.getSimpleName.replace("$","")
    implicit val ctx = Ctx.make(
      implicitly, implicitly, Name(name), BasePath(os.pwd), Segments(),
      Ctx.External(false), Ctx.Foreign(None), sourcecode.File(""), Caller("")
    )
    new SpringBootScalaModule {
      override def scalaVersion = "3.1.0"
      override def organization = this.getClass.getPackageName
      override def mainClass = Some("DummyMain")
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
      evaluator.evaluate(Agg(task))
        .results
        .collect {
          case (_, Success(v)) => v
          case (t, Exception(e, _)) => throw new RuntimeException(s"$t failed", e)
          case (t, Failure(msg, _)) => throw new RuntimeException(s"$t failed: $msg")
        }.last
    }
  }
}

object rootModule extends ExternalModule {
  override def millDiscover = ???
}
