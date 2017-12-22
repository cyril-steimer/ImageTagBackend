package ch.cyril.imagetag.backend.service

import ch.cyril.imagetag.backend.model.Image
import ch.cyril.imagetag.backend.util.PagingIterable

interface ImageQuery {
    fun apply(): PagingIterable<Image>
}