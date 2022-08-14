import $ivy.`com.wiwiwa::mill-spring-boot:1.8`, com.wiwiwa.mill.ScalaLibraryModule
import mill._
import mill.scalalib._

val springBootVersion = "2.7.0"
val uTestVersion = "0.7.10"


object millSpringBoot extends ScalaLibraryModule {
  def organization = "com.wiwiwa"
  override def scalaVersion = "2.13.7"
  val millVersion = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
    .getPath.replaceFirst(raw"^.*[/\-]([\d.]+)(\.jar)?!.*","$1")

  override def ivyDeps = Agg(
    ivy"org.springframework.boot:spring-boot-maven-plugin:$springBootVersion",
    ivy"org.apache.maven.shared:maven-common-artifact-filters:3.3.1",
    ivy"org.apache.maven:maven-core:3.8.6",
  )
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

object springBootScala extends ScalaLibraryModule {
  def organization = millSpringBoot.organization
  override def scalaVersion = "3.1.0"
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

object springBootTest extends ScalaLibraryModule {
  def organization = millSpringBoot.organization
  override def scalaVersion = springBootScala.scalaVersion
  override def ivyDeps = Agg(
    ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
    ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
    ivy"org.springframework.boot:spring-boot-starter-test:$springBootVersion",
  )
  override def moduleDeps = Seq(springBootScala)
}
