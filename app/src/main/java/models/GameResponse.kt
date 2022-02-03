package models

import com.google.gson.Gson

data class GameResponse(
    var count: Int,
    var next: String?,
    var previous: String?,
    var results: MutableList<Game>

    ) {
    override fun toString(): String {
        return Gson().toJson(this)

    }

}