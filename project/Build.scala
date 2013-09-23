import sbt._
import Keys._
import play.Project._

import java.io.File

object ApplicationBuild extends Build {
  val appName = "schoolbox"
  val appVersion = "0.1"
  
  val commonDependencies = Seq(
      "org.apache.directory.studio" % "org.apache.commons.codec" % "1.8",
      "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
      "ch.qos.logback" % "logback-core" % "1.0.13",
      "ch.qos.logback" % "logback-classic" % "1.0.13",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.5",
      "com.google.inject" % "guice" % "3.0",
      "com.tzavellas" % "sse-guice" % "0.7.1",
      "com.scalatags" %% "scalatags" % "0.1.4",
      "org.dupontmanual" %% "dm-forms" % "0.1-SNAPSHOT",
      "org.dupontmanual" %% "scalajdo" % "0.1-SNAPSHOT",
      "org.scalatest" %% "scalatest" % "2.0.M5b",
      "org.postgresql" % "postgresql" % "9.2-1003-jdbc4"
  )

  val users = play.Project("users", appVersion, path = file("modules/users")).settings(
    scalaVersion := "2.10.2",
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-bootclasspath", "/usr/lib/jvm/java-6-oracle/jre/lib/rt.jar"),
    scalacOptions ++= Seq("-deprecation", "-feature"),
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",  
    libraryDependencies ++= commonDependencies)
      
  val courses = play.Project("courses", appVersion, path = file("modules/courses")).settings(
    scalaVersion := "2.10.2",
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-bootclasspath", "/usr/lib/jvm/java-6-oracle/jre/lib/rt.jar"),
    scalacOptions ++= Seq("-deprecation", "-feature"),
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"  
  ).dependsOn(users)

  val jsDependencies = Seq(
    "org.webjars" % "tinymce-jquery" % "3.4.9",
    "org.webjars" % "datatables" % "1.9.4-2",
    "org.webjars" % "datatables-bootstrap" % "2-20120201-1")

  val appDependencies = Seq(
    "org.scala-lang" % "scala-compiler" % "2.10.2",
    "org.joda" % "joda-convert" % "1.3.1",
    "org.apache.poi" % "poi" % "3.9",
    "org.apache.poi" % "poi-ooxml" % "3.9",
    "com.h2database" % "h2" % "1.3.172",
    "javax.jdo" % "jdo-api" % "3.0.1",
    "com.itextpdf" % "itextpdf" % "5.4.2",
    "org.tukaani" % "xz" % "1.3",
    "javax.mail" % "mail" % "1.4.7",
    "net.sourceforge.htmlunit" % "htmlunit" % "2.12" % "test",
    "org.seleniumhq.selenium" % "selenium-java" % "2.33.0" % "test") ++ jsDependencies

  def customLessEntryPoints(base: File): PathFinder = (base / "app" / "assets" / "stylesheets" * "*.less")

  val main = play.Project(appName, appVersion, appDependencies).settings(
      (Seq(parallelExecution in Test := false,
          testOptions in Test += Tests.Argument("-oDF"),
          scalaVersion := "2.10.2",
          javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-bootclasspath", "/usr/lib/jvm/java-6-oracle/jre/lib/rt.jar"),
          scalacOptions ++= Seq("-deprecation", "-feature"),
          routesImport += "scala.language.reflectiveCalls",
          resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
          lessEntryPoints <<= baseDirectory(customLessEntryPoints)) ++
      Nucleus.settings): _*).dependsOn(users, courses)
}

object Nucleus {
  // defines our own ivy config that wont get packaged as part of your app
  // notice that it extends the Compile scope, so we inherit that classpath
  val Config = config("nucleus") extend Compile

  // our task
  val enhance = TaskKey[Unit]("enhance")

  val settings: Seq[Project.Setting[_]] = Seq(
    ivyConfigurations += Config,
    enhance <<= Seq(compile in Compile).dependOn,
    enhance in Config <<= (fullClasspath in Test, runner, streams) map { (cp, processRunner, str) =>
      val options = Seq("-v", "-pu", "schoolbox")
      val result = processRunner.run("org.datanucleus.enhancer.DataNucleusEnhancer", cp.files, options, str.log)
      result.foreach(sys.error)
    })
      
  /*def enhanceClasses(runner: ScalaRun, classpath: Seq[File], classes: File, streams: TaskStreams) = {
    val options = Seq("-v") ++ findAllClassesRecursively(classes).map(_.getAbsolutePath)
    val result = runner.run("org.datanucleus.enhancer.DataNucleusEnhancer", classpath, options, streams.log)
    result.foreach(sys.error)
  }
      
  def findAllClassesRecursively(dir: File): Seq[File] = {
    if (dir.isDirectory) {
      val files = dir.listFiles
      files.flatMap(findAllClassesRecursively(_))
    } else if (dir.getName.endsWith(".class")) {
      Seq(dir)
    } else {
      Seq.empty
    }
  }*/
}
