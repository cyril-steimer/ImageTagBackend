package ch.cyril.imagetag.backend.rest

import ch.cyril.imagetag.backend.model.*
import ch.cyril.imagetag.backend.service.*
import ch.cyril.imagetag.backend.util.PagingIterable
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Instant


class ImageRestHandler(private val imageDao: ImageDao, private val tagDao: TagDao) {

    companion object {
        val GSON = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, InstantTypeAdapter().nullSafe())
            .registerTypeAdapter(Tag::class.java, TagTypeAdapter().nullSafe())
            .registerTypeAdapter(Id::class.java, IdTypeAdapter().nullSafe())
            .registerTypeAdapter(ImageData::class.java, DataTypeAdapter().nullSafe())
            .addSerializationExclusionStrategy(ImageDataExclusionStrategy())
            .create()
    }

    private class InstantTypeAdapter: TypeAdapter<Instant>() {

        override fun read(reader: JsonReader): Instant {
            return Instant.ofEpochMilli(reader.nextLong())
        }

        override fun write(writer: JsonWriter, instant: Instant) {
            writer.value(instant.toEpochMilli())
        }
    }

    private class TagTypeAdapter: TypeAdapter<Tag>() {
        override fun write(writer: JsonWriter, tag: Tag) {
            writer.value(tag.tag)
        }

        override fun read(reader: JsonReader): Tag {
            return Tag(reader.nextString())
        }
    }

    private class IdTypeAdapter: TypeAdapter<Id>() {
        override fun write(writer: JsonWriter, id: Id) {
            writer.value(id.id)
        }

        override fun read(reader: JsonReader): Id {
            return Id(reader.nextString())
        }
    }

    private class DataTypeAdapter: TypeAdapter<ImageData>() {
        override fun write(writer: JsonWriter, data: ImageData) {
            writer.value(data.data)
        }

        override fun read(reader: JsonReader): ImageData {
            return ImageData(reader.nextString())
        }
    }

    private class ImageDataExclusionStrategy : ExclusionStrategy {

        override fun shouldSkipClass(cls: Class<*>): Boolean {
            return false
        }

        override fun shouldSkipField(field: FieldAttributes): Boolean {
            return field.declaringClass == Image::class.java
                && field.name.equals("data")
        }
    }

    private val queryParser = ImageQueryParser()

    fun getImagesByQuery(@Body body: JsonObject,
                         @QueryParam("start") start: Int?,
                         @QueryParam("count") count: Int?): RestResult {
        val parsed = queryParser.parse(body)
        val images = imageDao.getImages(parsed)
        return RestResult.json(GSON.toJson(paginate(images, start, count)))
    }

    fun getAllImages(@QueryParam("start") start: Int?,
                     @QueryParam("count") count: Int?): RestResult {
        val images = imageDao.getAllImages()
        return RestResult.json(GSON.toJson(paginate(images, start, count)))
    }

    fun getAllTags(): RestResult {
        val tags = tagDao.getAllTags()
        return RestResult.json(GSON.toJson(tags))
    }

    fun getImageById(@PathParam("id") id: String): RestResult {
        val image = imageDao.getOneImage(IdImageQuery(Id(id)))
        return RestResult.json(GSON.toJson(image))
    }

    fun getImagesByTag(@PathParam("tag") tag: String,
                       @QueryParam("start") start: Int?,
                       @QueryParam("count") count: Int?): RestResult {
        val images = imageDao.getImages(TagImageQuery(Tag(tag)))
        return RestResult.json(GSON.toJson(paginate(images, start, count)))
    }

    fun getImagesSince(@PathParam("since") since: Long,
                       @QueryParam("start") start: Int?,
                       @QueryParam("count") count: Int?): RestResult {
        val date = Instant.ofEpochMilli(since)
        val images = imageDao.getImages(SinceImageQuery(date))
        return RestResult.json(GSON.toJson(paginate(images, start, count)))
    }

    fun getImagesUntil(@PathParam("until") until: Long,
                       @QueryParam("start") start: Int?,
                       @QueryParam("count") count: Int?): RestResult {
        val date = Instant.ofEpochMilli(until)
        val images = imageDao.getImages(UntilImageQuery(date))
        return RestResult.json(GSON.toJson(paginate(images, start, count)))
    }

    fun getImageDataById(@PathParam("id") id: String): RestResult {
        val imageWithData = imageDao.getImageWithData(Id(id))
        return RestResult.imageData(imageWithData.data.getBytes(), imageWithData.image.type)
    }

    fun updateImage(@Body image: Image) {
        imageDao.updateImage(image)
    }

    fun uploadImage(@Header("fileName") fileName:String,
                    @QueryParam("tag") tagNames: Array<String>,
                    @Body body: ByteArray) {
        val id = Id(fileName)
        val type = ImageType.ofFileName(fileName)
        val tags = tagNames
                .map { t -> Tag(t) }
                .toMutableSet()
        val image = Image(id, type, Instant.now(), tags)
        val data = ImageData(body)
        imageDao.addImage(ImageWithData(image, data))
    }

    fun deleteImageById(@PathParam("id") id: String) {
        val images = imageDao.getImages(IdImageQuery(Id(id)))
        imageDao.deleteImage(images.single())
    }

    private fun <T> paginate(iterable: PagingIterable<T>, start: Int?, count: Int?): List<T> {
        if (start != null && count != null) {
            val end = start + count
            return iterable.page(start, end)
        }
        return iterable.toList()
    }
}