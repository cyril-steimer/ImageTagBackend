package ch.cyril.imagetag.backend.service.filebased

import ch.cyril.imagetag.backend.model.Tag
import ch.cyril.imagetag.backend.service.TagDao

internal class FileBasedTagDao(val tagReader: FileTagReaderWriter) : TagDao {

    override fun getAllTags(): Set<Tag> {
        val tags = tagReader.readAllTags()
        return tags.flatMap { e -> e.value }
                .toSet()
    }
}