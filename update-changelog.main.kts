#!/usr/bin/env kotlin
@file:JvmName("ChangelogScript")
@file:CompilerOptions("-jvm-target", "11")
@file:Repository("https://repo.maven.apache.org/maven2")

import java.io.*
import java.util.*

val prDetails = getPRDetails()
val isRelease = isReleaseBuild()

fun getPRDetails(): PRDetails {
    val message = "github.event.head_commit.message".runCommand()
    val prNumber = "github.event.number".runCommand()

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


fun String.runCommand(dir: File? = null): String {
    val parts = this.split("\\s".toRegex())
    val proc: Process = ProcessBuilder(*parts.toTypedArray())
        .directory(dir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    proc.waitFor()

    return proc.inputStream.bufferedReader().readText().trim()
}

data class PRDetails(
    val message: String,
    val number: String
)