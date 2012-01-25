import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play-eschool"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "org.datanucleus" % "datanucleus-core" % "3.0.5",
      "org.datanucleus" % "datanucleus-api-jdo" % "3.0.4",
      "org.datanucleus" % "datanucleus-enhancer" % "3.0.1",
      "org.datanucleus" % "datanucleus-jdo-query" % "3.0.1",
      "asm" % "asm" % "3.3.1",
      "javax.jdo" % "jdo-api" % "3.0",
      "org.datanucleus" % "datanucleus-rdbms" % "3.0.5",
      "org.datanucleus" % "datanucleus-jodatime" % "3.0.1",
      "com.h2database" % "h2" % "1.3.160"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
