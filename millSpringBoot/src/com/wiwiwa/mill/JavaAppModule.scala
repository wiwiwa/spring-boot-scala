package com.wiwiwa.mill

import ammonite.ops._
import mill.T
import mill.scalalib.JavaModule
import mill.scalalib.publish.{PomSettings, VersionControl}

import scala.util.matching.Regex.Groups

/**
 * With this module, the following features are enabled:
 * + `publishVersion`: auto versioning based on git tag`
 */
trait JavaAppModule extends JavaModule {
  def appDescription = ""
  def appUrl = ""
  def organization: String = ???

  override def artifactId = T {
    raw"([A-Z])([^A-Z])".r
      .replaceAllIn(super.artifactId(), {
        _ match {
          case Groups(a, b) => s"-${a.toLowerCase}$b"
        }
      })
  }

  def applicationVersion = T.input {
    implicit val pwd = millSourcePath / os.up
    val GitLog = """(?s)(.*?)\btag:\s+([^.]+)\.(\d*)([^)]*).*""".r
    %%.git("log","--decorate").out.string match {
      case GitLog(prefix, vLeft, vMinor, vRight) =>
        val isClean = %%.git('status).out.lines.last.endsWith(" clean")
        val isHead = !prefix.contains("\n")
        val version = {
          val minor = if(isHead && isClean) vMinor.toInt else vMinor.toInt+1
          s"$vLeft.${minor}$vRight"
        }
        if(isClean && !isHead) {
          println(s"Adding new git tag: $version")
          %.git("tag", version)
        }
        val snapshot = if(isClean) "" else "-SNAPSHOT"
        s"$version$snapshot"
      case _ => throw new IllegalStateException("A git tag not found in format for xxx.yyy")
    }
  }

  override def manifest = T {
    super.manifest().add(
      "ApplicationVersion" -> publishVersion(),
    )
  }

  def pomSettings = T{ PomSettings(
    description=appDescription,
    organization=organization,
    url=appUrl,
    licenses=Seq(),
    versionControl = VersionControl(Some(appUrl), None, None, None),
    developers = Seq()
  ) }
  def publishVersion = T {
    applicationVersion()
  }
}
