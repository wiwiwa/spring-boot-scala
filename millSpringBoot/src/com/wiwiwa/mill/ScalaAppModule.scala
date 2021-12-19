package com.wiwiwa.mill

import ammonite.ops.%%
import mill._
import mill.scalalib.{PublishModule, ScalaModule}

/**
 * With this module, the following features are enabled:
 * + `publishVersion`: auto versioning based on git tag`
 */
trait ScalaAppModule extends ScalaModule with PublishModule {
  def applicationVersion = T.input{
    implicit val pwd = millSourcePath/os.up
    val GitTag = """(.*)\.(\d+)(.*)""".r
    val GitTag(vLeft,vMinor,vRight) = %%.git('tag, "--sort=-creatordate").out.lines.head
    val gitStatus = %%.git('status).out
    val snapshot = if(gitStatus.lines.last.endsWith(" clean")) "" else "-SNAPSHOT"
    s"$vLeft.${vMinor.toInt+1}$vRight$snapshot"
  }
  override def publishVersion = T{ applicationVersion() }

  override def manifest = T{ super.manifest().add(
    "ApplicationVersion"-> publishVersion(),
  )}
}
