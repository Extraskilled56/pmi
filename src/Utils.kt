// Utils.kt
package app.utils

import java.io.File

// function that makes a directory
fun mkdir(path: String) {
    val directory = File(path)
    // Create the directory and any necessary parent directories
    if (!directory.exists()) {
        directory.mkdirs()
    }
}
fun printc(message: String, colorCode: String) {
    // ANSI escape codes for text colors
    val reset = "\u001B[0m"
    val coloredMessage = "\u001B[$colorCode$message$reset" // Corrected line
    print(coloredMessage)
}

fun printlnc(message: String, colorCode: String) {
    // ANSI escape codes for text colors
    val reset = "\u001B[0m"
    val coloredMessage = "\u001B[$colorCode$message$reset" // Corrected line
    println(coloredMessage)
}

// function runs a command in bash and returns the output
fun runCommand(command: String, stdout: Boolean = false): String {
    val processBuilder = ProcessBuilder("/bin/sh", "-c", command)
    processBuilder.redirectErrorStream(true)
    val process = processBuilder.start()
    val output = StringBuilder()

    // Handle output in separate thread to prevent deadlocks
    val outputThread = Thread {
        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                output.appendLine(line)
                if (stdout) println(line)
            }
        }
    }

    // Handle errors in separate thread
    val errorThread = Thread {
        process.errorStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                output.appendLine(line)
                if (stdout) System.err.println(line)
            }
        }
    }

    outputThread.start()
    errorThread.start()
    
    // Wait for the process to complete
    val exitCode = process.waitFor()
    
    // Wait for output processing to complete
    outputThread.join()
    errorThread.join()

    if (exitCode != 0) {
        val errorMessage = output.toString()
        if (errorMessage.isNotEmpty()) {
            throw RuntimeException("Command failed with exit code $exitCode: $errorMessage")
        }
    }

    return output.toString()
}

class ConfigLoader(configFile: String) {
    private val lists = mutableMapOf<String, List<String>>()
    private val values = mutableMapOf<String, String>()

    init {
        val file = File(configFile)
        if (file.exists()) {
            var currentList: MutableList<String>? = null
            var currentListName = ""

            file.readLines().forEach { line ->
                when {
                    line.trim().startsWith("#") -> return@forEach // Skip comments
                    line.trim().isEmpty() -> return@forEach // Skip empty lines
                    line.trim().startsWith("[") && line.trim().endsWith("]") -> {
                        // Start of a new list
                        currentListName = line.trim().substring(1, line.length - 1)
                        currentList = mutableListOf()
                        lists[currentListName] = currentList!!
                    }
                    line.contains("=") -> {
                        // Key-value pair
                        val parts = line.split("=", limit = 2)
                        if (parts.size == 2) {
                            values[parts[0].trim()] = parts[1].trim()
                        }
                    }
                    currentList != null -> {
                        // Add to current list
                        currentList!!.add(line.trim())
                    }
                }
            }
        }
    }

    fun getValue(key: String): String? = values[key]
    fun getList(key: String): List<String>? = lists[key]
}

const val red = "31m"
const val blue = "34m"
const val green = "32m"
const val yellow = "33m"
const val cyan = "36m"
const val white = "37m"
const val black = "30m"
const val magenta = "35m"