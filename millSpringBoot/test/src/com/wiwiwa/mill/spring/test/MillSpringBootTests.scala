package com.wiwiwa.mill.spring.test

import ammonite.ops._
import ammonite.util.Colors
import com.wiwiwa.springboot.SpringBootScalaModule
import mill.Agg
import mill.api.{BuildProblemReporter, Strict, TestReporter}
import mill.define._
import mill.eval.Evaluator.Evaluated
import mill.eval.{Evaluator, Result}
import mill.scalalib.publish.{License, PomSettings, VersionControl}
import mill.util.{ColorLogger, PrintLogger}
import sourcecode.{Enclosing, File, Line, Name}
import utest._

object MillSpringBootTests extends TestSuite {
  val evaluator = newEveluator()
  override def tests = Tests {
    val module = springBootMoudle
    val a = module.artifactId |> eval
    val v = module.publishVersion |> eval
    println(s"Current version: $a $v")
  }

  def springBootMoudle = {
    implicit val ctx = evaluator.rootModule.millOuterCtx.copy(
      segment=Segment.Label("millSpringBootTests"),
    )
    new SpringBootScalaModule{
      override def scalaVersion = "3.1.0"
      override def pomSettings = PomSettings(
        "test app","com.wiwiwa.test",
        "https://github.com/wiwiwa",Seq(License.MIT),
        VersionControl.github("wiwiwa","test"),
        Seq()
      )
    }
  }
  def newEveluator() = {
    val module = new BaseModule(os.pwd)(Enclosing(""),Line(0),Name(""),File(""),Caller(())) {
      override def millDiscover = Discover[this.type]
    }
    val logger = PrintLogger(true,false,Colors.Default,
      System.out, System.out, System.err, System.in,
      true, "context")
    new Evaluator(os.pwd, os.pwd/"out",null, module, logger){
      override protected def evaluateGroupCached(terminal: Terminal, group: Strict.Agg[Task[_]], results: collection.Map[Task[_], Result[(Any, Int)]], counterMsg: String, zincProblemReporter: Int => Option[BuildProblemReporter], testReporter: TestReporter, logger: ColorLogger) = {
        val (ret,evaluated) = evaluateGroup(group, results, -1, None, None, counterMsg, zincProblemReporter, testReporter, logger)
        Evaluated(ret, evaluated.toSeq, false)
      }
    }
  }
  def eval[T](t:Task[T]) = evaluator.evaluate(Agg(t)).values.head
  def evalAndPrint[T](t:Task[T]): Unit = println(eval(t))
}
