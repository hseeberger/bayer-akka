// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization := "rocks.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear := Some(2021),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := "3.0.1",
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-rewrite",
      "-indent",
      "-pagewidth",
      "100",
      "-source",
      "future",
      "-Xfatal-warnings",
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    scalafmtOnCompile := true,
    dynverSeparator := "_", // the default `+` is not compatible with docker tags
  )
)

// *****************************************************************************
// Projects
// *****************************************************************************

lazy val `bayer-akka` =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, DockerPlugin, JavaAppPackaging)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp.cross(CrossVersion.for3Use2_13),
        library.akkaHttp2Support.cross(CrossVersion.for3Use2_13),
        library.akkaHttpSprayJson.cross(CrossVersion.for3Use2_13),
        library.akkaStreamTyped.cross(CrossVersion.for3Use2_13),
      ),
      // Docker settings
      dockerBaseImage := "adoptopenjdk:11-jre-hotspot",
      dockerRepository := Some("hseeberger"),
      dockerExposedPorts := Seq(8080),
      Docker / maintainer := organizationName.value,
      // Publish settings
      Compile / packageDoc / publishArtifact := false, // speed up building Docker images
      Compile / packageSrc / publishArtifact := false, // speed up building Docker images
    )

// *****************************************************************************
// Project settings
// *****************************************************************************

lazy val commonSettings =
  Seq(
    // Also (automatically) format build definition together with sources
    Compile / scalafmt := {
      val _ = (Compile / scalafmtSbt).value
      (Compile / scalafmt).value
    },
  )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka     = "2.6.15"
      val akkaHttp = "10.2.5"
    }
    val akkaHttp          = "com.typesafe.akka" %% "akka-http"            % Version.akkaHttp
    val akkaHttp2Support  = "com.typesafe.akka" %% "akka-http2-support"   % Version.akkaHttp
    val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttp
    val akkaStreamTyped   = "com.typesafe.akka" %% "akka-stream-typed"    % Version.akka
  }
