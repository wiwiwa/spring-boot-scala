import mill._
import mill.scalalib._
import mill.scalalib.publish.{License, PomSettings, VersionControl}
import os.Path

val defaultPom = PomSettings(
  description="",
  organization = "com.wiwiwa.scala.spring",
  url = "https://github.com/wiwiwa/",
  licenses = Seq(License.MIT),
  versionControl = VersionControl.github("wiwiwa","spring-boot-scala"),
  developers = Seq()
)

object millPlugin extends ScalaModule{
  def scalaVersion = "2.13.7"
  override def ivyDeps = Agg(
    ivy"com.lihaoyi::mill-scalalib:$millVersion",
  )

  def millVersion = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
    .getPath.replaceFirst(raw"^.*[/\-]([\d.]+)(\.jar)?!.*","$1")

  object test extends ScalaModule{
    def springBootVersion = "2.6.1"
    override def ivyDeps = {
      Agg(
        ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
        ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
      )
    }
    override def scalaVersion = millPlugin.scalaVersion()

    override def mainClass = Some("mill.MillMain")
    override def run(args: String*) = super.run("springTest.assembly")
    override def forkWorkingDir = millSourcePath
    override def moduleDeps = Seq(millPlugin)
    override def runClasspath: T[Seq[PathRef]] = {
      val path = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
        .getPath.replaceFirst(raw"file:(.*)!.*","$1")
      Seq(PathRef(Path(path))) :+ millPlugin.compile().classes
    }
  }
}

object lib extends ScalaModule{
  def scalaVersion = "3.1.0"
}
