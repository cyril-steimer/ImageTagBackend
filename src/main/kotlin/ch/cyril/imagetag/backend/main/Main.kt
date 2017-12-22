package ch.cyril.imagetag.backend.main

import ch.cyril.imagetag.backend.rest.*
import ch.cyril.imagetag.backend.service.filebased.FileBasedServiceFactory
import io.javalin.Context
import io.javalin.Javalin
import java.nio.file.Paths
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

fun main(args: Array<String>) {
    val dir = Paths.get("/Users/csteimer/projects/eduself/testFolder")
    val serviceFactory = FileBasedServiceFactory(dir)
    val tagDao = serviceFactory.createTagDao()
    val imageDao = serviceFactory.createImageDao()
    val queryFactory = serviceFactory.createQueryFactory()

    val handler = ImageRestHandler(imageDao, tagDao, queryFactory)

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

    app.put("/api/v1/images") { ctx -> handle(handler::updateImage, ctx) }
    app.put("/api/v1/images/upload") { ctx -> handle(handler::uploadImage, ctx) }

    app.delete("/api/v1/images/id/:id") { ctx -> handle(handler::deleteImageById, ctx) }
}

fun handle(function: KFunction<Any>, ctx: Context) {

    try {

        val params= function.parameters
                //TODO Check that it's a string?
                .filter { p -> p.name != null }
                .map { p -> getParamValue(p, ctx) }
                .toTypedArray()

        val res = function.call(*params)
        //TODO Fix.
        if (res is RestResult) {
            ctx.contentType(res.contentType)
            ctx.result(res.data)
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        ctx.response().sendError(HttpServletResponse.SC_BAD_REQUEST)
    }
}

fun getParamValue(param: KParameter, ctx: Context): Any? {
    var value = doGetParamValue(param, ctx)
    value = parseParamValue(param.type.classifier as KClass<*>, value)
    validateParamValue(param, value)
    return value
}

fun validateParamValue(param: KParameter, value: Any?) {
    val nullable = param.type.isMarkedNullable
    if (!nullable && value == null) {
        throw IllegalArgumentException("Parameter '$param' is not nullable")
    }
    val cls = param.type.classifier as KClass<*>
    if (value != null && !cls.isInstance(value)) {
        throw IllegalArgumentException("Value '$value' for param '$param' is of wrong type")
    }
}

fun parseParamValue(cls: KClass<*>, value: Any?): Any? {
    if (!String::class.isInstance(value)) {
        return value
    }
    val stringVal = value as String
    if (cls == Int::class) {
        return stringVal.toInt()
    } else if (cls == Long::class) {
        return stringVal.toLong()
    } else if (cls == Float::class) {
        return stringVal.toFloat()
    } else if (cls == Double::class) {
        return stringVal.toDouble()
    } else if (cls == Boolean::class) {
        return stringVal.toBoolean()
    }
    return stringVal
}

fun doGetParamValue(param: KParameter, ctx: Context): Any? {
    if (param.findAnnotation<Body>() != null) {
        if (param.type.classifier == ByteArray::class) {
            return ctx.bodyAsBytes()
        }
        return ctx.body()
    }
    val header = param.findAnnotation<Header>()
    if (header != null) {
        return ctx.header(header.name)
    }
    val pathParam = param.findAnnotation<PathParam>()
    if (pathParam != null) {
        return ctx.param(pathParam.name)
    }
    val queryParam = param.findAnnotation<QueryParam>()!!
    if (isMany(param.type.classifier as KClass<*>)) {
        val params = ctx.queryParams(queryParam.name)
        return params ?: emptyArray<String>()
    }
    return ctx.queryParam(queryParam.name)
}

fun isMany(cls: KClass<*>): Boolean {
    return cls.qualifiedName.equals("kotlin.Array")
}