import $ivy.`com.wiwiwa::mill-spring-boot:0.11`
import com.wiwiwa.mill.ScalaAppModule
import mill._
import mill.scalalib._

val springBootVersion = "2.6.1"
val uTestVersion = "0.7.10"

object millSpringBoot extends ScalaAppModule with PublishModule {
  def scalaVersion = "2.13.7"
  override def organization = "com.wiwiwa"
  val millVersion = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
    .getPath.replaceFirst(raw"^.*[/\-]([\d.]+)(\.jar)?!.*","$1")

  override def compileIvyDeps ={
    Agg(ivy"com.lihaoyi::mill-scalalib:$millVersion")
  }

  object test extends Tests with TestModule.Utest {
    override def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:$uTestVersion",
      ivy"com.lihaoyi::mill-scalalib:$millVersion",
    )
  }
}

object springBootScala extends ScalaAppModule with PublishModule {
  def scalaVersion = "3.1.0"
  override def organization = millSpringBoot.organization

  override def ivyDeps = Agg(
    ivy"com.fasterxml.jackson.module::jackson-module-scala:2.13.1",
  )
  override def compileIvyDeps = Agg(
    ivy"org.springframework:spring-context:5.3.13",
  )

  object test extends Tests with TestModule.Utest {
    override def ivyDeps = T{ springBootScala.compileIvyDeps() ++ Seq(
      ivy"com.lihaoyi::utest:$uTestVersion",
      ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
      ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
      ivy"com.h2database:h2:1.4.200",
      ivy"javax.xml.bind:jaxb-api:2.3.1",
    ) }
  }
}
