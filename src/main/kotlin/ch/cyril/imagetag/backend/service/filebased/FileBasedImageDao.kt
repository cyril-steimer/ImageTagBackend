package ch.cyril.imagetag.backend.service.filebased

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.Image
import ch.cyril.imagetag.backend.model.ImageData
import ch.cyril.imagetag.backend.model.ImageWithData
import ch.cyril.imagetag.backend.service.ImageDao
import ch.cyril.imagetag.backend.service.ImageQuery
import ch.cyril.imagetag.backend.util.ListPagingIterable
import ch.cyril.imagetag.backend.util.PagingIterable
import java.nio.file.Files
import java.nio.file.Path


internal class FileBasedImageDao(directory: Path, val tagReaderWriter: FileTagReaderWriter) : ImageDao {


    private val util = FileBasedUtil(directory, tagReaderWriter)

    private val queryVisitor = FileBasedImageQueryVisitor(directory, tagReaderWriter)

    override fun getAllImages(): PagingIterable<Image> {
        val images = util.getAllImages()
        return ListPagingIterable(images)
    }
    override fun getImages(query: ImageQuery): PagingIterable<Image> {
        return query.accept(queryVisitor, null)
    }

    override fun addImage(imageWithData: ImageWithData) {
        val image = imageWithData.image
        val path = util.getImagePath(image.id)
        val existing = util.getImage(path)
        if (existing != null) {
            throw IllegalArgumentException("An image with id ${image.id} already exists")
        }
        Files.write(path, imageWithData.data.getBytes())
        tagReaderWriter.writeTags(path, image.tags)
    }

    override fun getImageWithData(id: Id): ImageWithData {
        val path = util.getImagePath(id)
        val image = util.getImage(path)
        val bytes = Files.readAllBytes(path)
        return ImageWithData(image!!, ImageData(bytes))
    }

    override fun updateImage(image: Image) {
        val path = util.getImagePath(image.id)
        tagReaderWriter.writeTags(path, image.tags)
    }

    override fun deleteImage(image: Image) {
        val path = util.getImagePath(image.id)
        tagReaderWriter.deleteTags(path)
        Files.deleteIfExists(path)
    }
}