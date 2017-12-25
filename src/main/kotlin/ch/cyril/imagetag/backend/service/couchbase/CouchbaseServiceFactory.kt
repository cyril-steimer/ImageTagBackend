package ch.cyril.imagetag.backend.service.couchbase

import ch.cyril.imagetag.backend.service.ImageDao
import ch.cyril.imagetag.backend.service.ServiceFactory
import ch.cyril.imagetag.backend.service.TagDao
import com.couchbase.client.java.Bucket
import com.couchbase.client.java.Cluster
import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment

class CouchbaseServiceFactory: ServiceFactory {

    val cluster: Cluster

    val images: Bucket
    val imageData: Bucket

    init {
        val env = DefaultCouchbaseEnvironment.builder().autoreleaseAfter(50_000).build()
        cluster = CouchbaseCluster.create(env, "127.0.0.1")
        cluster.authenticate("admin", "password")

        images = cluster.openBucket("images")
        images.bucketManager().createN1qlPrimaryIndex(true, false)

        imageData = cluster.openBucket("imageData")
    }

    override fun createImageDao(): ImageDao {
        return CouchbaseImageDao(images, imageData)
    }

    override fun createTagDao(): TagDao {
        return CouchbaseTagDao(images)
    }
}