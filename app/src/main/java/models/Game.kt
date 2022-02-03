package models

import com.google.gson.Gson

data class Game(
    val id : Int,
    val name : String,
    val slug : String,
    val description : String?,
    val released : String,
    val background_image : String,
    val metacritic : Int,
    val platforms : List<Platforms>,
    val genres : List<Genre>

    ) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}