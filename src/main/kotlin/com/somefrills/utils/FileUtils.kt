package com.somefrills.utils

import net.minecraft.util.Util
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object FileUtils {
    fun atomicWriteInternal(path: Path, content: String) {
        val parent = path.parent ?: throw IOException("Parent directory is null")
        val fileName = path.fileName.toString()
        val tempPath = parent.resolve(
            TextUtils.format(
                "{}-Temp-{}.{}",
                fileName.substring(0, fileName.indexOf(".")),
                Util.getMeasuringTimeMs(),
                fileName.substring(fileName.indexOf(".") + 1)
            )
        )

        if (!Files.exists(parent)) {
            Files.createDirectories(parent)
        }

        Files.writeString(tempPath, content)
        try {
            Files.move(
                tempPath, path,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING)
        }
        Files.deleteIfExists(tempPath)
    }

    fun createFileInternal(path: Path, content: String) {
        val parent = path.parent
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent)
        }
        Files.writeString(path, content)
    }
}

// ========== Path Extension Functions ==========

fun Path.atomicWrite(content: String) {
    FileUtils.atomicWriteInternal(this, content)
}

fun Path.writeText(content: String, createParents: Boolean = true) {
    if (createParents) {
        FileUtils.createFileInternal(this, content)
    } else {
        Files.writeString(this, content)
    }
}

fun Path.readText(): String {
    return Files.readString(this)
}

fun Path.createParentDirectories() {
    val parent = parent
    if (parent != null && !Files.exists(parent)) {
        Files.createDirectories(parent)
    }
}

fun Path.deleteRecursively() {
    if (Files.isDirectory(this)) {
        Files.list(this).use { stream ->
            stream.forEach { it.deleteRecursively() }
        }
    }
    Files.deleteIfExists(this)
}

val Path.exists: Boolean
    get() = Files.exists(this)

val Path.isDirectory: Boolean
    get() = Files.isDirectory(this)

val Path.isFile: Boolean
    get() = Files.isRegularFile(this)

val Path.size: Long
    get() = if (Files.exists(this)) Files.size(this) else 0L

fun Path.listFiles(): List<Path> {
    return if (Files.isDirectory(this)) {
        Files.list(this).use { stream -> stream.toList() }
    } else {
        emptyList()
    }
}

fun Path.extension(): String {
    val name = fileName.toString()
    val dotIndex = name.lastIndexOf(".")
    return if (dotIndex > 0) name.substring(dotIndex + 1) else ""
}

fun Path.nameWithoutExtension(): String {
    val name = fileName.toString()
    val dotIndex = name.lastIndexOf(".")
    return if (dotIndex > 0) name.substring(0, dotIndex) else name
}

// ========== Global File Operations ==========


fun readFile(path: Path): String {
    return path.readText()
}

fun writeFile(path: Path, content: String, createParents: Boolean = true) {
    path.writeText(content, createParents)
}

fun deleteFile(path: Path) {
    Files.deleteIfExists(path)
}

