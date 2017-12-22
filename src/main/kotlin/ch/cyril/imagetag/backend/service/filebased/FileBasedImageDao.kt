package ch.cyril.imagetag.backend.service.filebased

import ch.cyril.imagetag.backend.model.Image
import ch.cyril.imagetag.backend.service.ImageDao
import ch.cyril.imagetag.backend.util.PagingIterable
import java.nio.file.Files
import java.nio.file.Path


internal class FileBasedImageDao(val directory: Path, val tagReaderWriter: FileTagReaderWriter) : ImageDao {

    private val util = FileBasedUtil(directory, tagReaderWriter)

    override fun getAllImages(): PagingIterable<Image> {
        val images = util.getAllImages()
        return FileBasedImageQueryFactory.ListPagingIterable(images)
    }

    override fun addImage(image: Image) {
        val path = util.getImagePath(image)
        val existing = util.getImage(path)
        if (existing != null) {
            throw IllegalArgumentException("An image with id ${image.id} already exists")
        }
        Files.write(path, image.data!!.getBytes())
        tagReaderWriter.writeTags(path, image.tags)
    }

    override fun updateImage(image: Image) {
        val path = util.getImagePath(image)
        tagReaderWriter.writeTags(path, image.tags)
    }

    override fun deleteImage(image: Image) {
        val path = util.getImagePath(image)
        tagReaderWriter.deleteTags(path)
        Files.deleteIfExists(path)
    }
}