package ch.cyril.imagetag.backend.service.couchbase

import ch.cyril.imagetag.backend.model.*
import ch.cyril.imagetag.backend.service.ImageDao
import ch.cyril.imagetag.backend.service.ImageQuery
import ch.cyril.imagetag.backend.util.ListPagingIterable
import ch.cyril.imagetag.backend.util.PagingIterable
import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.StringDocument
import com.couchbase.client.java.document.json.JsonArray
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.N1qlQueryResult
import com.couchbase.client.java.query.Select
import java.time.Instant

class CouchbaseImageDao(private val images: Bucket, private val imageData: Bucket) : ImageDao  {

    companion object {
        fun imageToJson(image: Image): JsonObject {
            val res = JsonObject.create()
            res.put("id", image.id.id)
            res.put("type", image.type.name)
            res.put("creationDate", image.creationDate.toEpochMilli())
            val tags = image.tags
                    .map { t -> t.tag }
                    .toTypedArray()
            res.put("tags", JsonArray.from(*tags))
            return res
        }

        fun imageFromJson(json: JsonObject): Image {
            val id = Id(json.getString("id"))
            val type = ImageType.valueOf(json.getString("type"))
            val creationDate = Instant.ofEpochMilli(json.getLong("creationDate"))
            val tags = json.getArray("tags")
                    .map { j -> Tag(j.toString()) }
                    .toMutableSet()
            return Image(id, type, creationDate, tags)
        }
    }

    private val queryVisitor = CouchbaseImageQueryVisitor()

    override fun getAllImages(): PagingIterable<Image> {
        val query = Select.select("*").from("images")
        val result = images.query(query)
        return handleResult(result)
    }

    override fun getImages(query: ImageQuery): PagingIterable<Image> {
        val select = Select.select("*").from("images")
        val where = select.where(query.accept(queryVisitor, null))
        println(where)
        val result = images.query(where)
        return handleResult(result)
    }

    override fun getImageWithData(id: Id): ImageWithData {
        val image = imageFromJson(images[id.id].content())
        val data = imageData.get(id.id, StringDocument::class.java).content()
        return ImageWithData(image, ImageData(data))
    }

    override fun addImage(imageWithData: ImageWithData) {
        val image = imageWithData.image
        updateImage(image)
        imageData.insert(StringDocument.create(image.id.id, imageWithData.data.data))
    }

    override fun updateImage(image: Image) {
        val json = imageToJson(image)
        images.upsert(JsonDocument.create(image.id.id, json))
    }

    override fun deleteImage(image: Image) {
        val id = image.id.id
        images.remove(id)
        imageData.remove(id)
    }

    private fun handleResult(result: N1qlQueryResult): PagingIterable<Image> {
        val images = result.allRows()
                .map { r -> imageFromJson(r.value().getObject("images")) }
                .toList()
        return ListPagingIterable(images)
    }
}