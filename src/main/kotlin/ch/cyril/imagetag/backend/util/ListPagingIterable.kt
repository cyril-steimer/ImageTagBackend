package ch.cyril.imagetag.backend.util

class ListPagingIterable<T>(val list: List<T>) : PagingIterable<T> {

    override fun page(start: Int, end: Int): List<T> {
        if (start > list.size) {
            return emptyList()
        }
        val endFitted = minOf(end, list.size)
        return list.subList(start, endFitted)
    }

    override fun iterator(): Iterator<T> {
        return list.iterator()
    }

    override fun size(): Int {
        return list.size
    }
}