name := "user-api"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.2"

sbtPlugin := true

organization := "com.isuwang"

resolvers += Resolver.mavenLocal

lazy val commonSettings = Seq(
  organization := "com.isuwang",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.2"
)

lazy val api = (project in file("user-api-api"))
  .settings(
    commonSettings,
    name := "user-api-api",
    libraryDependencies ++= Seq(
      "com.isuwang" % "dapeng-remoting-api" % "1.3.0",
      "com.isuwang" % "dapeng-remoting-netty" % "1.3.0",
      "com.isuwang" % "dapeng-registry-zookeeper" % "1.3.0"
    )
  ).enablePlugins(ThriftGeneratorPlugin)


lazy val service = (project in file("user-api-service"))
  .dependsOn( api )
  .settings(
    commonSettings,
    name := "user-api_service",
    libraryDependencies ++= Seq(
      "com.isuwang" % "dapeng-spring" % "1.3.0",
      "com.github.wangzaixiang" %% "scala-sql" % "2.0.0",
      "org.slf4j" % "slf4j-api" % "1.7.13",
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "ch.qos.logback" % "logback-core" % "1.1.3",
      "org.codehaus.janino" % "janino" % "2.7.8", //logback (use if condition in logBack config file need this dependency)
      "mysql" % "mysql-connector-java" % "5.1.36",
      "com.alibaba" % "druid" % "1.0.17",
      "org.springframework" % "spring-context" % "4.2.4.RELEASE",
      "com.isuwang" % "dapeng-registry-zookeeper" % "1.3.0",
      "com.isuwang" % "dapeng-remoting-netty" % "1.3.0"
    )).enablePlugins(ImageGeneratorPlugin)
    .enablePlugins(DbGeneratorPlugin)
  .enablePlugins(RunContainerPlugin)