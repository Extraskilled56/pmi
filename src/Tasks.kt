package app.tasks
import app.utils.*
import java.io.File

fun task_script_create(name: String): Int {
    printlnc("Running Task: Create Script", yellow)
    val file = File("scripts/$name.sh")
    if (file.createNewFile()) {
        printlnc("File $name.sh created successfully.", green)
        runCommand("chmod +x scripts/$name.sh", true)
        return 0
    } else {
        printlnc("File $name.sh already exists.", red)
        return 1
    }
}
fun task_script_run(name: String): Int {
    printlnc("Running Task: Run Script", yellow)
    val file = File("scripts/$name.sh")
    if (file.exists()) {
        runCommand("./scripts/$name.sh", true)
        return 0
    } else {
        printlnc("File $name.sh does not exist.", red)
        return 1
    }
}
fun task_script_delete(name: String): Int {
    printlnc("Running Task: Delete Script", yellow)
    val file = File("scripts/$name.sh")
    if (file.exists()) {
        if (file.delete()) {
            printlnc("File $name.sh deleted successfully.", green)
            return 0
        } else {
            printlnc("File $name.sh could not be deleted.", red)
            return 1
        }
    } else {
        printlnc("File $name.sh does not exist.", red)
        return 1
    }
}
fun task_script_edit(name: String): Int {
    printlnc("Running Task: Edit Script", yellow)
    return 0
}
fun task_script_list(): Int {
    printlnc("Running Task: List Scripts", yellow)
    val scripts = File("scripts").list()
    if (scripts != null) {
        scripts.forEach { printlnc(it, green) }
        return 0
    } else {
        printlnc("No scripts found.", red)
        return 1
    }
}
fun task_build(srcfiles: List<String>?, module: Boolean = false): Int {
    printlnc("Running Task: Build", yellow)
    if (srcfiles == null) {
        printlnc("Error: srcfiles is null", red)
        return 1
    }

    if (!File(".pmi/").exists()) {
        mkdir(".pmi/")
    }

    if (module) {
        srcfiles?.forEach { printlnc(it, green) }
        runCommand("kotlinc ${srcfiles?.joinToString(" ")} -d .pmi/build.jar", true)
        if (File(".pmi/build.jar").exists()) {
            printlnc("Compiled successfully", green)
            return 0
        } else {
            printlnc("Compilation failed", red)
            return 1
        }
    } else {
        // check if pmi_modules directory exists
        if (File("pmi_modules/").exists()) {
            // get list of modules
            val modules = File("pmi_modules/").list()
            if (modules != null) {
                // add modules to srcfiles
                srcfiles?.forEach { printlnc(it, green) }
                modules.forEach { printlnc(it, green) }
                // Filter jar files and source files
                val jarFiles = modules.filter { it.endsWith(".jar") }
                val sourceFiles = modules.filter { !it.endsWith(".jar") }
                // Create classpath string
                val classpath = if (jarFiles.isNotEmpty()) {
                    "-cp " + jarFiles.map { "pmi_modules/$it" }.joinToString(":")
                } else ""
                // Build the compilation command
                runCommand("kotlinc $classpath ${srcfiles?.joinToString(" ")} ${sourceFiles.joinToString(" ")} -d .pmi/build.jar", true)
                if (File(".pmi/build.jar").exists()) {
                    printlnc("Compiled successfully", green)
                    return 0
                } else {
                    printlnc("Compilation failed", red)
                    return 1
                }
            } else {
                srcfiles?.forEach { printlnc(it, green) }
                runCommand("kotlinc ${srcfiles?.joinToString(" ")} -include-runtime -d .pmi/build.jar", true)
                if (File(".pmi/build.jar").exists()) {
                    printlnc("Compiled successfully", green)
                    return 0
                } else {
                    printlnc("Compilation failed", red)
                    return 1
                }
            }
        } else {
            srcfiles?.forEach { printlnc(it, green) }
            runCommand("kotlinc ${srcfiles?.joinToString(" ")} -include-runtime -d .pmi/build.jar", true)
            if (File(".pmi/build.jar").exists()) {
                printlnc("Compiled successfully", green)
                return 0
            } else {
                printlnc("Compilation failed", red)
                return 1
            }
        }
    }
}

fun task_run_build(srcfiles: List<String>, arguments: List<String> = emptyList(), overwrite: Boolean = true): Int {
    if (overwrite) {
        
        if (task_build(srcfiles) != 0) {
            return 1
        }
    }
    if (!File(".pmi/build.jar").exists()) {
        task_build(srcfiles)
    }
    val command = if (arguments.isNotEmpty()) {
        "kotlin .pmi/build.jar ${arguments.joinToString(" ")}"
    } else {
        "kotlin .pmi/build.jar"
    }
    runCommand(command, true)
    return 0
}
fun task_git(command: String): Int {
    runCommand("git $command", true)
    return 0
}
fun task_init(): Int {
    printlnc("Running Task: Initialize", yellow)
    println("Input project name:")
    val projectName = readln()
    val configFile = File("pmi.cfg").apply {
        if (createNewFile()) {
            println("File pmi.cfg created successfully.")
            printWriter().use { out ->
                out.println("projectName = $projectName")
                out.println("srcfiles = [src/Main.kt]")
                out.println("projectLanguage = kotlin")
                out.println("modules = [")
                out.println("    none")
                out.println("]")
            }
        } else {
            println("File pmi.cfg already exists.")
        }
    }
    mkdir("src")
    mkdir("scripts")
    val srcfiles = File("src/Main.kt").apply {
        if (createNewFile()) {
            println("File Main.kt created successfully.")
            printWriter().use { out ->
                out.println("fun main() {")
                out.println("    println(\"Hello, World!\")")
                out.println("}")
            }
        } else {
            println("File src/Main.kt already exists.")
        }
    }
    mkdir(".pmi")
    return 0
}

fun task_init_module(): Int {
    printlnc("Running Task: Initialize", yellow)
    println("Input project name:")
    val projectName = readln()
    val configFile = File("pmi.cfg").apply {
        if (createNewFile()) {
            println("File pmi.cfg created successfully.")
            printWriter().use { out ->
                out.println("projectName = $projectName")
                out.println("srcfiles = [src/Main.kt]")
                out.println("projectLanguage = kotlin")
                out.println("module = true")
                out.println("modules = [")
                out.println("    ")
                out.println("]")
            }
        } else {
            println("File pmi.cfg already exists.")
        }
    }
    mkdir("src")
    mkdir("scripts")
    val srcfiles = File("src/Main.kt").apply {
        if (createNewFile()) {
            println("File Main.kt created successfully.")
            printWriter().use { out ->
                out.println("package $projectName.main")
                out.println()
                out.println("fun printhello() {")
                out.println("    println(\"Hello, World!\")")
                out.println("}")
            }
        } else {
            println("File src/Main.kt already exists.")
        }
    }
    mkdir(".pmi")
    return 0
}

fun task_module_install(modules: List<String>): Int {
    printlnc("Running Task: Install Modules", yellow)
    // make sure pmi_modules directory exists
    if (!File("pmi_modules/").exists()) {
        mkdir("pmi_modules/")
    }
    if (modules.isEmpty()) {
        printlnc("Error: No modules specified in pmi.cfg", red)
        return 1
    } else {
        printlnc("Installing modules:", green)
        modules.forEach { printlnc(it, green) }
        modules.forEach { module ->
            try {
                runCommand("cp /home/zacharyj/Coding/pmi_module_server/$module/build.jar pmi_modules/$module.jar", true)
                if (File("pmi_modules/$module.jar").exists()) {
                    printlnc("Module $module installed successfully", green)
                } else {
                    printlnc("Failed to install module $module", red)
                }
            } catch (e: Exception) {
                printlnc("Failed to install module $module: ${e.message}", red)
            }
        }
    }
    
    return 0
}