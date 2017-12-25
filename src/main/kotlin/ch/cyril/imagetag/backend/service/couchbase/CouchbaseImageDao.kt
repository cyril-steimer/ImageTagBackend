package ch.cyril.imagetag.backend.service.couchbase

import ch.cyril.imagetag.backend.model.*
import ch.cyril.imagetag.backend.service.ImageDao
import ch.cyril.imagetag.backend.util.ListPagingIterable
import ch.cyril.imagetag.backend.util.PagingIterable
import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonArray
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.Select
import java.time.Instant

class CouchbaseImageDao(val images: Bucket) : ImageDao  {

    override fun getAllImages(): PagingIterable<Image> {
        val query = Select.select("*").from("images")
        val result = images.query(query)
        val images = result.allRows()
                .map { r -> imageFromJson(r.value().getObject("images")) }
                .toList()
        return ListPagingIterable(images)
    }

    override fun addImage(image: Image) {
        val json = imageToJson(image)
        images.insert(JsonDocument.create(image.id.id, json))
    }

    override fun updateImage(image: Image) {
        val json = imageToJson(image)
        images.upsert(JsonDocument.create(image.id.id, json))
    }

    override fun deleteImage(image: Image) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun imageFromJson(json: JsonObject): Image {
        val id = Id(json.getString("id"))
        val type = ImageType.ofFileName(id.id)
        val creationDate = Instant.ofEpochMilli(json.getLong("creationDate"))
        val tags = emptySet<Tag>().toMutableSet()
        val data = ImageData(json.getString("data"))
        return Image(id, type, creationDate, tags, data)
    }

    private fun imageToJson(image: Image): JsonObject {
        val res = JsonObject.create()
        res.put("id", image.id.id)
        res.put("creationDate", image.creationDate.toEpochMilli())
        res.put("tags", JsonArray.empty())
        res.put("data", image.data!!.data)
        return res
    }
}