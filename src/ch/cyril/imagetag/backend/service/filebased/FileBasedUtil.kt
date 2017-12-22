package ch.cyril.imagetag.backend.service.filebased

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.Image
import ch.cyril.imagetag.backend.model.ImageData
import ch.cyril.imagetag.backend.model.ImageType
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.util.function.BiPredicate
import java.util.stream.Collectors
import java.util.stream.Stream

internal class FileBasedUtil(val directory: Path, val tagReaderWriter: FileTagReaderWriter) {

    private val extensionsToType = mapOf(
            Pair("jpg", ImageType.JPG),
            Pair("jpeg", ImageType.JPG),
            Pair("png", ImageType.PNG),
            Pair("gif", ImageType.GIF))

    fun getAllImages(): List<Image> {
        return getImageFiles(directory)
                .map { f -> createImage(f) }
                .collect(Collectors.toList())
    }

    fun getImage(location: Path): Image? {
        try {
            return createImage(location)
        } catch (e: Exception) {
            return null
        }
    }

    fun getId(location: Path): Id {
        val relativePath = directory.relativize(location)
        val idEncoded = URLEncoder.encode(relativePath.toString(), "UTF-8")
        return Id(idEncoded)
    }

    fun getCreationTime(location: Path): Instant {
        return Files.readAttributes(location, BasicFileAttributes::class.java).creationTime().toInstant()
    }

    fun getImagePath(image: Image): Path {
        val decoded = URLDecoder.decode(image.id.id, "UTF-8")
        return directory.resolve(decoded)
    }

    private fun createImage(location: Path): Image {
        val id = getId(location)
        val type = getImageType(location)
        val data = Files.readAllBytes(location)
        val tags = tagReaderWriter.readTags(location).toMutableSet()
        return Image(id, type, getCreationTime(location), tags, ImageData(data))
    }

    private fun getImageFiles(directory: Path): Stream<Path> {
        val isImageFile = BiPredicate<Path, BasicFileAttributes> { p, a -> isImageFile(p) }
        return Files.find(directory, Int.MAX_VALUE, isImageFile)
    }

    private fun getImageType(path: Path): ImageType {
        return ImageType.ofFileName(path.fileName.toString())
    }

    private fun isImageFile(path: Path): Boolean {
        return ImageType.isImageFile(path.fileName.toString())
    }
}