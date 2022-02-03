package activities

import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import backend.ApiInterface
import backend.RetrofitHelper
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.yukon.videogamefinder.R
import models.Game
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameDetailActivity : AppCompatActivity() {
    companion object {
        var id = 0
        lateinit var game: Game
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var gameCover: ImageView
    private lateinit var description: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_VideoGameFinder)
        setContentView(R.layout.activity_game_detail)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        gameCover = findViewById(R.id.toolbar_game_cover)
        description = findViewById(R.id.description)
        id = intent.extras?.getInt("ID")!!
        val gameString = intent.extras?.getString("GAME")!!
        game = Gson().fromJson(gameString, Game::class.java)

        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        collapsingToolbar.title = game.name
        Picasso.get()
            .load(game.background_image)
            .placeholder(R.drawable.splash_screen)
            .fit()
            .centerCrop()
            .into(gameCover)

        val options: HashMap<String, String> = hashMapOf("key" to MainActivity.KEY)
        val request = RetrofitHelper.getInstance().create(ApiInterface::class.java).getGameDetails(id, options)
        request.enqueue(object: Callback<Game> {
            override fun onResponse(call: Call<Game>, response: Response<Game>) {
                if (response.isSuccessful) {
                    game = response.body()!!
                    description.text  = Html.fromHtml(game.description)

                }
            }

            override fun onFailure(call: Call<Game>, t: Throwable) {
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG)
                    .show()
            }

        } )

    }
}