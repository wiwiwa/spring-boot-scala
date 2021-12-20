# SpringBootScalaModule

`SpringBootScalaModule` is a mill plugin which:
* Build Spring Boot application as a fat Jar using `mill`
* Auto increase version number when building jar or publishing artifacts
* Add the `spring-boot-scala-lib` artifact to `ivyDeps`, which
  * Enable Scala class properties and class getter/setter as Spring bean properties

# Usage

In your `build.sc`:
```scala
import $ivy.`com.wiwiwa::millSpringBoot:0.1`, com.wiwiwa.springboot.SpringBootScalaModule
object springTest extends SpringBootScalaModule {
  override def scalaVersion = "3.1.0"
  ...
}
```

# Development

```bash
# To generate Idea project files
./millw mill.scalalib.GenIdea/idea

# To test
$ ./millw millSpringBoot.test
# To test building and running a Spring Boot fat jar
$ ./millw show millSpringBoot.testJar.assembly
$ java -jar out/millSpringBoot/testJar/assembly/overriden/mill/scalalib/JavaModule/assembly/dest/out.jar
```
