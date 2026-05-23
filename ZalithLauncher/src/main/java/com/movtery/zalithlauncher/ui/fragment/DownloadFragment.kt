package com.movtery.zalithlauncher.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.databinding.FragmentDownloadBinding
import com.movtery.zalithlauncher.feature.log.Logging
import com.movtery.zalithlauncher.ui.fragment.download.resource.ModDownloadFragment
import com.movtery.zalithlauncher.ui.fragment.download.resource.ModPackDownloadFragment
import com.movtery.zalithlauncher.ui.fragment.download.resource.ResourcePackDownloadFragment
import com.movtery.zalithlauncher.ui.fragment.download.resource.ShaderPackDownloadFragment
import com.movtery.zalithlauncher.ui.fragment.download.resource.WorldDownloadFragment

class DownloadFragment : FragmentWithAnim(R.layout.fragment_download) {
    companion object {
        const val TAG = "DownloadFragment"
    }

    private lateinit var binding: FragmentDownloadBinding
    private var currentCategory = -1
    private lateinit var tabs: List<TextView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDownloadBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupSearch()
        // Load initial tab
        switchCategory(0)
    }

    private fun setupTabs() {
        tabs = listOf(
            binding.tabMods,
            binding.tabModpacks,
            binding.tabShaders,
            binding.tabResourcePacks,
            binding.tabWorlds
        )

        tabs.forEachIndexed { index, tab ->
            tab.setOnClickListener { switchCategory(index) }
        }
    }

    private fun switchCategory(index: Int) {
        if (index == currentCategory) return
        currentCategory = index

        val activeColor = resources.getColor(R.color.flix_accent, null)
        val inactiveColor = resources.getColor(R.color.flix_text_muted, null)

        tabs.forEachIndexed { i, tab ->
            tab.setTextColor(if (i == index) activeColor else inactiveColor)
        }

        try {
            val fragment = when (index) {
                0 -> ModDownloadFragment()
                1 -> ModPackDownloadFragment()
                2 -> ShaderPackDownloadFragment()
                3 -> ResourcePackDownloadFragment()
                4 -> WorldDownloadFragment()
                else -> ModDownloadFragment()
            }

            childFragmentManager.beginTransaction()
                .replace(R.id.content_area, fragment)
                .commit()
        } catch (e: Exception) {
            Logging.e("DownloadFragment", "Failed to switch category", e)
            Toast.makeText(requireContext(), "Failed to load content", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearch() {
        binding.searchText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(textView.text.toString())
                true
            } else {
                false
            }
        }

        binding.searchButton.setOnClickListener {
            performSearch(binding.searchText.text.toString())
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return

        try {
            val currentFragment = childFragmentManager.findFragmentById(R.id.content_area)
            if (currentFragment is SearchableFragment) {
                currentFragment.search(query)
            }
        } catch (e: Exception) {
            Logging.e("DownloadFragment", "Search failed", e)
            Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.downloadContent, Animations.BounceInRight))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.downloadContent, Animations.FadeOutLeft))
    }
}

interface SearchableFragment {
    fun search(query: String)
}
