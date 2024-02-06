package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            likes = 9999,
            shares = 999999
        )

        with(binding) {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            likesCount.text = post.likes.format()
            sharesCount.text = post.shares.format()
            viewsCount.text = post.views.format()

            if (post.likedByMe) {
                like.setImageResource(R.drawable.ic_liked_24)
            }

            like.setOnClickListener {
                post.likedByMe = !post.likedByMe
                like.setImageResource(if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24)
                if (post.likedByMe) post.likes++ else post.likes--
                likesCount.text = post.likes.format()
//                Log.d("Clicks", "like_onclick")
            }

            share.setOnClickListener {
                post.shares++
                sharesCount.text = post.shares.format() //post.shares.format()
            }

//            root.setOnClickListener{
//                Log.d("Clicks", "root_onclick")
//            }

//            avatar.setOnClickListener{
//                Log.d("Clicks", "avatar_onclick")
//            }
        }
    }

    fun Int.format() : String {
        if(this<1_000) return this.toString()
        if(this<1_100) return "1K"
        if(this<10_000) return "%2.1fK".format((this.toBigDecimal().divide(BigDecimal(1000), 1, RoundingMode.DOWN).toDouble()))
        if(this<1_000_000) return "%dK".format(this/1000)
        if(this<1_000_000_000) return "%3.1fM".format((this.toBigDecimal().divide(BigDecimal(1_000_000), 1, RoundingMode.DOWN).toDouble()))
        return "###" // Number too big!
    }

}