// *****************************************************************************
// Build settings
// *****************************************************************************

inThisBuild(
  Seq(
    organization := "rocks.heikoseeberger",
    organizationName := "Heiko Seeberger",
    startYear := Some(2021),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    scalaVersion := "3.0.1-RC2",
    scalacOptions ++= Seq(
      "-deprecation",
      "-unchecked",
      "-new-syntax",
      "-rewrite",
      "-pagewidth",
      "100",
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

lazy val bayer =
  project
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp.cross(CrossVersion.for3Use2_13),
        library.akkaHttp2Support.cross(CrossVersion.for3Use2_13),
        library.akkaHttpSprayJson.cross(CrossVersion.for3Use2_13),
        library.akkaMgmt.cross(CrossVersion.for3Use2_13),
        library.akkaSlf4j.cross(CrossVersion.for3Use2_13),
        library.akkaStreamTyped.cross(CrossVersion.for3Use2_13),
        library.disruptor,
        library.log4jCore,
        library.log4jSlf4j,
        library.slf4s,
        library.munit           % Test,
        library.munitScalaCheck % Test,
      ),
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
      val akka      = "2.6.15"
      val akkaHttp  = "10.2.4"
      val akkaMgmt  = "1.1.0"
      val munit     = "0.7.26"
      val disruptor = "3.4.4"
      val log4j     = "2.14.1"
      val slf4s     = "0.2.0"
    }
    val akkaDiscovery     = "com.typesafe.akka" %% "akka-discovery"       % Version.akka
    val akkaHttp          = "com.typesafe.akka" %% "akka-http"            % Version.akkaHttp
    val akkaHttp2Support  = "com.typesafe.akka" %% "akka-http2-support"   % Version.akkaHttp
    val akkaHttpSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttp
    val akkaMgmt        = "com.lightbend.akka.management" %% "akka-management"   % Version.akkaMgmt
    val akkaSlf4j       = "com.typesafe.akka"             %% "akka-slf4j"        % Version.akka
    val akkaStreamTyped = "com.typesafe.akka"             %% "akka-stream-typed" % Version.akka
    val disruptor       = "com.lmax"                       % "disruptor"         % Version.disruptor
    val log4jCore       = "org.apache.logging.log4j"       % "log4j-core"        % Version.log4j
    val log4jSlf4j      = "org.apache.logging.log4j"       % "log4j-slf4j-impl"  % Version.log4j
    val munit           = "org.scalameta"                 %% "munit"             % Version.munit
    val munitScalaCheck = "org.scalameta"                 %% "munit-scalacheck"  % Version.munit
    val slf4s           = "rocks.heikoseeberger"          %% "slf4s"             % Version.slf4s
  }
