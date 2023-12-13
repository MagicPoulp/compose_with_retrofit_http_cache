package com.example.testcomposethierry.data.repositories

import com.example.testcomposethierry.data.models.DataResponseRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import javax.inject.Inject


class PersistentDataManager @Inject constructor(
    ) {
    private var realm: Realm
    private val responseHeaders = Headers.Builder()
        .add("content-type", "application/json")
        .add("strict-transport-security", "max-age=2592000")
        .add("request-context", "appId=cid-v1:5f69f122-c4a2-4f7d-8b1e-6d66af0e0e99")
        .add("x-powered-by", "ASP.NET")
        .add("x-cache", "CONFIG_NOCACHE")
        .build()

    init {
        val config = RealmConfiguration.create(schema = setOf(DataResponseRealm::class))
        realm = Realm.open(config)
    }

    fun saveResponse(url: String, response: Response) {
        try {
            response.body?.let { body2 ->
                val body3 = copyBody(body2, 1000000)
                body3?.let { body4 ->
                    realm.writeBlocking {
                        copyToRealm(
                            DataResponseRealm(
                                url = url,
                                body = body4.bytes(),
                            ), UpdatePolicy.ALL
                        )
                    }
                }
            }
        } catch(t: Throwable) {
            println(t.message)
        }
    }

    fun loadResponse(url: String, request: Request, response: Response?) : Response {
        try {
            return realm.writeBlocking {
                val data: DataResponseRealm = realm.query<DataResponseRealm>("url == $0", url).find().firstOrNull() ?: DataResponseRealm()
                val builder: Response.Builder = Response.Builder()
                builder
                    .protocol(response?.protocol ?: Protocol.HTTP_2)
                    .request(request)
                    .headers(responseHeaders)
                    .code(200)
                    .message("success")
                    .body(data.body.toResponseBody(String.format("application/json; charset=utf-8").toMediaType()))
                builder.build()
            }
        } catch(t: Throwable) {
            println(t.message)
            throw t
        }
    }

    /**
     * Returns a copy of `body` without consuming it. This buffers up to `limit` bytes of
     * `body` into memory.
     *
     * @throws IOException if `body` is longer than `limit`.
     */
    // https://github.com/square/okhttp/issues/1740
    @Throws(IOException::class)
    private fun copyBody(body: ResponseBody, limit: Long): ResponseBody? {
        val source = body.source()
        if (source.request(limit)) throw IOException("body too long!")
        val bufferedCopy: Buffer = source.buffer.clone()
        return bufferedCopy.asResponseBody(body.contentType(), body.contentLength())
    }

    fun close() {
        realm.close()
    }
}