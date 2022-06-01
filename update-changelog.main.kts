#!/usr/bin/env kotlin
@file:JvmName("ChangelogScript")
@file:CompilerOptions("-jvm-target", "11")
@file:Repository("https://repo.maven.apache.org/maven2")

val versionFile = File("/module-one/publish.properties")
val properties = Properties()
properties.load(FileInputStream(versionFile))
val version = properties["version"].toString()
println("version: $version")

if (version.contains("suffix")) {
    //not for release
    println("Not release")
} else {
    //should be released
    println("release")
}