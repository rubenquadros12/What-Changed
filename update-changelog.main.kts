#!/usr/bin/env kotlin
@file:JvmName("ChangelogScript")
@file:CompilerOptions("-jvm-target", "11")
@file:Repository("https://repo.maven.apache.org/maven2")

import java.io.*
import java.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jetbrains.kotlin.org.apache.commons.io.IOUtils

//get commit details
val prDetails = getPRDetails()

//get current version details
val versionInfo = getVersionDetails()

//write to CHANGELOG.md
writeChangeLog()

fun getPRDetails(): PRDetails {
    val message = "git log -1 --pretty=%B --first-parent".runCommand()
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

fun writeChangeLog() {

    fun hasUnreleasedChanges(firstLine: String?): Boolean = firstLine?.contains("Upcoming") == true

    val file = File("module-one/CHANGELOG.md")
    val firstLine = file.useLines { it.firstOrNull() }

    if (versionInfo.isRelease) {
        //check if un-released changes are present
        if (hasUnreleasedChanges(firstLine)) {
            //bump pre-release to release
            convertUpcomingToRelease(file)
        } else {
            //make new release
            makeNewRelease(file)
        }
    } else {
        //check if un-released changes are present
        if (hasUnreleasedChanges(firstLine)) {
            //add to upcoming release
            addToFutureRelease(file)
        } else {
            //make new future release
            makeNewFutureRelease(file)
        }
    }
}

fun convertUpcomingToRelease(file: File) {
    var isFirstLine = true
    val bufferedReader = BufferedReader(FileReader("module-one/CHANGELOG.md"))
    bufferedReader.use { reader ->
        val stringBuilder = StringBuilder()
        var line = reader.readLine()

        while (line != null) {
            if (isFirstLine) {
                stringBuilder.append("### v${versionInfo.version} | ${today()}")
                stringBuilder.append(System.lineSeparator())
                stringBuilder.append("* ${prDetails.message} [(${prDetails.sha})](https://github.com/rubenquadros12/What-Changed/commit/${prDetails.sha})")
                stringBuilder.append(System.lineSeparator())
                isFirstLine = false
            } else {
                stringBuilder.append(line)
                stringBuilder.append(System.lineSeparator())
            }
            line = reader.readLine()
        }
        val newChangeLog = stringBuilder.toString()
        val fileAccess = RandomAccessFile(file, "rw")
        fileAccess.seek(0)
        fileAccess.write(newChangeLog.toByteArray())

        fileAccess.close()
    }
}

fun makeNewRelease(file: File) {
    val fileInputStream = FileInputStream(file)
    fileInputStream.use { inputStream ->
        val currentChangeLog = IOUtils.toString(inputStream, "utf-8")
        val fileAccess = RandomAccessFile(file, "rw")
        fileAccess.seek(0)
        fileAccess.write("### v${versionInfo.version} | ${today()}".toByteArray())
        fileAccess.write("\n".toByteArray())
        fileAccess.write("* ${prDetails.message} [(${prDetails.sha})](https://github.com/rubenquadros12/What-Changed/commit/${prDetails.sha})".toByteArray())
        fileAccess.write("\n".toByteArray())
        fileAccess.write(currentChangeLog.toByteArray())

        fileAccess.close()
    }
}

fun makeNewFutureRelease(file: File) {
    val fileInputStream = FileInputStream(file)
    fileInputStream.use { inputStream ->
        val currentChangeLog = IOUtils.toString(inputStream, "utf-8")
        val fileAccess = RandomAccessFile(file, "rw")
        fileAccess.seek(0)
        fileAccess.write("### Upcoming Release".toByteArray())
        fileAccess.write("\n".toByteArray())
        fileAccess.write("* ${prDetails.message} [(${prDetails.sha})](https://github.com/rubenquadros12/What-Changed/commit/${prDetails.sha})".toByteArray())
        fileAccess.write("\n".toByteArray())
        fileAccess.write(currentChangeLog.toByteArray())

        fileAccess.close()
    }
}

fun addToFutureRelease(file: File) {
    var isFirstLine = true
    val bufferedReader = BufferedReader(FileReader("module-one/CHANGELOG.md"))
    bufferedReader.use { reader ->
        val stringBuilder = StringBuilder()
        var line = reader.readLine()

        while (line != null) {
            stringBuilder.append(line)
            stringBuilder.append(System.lineSeparator())
            if (isFirstLine) {
                stringBuilder.append("* ${prDetails.message} [(${prDetails.sha})](https://github.com/rubenquadros12/What-Changed/commit/${prDetails.sha})")
                stringBuilder.append(System.lineSeparator())
                isFirstLine = false
            }
            line = reader.readLine()
        }
        val newChangeLog = stringBuilder.toString()
        val fileAccess = RandomAccessFile(file, "rw")
        fileAccess.seek(0)
        fileAccess.write(newChangeLog.toByteArray())

        fileAccess.close()
    }
}

fun today(): String {
    val current = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-YYYY")
    return current.format(formatter)
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