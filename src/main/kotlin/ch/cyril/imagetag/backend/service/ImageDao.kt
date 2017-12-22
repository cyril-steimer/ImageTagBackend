package ch.cyril.imagetag.backend.service

import ch.cyril.imagetag.backend.model.Image
import ch.cyril.imagetag.backend.util.PagingIterable

interface ImageDao {

    fun getOneImage(query: ImageQuery): Image? {
        val images = getImages(query)
        return images.firstOrNull()
    }

    fun getImages(query: ImageQuery): PagingIterable<Image> {
        return query.apply()
    }

    fun getAllImages(): PagingIterable<Image>

    fun addImage(image: Image)

    fun updateImage(image: Image)

    fun deleteImage(image: Image)
}