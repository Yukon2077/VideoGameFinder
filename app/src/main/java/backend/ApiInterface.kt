package backend

import models.Game
import models.GameResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface ApiInterface {

    @GET("games")
    fun getGames(@QueryMap options: Map<String, String>): Call<GameResponse>

    @GET("games/{id}")
    fun getGameDetails(@Path("id") id: Int ,@QueryMap options: Map<String, String>): Call<Game>
}