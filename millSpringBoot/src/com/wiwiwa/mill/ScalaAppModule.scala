package com.wiwiwa.mill

import mill.T
import mill.eval.Evaluator
import mill.scalalib.{Dependency, JavaModule, ScalaModule}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.jdk.CollectionConverters._
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
    val repo = new FileRepositoryBuilder()
      .setWorkTree((millSourcePath / os.up).toIO)
      .build()
    val git = new Git(repo)
    if(!git.status().call().isClean) "SNAPSHOT"
    else {
      val refDB = repo.getRefDatabase
      val tag = refDB.getRefsByPrefix(Constants.R_TAGS).iterator.asScala
        .maxBy{ tag=> repo.parseCommit(tag.getObjectId).getCommitTime }
      val tagTarget = Option( refDB.peel(tag).getPeeledObjectId )
        .getOrElse{ tag.getObjectId }
      val head = repo.resolve("HEAD")
      val tagName = tag.getName.substring(Constants.R_TAGS.length)
      if(head==tagTarget) tagName
      else {
        val GitLog = """(.*)\.(\d+)(.*)""".r
        val newTagName = tagName match {
          case GitLog (vLeft, vMinor, vRight) =>
            val minor = vMinor.toInt + 1
            s"$vLeft.$minor$vRight"
          case _ => throw new IllegalStateException("Latest tag is not in format for xxx.yyy")
        }
        println(s"Adding new git tag: $newTagName")
        git.tag.setName(newTagName)
          .setObjectId(repo.parseCommit(head))
          .call()
        newTagName
      }
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
