# SpringBootScalaModule

`SpringBootScalaModule` is a mill plugin which:
* Build Spring Boot application as a fat Jar using `mill`
* Auto naming artifact to be publshed
  * Auto increase version number when building jar or publishing artifacts
  * Publish project `AbcDef` as artifact `abc-def`
* Add the `spring-boot-scala-lib` artifact to `ivyDeps`, which
  * Enable Scala class properties and class getter/setter as Spring bean properties

# Usage

In your `build.sc`:
```scala
import $ivy.`com.wiwiwa::millSpringBoot:0.11`, com.wiwiwa.springboot.SpringBootScalaModule
object springTest extends SpringBootScalaModule {
  override def scalaVersion = "3.1.0"
  ...
}
```

# Development

```bash
# To generate Idea project files
./mill mill.scalalib.GenIdea/idea

# To test
$ ./mill millSpringBoot.test
# To test building and running a Spring Boot fat jar
$ ./mill show millSpringBoot.testJar.assembly
$ java -jar out/millSpringBoot/testJar/assembly/overriden/mill/scalalib/JavaModule/assembly/dest/out.jar
# To run demo
$ cd springBootScala/test
$ ../../mill demo
# to release
$ ./mill _.publishLocal
```
