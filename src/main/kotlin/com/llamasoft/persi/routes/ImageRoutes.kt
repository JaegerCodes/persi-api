package com.llamasoft.persi.routes

import com.llamasoft.persi.api.ImageNasa
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.File
import java.text.SimpleDateFormat

const val nasaUrl = "https://mars.nasa.gov/mars2020/multimedia/raw-images/image-of-the-week"
val domQuery = Jsoup.connect(nasaUrl).get()?.select(".main_image > img")

fun Route.getWeeklyImage() {
    get("/") {

        val sdf = SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
        val currentDate = sdf.format(java.util.Date())

        val file = File("D:/${currentDate}.txt")

        val urlImage = if (file.exists()) {
            file.readLines().firstOrNull()?: ""
        } else {
            GlobalScope.async {
                return@async scrapImageFromNasaWeb()
            }.await().also {
                file.writeText(it)
            }
        }

        if (urlImage != "") {
            call.respond(ImageNasa(url = urlImage))
        } else {
            return@get call.respondText(
                "Not Found",
                status = HttpStatusCode.NotFound
            )
        }
    }
}

suspend fun scrapImageFromNasaWeb(): String {
    return withContext(Dispatchers.Default) {
        domQuery?.attr("src")?: ""
    }
}

fun Application.registerRoutes() {
    routing {
        getWeeklyImage()
    }
}