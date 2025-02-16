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

package me.zhanghai.android.fastscroll

import androidx.recyclerview.widget.RecyclerView

internal class SimpleRecyclerViewHelper(
    private val list: RecyclerView, popupTextProvider: PopupTextProvider? = null
) : RecyclerViewHelper(list, popupTextProvider) {

    override fun getScrollRange() =
        list.computeVerticalScrollRange() + list.paddingTop + list.paddingBottom

    override fun getScrollOffset() = list.computeVerticalScrollOffset()

    override fun scrollTo(offset: Int) {
        // Stop any scroll in progress for RecyclerView.
        list.stopScroll()
        val delta = offset - scrollOffset
        list.scrollBy(0, delta)
    }
}
