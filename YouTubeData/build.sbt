name := "YouTubeData"

version := "1.0"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.5"


resolvers ++= Seq(
  "apache-snapshots" at "http://repository.apache.org/snapshots/"
  
)

libraryDependencies ++= Seq(
  
  "org.joda" % "joda-convert" % "1.8",
  "joda-time" % "joda-time" % "2.9.3",
  "com.google.apis" % "google-api-services-youtube" % "v3-rev174-1.22.0"
)
