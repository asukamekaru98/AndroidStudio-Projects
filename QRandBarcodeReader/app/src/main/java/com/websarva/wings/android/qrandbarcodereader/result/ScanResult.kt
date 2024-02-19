/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */


package com.websarva.wings.android.qrandbarcodereader.result

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScanResult(
    val value: String,
    val type: String,
    val format: String,
    val isUrl: Boolean,
) : Parcelable
