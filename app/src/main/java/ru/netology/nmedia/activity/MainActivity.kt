package ru.netology.nmedia.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.onInteractionListener
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()

        val newPostLauncher = registerForActivityResult(NewPostContract) { result ->
            result ?: return@registerForActivityResult
            viewModel.updateSave(result)
        }

        val adapter = PostsAdapter(object : onInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
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
                newPostLauncher.launch(post.content)
            }
        })

        viewModel.data.observe(this) { posts ->
            val isNewPostAdded =
                adapter.currentList.size < posts.size && adapter.currentList.size > 0
            adapter.submitList(posts) {
                if (isNewPostAdded) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

//        viewModel.edited.observe(this) { post ->
//            if (post.id != 0L) {
//                binding.editGroup.visibility = View.VISIBLE
//                binding.contentPreview.setText(post.content)
//                binding.edit.focusAndShowKeyboard()
//            } else {
//                binding.editGroup.visibility = View.GONE
//            }
//            binding.edit.setText(post.content)
//        }

        binding.list.adapter = adapter

        binding.add.setOnClickListener {
            newPostLauncher.launch(null)
        }

//        binding.cancelEdit.setOnClickListener {
//            viewModel.cancelEdit()
//        }
    }

}