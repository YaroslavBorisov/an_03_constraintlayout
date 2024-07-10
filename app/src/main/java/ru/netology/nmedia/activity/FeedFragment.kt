package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.onInteractionListener
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory

class FeedFragment : Fragment() {
    private val dependencyContainer = DependencyContainer.getInstance()

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment,
        factoryProducer = {
            ViewModelFactory(dependencyContainer.repository, dependencyContainer.appAuth)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)



        val adapter = PostsAdapter(object : onInteractionListener {
            override fun onLike(post: Post) {
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

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }
                val chooser = Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(chooser)
                viewModel.shareById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    })
            }
        })

        binding.list.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            val isNewPostAdded =
                adapter.currentList.size < state.posts.size && adapter.currentList.size > 0
            adapter.submitList(state.posts) {
                if (isNewPostAdded) {
                    binding.list.smoothScrollToPosition(0)
                }
            }

            binding.emptyText.isVisible = state.empty

        }

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            //binding.errorGroup.isVisible = state.error
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_updating, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        viewModel.refreshPosts()
                    }
                    //.setAnchorView(binding.add)
                    .show()
            }
        }

        viewModel.newerCount.observe(viewLifecycleOwner) { newMsgCount ->
//            if(newMsgCount == 0) {
//                binding.newPosts.isVisible = false
//                return@observe
//            }
            binding.newPosts.isVisible = (newMsgCount != 0)

            Log.d("FeedFragment", "Newer count: $newMsgCount")
        }

        binding.newPosts.setOnClickListener {
            binding.newPosts.isVisible = false
            viewModel.showHiddenPosts()
        }

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if(positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
                super.onItemRangeInserted(positionStart, itemCount)
            }
        })

        binding.retry.setOnClickListener {
            viewModel.load()
        }

        binding.add.setOnClickListener {
            if (DependencyContainer.getInstance().appAuth.isAuthorized) {
                viewModel.clearEditedPost()
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.not_authorized),
                    Snackbar.LENGTH_SHORT
                )
                    .setAction(R.string.sign_in) {
                        findNavController().navigate(R.id.action_global_loginFragment)
                    }
                    .setAnchorView(binding.add)
                    .show()
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }

        return binding.root
    }
}