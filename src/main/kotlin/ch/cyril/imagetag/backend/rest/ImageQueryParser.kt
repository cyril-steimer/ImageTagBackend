package ch.cyril.imagetag.backend.rest

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.ImageType
import ch.cyril.imagetag.backend.model.Tag
import ch.cyril.imagetag.backend.service.*
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.time.Instant

class ImageQueryParser {

    fun parse(obj: JsonObject): ImageQuery {
        val key = obj.keySet().single()
        val value = obj[key]
        val res = handleSimpleQuery(key, value)
        return res ?: handleCompositeQuery(key, value)
    }

    private fun handleSimpleQuery(key: String, value: JsonElement): ImageQuery? {
        try {
            return SimpleImageQueryDescriptor.valueOf(key).createQuery(value.asString)
        } catch (e: IllegalArgumentException) {
            return null
        }
    }

    private fun handleCompositeQuery(key: String, value: JsonElement): ImageQuery {
        val queries = getSubQueries(value)
        return CompositeImageQueryDescriptor.valueOf(key).createQuery(queries)
    }

    private fun getSubQueries(obj: JsonElement): Iterable<ImageQuery> {
        return obj.asJsonArray
                .map { e -> parse(e.asJsonObject) }
                .toList()
    }
}