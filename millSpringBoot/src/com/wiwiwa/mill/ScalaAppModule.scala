package com.wiwiwa.mill

import mill._
import mill.eval.Evaluator
import mill.scalalib._

import scala.util.matching.Regex.Groups

/**
 * With this module, the following features are enabled:
 * + `publishVersion`: auto versioning based on git tag`
 */
trait JavaAppModule extends JavaModule {

  override def artifactId = T {
    raw"(^|[^A-Z])([A-Z])".r
      .replaceAllIn(super.artifactId(), {
        _ match {
          case Groups("", upper) => upper.toLowerCase
          case Groups(a, upper) => s"$a-${upper.toLowerCase}"
        }
      })
  }
  def publishVersion = T {
    applicationVersion()
  }
  def applicationVersion = T.input {
    implicit val pwd: os.Path = millSourcePath / os.up
    val GitLog = """(.*)\.(\d+)(.*)""".r
    val isClean = os.proc("git","status").call().out.lines.last.endsWith(" clean")
    if(!isClean) "SNAPSHOT" else
      os.proc("git","tag","--sort=-authordate").call().out.lines.head match {
        case GitLog(vLeft, vMinor, vRight) =>
          val isHead = os.proc("git", "show").call().out.lines.head.contains("HEAD")
          val minor = if (isHead) vMinor.toInt else vMinor.toInt + 1
          val version = s"$vLeft.$minor$vRight"
          if (!isHead) {
            println(s"Adding new git tag: $version")
            os.proc("git", "tag", version).call()
          }
          version
        case _ => throw new IllegalStateException("Latest tag is not in format for xxx.yyy")
      }
  }
  override def manifest = T {
    super.manifest().add(
      "Implementation-Version" -> publishVersion(),
    )
  }

  def showUpdates(ev: Evaluator) = Dependency.showUpdates(ev)
}

trait ScalaAppModule extends ScalaModule with JavaAppModule
