package com.example.testsecuritythierry.data.repositories

import com.example.testsecuritythierry.data.models.DataResponseRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.UpdatePolicy
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import java.io.IOException
import javax.inject.Inject


class PersistentDataManager @Inject constructor(
    ) {
    private var realm: Realm

    init {
        val config = RealmConfiguration.create(schema = setOf(DataResponseRealm::class))
        realm = Realm.open(config)
    }

    fun saveResponse(url: String, response: Response) {
        try {
            response.body?.let { body2 ->
                val body3 = copyBody(body2, 1000000)
                body3?.let { body4 ->
                    //val data = realm.query<DataResponseRealm>("url == $0", url).find().first()
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
        return ResponseBody.create(body.contentType(), body.contentLength(), bufferedCopy)
    }

    fun close() {
        realm.close()
    }
}