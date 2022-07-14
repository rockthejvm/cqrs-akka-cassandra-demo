scalaVersion    := "2.13.4"
name := "cqrs-cassandra-live"

lazy val akkaHttpVersion = "10.2.8"
lazy val akkaVersion     = "2.6.18"
lazy val circeVersion    = "0.14.1"
lazy val akkaHttpJsonSerializersVersion = "1.39.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"                  % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed"           % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"                % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed"     % akkaVersion,
  "com.datastax.oss"  %  "java-driver-core"           % "4.13.0",   // See https://github.com/akka/alpakka/issues/2556
  "com.typesafe.akka" %% "akka-persistence-cassandra" % "1.0.5",
  "io.circe"          %% "circe-core"                 % circeVersion,
  "io.circe"          %% "circe-generic"              % circeVersion,
  "io.circe"          %% "circe-parser"               % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe"            % akkaHttpJsonSerializersVersion,
  "de.heikoseeberger" %% "akka-http-jackson"          % akkaHttpJsonSerializersVersion,
  "ch.qos.logback"    % "logback-classic"             % "1.2.10",

  // projections tutorial
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
  "com.lightbend.akka" %% "akka-projection-core" % "1.2.4",
  "com.lightbend.akka" %% "akka-projection-eventsourced" % "1.2.4",
  "com.lightbend.akka" %% "akka-projection-cassandra" % "1.2.4"

)
