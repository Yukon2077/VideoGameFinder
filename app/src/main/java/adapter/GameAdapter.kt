package adapter

import activities.GameDetailActivity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.yukon.videogamefinder.R
import models.Game

class GameAdapter(private var gameList: List<Game>, private val viewType: Int): RecyclerView.Adapter<GameAdapter.ViewHolder>() {
    companion object {
        const val VIEW_TYPE_GRID = 0
        const val VIEW_TYPE_LIST = 1
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameAdapter.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LIST -> { ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_game_list, parent, false)) }
            else -> { ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_game_grid, parent, false)) }
        }
    }

    override fun onBindViewHolder(holder: GameAdapter.ViewHolder, position: Int) {
        holder.name.text = gameList[position].name
        Picasso.get()
            .load(gameList[position].background_image)
            .placeholder(R.drawable.splash_screen)
            .fit()
            .centerCrop()
            .into(holder.image)
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, GameDetailActivity::class.java)
            intent.putExtra("ID", gameList[position].id)
            intent.putExtra("GAME", gameList[position].toString())
            it.context.startActivity(intent)
        }
        val score = gameList[position].metacritic
        holder.metacritic.text = score.toString()
        when {
            score>=75 -> {
                holder.metacritic.backgroundTintList = holder.itemView.context.resources.getColorStateList(R.color.metacritic_high)
            }
            score>=50 -> {
                holder.metacritic.backgroundTintList = holder.itemView.context.resources.getColorStateList(R.color.metacritic_med)
            }
            score>=1 -> {
                holder.metacritic.backgroundTintList = holder.itemView.context.resources.getColorStateList(R.color.metacritic_low)
            }
            else -> {
                holder.metacritic.backgroundTintList = null
            }
        }
        if (viewType == VIEW_TYPE_LIST) {
            holder.releaseDate = holder.itemView.findViewById(R.id.description)
            holder.genre = holder.itemView.findViewById(R.id.genre)
            holder.platform = holder.itemView.findViewById(R.id.platform)
            holder.releaseDate.text = gameList[position].released
            holder.genre.text = gameList[position].genres.joinToString(separator = ", ")
            holder.platform.text = gameList[position].platforms.joinToString(separator = ", ")

        }
    }

    override fun getItemCount(): Int {
        return gameList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)
        var name: TextView = itemView.findViewById(R.id.name)
        var metacritic: TextView = itemView.findViewById(R.id.metacritic)
        lateinit var releaseDate: TextView
        lateinit var genre: TextView
        lateinit var platform: TextView

    }

}