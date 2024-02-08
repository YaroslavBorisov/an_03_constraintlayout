package ru.netology.nmedia.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel : PostViewModel by viewModels()
        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                likesCount.text = post.likes.format()
                sharesCount.text = post.shares.format()
                viewsCount.text = post.views.format()
                like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)

            }
        }

        binding.like.setOnClickListener {
            viewModel.like()
        }

        binding.share.setOnClickListener {
            viewModel.share()
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

}