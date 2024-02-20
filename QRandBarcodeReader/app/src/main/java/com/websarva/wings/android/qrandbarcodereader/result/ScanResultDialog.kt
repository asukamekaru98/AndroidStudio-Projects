/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package com.websarva.wings.android.qrandbarcodereader.result

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.websarva.wings.android.qrandbarcodereader.R
import com.websarva.wings.android.qrandbarcodereader.databinding.ScanResultBinding
class ScanResultDialog : DialogFragment() {

    //スキャン結果表示
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        Log.d("TAG", "onCreateDialog")

        val activity = requireActivity()
        val binding = ScanResultBinding.inflate(activity.layoutInflater)
        val result: ScanResult? = requireArguments().getParcelable(KEY_SCAN_RESULT)
        result?.let {
            binding.resultValue.text = result.value
            binding.resultType.text = result.type
            binding.resultFormat.text = result.format

        }
        return AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_title_select_action)
            .setView(binding.root)
            .create()
    }

    companion object {
        private const val TAG = "ScanResultDialog"
        private const val KEY_SCAN_RESULT = "KEY_SCAN_RESULT"

        fun show(activity: FragmentActivity, result: ScanResult) {

            Log.d("TAG", "onCreateDialog.show")

            val manager = activity.supportFragmentManager
            if (manager.isStateSaved) return
            if (manager.findFragmentByTag(TAG) != null) return
            ScanResultDialog().also {
                it.arguments = bundleOf(KEY_SCAN_RESULT to result)
            }.show(manager, TAG)
        }
    }
}