name := "awb-generator"
version := "2.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayJava)

scalaVersion := "2.11.12"

libraryDependencies ++= Seq(
  javaWs,
  "org.projectlombok"                % "lombok"                                  % "1.16.14",
  "com.itextpdf"                     % "itextpdf"                                % "5.5.6",
  "com.itextpdf.tool"                % "xmlworker"                               % "5.5.11"
)