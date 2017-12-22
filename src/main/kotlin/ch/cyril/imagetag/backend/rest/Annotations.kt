package ch.cyril.imagetag.backend.rest


annotation class Body

annotation class Header(val name: String)

annotation class PathParam(val name: String)

annotation class QueryParam(val name: String)