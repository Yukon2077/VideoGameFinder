package adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.yukon.videogamefinder.R
import models.Screenshot

class ScreenshotAdapter(private var screenshots: List<Screenshot>): RecyclerView.Adapter<ScreenshotAdapter.ScreenshotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenshotAdapter.ScreenshotViewHolder {
        return ScreenshotViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_screenshot, parent, false))
    }

    override fun onBindViewHolder(holder: ScreenshotAdapter.ScreenshotViewHolder, position: Int) {
        Picasso.get()
            .load(screenshots[position].image)
            .placeholder(R.drawable.splash_screen)
            .fit()
            .centerCrop()
            .into(holder.image)
    }

    override fun getItemCount(): Int {
        return screenshots.size
    }

    class ScreenshotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById(R.id.image)

    }
}