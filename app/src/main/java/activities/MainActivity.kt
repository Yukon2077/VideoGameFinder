package activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import backend.ApiInterface
import backend.RetrofitHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.slider.RangeSlider
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yukon.videogamefinder.R
import models.GameResponse
import models.Genre
import models.Platforms
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        lateinit var GenreList: List<Genre>
        lateinit var PlatformList: List<Platforms.Platform>
        const val KEY = "02a402186378439dbd7845e8b7083c57"
        var isLoading = false
        var id = 1
        val sortItems = listOf(
            "default",
            "name",
            "released",
            "added",
            "created",
            "updated",
            "rating",
            "metacritic"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_VideoGameFinder)
        setContentView(R.layout.activity_main)

        findViewById<MaterialButton>(R.id.search_button).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.start_date_button).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.end_date_button).setOnClickListener(this)

        val adapter = ArrayAdapter(this, R.layout.sort_list_item, sortItems)
        val sortByTextInputLayout = findViewById<TextInputLayout>(R.id.sort_by_text_input_layout)
        val sortByCheckBox = findViewById<MaterialCheckBox>(R.id.sort_by_checkbox)
        (sortByTextInputLayout.editText as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setText(sortItems[0], false)
        }

        sortByTextInputLayout.editText?.addTextChangedListener(
            afterTextChanged = {
                sortByCheckBox.isEnabled = it.toString() != "default"
            }
        )

        getGameAttributesFromFile()
        generateChips(GenreList, findViewById(R.id.genre_chip_group))
        generateChips(PlatformList, findViewById(R.id.platforms_chip_group))

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.clear_text -> {
                    clearAllFilters()
                    true
                }
                else -> {
                    false
                }
            }
        }

    }

    override fun onClick(view: View?) {
        if (view is MaterialButton)
            when (view.id) {
                R.id.search_button -> { search() }
                R.id.start_date_button, R.id.end_date_button -> { setDate(view) }
            }
    }

    private fun search() {
        if (!isLoading) {
            isLoading = true
            val options: HashMap<String, String> = hashMapOf("key" to KEY)
            val nameTextInputLayout = findViewById<TextInputLayout>(R.id.name_text_input_layout)
            val ordering = findViewById<TextInputLayout>(R.id.sort_by_text_input_layout)
            val sortByCheckBox = findViewById<MaterialCheckBox>(R.id.sort_by_checkbox)
            val genreChipGroup = findViewById<ChipGroup>(R.id.genre_chip_group)
            val platformChipGroup = findViewById<ChipGroup>(R.id.platforms_chip_group)
            val metacriticRangeSlider = findViewById<RangeSlider>(R.id.metacritic_seekbar)
            val startDateButton = findViewById<MaterialButton>(R.id.start_date_button)
            val endDateButton = findViewById<MaterialButton>(R.id.end_date_button)

            val name: String = nameTextInputLayout.editText?.text.toString()
            if (name.isNotBlank()) {
                options += mapOf("search" to name)
            }

            var sortBy: String = (ordering.editText as? AutoCompleteTextView)?.text.toString()

            if (sortBy != "default") {
                if (sortByCheckBox.isChecked) {
                    sortBy = "-$sortBy"
                }
                options += mapOf("ordering" to sortBy)
            }

            val genreChipIdList = genreChipGroup.checkedChipIds
            val genres: MutableSet<Int> = mutableSetOf()
            genreChipIdList.forEach { id ->
                val genreName = findViewById<Chip>(id).text
                GenreList.forEach {
                    if (it.equals(genreName)) {
                        genres.add(it.id)
                    }
                }
            }

            if (genres.isNotEmpty()) {
                options += mapOf("genres" to genres.joinToString(separator = ","))
            }

            val platformChipIdList = platformChipGroup.checkedChipIds
            val platforms: MutableSet<Int> = mutableSetOf()
            platformChipIdList.forEach { id ->
                val platformName = findViewById<Chip>(id).text
                PlatformList.forEach {
                    if (it.equals(platformName)) {
                        platforms.add(it.id)
                    }
                }
            }

            if (platforms.isNotEmpty()) {
                options += mapOf("platforms" to platforms.joinToString(separator = ","))
            }

            val minMetacriticScore = metacriticRangeSlider.values[0].toInt()
            val maxMetacriticScore = metacriticRangeSlider.values[1].toInt()
            options += mapOf("metacritic" to "$minMetacriticScore,$maxMetacriticScore")

            var startDate = startDateButton.text.toString()
            var endDate = endDateButton.text.toString()
            if (startDate != resources.getString(R.string.start_date) || endDate != resources.getString(
                    R.string.end_date
                )
            ) {
                if (startDate == resources.getString(R.string.start_date)) {
                    startDate = "1900-01-01"
                }
                if (endDate == resources.getString(R.string.end_date)) {
                    endDate = "2100-12-31"
                }
                options += mapOf("dates" to "$startDate,$endDate")
            }

            /*Toast.makeText(applicationContext, options.toString(), Toast.LENGTH_LONG).show()*/
            val request = RetrofitHelper.getInstance().create(ApiInterface::class.java).getGames(options)
            request.enqueue(object : Callback<GameResponse> {
                override fun onResponse(
                    call: Call<GameResponse>,
                    response: Response<GameResponse>
                ) {
                    if (response.isSuccessful) {
                        val gameResponse = response.body()
                        val intent = Intent(applicationContext, GameListActivity::class.java)
                            .putExtra("RESULT", gameResponse.toString())
                            .putExtra("OPTIONS", options)
                        startActivity(intent)
                        isLoading = false
                    }
                }

                override fun onFailure(call: Call<GameResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, t.toString(), Toast.LENGTH_LONG)
                        .show()
                    isLoading = false
                }
            })
        }
    }

    private fun setDate(view: MaterialButton) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build().apply {
                this.addOnPositiveButtonClickListener {
                    val requestFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    view.text = requestFormat.format(Date(it))
                }
                this.show(supportFragmentManager, "Date")
            }
    }

    private fun clearAllFilters() {
        findViewById<ChipGroup>(R.id.genre_chip_group).clearCheck()
        findViewById<ChipGroup>(R.id.platforms_chip_group).clearCheck()
        findViewById<RangeSlider>(R.id.metacritic_seekbar).values = listOf(0.0.toFloat(), 100.0.toFloat())
        findViewById<TextInputLayout>(R.id.name_text_input_layout).editText?.text = null
        findViewById<MaterialButton>(R.id.start_date_button).text = resources.getString(R.string.start_date)
        findViewById<MaterialButton>(R.id.end_date_button).text = resources.getString(R.string.end_date)
        (findViewById<TextInputLayout>(R.id.sort_by_text_input_layout).editText as? AutoCompleteTextView)?.setText(sortItems[0], false)
        findViewById<MaterialCheckBox>(R.id.sort_by_checkbox).apply {
            this.isChecked = true
            this.isEnabled = false
        }

    }

    private fun getGameAttributesFromFile() {
        val fileNames = listOf<String>("genre.json", "platform.json")
        fileNames.forEach { fileName ->
            val contents = assets.open(fileName)
                .bufferedReader()
                .use { it.readText() }

            if (fileName == "genre.json") {
                GenreList = Gson().fromJson(contents, object : TypeToken<List<Genre>>() {}.type)
            } else if (fileName == "platform.json") {
                PlatformList = Gson().fromJson(contents, object : TypeToken<List<Platforms.Platform>>() {}.type)
            }
        }

    }

    private fun generateChips(list: List<Any>, chipGroup: ChipGroup) {
        list.forEach {
            val chip = Chip(chipGroup.context)
            if (it is Genre) {
                chip.text = it.name
            } else if (it is Platforms.Platform) {
                chip.text = it.name
            }
            chip.isCheckable = true
            chip.isClickable = true
            chip.id = id
            id += 1
            chip.setEnsureMinTouchTargetSize(false)
            chip.setChipDrawable(
                ChipDrawable.createFromAttributes(
                    chipGroup.context, null, 0,
                    R.style.Widget_Material3_Chip_Filter
                )
            )
            chipGroup.addView(chip)
        }
    }

}