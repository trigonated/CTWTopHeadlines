package com.trigonated.ctwtopheadlines.ui.top_headlines

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trigonated.ctwtopheadlines.R
import com.trigonated.ctwtopheadlines.databinding.ListItemArticleBinding
import com.trigonated.ctwtopheadlines.model.objects.Article
import com.trigonated.ctwtopheadlines.ui.misc.srcUrl

class TopHeadlinesArticleListAdapter :
    ListAdapter<Article, TopHeadlinesArticleListAdapter.ArticleViewHolder>(ArticleDiffCallback()) {
    var onItemClickListener: ((item: Article) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            ListItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ArticleViewHolder(
        private val binding: ListItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var item: Article

        init {
            binding.card.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }

        fun bind(item: Article) {
            this.item = item
            binding.image.srcUrl = item.thumbnailUrl
            binding.titleText.text =
                item.title ?: binding.root.resources.getString(R.string.list_item_article_no_title)
        }
    }
}

private class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {

    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}