package ch.cyril.imagetag.backend.service

import ch.cyril.imagetag.backend.service.couchbase.CouchbaseServiceFactory
import ch.cyril.imagetag.backend.service.filebased.FileBasedServiceFactory
import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ServiceFactoryLoader(private val configFile: Path) {

    private enum class Type {
        COUCHBASE, FILE_BASED
    }

    private class Config(val type: Type, val directory: String)

    fun load(): ServiceFactory {
        if (!Files.exists(configFile)) {
            throw IllegalStateException("Create a configuration file at location ${configFile.toAbsolutePath()}")
        }
        val content = Files.readAllLines(configFile)
                .joinToString("\n")
        val config = Gson().fromJson(content, Config::class.java)
        if (config.type == Type.COUCHBASE) {
            return CouchbaseServiceFactory()
        }
        return FileBasedServiceFactory(Paths.get(config.directory))
    }
}