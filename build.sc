import mill._
import mill.scalalib._
import mill.scalalib.publish.{License, PomSettings, VersionControl}
import os.Path

val defaultPom = PomSettings(
  description="",
  organization = "com.wiwiwa",
  url = "https://github.com/wiwiwa/",
  licenses = Seq(License.MIT),
  versionControl = VersionControl.github("wiwiwa","spring-boot-scala"),
  developers = Seq()
)

object millSpringBoot extends ScalaModule with PublishModule{
  def scalaVersion = "2.13.7"
  def publishVersion = "0.1"
  def pomSettings = defaultPom.copy(description="A mill plugin that can build Spring application with Scala")

  override def ivyDeps ={
    val millVersion = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
      .getPath.replaceFirst(raw"^.*[/\-]([\d.]+)(\.jar)?!.*","$1")
    Agg(
      ivy"com.lihaoyi::mill-scalalib:$millVersion",
    )
  }

  object test extends ScalaModule{
    def springBootVersion = "2.6.1"
    override def ivyDeps = {
      Agg(
        ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
        ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
      )
    }
    override def scalaVersion = millSpringBoot.scalaVersion()

    override def mainClass = Some("mill.MillMain")
    override def run(args: String*) = T.command{
      super.run("springTest.assembly")
    }
    override def forkWorkingDir = millSourcePath
    override def runClasspath: T[Seq[PathRef]] = {
      val path = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
        .getPath.replaceFirst(raw"file:(.*)!.*","$1")
      Seq(PathRef(Path(path))) :+ millSpringBoot.compile().classes
    }
  }
}

object SpringBootScala extends ScalaModule{
  def scalaVersion = "3.1.0"
}
