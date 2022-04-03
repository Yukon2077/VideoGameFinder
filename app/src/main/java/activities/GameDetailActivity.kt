package activities

import adapter.ScreenshotAdapter
import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import backend.ApiInterface
import backend.RetrofitHelper
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.yukon.videogamefinder.R
import models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GameDetailActivity : AppCompatActivity() {
    companion object {
        var id = 0
        lateinit var game: Game
        lateinit var screenshotResponse: ScreenshotResponse
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var options: HashMap<String, String>
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var gameCover: ImageView
    private lateinit var description: TextView
    private var isLoading: Boolean = true

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

        generateChips(game.genres, findViewById(R.id.genre_chip_group))
        generateChips(game.platforms, findViewById(R.id.platforms_chip_group))

        options = hashMapOf("key" to MainActivity.KEY)
        val gameDetailRequest = RetrofitHelper.getInstance().create(ApiInterface::class.java).getGameDetails(id, options)
        gameDetailRequest.enqueue(object: Callback<Game> {
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

        val gameScreenshotRequest = RetrofitHelper.getInstance().create(ApiInterface::class.java).getGameScreenshots(id, options)
        gameScreenshotRequest.enqueue(object: Callback<ScreenshotResponse> {
            override fun onResponse(call: Call<ScreenshotResponse>, response: Response<ScreenshotResponse>) {
                if (response.isSuccessful) {
                    screenshotResponse = response.body()!!
                    val recyclerView = findViewById<RecyclerView>(R.id.screenshots_recyclerview)
                    val layoutManager = LinearLayoutManager(this@GameDetailActivity, RecyclerView.HORIZONTAL, false)
                    recyclerView.layoutManager = layoutManager
                    val adapter = ScreenshotAdapter(screenshotResponse.results)
                    recyclerView.adapter = adapter
                    isLoading = false
                    if (screenshotResponse.next != null) {
                        options += mapOf("page" to screenshotResponse.next.toString())
                        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                            override fun onScrollStateChanged(
                                recyclerView: RecyclerView,
                                newState: Int
                            ) {
                                if (layoutManager.findLastCompletelyVisibleItemPosition() == screenshotResponse.results.size - 1 && !isLoading) {
                                    isLoading = true
                                    loadMoreScreenshots()
                                }

                            }
                        })
                    }

                }
            }

            override fun onFailure(call: Call<ScreenshotResponse>, t: Throwable) {
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG)
                    .show()
                isLoading = false
            }

        })

    }

    private fun loadMoreScreenshots() {
        if (screenshotResponse.next == null) return
        options["page"] = screenshotResponse.next.toString()
        val gameScreenshotRequest = RetrofitHelper.getInstance().create(ApiInterface::class.java).getGameScreenshots(id, options)
        gameScreenshotRequest.enqueue(object: Callback<ScreenshotResponse> {
            override fun onResponse(call: Call<ScreenshotResponse>, response: Response<ScreenshotResponse>) {
                if (response.isSuccessful) {
                    val newScreenshotResponse = response.body()
                    val positionStart = screenshotResponse.results.size + 1
                    screenshotResponse.results += newScreenshotResponse!!.results
                    screenshotResponse.next = newScreenshotResponse?.next
                    val itemCount = newScreenshotResponse?.results?.size
                    findViewById<RecyclerView>(R.id.screenshots_recyclerview).adapter?.notifyItemRangeInserted(positionStart, itemCount!!)
                    isLoading = false
                }
            }

            override fun onFailure(call: Call<ScreenshotResponse>, t: Throwable) {
                Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG)
                    .show()
                isLoading = false
            }

        })
    }

    private fun generateChips(list: List<Any>, chipGroup: ChipGroup) {
        list.forEach {
            val chip = Chip(chipGroup.context)
            if (it is Genre) {
                chip.text = it.name
            } else if (it is Platforms) {
                chip.text = it.platform.name
            }
            chip.setEnsureMinTouchTargetSize(false)
            chip.setChipDrawable(
                ChipDrawable.createFromAttributes(
                    chipGroup.context, null, 0,
                    R.style.Widget_Material3_Chip_Assist
                )
            )
            chipGroup.addView(chip)
        }
    }


}