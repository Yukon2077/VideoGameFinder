package activities

import adapter.GameAdapter
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import backend.ApiInterface
import backend.RetrofitHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.yukon.videogamefinder.R
import models.GameResponse
import retrofit2.Call
import retrofit2.Callback

class GameListActivity : AppCompatActivity(){

    companion object{
        lateinit var gameResult: GameResponse
        lateinit var options: HashMap<String, String>
        lateinit var gameAdapter: GameAdapter
        lateinit var recyclerView: RecyclerView
        var isLoading = false
        var viewType = GameAdapter.VIEW_TYPE_GRID
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_VideoGameFinder)
        setContentView(R.layout.activity_game_list)

        val result = intent.extras?.getString("RESULT")
        gameResult = Gson().fromJson(result, GameResponse::class.java)
        options = intent.extras?.getSerializable("OPTIONS") as HashMap<String, String>

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.grid_item -> {
                    it.isChecked = true
                    viewType = GameAdapter.VIEW_TYPE_GRID
                    initializeRecyclerView()
                    true
                }
                R.id.list_item -> {
                    it.isChecked = true
                    viewType = GameAdapter.VIEW_TYPE_LIST
                    initializeRecyclerView()
                    true
                }
                else -> super.onOptionsItemSelected(it)
            }
        }
        val count = gameResult.count
        toolbar.title = when (count) {
            1 -> "1 result"
            else -> "$count results"
        }
        initializeRecyclerView()

    }

    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        gameAdapter = GameAdapter(gameResult.results, viewType)
        if (viewType == GameAdapter.VIEW_TYPE_GRID) {
            recyclerView.layoutManager = GridLayoutManager(this, 2)
        } else if (viewType == GameAdapter.VIEW_TYPE_LIST) {
            recyclerView.layoutManager = LinearLayoutManager(this)
        }
        recyclerView.adapter = gameAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (viewType == GameAdapter.VIEW_TYPE_GRID) {
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == gameResult.results.size - 1 && !isLoading) {
                        isLoading = true
                        loadNextPage(getNextPageNumber(gameResult.next))
                    }
                } else if (viewType == GameAdapter.VIEW_TYPE_LIST) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if (layoutManager.findLastCompletelyVisibleItemPosition() == gameResult.results.size - 1 && !isLoading) {
                        isLoading = true
                        loadNextPage(getNextPageNumber(gameResult.next))
                    }
                }
            }
        })


    }

    private fun loadNextPage(next: String?) {
        if (next != null) {
            options += mapOf("page" to next)
            val result: Call<GameResponse> = RetrofitHelper.getInstance().create(ApiInterface::class.java).getGames(options)
            result.enqueue(object : Callback<GameResponse> {
                override fun onResponse(call: Call<GameResponse>, response: retrofit2.Response<GameResponse>) {
                    if (response.isSuccessful) {
                        val gameResponse = response.body()
                        if (gameResponse != null) {
                            val lastPosition = gameResult.results.size
                            gameResult.next = gameResponse.next
                            gameResult.results.addAll(gameResponse.results)
                            gameAdapter.notifyItemRangeInserted(lastPosition + 1, gameResponse.results.size)
                        }
                    }
                    isLoading = false
                }

                override fun onFailure(call: Call<GameResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG)
                        .show()
                    isLoading = false
                }
            })
        }
    }

    private fun getNextPageNumber(uriString: String?): String? {
        if (uriString == null) { return null }
        return Uri.parse(uriString).getQueryParameter("page").toString()
    }

}

