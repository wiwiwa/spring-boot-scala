package com.wiwiwa.scala.spring

import mill._
import mill.modules.Assembly.Rule
import mill.scalalib.ScalaModule

import java.net.URLClassLoader
import java.util.Properties
import scala.jdk.CollectionConverters._
import scala.util.Using

/**
 * SpringBootScalaModule enables Scala support for Spring Boot framework
 * With this module, the following features are enabled:
 * + mill `assembly` command can build a Spring Boot fat jar
 * + Scala class can be used as Spring beans
 */
trait SpringBootScalaModule extends ScalaModule {
  override def assembly = T {
    springFactories()
    super.assembly()
  }

  override def javacOptions = T {
    Seq("-parameters")
  }

  override def assemblyRules = super.assemblyRules ++ Agg(
    Rule.Append("META-INF/spring.handlers", "\n"),
  )

  def springFactories = T {
    val target = compile().classes.path / "META-INF" / "spring.factories"
    val classPath = compileClasspath()
      .map(_.path.toIO.toURI.toURL)
      .iterator.toArray
    val text = new URLClassLoader(classPath)
      .getResources("META-INF/spring.factories").asScala
      .map(_.openStream)
      .map(Using(_) { f => val p = new Properties(); p.load(f); p })
      .flatMap(_.get.asScala)
      .flatMap { case (k, v) => v.split(',').map(k -> _) }
      .iterator.toSeq
      .groupMap(_._1)(_._2).view
      .mapValues(_.mkString(","))
      .map { case (k, v) => s"$k=$v" }
      .mkString("\n")
    os.write.over(target, text, createFolders = true)
    target
  }
}
