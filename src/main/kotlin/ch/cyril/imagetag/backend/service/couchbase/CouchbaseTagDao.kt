package ch.cyril.imagetag.backend.service.couchbase

import ch.cyril.imagetag.backend.model.Image
import ch.cyril.imagetag.backend.model.Tag
import ch.cyril.imagetag.backend.service.TagDao
import com.couchbase.client.java.Bucket
import com.couchbase.client.java.query.N1qlQuery

class CouchbaseTagDao(private val images: Bucket) : TagDao {

    override fun getAllTags(): Set<Tag> {
        //TODO Should this method really exists? Its performance will be dreadful...
        //Or maybe we should separately store tags?
        val res = images.query(N1qlQuery.simple("SELECT * FROM images"))
        return res.allRows()
                .map { r -> CouchbaseImageDao.imageFromJson(r.value().getObject("images")) }
                .flatMap { i -> i.tags }
                .toSet()
    }
}