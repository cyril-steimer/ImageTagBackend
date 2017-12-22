package ch.cyril.imagetag.backend.service

import ch.cyril.imagetag.backend.model.Tag

interface TagDao {

    fun getAllTags(): Set<Tag>
}