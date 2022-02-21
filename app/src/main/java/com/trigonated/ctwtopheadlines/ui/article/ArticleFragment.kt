package com.trigonated.ctwtopheadlines.ui.article

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.trigonated.ctwtopheadlines.R
import com.trigonated.ctwtopheadlines.databinding.FragmentArticleBinding
import com.trigonated.ctwtopheadlines.misc.Result
import com.trigonated.ctwtopheadlines.misc.collectIn
import com.trigonated.ctwtopheadlines.ui.misc.IntentUtils
import com.trigonated.ctwtopheadlines.ui.misc.srcUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArticleFragment : Fragment() {
    private val viewModel: ArticleViewModel by viewModels()
    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!
    private var isOpenInBrowserActionAvailable: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Setup the UI
        requireActivity().actionBar?.setDisplayShowHomeEnabled(true)
        setHasOptionsMenu(true)
        requireActivity().setTitle(R.string.source_name)
        _binding = FragmentArticleBinding.inflate(inflater, container, false)

        // UI Events
        lifecycleScope.launch {
            whenCreated { viewModel.onCreated() }
        }
        binding.errorLayout.refreshButton.isVisible = false
        binding.body.readFullArticleButton.setOnClickListener { viewModel.openInBrowser() }

        // Observe the ViewModel
        viewModel.loadingStatus.collectIn(viewLifecycleOwner) { loadingStatus ->
            // Update the UI states
            binding.errorLayout.root.isVisible = (loadingStatus == Result.Status.ERROR)
            binding.loadingLayout.root.isVisible = (loadingStatus == Result.Status.LOADING)
            binding.contentLayout.isVisible = (loadingStatus == Result.Status.SUCCESS)
        }
        viewModel.article.collectIn(viewLifecycleOwner) { article ->
            // Update the header
            binding.header.image.srcUrl = article.thumbnailUrl
            // Update the body
            binding.body.titleText.text = article.title ?: getString(R.string.fragment_article_no_title)
            binding.body.descriptionText.isVisible = (article.description != null)
            binding.body.descriptionText.text = article.description
            binding.body.contentText.text = article.content ?: getString(R.string.fragment_article_no_content)
            binding.body.readFullArticleButton.isVisible =
                ((article.isContentTruncated) && (article.url != null))
            // Update the toolbar
            isOpenInBrowserActionAvailable = (article.url != null)
            activity?.invalidateOptionsMenu()
        }
        viewModel.onNavigationToUrlRequested.collectIn(viewLifecycleOwner) { url ->
            IntentUtils.openUrl(requireContext(), url)
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_article, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_open_in_browser).isVisible = isOpenInBrowserActionAvailable
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open_in_browser -> {
                viewModel.openInBrowser()
                true
            }
            else -> false
        }
    }
}