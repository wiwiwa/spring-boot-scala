package com.wiwiwa.springboot

import com.wiwiwa.mill.ScalaAppModule
import mill._
import mill.scalalib.DepSyntax
import org.apache.maven.artifact.handler.DefaultArtifactHandler
import org.apache.maven.artifact.{Artifact, DefaultArtifact}
import org.apache.maven.execution.{DefaultMavenExecutionRequest, MavenSession}
import org.apache.maven.project.MavenProject
import org.springframework.boot.maven.RepackageMojo

import java.io._
import java.util
import scala.jdk.CollectionConverters._

/**
 * SpringBootScalaModule enables Scala support for Spring Boot framework
 * With this module, the following features are enabled:
 * + mill `assembly` command can build a Spring Boot fat jar
 * + Scala class can be used as Spring beans
 */
trait SpringBootScalaModule extends ScalaAppModule {
  override def mandatoryIvyDeps = T{
    val springBootScalaVersion = classOf[SpringBootScalaModule].getPackage.getImplementationVersion match {
      case null => "SNAPSHOT"
      case v => v
    }
    super.mandatoryIvyDeps() ++ Agg(
      ivy"com.wiwiwa::spring-boot-scala:$springBootScalaVersion",
    )
  }
  override def javacOptions = Seq("-parameters")

  override def assembly = T {
    val jarPath = T.workspace / "out" / s"${artifactId()}-${publishVersion()}.jar"
    val mavenProject = new MavenProject {
      override def getArtifact = new DefaultArtifact("my.organization", artifactId(), publishVersion(), "compile", "", null, new DefaultArtifactHandler("jar"))
      override def getArtifacts = resolvedRunIvyDeps().map(_.path)
        .map { p =>
          val List(version, name, group) = p.segments.toList.reverse match {
            case List(_,"jars",v,n,g,_*) => List(v,n,g)
            case List(_,v,n,g,_*) => List(v,n,g)
          }
          val a = new DefaultArtifact(group, name, version, "compile", "", null, new DefaultArtifactHandler(p.ext))
          a.setFile(p.toIO)
          a
        }.toSet.asJava.asInstanceOf[util.Set[Artifact]]
    }
    val mojo = new RepackageMojo{
      project = mavenProject
      session = new MavenSession(null,new DefaultMavenExecutionRequest,null,project)
      override def getSourceArtifact(classifier: String) =
        new DefaultArtifact("g","a","0","","","",null){
          override def getFile = jar().path.toIO
        }
      override def getTargetFile(finalName: String, classifier: String, targetDirectory: File) = jarPath.toIO
    }
    mojo.execute()
    println(s"Built Spring Boot jar file $jarPath")
    PathRef(jarPath)
  }
  override def artifactSuffix = ""

  trait Tests extends SpringBootTests
  trait SpringBootTests extends ScalaModuleTests {
    override def ivyDeps = T{
      val springBootScalaVersion = classOf[SpringBootScalaModule].getPackage.getImplementationVersion match {
        case null => "SNAPSHOT"
        case v => v
      }
      val compileSpringBootVersion = classOf[RepackageMojo].getPackage.getImplementationVersion
      Agg(
        ivy"com.wiwiwa::spring-boot-test:$springBootScalaVersion",
        ivy"org.springframework.boot:spring-boot-test-autoconfigure:$compileSpringBootVersion",
      )
    }
  }
}
