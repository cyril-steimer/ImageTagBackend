package ch.cyril.imagetag.backend.rest

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.Tag
import ch.cyril.imagetag.backend.service.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.time.Instant

class ImageQueryParser {

    private val handlers = mapOf<String, (JsonElement) -> ImageQuery>(
            Pair("tag", { elem -> TagImageQuery(Tag(elem.asString)) }),
            Pair("since", { elem -> SinceImageQuery(Instant.ofEpochMilli(elem.asLong)) }),
            Pair("until", { elem -> UntilImageQuery(Instant.ofEpochMilli(elem.asLong)) }),
            Pair("id", { elem -> IdImageQuery(Id(elem.asString)) }),
            Pair("and", this::handleAnd),
            Pair("or", this::handleOr))

    fun parse(obj: JsonObject): ImageQuery {
        val key = obj.keySet().single()
        val handler = getHandler(key)
        return handler.invoke(obj.get(key))
    }

    private fun getHandler(key: String): (JsonElement) -> ImageQuery {
        return handlers.get(key)!!
    }

    private fun handleAnd(elem: JsonElement): ImageQuery {
        val queries = getSubQueries(elem)
        return AndImageQuery(*queries)
    }

    private fun handleOr(elem: JsonElement): ImageQuery {
        val queries = getSubQueries(elem)
        return OrImageQuery(*queries)
    }

    private fun getSubQueries(obj: JsonElement): Array<ImageQuery> {
        return obj.asJsonArray
                .map { e -> parse(e.asJsonObject) }
                .toTypedArray()
    }
}