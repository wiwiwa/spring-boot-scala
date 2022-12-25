import $ivy.`com.wiwiwa::mill-spring-boot:1.20`, com.wiwiwa.mill.ScalaLibraryModule
import mill._
import mill.scalalib._

val springBootVersion = "3.0.1"
val uTestVersion = "0.8.1"

object millSpringBoot extends ScalaLibraryModule {
  def organization = "com.wiwiwa"
  override def scalaVersion = "2.13.10"
  val millVersion = classOf[JavaModule].getResource(classOf[JavaModule].getSimpleName+".class")
    .getPath.replaceFirst(raw"^.*[/\-]([\d.]+)(\.jar)?!.*","$1")

  override def ivyDeps = Agg(
    ivy"org.springframework.boot:spring-boot-maven-plugin:$springBootVersion",
    ivy"org.apache.maven.shared:maven-common-artifact-filters:3.3.1",
    ivy"org.apache.maven:maven-core:3.8.6",
    ivy"org.openl.jgit:org.eclipse.jgit:6.3.0.202209071007-openl-2",
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
  override def scalaVersion = "3.2.1"
  override def ivyDeps = Agg(
    ivy"com.fasterxml.jackson.module::jackson-module-scala:2.14.1",
  )
  override def compileIvyDeps = Agg(
    ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
    ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
  )

  object test extends Tests with TestModule.Utest {
    override def ivyDeps = T{ springBootScala.compileIvyDeps() ++ Seq(
      ivy"com.lihaoyi::utest:$uTestVersion",
      ivy"com.h2database:h2:2.1.212",
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
