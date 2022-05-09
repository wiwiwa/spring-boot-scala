package com.wiwiwa.springboot

import com.wiwiwa.mill.ScalaAppModule
import mill._
import mill.modules.Jvm
import mill.scalalib.DepSyntax

import java.io.{ByteArrayInputStream, File, FileInputStream, FileOutputStream, InputStream}
import java.util.zip.{CRC32, ZipEntry, ZipFile, ZipOutputStream}
import scala.collection.immutable.HashSet
import scala.jdk.CollectionConverters._
import scala.util.Using

/**
 * SpringBootScalaModule enables Scala support for Spring Boot framework
 * With this module, the following features are enabled:
 * + mill `assembly` command can build a Spring Boot fat jar
 * + Scala class can be used as Spring beans
 */
trait SpringBootScalaModule extends ScalaAppModule {
  override def mandatoryIvyDeps = T{
    val springBootScalaVersion = classOf[SpringBootScalaModule].getPackage.getImplementationVersion match {
      case null => "SNAPSHOT"
      case v => v
    }
    super.mandatoryIvyDeps() ++ Agg(
      ivy"com.wiwiwa::spring-boot-scala:$springBootScalaVersion",
    )
  }
  override def javacOptions = Seq("-parameters")

  override def assembly = T {
    val jarPath = T.workspace / "out" / s"${artifactId()}-${publishVersion()}.jar"
    Using.resource(new BootJar(jarPath.toIO)){ jar=>
      //write MANIFEST
      val manifest = s"""Manifest-Version: 1.0,
                       |Implementation-Version: ${publishVersion()}
                       |Main-Class: org.springframework.boot.loader.JarLauncher
                       |Start-Class: ${finalMainClass()}
                       |Spring-Boot-Version: ${compileSpringBootVersion()}
                       |Spring-Boot-Classes: BOOT-INF/classes/
                       |Spring-Boot-Lib: BOOT-INF/lib/
                       |Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
                       |Spring-Boot-Layers-Index: BOOT-INF/layers.idx
                       |""".stripMargin
      jar.save("META-INF/MANIFEST.MF", manifest)
      //write spring-boot-loader
      val loaderJar = new ZipFile( resolveDeps(ivySpringBootLoader)().iterator.next.path.toIO )
      Using.resource(loaderJar) { _=>
        loaderJar.entries.asScala
          .filter(!_.getName.startsWith("META-INF/"))
          .filter(!_.getName.endsWith("/"))
          .foreach{ e=> Using.resource(loaderJar.getInputStream(e)) {
            jar.save(e, _)
          } }
      }
      //write ivy libs
      val layerTools = resolveDeps(ivySpringBootJarmodeLayerTools)()
      (resolvedIvyDeps() ++ layerTools).iterator
        .map(_.path.toIO)
        .foreach{ f=> jar.save(s"BOOT-INF/lib/${f.getName}", f) }
      //write class files
      localClasspath().map(_.path.toIO)
        .foreach{ f=> jar.save(s"BOOT-INF/${f.getName}", f) }
      //write idx files
      val classpath = resolvedIvyDeps().iterator
        .map(_.path.toIO.getName).map{s=>s"- \"BOOT-INF/lib/$s\""}
        .mkString("\n") + "\n"
      jar.save("BOOT-INF/classpath.idx", classpath)
      val layerIdx = """- "dependencies":
                       |  - "BOOT-INF/lib/"
                       |- "spring-boot-loader":
                       |  - "org/"
                       |- "snapshot-dependencies":
                       |- "application":
                       |  - "BOOT-INF/classes/"
                       |  - "BOOT-INF/classpath.idx"
                       |  - "BOOT-INF/layers.idx"
                       |  - "META-INF/"
                       |""".stripMargin
      jar.save("BOOT-INF/layers.idx", layerIdx)
    }
    println(s"Built Spring Boot jar file $jarPath")
    PathRef(jarPath)
  }
  def ivySpringBootLoader = T{ Agg(ivy"org.springframework.boot:spring-boot-loader:${compileSpringBootVersion()}") }
  def ivySpringBootJarmodeLayerTools = T{ Agg(ivy"org.springframework.boot:spring-boot-jarmode-layertools:${compileSpringBootVersion()}") }
  def compileSpringBootVersion = T{
    val depToJavaDep = resolveCoursierDependency().apply(_)
    val deps = transitiveIvyDeps()
    val (_, resolution) = Jvm.resolveDependenciesMetadata(
      repositoriesTask(),
      deps.map(depToJavaDep),
      deps.filter(!_.force).map(depToJavaDep),
      None, None)
    resolution.dependencies
      .find{d=> d.module.organization.value=="org.springframework.boot" && d.module.name.value=="spring-boot"}
      .map(_.version).get
  }
  override def artifactSuffix = ""

  trait Tests extends ScalaModuleTests {
    override def ivyDeps = {
      val springBootScalaVersion = classOf[SpringBootScalaModule].getPackage.getImplementationVersion match {
        case null => "SNAPSHOT"
        case v => v
      }
      Agg( ivy"com.wiwiwa::spring-boot-test:$springBootScalaVersion" )
    }
  }
}

class BootJar(zip:ZipOutputStream) extends AutoCloseable{
  var directories = HashSet.empty[String]

  def this(file:File) = this{ new ZipOutputStream(new FileOutputStream(file)) }

  def save(entry:ZipEntry, data:InputStream): Unit = {
    def saveParentDir(entryPath:String): Unit = {
      val parent = entryPath.lastIndexOf('/', entryPath.length-2) match {
        case -1 => return
        case pos => entryPath.substring(0,pos+1)
      }
      if(directories.contains(parent))
        return
      saveParentDir(parent)
      directories += parent
      zip.putNextEntry(new ZipEntry(parent))
    }
    saveParentDir(entry.getName)
    zip.putNextEntry(entry)
    data.transferTo(zip)
  }
  def save(entryPath:String, file:File): Unit = {
    if(!file.exists()) return
    else if(file.isDirectory)
      file.listFiles.iterator.foreach{ f=>
        save(entryPath+"/"+f.getName, f)
      }
    else {
      val crc = Using.resource(new FileInputStream(file)) { is =>
        val crc32 = new CRC32()
        val buffer = new Array[Byte](32*1024)
        def update(): Long = is.read(buffer) match {
          case -1 => crc32.getValue
          case n =>
            crc32.update(buffer,0,n)
            update()
        }
        update()
      }
      Using.resource(new FileInputStream(file)) { is=>
        val e = new ZipEntry(entryPath)
        e.setMethod(ZipEntry.STORED)
        e.setSize(file.length)
        e.setTime(file.lastModified)
        e.setCrc(crc)
        save(e, is)
      }
    }
  }
  def save(entryPath:String, data:String): Unit = {
    val is = new ByteArrayInputStream(data.getBytes("utf-8"))
    save(new ZipEntry(entryPath), is)
  }

  override def close() = zip.close()
}
