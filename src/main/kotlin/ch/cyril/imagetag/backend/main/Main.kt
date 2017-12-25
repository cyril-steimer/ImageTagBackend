package ch.cyril.imagetag.backend.main

import ch.cyril.imagetag.backend.rest.*
import ch.cyril.imagetag.backend.rest.javalin.JavalinRestContext
import ch.cyril.imagetag.backend.service.couchbase.CouchbaseServiceFactory
import io.javalin.Context
import io.javalin.Javalin
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KFunction

fun main(args: Array<String>) {

    val serviceFactory = CouchbaseServiceFactory()
    val tagDao = serviceFactory.createTagDao()
    val imageDao = serviceFactory.createImageDao()

    val handler = ImageRestHandler(imageDao, tagDao)

    val app = Javalin.create()
            .port(8100)
            .start()

    app.get("/api/v1/images") { ctx -> handle(handler::getAllImages, ctx) }
    app.get("/api/v1/images/id/:id") { ctx -> handle(handler::getImageById, ctx) }
    app.get("/api/v1/images/:type/:arg") { ctx -> handle(handler::getImagesBySimpleQuery, ctx) }

    app.get("/api/v1/tags") { ctx -> handle(handler::getAllTags, ctx) }
    app.get("/api/v1/imagedata/:id") { ctx -> handle(handler::getImageDataById, ctx) }

    app.post("/api/v1/images") { ctx -> handle(handler::getImagesByCompositeQuery, ctx) }

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