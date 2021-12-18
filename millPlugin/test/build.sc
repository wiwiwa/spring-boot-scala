import com.wiwiwa.scala.spring.SpringBootScalaModule
import mill._
import mill.scalalib._

object springTest extends ScalaModule with SpringBootScalaModule {
  override def scalaVersion = "2.13.7"
  def springBootVersion = "2.6.1"

  override def ivyDeps = {
    Agg(
      ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
//      ivy"org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion",
//      ivy"jakarta.xml.bind:jakarta.xml.bind-api:2.3.3", //required by Spring Boot
      ivy"com.h2database:h2:1.4.200",
    )
  }

  override def mainClass = Some("com.wiwiwa.mill.spring.test.TestSpringBootApplication")
  override def millSourcePath = os.pwd
}
