package ch.cyril.imagetag.backend.service.filebased

import ch.cyril.imagetag.backend.service.ImageDao
import ch.cyril.imagetag.backend.service.ImageQueryFactory
import ch.cyril.imagetag.backend.service.ServiceFactory
import ch.cyril.imagetag.backend.service.TagDao
import java.nio.file.Path

class FileBasedServiceFactory(val directory: Path) : ServiceFactory {

    private val tagReaderWriter = FileTagReaderWriter(directory.resolve("tags"), directory.resolve("images"))

    override fun createImageDao(): ImageDao {
        return FileBasedImageDao(directory.resolve("images"), tagReaderWriter)
    }

    override fun createTagDao(): TagDao {
        return FileBasedTagDao(tagReaderWriter)
    }

    override fun createQueryFactory(): ImageQueryFactory {
        return FileBasedImageQueryFactory(directory.resolve("images"), tagReaderWriter)
    }
}