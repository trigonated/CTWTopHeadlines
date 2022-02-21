package com.trigonated.ctwtopheadlines.ui.misc

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/** Extension for easily loading images from urls. */
var ImageView.srcUrl: String?
    get() = tag as? String?
    set(value) {
        tag = value
        Glide.with(context)
            .load(value)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }

/** Add a [listener] for when the bottom of the recyclerview is reached. */
fun RecyclerView.addOnBottomReachedListener(listener: () -> Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if ((!recyclerView.canScrollVertically(1)) && (newState == RecyclerView.SCROLL_STATE_IDLE)) {
                listener()
            }
        }
    })
}