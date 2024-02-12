package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import java.math.BigDecimal
import java.math.RoundingMode

typealias onLikeListener = (Post) -> Unit
typealias onShareListener = (Post) -> Unit

class PostsAdapter(private val onLike: onLikeListener, private val onShare: onShareListener) :
    ListAdapter<Post, PostViewHolder>(PostDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(view, onLike, onShare)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onLike: onLikeListener,
    private val onShare: onShareListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likesCount.text = post.likes.format()
            sharesCount.text = post.shares.format()
            viewsCount.text = post.views.format()
            like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)
//            if (post.likedByMe) {
//                like.setImageResource(R.drawable.ic_liked_24)
//            }

            like.setOnClickListener {
                onLike(post)
            }

            share.setOnClickListener {
                onShare(post)
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