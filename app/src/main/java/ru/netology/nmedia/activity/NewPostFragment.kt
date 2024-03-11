package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

class NewPostFragment : Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private lateinit var binding: FragmentNewPostBinding
    private var isNewPost: Boolean = false

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This callback is only called when MyFragment is at least started
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isNewPost) viewModel.saveDraft(binding.edit.text.toString())
            findNavController().navigateUp()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentNewPostBinding.inflate(inflater, container, false)

        arguments?.textArg?.let(binding.edit::setText) ?: run {
            binding.edit.setText(viewModel.getDraft())
            isNewPost = true
        }

        binding.ok.setOnClickListener {
            viewModel.updateSave(binding.edit.text.toString())
            viewModel.deleteDraft()
            AndroidUtils.hideKeyboard(requireView())
            findNavController().navigateUp()
        }
        return binding.root
    }
}

