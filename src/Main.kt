import app.utils.*
import app.tasks.*

fun help() {
    val help_content = """
Usage pmi: <command>
    init         Initialize a new project
    init module (EXPERIMENTAL) Initialize a new module
Usage: pmi git <command>
    -- git wrapper
Usage: pmi code <command>
    build        Build the project
    run          Run the project  
Usage: pmi script <command>
    list         List all scripts
    create       Create a new script
    run          Run a script
    edit         Edit a script
    delete       Delete a script
Experimental!! Usage: pmi module <command>
    install      Install modules
    """
    println(help_content)
}

fun init() {
    mkdir(".pmi")
    printlnc("Initialized project", green)
}

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printlnc("Error: No arguments provided", red)
        help()
        return
    }
    
    val configLoader = ConfigLoader("pmi.cfg")
    val projectName = configLoader.getValue("projectName")
    val srcfiles = configLoader.getList("srcfiles")
    val projectLanguage = configLoader.getValue("projectLanguage")
    var modules = configLoader.getList("modules")

    if (projectName == null) {
        printlnc("Warning: Project name is not defined in pmi.cfg", yellow)
    } else if (srcfiles == null) {
        printlnc("Warning: srcfiles is not defined in pmi.cfg", yellow)
    } else if (projectLanguage == null) {
        printlnc("Warning: projectLanguage is not defined in pmi.cfg", yellow)
    } else if (modules == null) {
        printlnc("Warning: modules is not defined in pmi.cfg", yellow)
    }
    printlnc("Project Name: $projectName", cyan)

    when (args[0]) {
        "init" -> {
            if (args.size > 1) {
                task_init_module()
            } else {
                task_init()
            }
        }
        "git" -> {
            if (args.size < 2) {
                printlnc("Error: Git command requires arguments", red)
                help()
            } else {
                val gitArgs = args.drop(1).joinToString(" ")
                task_git(gitArgs)
            }
        }
        "code" -> {
            if (args.size < 2) {
                printlnc("Error: Code command requires a subcommand (build/run)", red)
                help()
            } else {
                when (args[1]) {
                    "build" -> task_build(srcfiles)
                    "run" -> {
                        val override = args.contains("--override")
                        task_run_build(srcfiles ?: emptyList(), args.drop(2).filter { it != "--override" }, override)
                    }
                    else -> {
                        printlnc("Error: Invalid code subcommand. Use 'build' or 'run'", red)
                        help()
                    }
                }
            }
        }
        "script" -> {
            if (args.size < 2) {
                printlnc("Error: Script command requires a subcommand (list/create/run/delete/edit)", red)
                help()
            } else {
                when (args[1]) {
                    "list" -> task_script_list()
                    "create" -> {
                        if (args.size < 3) {
                            printlnc("Error: Script create requires a name", red)
                            help()
                        } else {
                            task_script_create(args[2])
                        }
                    }
                    "run" -> {
                        if (args.size < 3) {
                            printlnc("Error: Script run requires a name", red)
                            help()
                        } else {
                            task_script_run(args[2])
                        }
                    }
                    "delete" -> {
                        if (args.size < 3) {
                            printlnc("Error: Script delete requires a name", red)
                            help()
                        } else {
                            task_script_delete(args[2])
                        }
                    }
                    "edit" -> {
                        if (args.size < 3) {
                            printlnc("Error: Script edit requires a name", red)
                            help()
                        } else {
                            task_script_edit(args[2])
                        }
                    }
                    else -> {
                        printlnc("Error: Invalid script subcommand. Use list/create/run/delete/edit", red)
                        help()
                    }
                }
            }
        }
        "module" -> {
            printlnc("WARNING: Module command is still being worked on. Use With Caution!", yellow)
            if (args.size < 2) {
                printlnc("Error: Module command requires a subcommand (install)", red)
                help()
            } else {
                when (args[1]) {
                    "install" -> {
                        if (modules == null) {
                            printlnc("Error: modules is not defined in pmi.cfg", red)
                        } else {
                            task_module_install(modules)
                        }
                    }
                    else -> {
                        printlnc("Error: Invalid module subcommand. Use install", red)
                        help()
                    }
                }
            }
        }
        else -> {
            printlnc("Error: Invalid command", red)
            help()
        }
    }
}