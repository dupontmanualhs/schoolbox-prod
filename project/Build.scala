import sbt._
import Keys._
import play.Project._

import java.io.File

object ApplicationBuild extends Build {

    val appName         = "play-eschool"
    val appVersion      = "1.0"

    val jsDependencies = Seq(
      "org.webjars" %% "webjars-play" % "2.1.0-2",
      "org.webjars" % "jquery" % "2.0.0",
      "org.webjars" % "bootstrap" % "2.3.2",
      "org.webjars" % "tinymce-jquery" % "3.4.9",
      "org.webjars" % "jquery-ui" % "1.10.2-1"
    )
      
    val appDependencies = Seq(
      "org.scala-lang" % "scala-compiler" % "2.10.2",            
      "org.joda" % "joda-convert" % "1.3.1",
      "org.apache.poi" % "poi" % "3.9",
      "org.apache.poi" % "poi-ooxml" % "3.9",
      "org.datanucleus" % "datanucleus-core" % "3.2.3",
      "org.datanucleus" % "datanucleus-api-jdo" % "3.2.2",
      "org.datanucleus" % "datanucleus-enhancer" % "3.1.1",
      "org.datanucleus" % "datanucleus-jdo-query" % "3.0.2",
      "org.datanucleus" % "datanucleus-rdbms" % "3.2.2",
      "com.h2database" % "h2" % "1.3.172",
      "javax.jdo" % "jdo-api" % "3.0.1",
      "log4j" % "log4j" % "1.2.17",
      "org.scalatest" %% "scalatest" % "1.9.1" % "test",
      "com.itextpdf" % "itextpdf" % "5.4.2",
      "org.tukaani" % "xz" % "1.3",
      "javax.mail" % "mail" % "1.4.7",
      "org.seleniumhq.selenium" % "selenium-firefox-driver" % "2.33.0" % "test",
      "org.seleniumhq.selenium" % "selenium-chrome-driver" % "2.33.0" % "test",
      "org.seleniumhq.selenium" % "selenium-htmlunit-driver" % "2.33.0" % "test"
    ) ++ jsDependencies
    
    System.setProperty("log4j.configuration", "file:conf/log4j.properties")

    val main = play.Project(appName, appVersion, appDependencies).settings(
      ((testOptions in Test := Nil) +:
       (scalaVersion := "2.10.2") +:
       (javacOptions ++= Seq("-source", "1.6", "-target", "1.6")) +:
       (scalacOptions ++= Seq("-deprecation", "-feature")) +:
       Nucleus.settings): _*
    ) dependsOn RootProject( uri("git://github.com/toddobryan/scalajdo.git") )
}

object Nucleus {
  // defines our own ivy config that wont get packaged as part of your app
  // notice that it extends the Compile scope, so we inherit that classpath
  val Config = config("nucleus") extend Compile

  // our task
  val enhance = TaskKey[Unit]("enhance")
  
  // implementation
  val settings:Seq[Project.Setting[_]] = Seq(
    // let ivy know about our "nucleus" config
    ivyConfigurations += Config,
    // add the enhancer dependency to our nucleus ivy config
    libraryDependencies += "org.datanucleus" % "datanucleus-enhancer" % "3.0.1" % Config.name,
    // fetch the classpath for our nucleus config
    // as we inherit Compile this will be the fullClasspath for Compile + "datanucleus-enhancer" jar 
    //fullClasspath in Config <<= (classpathTypes in enhance, update).map{(ct, report) =>
    //  Classpaths.managedJars(Config, ct, report)
    //},
    // add more parameters as your see fit
    //enhance in Config <<= (fullClasspath in Config, runner, streams).map{(cp, run, s) =>
    enhance <<= Seq(compile in Compile).dependOn,
    enhance in Config <<= (dependencyClasspath in Compile, classDirectory in Compile, runner, streams)
        map { (deps, classes, run, s) => 

      // Properties
      val classpath = (deps.files :+ classes)
      
      
      // the classpath is attributed, we only want the files
      //val classpath = cp.files
      // the options passed to the Enhancer... 
      val options = Seq("-v") ++ findAllClassesRecursively(classes).map(_.getAbsolutePath)
      Thread.sleep(1000)
      
      // run returns an option of errormessage
      val result = run.run("org.datanucleus.enhancer.DataNucleusEnhancer", classpath, options, s.log)
      // if there is an errormessage, throw an exception
      result.foreach(sys.error)
    }
  )
  
  def findAllClassesRecursively(dir: File): Seq[File] = {
    if (dir.isDirectory) {
      val files = dir.listFiles
      files.flatMap(findAllClassesRecursively(_)) 
    } else if (dir.getName.endsWith(".class")) {
      Seq(dir)
    } else {
      Seq.empty
    }
  }
}
