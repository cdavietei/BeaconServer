lazy val root = (project in file(".")).
  settings(
    name := "Beacon",
    libraryDependencies += "org.mongodb" % "mongodb-driver" % "3.2.2"
  )
