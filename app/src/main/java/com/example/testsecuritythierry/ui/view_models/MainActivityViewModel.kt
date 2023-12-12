package com.example.testsecuritythierry.ui.view_models


import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.example.testsecuritythierry.R
import com.example.testsecuritythierry.data.config.AppConfig
import com.example.testsecuritythierry.data.models.DataNewsElement
import com.example.testsecuritythierry.data.models.DataNewsFull
import com.example.testsecuritythierry.data.repositories.NewsDataRepository
import com.example.testsecuritythierry.ui.MainActivity
import com.example.testsecuritythierry.ui.setup.safeSubList
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.SerializationFeature
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.security.KeyStore
import javax.inject.Inject


@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val newsDataRepository: NewsDataRepository,
) : ViewModel() {

    private lateinit var newsDataFlow: Flow<List<DataNewsElement>>
    var savedNewsData: List<DataNewsElement>? = null

    // ------------------------------------------
    // non flow variables
    private var initialized = false

    fun init(owner: LifecycleOwner) {
        if (initialized) {
            return
        }
        initialized = true

        newsDataFlow = newsDataRepository.getNewsFlow(owner)
        owner.lifecycleScope.launch {
            owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newsDataFlow.collect {
                    savedNewsData = it
                }
            }
        }
    }

    fun embedServer(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pass1 = "Pass1!!"
                val pass2 = "Pass2!!"
                val aliasName = "sampleAlias"

                // Android does not support JKS and this command here throws an exception: KeyStore.getInstance("JKS")
                // hence, we use BKS using ./mkcert.sh at the second link below
                // an also download bcprov-jdk18on-175.jar at the thrid link belows and rename it bcprov.jar where the mkcert.sh script lies
                // https://stackoverflow.com/questions/9312193/does-android-support-jks-keystore-type
                // https://stackoverflow.com/questions/70000973/how-to-add-https-to-ktor-server-running-on-android-device
                // https://www.bouncycastle.org/latest_releases.html
                // https://tomzurkan.medium.com/using-logback-with-android-to-extend-or-enhance-your-logging-6217bfd486dc
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                    context.resources.openRawResource(R.raw.keystore).use {
                        load(it, pass1.toCharArray())
                    }
                }
                val environment = applicationEngineEnvironment {
                    log = LoggerFactory.getLogger("com.example.testsecuritythierry")
                    connector {
                        port = 8080
                    }
                    sslConnector(
                        keyStore = keyStore,
                        keyAlias = aliasName,
                        keyStorePassword = { pass2.toCharArray() },
                        privateKeyPassword = { pass1.toCharArray() }) {
                        port = AppConfig.HTTPSPort
                    }
                    module {
                        install(ContentNegotiation) {
                            jackson {
                                // customize the Jackson serializer as usual
                                configure(SerializationFeature.INDENT_OUTPUT, true)
                                setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                                    indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
                                })
                            }
                        }
                        // this is an Application module
                        routing {
                            get("/news") {
                                try {
                                    // sleep to show the loading icon at the start
                                    Thread.sleep(1000)
                                    val pageSize: Int =
                                        call.request.queryParameters["pageSize"]?.toInt() ?: -1
                                    val pageOffset: Int =
                                        call.request.queryParameters["pageOffset"]?.toInt() ?: -1
                                    if (pageSize <= 0 || pageOffset <= 0) {
                                        call.respondText(
                                            text = "Invalid query parameters",
                                            status = HttpStatusCode.InternalServerError
                                        )
                                        return@get
                                    }
                                    // if missing parameters, return an error
                                    // or if the saved data is missing, return an error
                                    savedNewsData?.let { it1 ->
                                        call.respond(
                                            DataNewsFull(
                                                artObjects = it1.safeSubList(
                                                    (pageOffset - 1) * pageSize,
                                                    pageOffset * pageSize,
                                                )
                                            )
                                        )
                                    }
                                        ?: run {
                                            call.respondText(
                                                text = "Saved data not ready yet",
                                                status = HttpStatusCode.InternalServerError
                                            )
                                        }
                                } catch (e: Exception) {
                                    // future work: we should also log the error for archiving and debugging later
                                    call.respondText(
                                        text = "Unexpected error",
                                        status = HttpStatusCode.InternalServerError
                                    )
                                }
                            }
                        }
                    }
                }

                // https://diamantidis.github.io/2019/11/10/running-an-http-server-on-an-android-app
                embeddedServer(Netty, environment).start(wait = false)

            } catch (e: Exception) {
                Log.e(MainActivity::class.java.name, "exception", e);
            }
        }
    }
}
