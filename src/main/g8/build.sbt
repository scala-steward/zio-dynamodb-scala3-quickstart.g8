val zioVersion            = "2.0.21"
val zioJsonVersion        = "0.6.2"
val zioConfigVersion      = "4.0.1"
val zioLoggingVersion     = "2.1.16"
val logbackClassicVersion = "1.4.14"
val awsSdk                = "1.11.479"
val testContainersVersion = "0.40.15"
val zioMockVersion        = "1.0.0-RC12"
val zioHttpVersion        = "3.0.0-RC4"
val zioDynamodb           = "0.2.13"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        organization := "$organization$",
        scalaVersion := "$scala_version$",
      )
    ),
    name           := "$name$",
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio-dynamodb"        % zioDynamodb,
      "dev.zio"       %% "zio"                 % zioVersion,
      "dev.zio"       %% "zio-streams"         % zioVersion,
      "dev.zio"       %% "zio-http"            % zioHttpVersion,
      "dev.zio"       %% "zio-config"          % zioConfigVersion,
      "dev.zio"       %% "zio-config-typesafe" % zioConfigVersion,
      "ch.qos.logback" % "logback-classic"     % logbackClassicVersion,
      "dev.zio"       %% "zio-json"            % zioJsonVersion,

      // logging
      "dev.zio"       %% "zio-logging"       % zioLoggingVersion,
      "dev.zio"       %% "zio-logging-slf4j" % zioLoggingVersion,
      "ch.qos.logback" % "logback-classic"   % logbackClassicVersion,

      // test
      "dev.zio"      %% "zio-test"                      % zioVersion            % Test,
      "dev.zio"      %% "zio-test-sbt"                  % zioVersion            % Test,
      "dev.zio"      %% "zio-test-junit"                % zioVersion            % Test,
      "dev.zio"      %% "zio-mock"                      % zioMockVersion        % Test,
      "com.dimafeng" %% "testcontainers-scala-dynalite" % testContainersVersion % Test,
      "com.amazonaws" % "aws-java-sdk-dynamodb"         % awsSdk                % Test,
      "dev.zio"      %% "zio-test-magnolia"             % zioVersion            % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(JavaAppPackaging)
