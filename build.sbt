name := "hw3"

version := "0.1"

scalaVersion := "2.12.7"

resolvers := Seq("oss.sonatype.org" at "https://oss.sonatype.org/content/repositories/snapshots/")

libraryDependencies += "org.lwjgl" % "lwjgl" % "3.1.3"

libraryDependencies += "org.lwjgl" % "lwjgl-opengl" % "3.1.3"

libraryDependencies += "org.lwjgl" % "lwjgl-opengles" % "3.1.3"

libraryDependencies += "org.lwjgl" % "lwjgl-stb" % "3.1.3"

libraryDependencies += "org.lwjgl" % "lwjgl-glfw" % "3.1.3"

libraryDependencies += "org.joml" % "joml-camera" % "1.2.0-SNAPSHOT"

libraryDependencies += "org.lwjgl" % "lwjgl-jemalloc" % "3.1.3"

libraryDependencies += "org.lwjgl" % "lwjgl-openal" % "3.1.3"


libraryDependencies += "org.lwjgl" % "lwjgl" % "3.1.3" classifier "natives-linux" classifier "natives-macos" classifier "natives-windows"

libraryDependencies += "org.lwjgl" % "lwjgl-opengl" % "3.1.3" classifier "natives-linux" classifier "natives-macos" classifier "natives-windows"

libraryDependencies += "org.lwjgl" % "lwjgl-opengles" % "3.1.3" classifier "natives-linux" classifier "natives-macos" classifier "natives-windows"

libraryDependencies += "org.lwjgl" % "lwjgl-stb" % "3.1.3" classifier "natives-linux" classifier "natives-macos" classifier "natives-windows"

libraryDependencies += "org.lwjgl" % "lwjgl-glfw" % "3.1.3" classifier "natives-linux" classifier "natives-macos" classifier "natives-windows"

libraryDependencies += "org.lwjgl" % "lwjgl-jemalloc" % "3.1.3" classifier "natives-linux" classifier "natives-macos" classifier "natives-windows"

libraryDependencies += "org.lwjgl" % "lwjgl-openal" % "3.1.3" classifier "natives-linux" classifier "natives-macos" classifier "natives-windows"

mainClass in (Compile, run) := Some("scene.Main")
mainClass in assembly := Some("scene.Main")