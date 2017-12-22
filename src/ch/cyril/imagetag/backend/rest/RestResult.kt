package ch.cyril.imagetag.backend.rest

import ch.cyril.imagetag.backend.model.ImageType
import java.io.ByteArrayInputStream
import java.io.InputStream

class RestResult(val contentType: String, val data: InputStream) {

    companion object {
        fun json(json: String): RestResult {
            return RestResult("application/json", ByteArrayInputStream(json.toByteArray(Charsets.UTF_8)))
        }

        fun imageData(data: ByteArray, type: ImageType): RestResult {
            return RestResult(type.contentType, ByteArrayInputStream(data))
        }
    }

}