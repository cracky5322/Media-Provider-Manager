/*
 * Copyright 2021 Green Mushroom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.gm.cleaner.plugin.module.appmanagement

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.transition.platform.MaterialContainerTransform
import me.gm.cleaner.plugin.R
import me.gm.cleaner.plugin.databinding.AppFragmentBinding
import me.gm.cleaner.plugin.ktx.*
import me.gm.cleaner.plugin.model.Template
import me.gm.cleaner.plugin.model.Templates
import me.gm.cleaner.plugin.module.ModuleFragment
import me.gm.cleaner.plugin.module.settings.CreateTemplateFragment
import rikka.recyclerview.fixEdgeEffect
import java.text.Collator

class AppFragment : ModuleFragment() {
    private val args: AppFragmentArgs by navArgs()
    private val navController by lazy { findNavController() }
    var lastTemplateName: String? = null

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = AppFragmentBinding.inflate(layoutInflater)

        val templatesAdapter = TemplatesAdapter(this)
        val templatesFooterAdapter = TemplatesFooterAdapter(this)
        val adapters =
            ConcatAdapter(AppHeaderAdapter(this), templatesAdapter, templatesFooterAdapter)
        val list = binding.list
        list.adapter = adapters
        list.layoutManager = GridLayoutManager(requireContext(), 1)
        list.setHasFixedSize(true)
        list.fixEdgeEffect(false)
        list.overScrollIfContentScrollsPersistent()
        list.addLiftOnScrollListener { appBarLayout.isLifted = it }
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                supportActionBar?.title = if (recyclerView.isItemCompletelyInvisible(0)) {
                    args.label
                } else {
                    navController.currentDestination?.label
                }
            }
        })
        list.fitsSystemWindowInsetBottom()
        list.addItemDecoration(DividerDecoration(list).apply {
            setDivider(resources.getDrawable(R.drawable.list_divider_material, null))
            setAllowDividerAfterLastItem(false)
        })

        binderViewModel.remoteSpCacheLiveData.observe(viewLifecycleOwner) {
            val currentList = prepareCurrentList()
            templatesAdapter.submitList(currentList)
            if (currentList.any { it.templateName == args.label }) {
                adapters.removeAdapter(templatesFooterAdapter)
            } else {
                adapters.addAdapter(templatesFooterAdapter)
            }
        }

        prepareSharedElementTransition(list)
        setFragmentResultListener(CreateTemplateFragment::class.java.name) { _, bundle ->
            lastTemplateName = bundle.getString(CreateTemplateFragment.KEY_TEMPLATE_NAME)
            var position = prepareCurrentList().indexOfFirst { it.templateName == lastTemplateName }
            if (position != -1) {
                position++
            } else {
                position = adapters.itemCount - templatesFooterAdapter.itemCount
                lastTemplateName = CreateTemplateFragment.NULL_TEMPLATE_NAME
            }
            prepareTransitions(list, position)
            postponeEnterTransition()
            scrollToPosition(list, position)
        }
        return binding.root
    }

    private fun prepareCurrentList(): List<Template> {
        val collator = Collator.getInstance()
        return Templates(binderViewModel.readSp(R.xml.template_preferences))
            .asSequence()
            .filter { it.applyToApp?.contains(args.pi.packageName) == true }
            .sortedWith { o1, o2 -> collator.compare(o1?.templateName, o2?.templateName) }
            .toList()
    }

    private fun prepareTransitions(list: RecyclerView, position: Int) {
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>, sharedElements: MutableMap<String, View>
            ) {
                val selectedViewHolder = list.findViewHolderForAdapterPosition(position) ?: return
                sharedElements[names[0]] = selectedViewHolder.itemView
            }
        })
    }

    private fun prepareSharedElementTransition(list: RecyclerView) {
        setFragmentResult(
            AppFragment::class.java.name, bundleOf(KEY_PACKAGENAME to args.pi.packageName)
        )
        list.transitionName = args.pi.packageName

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host
            setAllContainerColors(requireContext().colorSurface)
            interpolator = FastOutSlowInInterpolator()
            fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            duration = requireContext().mediumAnimTime
        }

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>, sharedElements: MutableMap<String, View>
            ) {
                if (names.isNotEmpty()) {
                    sharedElements[names[0]] = list
                }
            }
        })
    }

    private fun scrollToPosition(list: RecyclerView, position: Int) {
        list.doOnPreDraw {
            val layoutManager = list.layoutManager as? LinearLayoutManager ?: return@doOnPreDraw
            val viewAtPosition = layoutManager.findViewByPosition(position)
            // Scroll to position if the view for the current position is null (not currently part of
            // layout manager children), or it's not completely visible.
            if (viewAtPosition == null ||
                layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)
            ) {
                val lastPosition = layoutManager.findLastCompletelyVisibleItemPosition()
                if (position >= lastPosition && lastPosition - layoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
                    layoutManager.scrollToPosition(position)
                } else {
                    layoutManager.scrollToPositionWithOffset(position, list.paddingTop)
                }
            }
        }
    }

    companion object {
        const val KEY_PACKAGENAME = "me.gm.cleaner.plugin.key.packageName"
    }
}
