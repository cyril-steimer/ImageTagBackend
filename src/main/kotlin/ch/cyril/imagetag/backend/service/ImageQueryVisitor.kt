package ch.cyril.imagetag.backend.service

interface ImageQueryVisitor<A, R> {

    fun visitTagQuery(query: TagImageQuery, arg: A): R

    fun visitSinceQuery(query: SinceImageQuery, arg: A): R

    fun visitUntilQuery(query: UntilImageQuery, arg: A): R

    fun visitIdQuery(query: IdImageQuery, arg: A): R

    fun visitAndQuery(query: AndImageQuery, arg: A): R

    fun visitOrQuery(query: OrImageQuery, arg: A): R
}