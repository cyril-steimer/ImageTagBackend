package ch.cyril.imagetag.backend.service.filebased

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.Image
import ch.cyril.imagetag.backend.model.Tag
import ch.cyril.imagetag.backend.service.ImageQuery
import ch.cyril.imagetag.backend.service.ImageQueryFactory
import ch.cyril.imagetag.backend.util.ListPagingIterable
import ch.cyril.imagetag.backend.util.PagingIterable
import java.net.URLEncoder
import java.nio.file.Path
import java.time.Instant

internal class FileBasedImageQueryFactory(val directory: Path, val tagReader: FileTagReaderWriter) : ImageQueryFactory {

    private val util = FileBasedUtil(directory, tagReader)

    private inner class CompositeQuery(val joiner: (List<Image>, List<Image>) -> List<Image>,
                                       val queries: Array<out ImageQuery>) : ImageQuery {

        override fun apply(): PagingIterable<Image> {
            var res: List<Image>? = null
            for (query in queries) {
                val queryRes = (query.apply() as ListPagingIterable<Image>).list
                if (res == null) {
                    res = queryRes
                } else {
                    res = joiner.invoke(res, queryRes)
                }
            }
            res = res ?: emptyList()
            return ListPagingIterable(res)
        }
    }

    private inner class TagQuery(val tag: Tag) :ImageQuery {

        override fun apply(): PagingIterable<Image> {
            val images = util.getAllImages()
                    .filter { img -> img.tags.contains(tag) }
            return ListPagingIterable(images)
        }
    }

    private inner class DateQuery(val comparison: (Instant) -> Boolean) : ImageQuery {

        override fun apply(): PagingIterable<Image> {
            val images = util.getAllImages()
                    .filter { img -> comparison.invoke(img.creationDate) }
            return ListPagingIterable(images)
        }
    }

    private inner class IdQuery(val id: Id) : ImageQuery {

        override fun apply(): PagingIterable<Image> {
            //TODO Can we make sure the URLs are never decoded in Javalin?
            val encoded = Id(URLEncoder.encode(id.id, "UTF-8"))
            val images = util.getAllImages()
                    .filter { img -> img.id.equals(encoded) || img.id.equals(id) }
            return ListPagingIterable(images)
        }
    }

    override fun since(date: Instant): ImageQuery {
        return DateQuery { creationTime -> !date.isAfter(creationTime) }
    }

    override fun until(date: Instant): ImageQuery {
        return DateQuery { creationTime -> !date.isBefore(creationTime) }
    }

    override fun withTag(tag: Tag): ImageQuery {
        return TagQuery(tag)
    }

    override fun withId(id: Id): ImageQuery {
        return IdQuery(id)
    }

    override fun and(vararg queries: ImageQuery): ImageQuery {
        return CompositeQuery(this::intersection, queries)
    }

    override fun or(vararg queries: ImageQuery): ImageQuery {
        return CompositeQuery(this::union, queries)
    }

    private fun union(list1: List<Image>, list2: List<Image>): List<Image> {
        val union = list1.toMutableSet()
        union.addAll(list2)
        return union.toList()
    }

    private fun intersection(list1: List<Image>, list2: List<Image>): List<Image> {
        val intersection = list1.toMutableSet()
        intersection.retainAll(list2)
        return intersection.toList()
    }
}