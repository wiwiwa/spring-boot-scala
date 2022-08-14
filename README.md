# mill-spring-boot
A Mill plugin that project the following modules:

## ScalaAppModule
`ScalaAppModule` is a mill plugin which:
* set default scala version to `3.x.x`
* Auto naming artifact to be published
  * Auto increase version number when building jar or publishing artifacts
  * Publish project `AbcDef` as artifact `abc-def`
* Add mill command `showUpdates` to list upgradable dependencies

## SpringBootScalaModule
`SpringBootScalaModule` is a mill plugin which:
* has all features provided by `ScalaAppModule`
* Build Spring Boot application as a fat Jar using `mill`
* Add the `spring-boot-scala-lib` artifact to `ivyDeps`, which
  * Enable Scala class properties and class getter/setter as Spring bean properties
* Provides trait `Tests` and `SpringBootTests`. Both of them can be used as base class of test module, which:
  * Add artifact `spring-boot-test` to `ivyDeps`, which
    * provide class `MockSpringBoot`, which
      * `get()`, `post()`: mocked http methods, which return mocked response
      * Mocked response has method:
        * `json<T>`: parse response as String, Integer, Array, and Map
        * `assertJson(path)`: assert value at json path `path` to be truthy
        * `assertJson(path, value)`: assert value at json path `path` equals `value`

# Usage

In your `build.sc`:
```scala
import $ivy.`com.wiwiwa::millSpringBoot:x.xx`, com.wiwiwa.springboot.SpringBootScalaModule
object mySpringApp extends SpringBootScalaModule {
  object test extends Tests with TestModule.Utest
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
# to publish locally for testing
$ ./mill _.publishLocal
# to release to official maven repository
$ SONATYPE_USER='<sontype-user>'
$ read -s SONATYPE_PASSWORD # input password without echo
$ for PROJECT in millSpringBoot springBootScala springBootTest; do
  ./mill -i millSpringBoot.publish --sonatypeCreds "$SONATYPE_USER:$SONATYPE_PASSWORD" --release true
done
```
