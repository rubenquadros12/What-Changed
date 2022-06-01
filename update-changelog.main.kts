#!/usr/bin/env kotlin
@file:JvmName("ChangelogScript")
@file:CompilerOptions("-jvm-target", "11")
@file:Repository("https://repo.maven.apache.org/maven2")

import java.io.*
import java.util.*

val prDetails = getPRDetails()
val isRelease = isReleaseBuild()

fun getPRDetails(): PRDetails {
    val message = "github.event.head_commit.message".runCommandWithRedirect()
    val prNumber = "github.event.number".runCommandWithRedirect()

    println("message: $message, prNumber: $prNumber")

    return PRDetails(message = message, number = prNumber)
}

fun isReleaseBuild(): Boolean {
    val versionFile = File("module-one/publish.properties")
    val properties = Properties()
    properties.load(FileInputStream(versionFile))
    val version = properties["version"].toString()
    println("version: $version")

    return if (version.contains("suffix")) {
        //not for release
        println("Not release")
        true
    } else {
        //should be released
        println("release")
        false
    }
}

fun writeChangeLog() {

}

fun String.runCommandWithRedirect(dir: File? = null) =
    ProcessBuilder("/bin/sh", "-c", this)
        .redirectErrorStream(true)
        .inheritIO()
        .directory(dir)
        .start()
        .waitFor()

data class PRDetails(
    val message: Int,
    val number: Int
)