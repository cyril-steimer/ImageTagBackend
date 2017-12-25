package ch.cyril.imagetag.backend.service.filebased

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.Image
import ch.cyril.imagetag.backend.model.Tag
import ch.cyril.imagetag.backend.service.*
import ch.cyril.imagetag.backend.util.ListPagingIterable
import ch.cyril.imagetag.backend.util.PagingIterable
import java.net.URLEncoder
import java.nio.file.Path
import java.time.Instant

internal class FileBasedImageQueryVisitor(val directory: Path, tagReader: FileTagReaderWriter)
    : ImageQueryVisitor<Any?, PagingIterable<Image>> {

    override fun visitTagQuery(query: TagImageQuery, arg: Any?): PagingIterable<Image> {
        val images = util.getAllImages()
                .filter { img -> img.tags.contains(query.tag) }
        return ListPagingIterable(images)
    }

    override fun visitSinceQuery(query: SinceImageQuery, arg: Any?): PagingIterable<Image> {
        return filterByDate { date -> !query.since.isAfter(date) }
    }

    override fun visitUntilQuery(query: UntilImageQuery, arg: Any?): PagingIterable<Image> {
        return filterByDate { date -> !query.until.isBefore(date) }
    }

    override fun visitIdQuery(query: IdImageQuery, arg: Any?): PagingIterable<Image> {
        //TODO Does not really work for paths with folders...
        val id = query.id
        val encoded = Id(URLEncoder.encode(id.id, "UTF-8"))
        val images = util.getAllImages()
                .filter { img -> img.id.equals(encoded) || img.id.equals(id) }
        return ListPagingIterable(images)
    }

    override fun visitTypeQuery(query: TypeImageQuery, arg: Any?): PagingIterable<Image> {
        val images = util.getAllImages()
                .filter { img -> img.type == query.type }
        return ListPagingIterable(images)
    }

    override fun visitAndQuery(query: AndImageQuery, arg: Any?): PagingIterable<Image> {
        return composite(query.queries, this::intersection)
    }

    override fun visitOrQuery(query: OrImageQuery, arg: Any?): PagingIterable<Image> {
        return composite(query.queries, this::union)
    }

    private val util = FileBasedUtil(directory, tagReader)

    private fun composite(queries: Array<out ImageQuery>, joiner: (List<Image>, List<Image>) -> List<Image>): PagingIterable<Image> {
        var res: List<Image>? = null
        for (query in queries) {
            val queryRes = (query.accept(this, null) as ListPagingIterable<Image>).list
            if (res == null) {
                res = queryRes
            } else {
                res = joiner.invoke(res, queryRes)
            }
        }
        res = res ?: emptyList()
        return ListPagingIterable(res)
    }

    private fun filterByDate(comparison: (Instant) -> Boolean): PagingIterable<Image> {
        val images = util.getAllImages()
                .filter { img -> comparison.invoke(img.creationDate) }
        return ListPagingIterable(images)
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