/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package com.websarva.wings.android.qrandbarcodereader.constant

import com.google.mlkit.vision.barcode.common.Barcode

fun Barcode.typeStr(): String =
    when (valueType) {
        Barcode.TYPE_ISBN -> "図書コード"
        Barcode.TYPE_PRODUCT -> "商品コード"
        Barcode.TYPE_TEXT -> "テキスト文言"
        else -> "unknown"
    }

fun Barcode.formatStr(): String =
    when (format) {
        Barcode.FORMAT_CODE_128 -> "CODE128"
        Barcode.FORMAT_CODE_39 -> "CODE39"
        Barcode.FORMAT_CODE_93 -> "CODE93"
        Barcode.FORMAT_CODABAR -> "Codabar/NW-7"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        Barcode.FORMAT_EAN_13 -> "JAN13/EAN13"
        Barcode.FORMAT_EAN_8 -> "JAN8/EAN8"
        Barcode.FORMAT_ITF -> "ITF"
        Barcode.FORMAT_QR_CODE -> "QR"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_AZTEC -> "Aztec"
        else -> "unknown"
    }