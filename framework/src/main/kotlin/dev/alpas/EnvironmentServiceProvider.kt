package dev.alpas

import io.github.cdimascio.dotenv.dotenv
import java.io.File
import java.nio.file.Paths
import java.util.*

internal class EnvironmentServiceProvider : ServiceProvider {
    private val runMode by lazy {
        val mode = System.getenv(RUN_MODE) ?: System.getProperty(RUN_MODE) ?: "server"
        RunMode.valueOf(mode.uppercase(Locale.getDefault()))
    }

    override fun register(app: Application) {
        val envPath = envPath(app)
        val rootPath = envPath.parentDir.absolutePath

        System.setProperty(ROOT_DIR_KEY, rootPath)
        System.setProperty(SRC_DIR_KEY, Paths.get(rootPath, "src", "main", "kotlin").toAbsolutePath().toString())
        System.setProperty(RUN_MODE, runMode.name)

        app.bufferDebugLog("${envPath.envFile} found at: ${envPath.parentDir.path}")
        app.bufferDebugLog("Root is at: $rootPath")
        val dotenv = dotenv {
            filename = envPath.envFile
            directory = rootPath
        }
        app.singleton(Environment(dotenv, rootPath, app.srcPackage, runMode))
    }

    private fun envPath(app: Application): EnvPath {
        return if (runMode.isConsole()) {
            EnvPath(File(System.getenv(ROOT_DIR_KEY)), ".env")
        } else {
            try {
                findEnvDir(app.cwd)
            } catch (e: IllegalStateException) {
                throw IllegalStateException("Looks like the .env file is missing. Create one in your root project.")
            }
        }
    }

    private fun findEnvDir(at: File): EnvPath {
        if (File(at, ".env").exists()) {
            if (runMode == RunMode.TEST && File(at, ".env.testing").exists()) {
                return EnvPath(at, ".env.testing")
            }
            return EnvPath(at, ".env")
        }
        return findEnvDir(at.parentFile)
    }

    private data class EnvPath(val parentDir: File, val envFile: String)
}
