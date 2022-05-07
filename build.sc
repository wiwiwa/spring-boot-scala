import $ivy.`com.wiwiwa::mill-spring-boot:1.0`
import com.wiwiwa.mill.ScalaAppModule
import mill._
import mill.scalalib._

val scala3 = "3.1.0"
val springBootVersion = "2.6.7"
val uTestVersion = "0.7.10"

object millSpringBoot extends ScalaAppModule with PublishModule {
  def scalaVersion = "2.13.7"
  override def organization = "com.wiwiwa"
  val millVersion = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
    .getPath.replaceFirst(raw"^.*[/\-]([\d.]+)(\.jar)?!.*","$1")

  override def compileIvyDeps = Agg(
    ivy"com.lihaoyi::mill-scalalib:$millVersion",
  )

  object test extends Tests with TestModule.Utest {
    override def ivyDeps = Agg(
      ivy"com.lihaoyi::utest:$uTestVersion",
      ivy"com.lihaoyi::mill-scalalib:$millVersion",
      ivy"org.springframework.boot:spring-boot:$springBootVersion"
        .excludeName("log4j-to-slf4j"), //log4j-to-slf4j conflicts with mill-scalalib-worker
    )
  }
}

object springBootScala extends ScalaAppModule with PublishModule {
  def scalaVersion = scala3
  override def organization = millSpringBoot.organization

  override def ivyDeps = Agg(
    ivy"com.fasterxml.jackson.module::jackson-module-scala:2.13.1",
  )
  override def compileIvyDeps = Agg(
    ivy"org.springframework.boot:spring-boot:$springBootVersion",
  )

  object test extends Tests with TestModule.Utest {
    override def ivyDeps = T{ springBootScala.compileIvyDeps() ++ Seq(
      ivy"com.lihaoyi::utest:$uTestVersion",
      ivy"com.h2database:h2:1.4.200",
      ivy"javax.xml.bind:jaxb-api:2.3.1",
    ) }
    override def moduleDeps = Seq(springBootTest)
  }
}

object springBootTest extends ScalaAppModule with PublishModule {
  override def scalaVersion = scala3
  override def ivyDeps = Agg(
    ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
    ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
    ivy"org.springframework.boot:spring-boot-starter-test:$springBootVersion",
  )
}
