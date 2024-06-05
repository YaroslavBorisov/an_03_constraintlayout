package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.util.AndroidUtils.format
import ru.netology.nmedia.util.load
import ru.netology.nmedia.viewmodel.PostViewModel

private const val ARG_POST_ID = "postID"

class PostFragment : Fragment() {
    private var postID: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postID = it.getLong(ARG_POST_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(inflater, container, false)

        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val post = viewModel.data.value?.posts?.find { it.id == postID }

        if (post != null) {
            refresh(binding, viewModel)

            with(binding) {

                like.setOnClickListener {
                    //onInteractionListener.onLike(post)
                    viewModel.likeById(post.id, post.likedByMe)
                }

                share.setOnClickListener {
                    //onInteractionListener.onShare(post)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, post.content)
                    }
                    val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(chooser)
                    viewModel.shareById(post.id)

                }

                if (!post.videoUrl.isNullOrBlank()) {

                    fun viewVideo() {
                        (binding.root.context as AppActivity).viewVideo(post.videoUrl)
                    }

                    binding.viewVideo.setOnClickListener {
                        viewVideo()
                    }
                    binding.videoPreview.setOnClickListener {
                        viewVideo()
                    }
                }
                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    //onInteractionListener.onRemove(post)
                                    viewModel.removeById(post.id)
                                    findNavController().navigateUp()
                                    true
                                }

                                R.id.edit -> {
                                    //onInteractionListener.onEdit(post)
                                    viewModel.edit(post)
                                    findNavController().navigate(R.id.action_postFragment_to_newPostFragment,
                                        Bundle().apply {
                                            textArg = post.content
                                        })
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

            }

            viewModel.data.observe(viewLifecycleOwner) {
                refresh(binding, viewModel)
            }

        }
        return binding.root
    }

    private fun refresh(binding: FragmentPostBinding, viewModel: PostViewModel) {
        with(binding) {
            val state = viewModel.data.value ?: return
            val modelState = viewModel.dataState.value ?:return

            errorGroup.isVisible = modelState.error
            progress.isVisible = modelState.loading
            emptyText.isVisible = state.empty

            val post = state.posts.find { it.id == postID }

            if (post != null) {
                author.text = post.author
                avatar.load("http://10.0.2.2:9999/avatars/${post.authorAvatar}")
                published.text = post.published

                content.text = post.content

                like.text = post.likes.format()
                share.text = post.shares.format()
                view.text = post.views.format()

                like.isChecked = post.likedByMe

                if (post.attachment == null) {
                    attachmentGroup.visibility = View.GONE
                } else {
                    attachmentGroup.visibility = View.VISIBLE
                    attachmentImage.load("http://10.0.2.2:9999/images/${post.attachment.url}")
                    avatar.contentDescription = post.attachment.description
                }

                if (post.videoUrl.isNullOrBlank()) {
                    binding.previewGroup.visibility = View.GONE
                } else {
                    binding.previewGroup.visibility = View.VISIBLE
                }
            }
        }
    }

}