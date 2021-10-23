package com.mashup.lastgarden.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.mashup.base.autoCleared
import com.mashup.base.image.GlideRequests
import com.mashup.lastgarden.databinding.FragmentMainBinding
import com.mashup.lastgarden.ui.BaseViewModelFragment
import com.mashup.lastgarden.ui.editor.EditorActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseViewModelFragment() {

    private var binding by autoCleared<FragmentMainBinding>()

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var glideRequests: GlideRequests

    private val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.getUriFilePath(requireContext())?.let(::showEditorActivity)
        }
    }

    @Inject
    lateinit var todayPerfumeStoryAdapter: TodayPerfumeStoryAdapter

    @Inject
    lateinit var hotStoryAdapter: HotStoryAdapter

    @Inject
    lateinit var rankingAdapter: PerfumeRankingAdapter

    @Inject
    lateinit var recommendAdapter: PerfumeRecommendAdapter

    private lateinit var adapter: MainAdapter

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        adapter = MainAdapter(
            glideRequests = glideRequests,
            todayPerfumeStoryAdapter = todayPerfumeStoryAdapter,
            hotStoryAdapter = hotStoryAdapter,
            rankingAdapter = rankingAdapter,
            recommendAdapter = recommendAdapter
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    private fun showImagePicker() {
        cropImageLauncher.launch(options { setGuidelines(CropImageView.Guidelines.ON) })
    }

    private fun showEditorActivity(imageUrl: String) {
        EditorActivity.createIntent(requireContext(), imageUrl).run {
            startActivity(this)
        }
    }

    override fun onSetupViews(view: View) {
        super.onSetupViews(view)
        binding.recyclerView.adapter = adapter
    }

    override fun onBindViewModelsOnCreate() {
        lifecycleScope.launchWhenCreated {
            viewModel.mainItems
                .filterNotNull()
                .collectLatest {
                    adapter.submitList(it)
                }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.todayPerfumeStoriesItem
                .filterNotNull()
                .collectLatest {
                    todayPerfumeStoryAdapter.submitList(it.storyItems)
                }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.hotStories
                .filterNotNull()
                .collectLatest {
                    hotStoryAdapter.submitList(it.stories)
                }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.rankingsItem
                .filterNotNull()
                .collectLatest {
                    rankingAdapter.submitList(it.perfumeItems)
                }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.recommendsItem
                .filterNotNull()
                .collectLatest {
                    recommendAdapter.submitList(it.perfumeItems)
                }
        }
    }
}