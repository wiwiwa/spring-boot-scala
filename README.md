# SpringBootScalaModule

`SpringBootScalaModule` is a mill plugin which:
* Build Spring Boot application as a fat Jar using `mill`
* Auto increase version number when building jar or publishing artifacts
* Add the `spring-boot-scala-lib` artifact to `ivyDeps`, which
  * Enable Scala class properties and class getter/setter as Spring bean properties

# Development

```bash
## To Test
# build a fat jar
$ ./mill millSpringBoot.test
# run fat jar. This will start the test Spring application
$  java -jar millPlugin/test/out/springTest/assembly/overriden/mill/scalalib/JavaModule/assembly/dest/out.jar
```
