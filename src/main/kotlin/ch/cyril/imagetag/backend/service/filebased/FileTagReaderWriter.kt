package ch.cyril.imagetag.backend.service.filebased

import ch.cyril.imagetag.backend.model.Tag
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


internal class FileTagReaderWriter(val tagFile: Path, imageDirectory: Path) {

    private val util = FileBasedUtil(imageDirectory, this)

    @Synchronized fun readAllTags(): Map<String, Set<Tag>> {
        val lines = readTagLines()
        return lines.map { l -> l.split("=") }
                .associateBy( { l -> l[0] }, { l -> parseTags(l[1]) } );
    }

    @Synchronized fun readTags(imageLocation: Path): Set<Tag> {
        val lines = readTagLines()
        val index = getTagLineIndex(lines, imageLocation)
        if (index >= 0) {
            val prefix = getTagLinePrefix(imageLocation)
            val suffix = lines[index].substring(prefix.length)
            return parseTags(suffix)
        }
        return emptySet()
    }

    @Synchronized fun writeTags(imageLocation: Path, tags: Set<Tag>) {
        val lines = readTagLines()
        val index = getTagLineIndex(lines, imageLocation)
        val newLine = getTagLinePrefix(imageLocation) + tags.map { t -> t.tag }.joinToString(",")
        if (index >= 0) {
            lines[index] = newLine
        } else {
            lines.add(newLine)
        }
        Files.write(tagFile, lines)
    }

    @Synchronized fun deleteTags(imageLocation: Path) {
        val lines = readTagLines()
        val index = getTagLineIndex(lines, imageLocation)
        if (index >= 0) {
            lines.removeAt(index)
        }
        Files.write(tagFile, lines)
    }

    private fun parseTags(tags: String): Set<Tag> {
        return tags.split(",")
                .map { s -> s.trim() }
                .map { t -> Tag(t) }
                .toSet()
                .minus(Tag(""))
    }

    private fun getTagLinePrefix(imageLocation: Path): String {
        return util.getId(imageLocation).id + "="
    }

    private fun getTagLineIndex(lines: List<String>, imageLocation: Path): Int {
        for (i in 0 until lines.size) {
            val line = lines[i]
            if (line.startsWith(getTagLinePrefix(imageLocation))) {
                return i
            }
        }
        return -1
    }

    private fun readTagLines(): MutableList<String> {
        try {
            return Files.readAllLines(tagFile)
        } catch (e: IOException) {
            return ArrayList();
        }
    }
}