package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.databinding.FragmentAttachmentBinding
import ru.netology.nmedia.util.load

private const val ARG_ATTACHMENT_URI = "attachmentUri"

@AndroidEntryPoint
class AttachmentFragment: Fragment() {
    private var uri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            uri = it.getString(ARG_ATTACHMENT_URI)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAttachmentBinding.inflate(inflater, container, false)

        binding.imageAttachment.load("http://10.0.2.2:9999/media/${uri}")

        return binding.root
    }
}