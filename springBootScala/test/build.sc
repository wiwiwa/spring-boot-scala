import $ivy.`com.wiwiwa::mill-spring-boot:0.8`
import com.wiwiwa.springboot.SpringBootScalaModule
import mill._
import mill.scalalib._
import os.Path

object demo extends SpringBootScalaModule {
  def scalaVersion = "3.1.0"
  val springBootVersion = "2.6.1"

  override def ivyDeps = T{ super.ivyDeps() ++ Seq(
    ivy"com.lihaoyi::utest:0.7.10",
    ivy"org.springframework.boot:spring-boot-starter-web:$springBootVersion",
  ) }
  override def millSourcePath: Path = super.millSourcePath/os.up
}
