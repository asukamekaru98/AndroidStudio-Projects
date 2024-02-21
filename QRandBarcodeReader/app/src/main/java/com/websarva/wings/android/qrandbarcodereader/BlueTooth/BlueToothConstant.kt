package com.websarva.wings.android.qrandbarcodereader.constant

import com.google.mlkit.vision.barcode.common.Barcode

fun Barcode.bcdFormatStr(): String =
	when (format) {
		Barcode.FORMAT_CODE_128 -> "K"
		Barcode.FORMAT_CODE_39 -> "M"
		//Barcode.FORMAT_CODE_93 -> ""
		Barcode.FORMAT_CODABAR -> "N"
		//Barcode.FORMAT_DATA_MATRIX -> ""
		Barcode.FORMAT_EAN_13 -> "A"
		Barcode.FORMAT_EAN_8 -> "B"
		Barcode.FORMAT_ITF -> "I"
		Barcode.FORMAT_QR_CODE -> "q"
		Barcode.FORMAT_UPC_A -> "A"
		Barcode.FORMAT_UPC_E -> "C"
		Barcode.FORMAT_PDF417 -> "p"
		//Barcode.FORMAT_AZTEC -> ""
		else -> "X"
	}