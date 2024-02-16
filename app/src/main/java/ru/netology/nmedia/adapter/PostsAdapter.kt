package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import java.math.BigDecimal
import java.math.RoundingMode

interface onInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
}


class PostsAdapter(private val onInteractionListener: onInteractionListener) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(view, onInteractionListener)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: onInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            published.text = post.published

            content.text = post.content

            like.text = post.likes.format()
            share.text = post.shares.format()
            viewsCount.text = post.views.format()

            like.isChecked = post.likedByMe

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when(item.itemId) {
                            R.id.remove  -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit  -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

fun Int.format(): String {
    if (this < 1_000) return this.toString()
    if (this < 1_100) return "1K"
    if (this < 10_000) return "%2.1fK".format(
        (this.toBigDecimal().divide(BigDecimal(1000), 1, RoundingMode.DOWN).toDouble())
    )
    if (this < 1_000_000) return "%dK".format(this / 1000)
    if (this < 1_000_000_000) return "%3.1fM".format(
        (this.toBigDecimal().divide(BigDecimal(1_000_000), 1, RoundingMode.DOWN).toDouble())
    )
    return "###" // Number too big!
}

object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
}