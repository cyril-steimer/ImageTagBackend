package ch.cyril.imagetag.backend.service


import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.Tag
import java.time.Instant

interface ImageQueryFactory {
    fun since(date: Instant): ImageQuery

    fun until(date: Instant): ImageQuery

    fun withTag(tag: Tag): ImageQuery

    fun withId(id: Id): ImageQuery

    fun and(vararg queries: ImageQuery): ImageQuery

    fun or(vararg queries: ImageQuery): ImageQuery
}