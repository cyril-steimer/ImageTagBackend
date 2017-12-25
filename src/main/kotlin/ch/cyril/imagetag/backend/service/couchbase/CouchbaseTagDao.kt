package ch.cyril.imagetag.backend.service.couchbase

import ch.cyril.imagetag.backend.model.Tag
import ch.cyril.imagetag.backend.service.TagDao

class CouchbaseTagDao: TagDao {

    override fun getAllTags(): Set<Tag> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}