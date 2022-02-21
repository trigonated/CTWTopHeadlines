package com.trigonated.ctwtopheadlines.ui.top_headlines

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.trigonated.ctwtopheadlines.R
import com.trigonated.ctwtopheadlines.databinding.FragmentTopHeadlinesBinding
import com.trigonated.ctwtopheadlines.misc.Result.Status
import com.trigonated.ctwtopheadlines.misc.collectIn
import com.trigonated.ctwtopheadlines.ui.misc.SimpleBiometricPrompt
import com.trigonated.ctwtopheadlines.ui.misc.SimpleBiometricPrompt.BiometricPromptResult.FAILED
import com.trigonated.ctwtopheadlines.ui.misc.addOnBottomReachedListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopHeadlinesFragment : Fragment() {
    private val viewModel: TopHeadlinesViewModel by viewModels()
    private var _binding: FragmentTopHeadlinesBinding? = null
    private val binding get() = _binding!!
    private val listAdapter: TopHeadlinesArticleListAdapter get() = binding.list.adapter as TopHeadlinesArticleListAdapter
    private lateinit var biometricPrompt: SimpleBiometricPrompt

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // UI Setup
        biometricPrompt = SimpleBiometricPrompt(this)
        requireActivity().setTitle(R.string.source_name)
        _binding = FragmentTopHeadlinesBinding.inflate(inflater, container, false).apply {
            list.adapter = TopHeadlinesArticleListAdapter()
        }

        // UI Events
        biometricPrompt.onAuthenticationCompleted = { result ->
            // Take an UNSUPPORTED result as SUCCESS
            viewModel.onAuthenticationResult(result != FAILED)
        }
        binding.authLayout.authenticateButton.setOnClickListener { viewModel.authenticate() }
        binding.errorLayout.refreshButton.setOnClickListener { viewModel.refresh() }
        binding.contentLayout.setOnRefreshListener { viewModel.refresh() }
        listAdapter.onItemClickListener = { viewModel.onItemClicked(it) }
        binding.list.addOnBottomReachedListener { viewModel.loadMoreItems() }

        // Observe the ViewModel
        viewModel.loadingStatus.collectIn(viewLifecycleOwner) { loadingStatus ->
            // Update the UI states
            binding.authLayout.root.isVisible = (loadingStatus == Status.AWAITING_USER_ACTION)
            binding.errorLayout.root.isVisible = (loadingStatus == Status.ERROR)
            binding.loadingLayout.root.isVisible = (loadingStatus == Status.LOADING)
            binding.contentLayout.isVisible = loadingStatus.hasData()
            // Update the content layout
            binding.contentLayout.isRefreshing = (loadingStatus == Status.REFRESHING)
            binding.listLoadingItem.root.isVisible = (loadingStatus == Status.LOADING_EXTRA_DATA)
        }
        viewModel.items.collectIn(viewLifecycleOwner) { items ->
            // Update the list
            listAdapter.submitList(items)
        }
        viewModel.onNonFatalErrorOccurred.collectIn(viewLifecycleOwner) {
            // A non-fatal (refresh/load more items) error occurred
            showError()
        }
        viewModel.onNavigationToArticleRequested.collectIn(viewLifecycleOwner) { articleId ->
            findNavController().navigate(
                TopHeadlinesFragmentDirections.actionTopHeadlinesFragmentToArticleFragment(articleId = articleId)
            )
        }
        viewModel.onAuthenticationRequested.collectIn(viewLifecycleOwner) {
            showBiometricPrompt()
        }
        return binding.root
    }

    /** Open a biometric authentication prompt, if supported/configured. */
    private fun showBiometricPrompt() {
        biometricPrompt.show()
    }

    /** Show a toast to notify the user of a non-fatal (refresh/load more items) error. */
    private fun showError() {
        Snackbar.make(
            binding.root,
            R.string.fragment_top_headlines_toast_message,
            BaseTransientBottomBar.LENGTH_SHORT
        ).show()
    }
}