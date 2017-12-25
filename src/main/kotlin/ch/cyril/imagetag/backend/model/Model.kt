package ch.cyril.imagetag.backend.model

import java.time.Instant
import java.util.*

data class Image(val id: Id, val type: ImageType, val creationDate: Instant, val tags: MutableSet<Tag>)

data class ImageWithData(val image: Image, val data: ImageData)

data class Id(val id: String)

data class Tag(val tag: String)

enum class ImageType(val contentType: String) {
    JPG("image/jpg"),
    PNG("image/png"),
    GIF("image/gif");


    companion object {
        fun ofFileName(fileName: String): ImageType {
            val lowerCase = fileName.toLowerCase()
            if (lowerCase.endsWith(".jpg") || lowerCase.endsWith(".jpeg")) {
                return JPG
            } else if (lowerCase.endsWith(".png")) {
                return PNG
            } else if (lowerCase.endsWith(".gif")) {
                return GIF
            }
            throw IllegalArgumentException("File name '$fileName' is not a valid image type")
        }

        fun isImageFile(fileName: String): Boolean {
            try {
                ofFileName(fileName)
                return true
            } catch (e: IllegalArgumentException) {
                return false
            }
        }
    }
}

data class ImageData(val base64: String) {
    constructor(bytes: ByteArray) : this(Base64.getEncoder().encodeToString(bytes))

    fun getBytes(): ByteArray {
        return Base64.getDecoder().decode(base64)
    }
}