package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.util.AndroidUtils.format
import ru.netology.nmedia.util.load
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

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

        val dependencyContainer = DependencyContainer.getInstance()

        val viewModel: PostViewModel by viewModels(
            ownerProducer = ::requireParentFragment,
            factoryProducer = {
                ViewModelFactory(dependencyContainer.repository, dependencyContainer.appAuth)
            })

        val post = viewModel.data.value?.posts?.find { it.id == postID }

        if (post != null) {
            refresh(binding, viewModel)

            with(binding) {

                like.setOnClickListener {
                    //onInteractionListener.onLike(post)
                    like.isChecked = post.likedByMe
                    if (DependencyContainer.getInstance().appAuth.isAuthorized) {
                        viewModel.likeById(post.id, post.likedByMe)
                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.not_authorized),
                            Snackbar.LENGTH_SHORT
                        )
                            .setAction(R.string.sign_in) {
                                findNavController().navigate(R.id.action_global_loginFragment)
                            }
                            //.setAnchorView(binding.add)
                            .show()
                    }
                }

                share.setOnClickListener {
                    //onInteractionListener.onShare(post)
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, post.content)
                    }
                    val chooser =
                        Intent.createChooser(intent, getString(R.string.chooser_share_post))
                    startActivity(chooser)
                    viewModel.shareById(post.id)

                }

                attachmentImage.setOnClickListener {
                    binding.root.findNavController().navigate(
                        R.id.action_postFragment_to_attachmentFragment,
                        bundleOf("attachmentUri" to post.attachment?.url)
                    )
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


                menu.isVisible = post.ownedByMe

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
            val modelState = viewModel.dataState.value ?: return

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
                    attachmentImage.load("http://10.0.2.2:9999/media/${post.attachment.url}")
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