import $ivy.`com.wiwiwa::mill-spring-boot:SNAPSHOT`, com.wiwiwa.springboot.SpringBootScalaModule
import mill._
import mill.scalalib._
import os.Path

val springBootVersion = "3.0.1"

object demo extends SpringBootScalaModule {
  def scalaVersion = "3.2.1"
  override def ivyDeps = T{ super.ivyDeps() ++ Seq(
    ivy"com.wiwiwa::spring-boot-scala:SNAPSHOT",
    ivy"com.wiwiwa::spring-boot-test:SNAPSHOT",
    ivy"com.lihaoyi::utest:0.8.1",
    ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
    ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
    ivy"com.h2database:h2:2.1.212",
    ivy"javax.xml.bind:jaxb-api:2.3.1",
  ) }
  override def millSourcePath: Path = super.millSourcePath/os.up
}
