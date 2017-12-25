package ch.cyril.imagetag.backend.service

interface ServiceFactory {
    fun createImageDao(): ImageDao

    fun createTagDao(): TagDao
}
