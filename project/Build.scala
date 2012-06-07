import sbt._
import Keys._
import PlayProject._

import java.io.File

object ApplicationBuild extends Build {

    val appName         = "play-eschool"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "org.joda" % "joda-convert" % "1.2",
      "org.apache.poi" % "poi" % "3.8-beta5",
      "org.apache.poi" % "poi-ooxml" % "3.8-beta5",
      "org.datanucleus" % "datanucleus-core" % "3.0.5",
      "org.datanucleus" % "datanucleus-api-jdo" % "3.0.4",
      "org.datanucleus" % "datanucleus-enhancer" % "3.0.1",
      "org.datanucleus" % "datanucleus-jdo-query" % "3.0.1",
      "asm" % "asm" % "3.3.1",
      "javax.jdo" % "jdo-api" % "3.0",
      "org.datanucleus" % "datanucleus-rdbms" % "3.0.5",
      "org.datanucleus" % "datanucleus-jodatime" % "3.0.1",
      "com.h2database" % "h2" % "1.3.165",
      "org.scalatest" %% "scalatest" % "1.7.2" % "test"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
        ((testOptions in Test := Nil) +: Nucleus.settings): _*
    )

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
      
      // run returns an option of errormessage
      val result = run.run("org.datanucleus.enhancer.DataNucleusEnhancer", classpath, options, s.log)
      // if there is an errormessage, throw an exception
      result.foreach(sys.error)
    }
  )
  
  def findAllClassesRecursively(dir: File): Seq[File] = {
    if (dir.isDirectory) {
      dir.listFiles.flatMap(findAllClassesRecursively(_)) 
    } else if (dir.getName.endsWith(".class")) {
      Seq(dir)
    } else {
      Seq.empty
    }
  }
}
