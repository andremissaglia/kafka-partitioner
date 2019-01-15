
name := "kafka-partitioner"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "2.1.0",
  "org.apache.kafka" %% "kafka-streams-scala" % "2.1.0",
  "com.jayway.jsonpath" % "json-path" % "2.4.0",
  "org.slf4j" % "slf4j-log4j12" % "1.7.25"
)

