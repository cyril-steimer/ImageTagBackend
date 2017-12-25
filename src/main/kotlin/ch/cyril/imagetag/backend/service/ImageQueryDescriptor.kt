package ch.cyril.imagetag.backend.service

import ch.cyril.imagetag.backend.model.Id
import ch.cyril.imagetag.backend.model.ImageType
import ch.cyril.imagetag.backend.model.Tag
import java.time.Instant


enum class SimpleImageQueryDescriptor(private val factory: (String) -> ImageQuery) {

    id( {arg -> IdImageQuery(Id(arg)) } ),
    tag( { arg -> TagImageQuery(Tag(arg)) } ),
    since( { arg -> SinceImageQuery(Instant.ofEpochMilli(arg.toLong())) } ),
    until( { arg -> UntilImageQuery(Instant.ofEpochMilli(arg.toLong())) } ),
    type( { arg -> TypeImageQuery(ImageType.valueOf(arg)) } );


    fun createQuery(arg: String): ImageQuery {
        return factory.invoke(arg)
    }
}

enum class CompositeImageQueryDescriptor(private val factory: (Iterable<ImageQuery>) -> ImageQuery) {

    or( { arg -> OrImageQuery(arg) } ),
    and( { arg -> AndImageQuery(arg) } );

    fun createQuery(arg: Iterable<ImageQuery>): ImageQuery {
        return factory.invoke(arg)
    }
}