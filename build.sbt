scalaVersion := "3.8.4"

lazy val demoClean = (project in file("DemoClean"))
  .settings(
    name := "demo-clean"
  )

lazy val demoMessy = (project in file("DemoMessy"))
  .settings(
    name := "demo-messy"
  )

lazy val root = rootProject
  .aggregate(demoClean, demoMessy)
  .settings(
    name := "Demo-ROP-FC-IS"
  )
