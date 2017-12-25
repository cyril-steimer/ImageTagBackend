package ch.cyril.imagetag.backend.service

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.ImageType
import ch.cyril.imagetag.backend.model.Tag
import java.time.Instant

interface ImageQuery {

    fun <A, R> accept(visitor: ImageQueryVisitor<A, R>, arg: A): R
}

class TagImageQuery(val tag: Tag) : ImageQuery {

    override fun <A, R> accept(visitor: ImageQueryVisitor<A, R>, arg: A): R {
        return visitor.visitTagQuery(this, arg)
    }
}

class SinceImageQuery(val since: Instant) : ImageQuery {

    override fun <A, R> accept(visitor: ImageQueryVisitor<A, R>, arg: A): R {
        return visitor.visitSinceQuery(this, arg)
    }
}

class UntilImageQuery(val until: Instant) : ImageQuery {

    override fun <A, R> accept(visitor: ImageQueryVisitor<A, R>, arg: A): R {
        return visitor.visitUntilQuery(this, arg)
    }
}

class IdImageQuery(val id: Id) : ImageQuery {

    override fun <A, R> accept(visitor: ImageQueryVisitor<A, R>, arg: A): R {
        return visitor.visitIdQuery(this, arg)
    }
}

class TypeImageQuery(val type: ImageType): ImageQuery {

    override fun <A, R> accept(visitor: ImageQueryVisitor<A, R>, arg: A): R {
        return visitor.visitTypeQuery(this, arg)
    }
}

class AndImageQuery(vararg val queries: ImageQuery) : ImageQuery {

    override fun <A, R> accept(visitor: ImageQueryVisitor<A, R>, arg: A): R {
        return visitor.visitAndQuery(this, arg)
    }
}

class OrImageQuery(vararg val queries: ImageQuery) : ImageQuery {

    override fun <A, R> accept(visitor: ImageQueryVisitor<A, R>, arg: A): R {
        return visitor.visitOrQuery(this, arg)
    }
}