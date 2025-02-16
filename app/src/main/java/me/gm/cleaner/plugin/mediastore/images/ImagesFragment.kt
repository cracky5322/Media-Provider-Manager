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

package me.gm.cleaner.plugin.mediastore.images

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.gm.cleaner.plugin.R
import me.gm.cleaner.plugin.dao.ModulePreferences
import me.gm.cleaner.plugin.databinding.MediaStoreFragmentBinding
import me.gm.cleaner.plugin.ktx.LayoutCompleteAwareGridLayoutManager
import me.gm.cleaner.plugin.ktx.buildStyledTitle
import me.gm.cleaner.plugin.ktx.fitsSystemWindowInsetBottom
import me.gm.cleaner.plugin.ktx.isItemCompletelyVisible
import me.gm.cleaner.plugin.mediastore.MediaStoreAdapter
import me.gm.cleaner.plugin.mediastore.MediaStoreFragment
import me.gm.cleaner.plugin.mediastore.MediaStoreModel
import me.gm.cleaner.plugin.mediastore.imagepager.ImagePagerFragment
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import me.zhanghai.android.fastscroll.NoInterceptionRecyclerViewHelper

class ImagesFragment : MediaStoreFragment() {
    override val viewModel: ImagesViewModel by viewModels()
    var lastPosition = 0

    override fun onCreateAdapter(): MediaStoreAdapter<MediaStoreModel, *> =
        ImagesAdapter(this) as MediaStoreAdapter<MediaStoreModel, *>

    override fun onBindView(binding: MediaStoreFragmentBinding) {
        val layoutManager =
            LayoutCompleteAwareGridLayoutManager(requireContext(), ModulePreferences.spanCount)
                .setOnLayoutCompletedListener {
                    appBarLayout.isLifted =
                        list.adapter?.itemCount != 0 && !list.isItemCompletelyVisible(0)
                }
        list.layoutManager = layoutManager
        // Build FastScroller after SelectionTracker so that we can intercept SelectionTracker's OnItemTouchListener.
        val fastScroller = FastScrollerBuilder(list)
            .useMd2Style()
            .setViewHelper(NoInterceptionRecyclerViewHelper(list, null))
            .build()
        list.fitsSystemWindowInsetBottom(fastScroller)
        list.addOnItemTouchListener(ScaleGestureListener(requireContext(), layoutManager))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState ?: dispatchRequestPermissions(requiredPermissions, savedInstanceState)
    }

    override fun onRequestPermissionsSuccess(
        permissions: Set<String>, savedInstanceState: Bundle?
    ) {
        super.onRequestPermissionsSuccess(permissions, savedInstanceState)
        setFragmentResultListener(ImagePagerFragment::class.java.name) { _, bundle ->
            lastPosition = bundle.getInt(ImagePagerFragment.KEY_POSITION)
            prepareTransitions()
            postponeEnterTransition()
            scrollToPosition(list, lastPosition)
        }
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private fun prepareTransitions() {
        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>, sharedElements: MutableMap<String, View>
            ) {
                // Locate the ViewHolder for the clicked position.
                val selectedViewHolder =
                    list.findViewHolderForAdapterPosition(lastPosition) ?: return

                // Map the first shared element name to the child ImageView.
                sharedElements[names[0]] = selectedViewHolder.itemView.findViewById(R.id.image)
            }
        })
    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid. This is important when
     * navigating back from the grid.
     */
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (selectionTracker.hasSelection()) {
            return
        }
        inflater.inflate(R.menu.images_toolbar, menu)

        when (ModulePreferences.sortMediaBy) {
            ModulePreferences.SORT_BY_PATH ->
                menu.findItem(R.id.menu_sort_by_path).isChecked = true
            ModulePreferences.SORT_BY_DATE_TAKEN, ModulePreferences.SORT_BY_SIZE ->
                menu.findItem(R.id.menu_sort_by_date_taken).isChecked = true
        }
        arrayOf(menu.findItem(R.id.menu_header_sort)).forEach {
            it.title = requireContext().buildStyledTitle(it.title)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sort_by_path -> {
                item.isChecked = true
                ModulePreferences.sortMediaBy = ModulePreferences.SORT_BY_PATH
            }
            R.id.menu_sort_by_date_taken -> {
                item.isChecked = true
                ModulePreferences.sortMediaBy = ModulePreferences.SORT_BY_DATE_TAKEN
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}
