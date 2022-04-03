package models

import com.google.gson.Gson

class ScreenshotResponse(
    var count: Int,
    var next: String?,
    var previous: String?,
    var results: MutableList<Screenshot>) {

    override fun toString(): String {
        return Gson().toJson(this)

    }
}