package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.onInteractionListener
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.AndroidUtils.focusAndShowKeyboard
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()

        val adapter = PostsAdapter(object : onInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                viewModel.shareById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
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

        viewModel.edited.observe(this) { post ->
            if (post.id != 0L) {
                binding.editGroup.visibility = View.VISIBLE
                binding.contentPreview.setText(post.content)
                binding.edit.focusAndShowKeyboard()
            } else {
                binding.editGroup.visibility = View.GONE
            }
            binding.edit.setText(post.content)
        }

        binding.list.adapter = adapter

        binding.save.setOnClickListener {
            val text = binding.edit.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, R.string.error_empty_content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.updateSave(text)

            binding.edit.setText("")
            binding.edit.clearFocus()
            AndroidUtils.hideKeyboard(it)

        }
        binding.cancelEdit.setOnClickListener {
            viewModel.cancelEdit()
        }
    }

}