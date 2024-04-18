package ru.netology.nmedia.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.AppActivity
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils.format
import ru.netology.nmedia.util.load

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
            avatar.load("http://10.0.2.2:9999/avatars/${post.authorAvatar}")

            content.text = post.content

            like.text = post.likes.format()
            share.text = post.shares.format()
            view.text = post.views.format()

            like.isChecked = post.likedByMe

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }


            if (post.attachment == null) {
                attachmentGroup.visibility = View.GONE
            } else {
                attachmentGroup.visibility = View.VISIBLE
                attachmentImage.load("http://10.0.2.2:9999/images/${post.attachment.url}")
                avatar.contentDescription = post.attachment.description
            }


            if (post.videoUrl.isNullOrBlank()) {
                previewGroup.visibility = View.GONE
            } else {
                previewGroup.visibility = View.VISIBLE

                fun viewVideo() {
                    (root.context as AppActivity).viewVideo(post.videoUrl)
                }

                viewVideo.setOnClickListener() {
                    viewVideo()
                }
                videoPreview.setOnClickListener() {
                    viewVideo()
                }
            }
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
            binding.root.setOnClickListener {
                //Log.wtf("Test", "Test ${post.id}")
                binding.root.findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    bundleOf("postID" to post.id)
                )
            }

        }
    }

}


object PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Post, newItem: Post) = oldItem == newItem
}