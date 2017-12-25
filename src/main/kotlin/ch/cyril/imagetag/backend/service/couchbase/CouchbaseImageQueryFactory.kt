package ch.cyril.imagetag.backend.service.couchbase

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.Tag
import ch.cyril.imagetag.backend.service.ImageQuery
import ch.cyril.imagetag.backend.service.ImageQueryFactory
import java.time.Instant

class CouchbaseImageQueryFactory : ImageQueryFactory {

    override fun since(date: Instant): ImageQuery {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun until(date: Instant): ImageQuery {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withTag(tag: Tag): ImageQuery {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun withId(id: Id): ImageQuery {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun and(vararg queries: ImageQuery): ImageQuery {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun or(vararg queries: ImageQuery): ImageQuery {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}