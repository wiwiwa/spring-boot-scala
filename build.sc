import $ivy.`com.wiwiwa::mill-spring-boot:0.6`, com.wiwiwa.mill.ScalaAppModule
import mill._
import mill.scalalib._
import mill.scalalib.publish.{License, PomSettings, VersionControl}

val springBootVersion = "2.6.1"

val defaultPom = PomSettings(
  description="",
  organization = "com.wiwiwa",
  url = "https://github.com/wiwiwa/",
  licenses = Seq(License.MIT),
  versionControl = VersionControl.github("wiwiwa","spring-boot-scala"),
  developers = Seq()
)

object millSpringBoot extends ScalaAppModule {
  def scalaVersion = "2.13.7"
  def pomSettings = defaultPom.copy(
    description="A mill plugin that can build Spring application with Scala"
  )

  override def compileIvyDeps ={
    val millVersion = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
      .getPath.replaceFirst(raw"^.*[/\-]([\d.]+)(\.jar)?!.*","$1")
    Agg(
      ivy"com.lihaoyi::mill-scalalib:$millVersion",
    )
  }
}

object springBootScala extends ScalaModule {
  def scalaVersion = "3.1.0"

  override def compileIvyDeps = Agg(
    ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
    ivy"com.fasterxml.jackson.module:jackson-module-scala_3:2.13.1",
  )

  object test extends Tests with TestModule.Utest {
    override def ivyDeps = T{ springBootScala.compileIvyDeps() ++ Seq(
      ivy"com.lihaoyi::utest:0.7.10",
    ) }
  }
}
