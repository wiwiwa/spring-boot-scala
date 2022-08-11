import $ivy.`com.wiwiwa::mill-spring-boot:SNAPSHOT`, com.wiwiwa.springboot.SpringBootScalaModule
import mill._
import mill.scalalib._
import os.Path

val springBootVersion = "2.7.0"

object demo extends SpringBootScalaModule {
  override def ivyDeps = T{ super.ivyDeps() ++ Seq(
    ivy"com.wiwiwa::spring-boot-test:SNAPSHOT",
    ivy"com.lihaoyi::utest:0.7.10",
    ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
    ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
    ivy"com.h2database:h2:1.4.200",
    ivy"javax.xml.bind:jaxb-api:2.3.1",
  ) }
  override def millSourcePath: Path = super.millSourcePath/os.up
}
