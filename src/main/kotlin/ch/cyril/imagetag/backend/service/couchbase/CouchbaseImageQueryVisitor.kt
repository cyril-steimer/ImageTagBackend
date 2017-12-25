package ch.cyril.imagetag.backend.service.couchbase

import ch.cyril.imagetag.backend.service.*
import com.couchbase.client.java.query.dsl.Expression

import com.couchbase.client.java.query.dsl.Expression.*;

class CouchbaseImageQueryVisitor : ImageQueryVisitor<Any?, Expression> {

    override fun visitTagQuery(query: TagImageQuery, arg: Any?): Expression {
        return s(query.tag.tag).`in`("tags")
    }

    override fun visitSinceQuery(query: SinceImageQuery, arg: Any?): Expression {
        return x("creationDate").gte(query.since.toEpochMilli())
    }

    override fun visitUntilQuery(query: UntilImageQuery, arg: Any?): Expression {
        return x("creationDate").lte(query.until.toEpochMilli())
    }

    override fun visitIdQuery(query: IdImageQuery, arg: Any?): Expression {
        return x("id").eq(s(query.id.id))
    }

    override fun visitTypeQuery(query: TypeImageQuery, arg: Any?): Expression {
        return x("type").eq(s(query.type.name))
    }

    override fun visitAndQuery(query: AndImageQuery, arg: Any?): Expression {
        return visitComposite(query.queries) { e1, e2 -> e1.and(e2) }
    }

    override fun visitOrQuery(query: OrImageQuery, arg: Any?): Expression {
        return visitComposite(query.queries) { e1, e2 -> e1.or(e2) }
    }

    private fun visitComposite(queries: Iterable<ImageQuery>, joiner: (Expression, Expression) -> Expression): Expression {
        var res: Expression? = null
        for (query in queries) {
            val expr = query.accept(this, null)
            if (res == null) {
                res = expr
            } else {
                res = joiner.invoke(res, expr)
            }
        }
        //TODO What if there are no sub queries?
        return res!!
    }
}