package ch.cyril.imagetag.backend.main

import ch.cyril.imagetag.backend.rest.*
import ch.cyril.imagetag.backend.rest.javalin.JavalinRestContext
import ch.cyril.imagetag.backend.service.couchbase.CouchbaseServiceFactory
import ch.cyril.imagetag.backend.service.filebased.FileBasedServiceFactory
import com.google.gson.Gson
import io.javalin.Context
import io.javalin.Javalin
import java.nio.file.Paths
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

fun main(args: Array<String>) {

    val dir = Paths.get("C:\\Users\\Cyril Steimer\\Documents\\Projects\\ImageTag\\images")
    val serviceFactory = FileBasedServiceFactory(dir)
    val tagDao = serviceFactory.createTagDao()
    val imageDao = serviceFactory.createImageDao()

    val handler = ImageRestHandler(imageDao, tagDao)

    val app = Javalin.create()
            .port(8100)
            .start()

    app.get("/api/v1/images") { ctx -> handle(handler::getAllImages, ctx) }
    app.get("/api/v1/images/tag/:tag") { ctx -> handle(handler::getImagesByTag, ctx) }
    app.get("/api/v1/images/since/:since") { ctx -> handle(handler::getImagesSince, ctx) }
    app.get("/api/v1/images/until/:until") { ctx -> handle(handler::getImagesUntil, ctx) }
    app.get("/api/v1/images/id/:id") { ctx -> handle(handler::getImageById, ctx) }

    app.get("/api/v1/tags") { ctx -> handle(handler::getAllTags, ctx) }
    app.get("/api/v1/imagedata/:id") { ctx -> handle(handler::getImageDataById, ctx) }

    app.post("/api/v1/images") { ctx -> handle(handler::getImagesByQuery, ctx) }

    app.put("/api/v1/images") { ctx -> handleVoid(handler::updateImage, ctx) }
    app.put("/api/v1/images/upload") { ctx -> handleVoid(handler::uploadImage, ctx) }

    app.delete("/api/v1/images/id/:id") { ctx -> handleVoid(handler::deleteImageById, ctx) }
}

fun handle(function: KFunction<RestResult>, ctx: Context) {
    try {
        RestHandlerInvoker().invoke(function, JavalinRestContext(ctx))
    } catch (e: Throwable) {
        e.printStackTrace()
        ctx.response().sendError(HttpServletResponse.SC_BAD_REQUEST)
    }
}

fun handleVoid(function: KFunction<Unit>, ctx: Context) {
    try {
        RestHandlerInvoker().invokeVoid(function, JavalinRestContext(ctx))
    } catch (e: Throwable) {
        e.printStackTrace()
        ctx.response().sendError(HttpServletResponse.SC_BAD_REQUEST)
    }
}