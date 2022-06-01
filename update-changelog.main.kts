#!/usr/bin/env kotlin
@file:JvmName("ChangelogScript")
@file:CompilerOptions("-jvm-target", "11")
@file:Repository("https://repo.maven.apache.org/maven2")

import java.io.*
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//get commit details
val prDetails = getPRDetails()

//get current version details
val versionInfo = getVersionDetails()

//write to CHANGELOG.md
writeChangeLog(versionDetails = versionInfo, prDetails = prDetails)

fun getPRDetails(): PRDetails {
    val message = "git log -1 --pretty=%B".runCommand()
    val commitSha = "git log --pretty=format:%h -1".runCommand()

    println("message: $message, sha: $commitSha")

    return PRDetails(message = message, sha = commitSha)
}

fun getVersionDetails(): VersionDetails {
    val versionFile = File("module-one/publish.properties")
    val properties = Properties()
    properties.load(FileInputStream(versionFile))
    val version = properties["version"].toString()
    val isRelease = version.contains("suffix").not()

    return VersionDetails(version = version, isRelease = isRelease)
}

fun writeChangeLog(versionDetails: VersionDetails, prDetails: PRDetails) {

    fun hasUnreleasedChanges(firstLine: String?): Boolean = firstLine?.contains("Upcoming") == true

    fun today(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-YYYY")
        return current.format(formatter)
    }

    val file = File("module-one/CHANGELOG.md")
    val firstLine = file.useLines { it.firstOrNull() }

    if (versionDetails.isRelease) {
        //check if un-released changes are present
        if (hasUnreleasedChanges(firstLine)) {
            //make it release

        } else {
            //make new release
            val fileAccess = RandomAccessFile(file, "rw")
            fileAccess.seek(0)
            fileAccess.write("### v${versionDetails.version} | ${today()}".toByteArray())
            fileAccess.write("\n".toByteArray())
            fileAccess.write("${prDetails.message} (${prDetails.sha})".toByteArray())

            fileAccess.close()
        }

        //add current commit details
    } else {
        //check if un-released changes are present
        if (hasUnreleasedChanges(firstLine)) {
            //add to second line

        } else {

        }
    }
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
    val sha: String
)

data class VersionDetails(
    val version: String,
    val isRelease: Boolean
)