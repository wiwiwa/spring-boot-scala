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
    val GitLog = """(?s)(.*?)\btag:\s+([^.]+)\.(\d*)([^,)]*).*""".r
    os.proc("git","log","--decorate").call().out.text match {
      case GitLog(prefix, vLeft, vMinor, vRight) =>
        val isClean = os.proc("git","status").call().out.lines.last.endsWith(" clean")
        val isHead = !prefix.contains("\n")
        val version = {
          val minor = if(isHead && isClean) vMinor.toInt else vMinor.toInt+1
          s"$vLeft.${minor}$vRight"
        }
        if(isClean && !isHead) {
          println(s"Adding new git tag: $version")
          os.proc("git","tag", version).call()
        }
        if(isClean) version else "DEV-SNAPSHOT"
      case _ => throw new IllegalStateException("A git tag not found in format for xxx.yyy")
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
