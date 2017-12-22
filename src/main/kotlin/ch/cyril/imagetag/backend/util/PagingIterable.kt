package ch.cyril.imagetag.backend.util

interface PagingIterable<T> : Iterable<T> {
    fun page(start: Int, end: Int): List<T>
    fun size(): Int
}