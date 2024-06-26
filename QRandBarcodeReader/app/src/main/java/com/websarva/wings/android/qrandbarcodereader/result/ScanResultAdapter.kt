/*
 * Copyright (c) 2021 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package com.websarva.wings.android.qrandbarcodereader.result

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.websarva.wings.android.qrandbarcodereader.databinding.ScanResultBinding
import com.websarva.wings.android.qrandbarcodereader.result.ScanResultAdapter.ViewHolder

class ScanResultAdapter(
    context: Context,
    private val onItemClickListener: (ScanResult) -> Unit,
) : RecyclerView.Adapter<ViewHolder>(), Observer<List<ScanResult>> {
    private var results: List<ScanResult> = emptyList()
    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ScanResultBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Log.d("TAG", "onBindViewHolder")

        val result = results[position]
        holder.apply(result)
        holder.itemView.setOnClickListener {
            onItemClickListener(result)
        }
    }

    override fun getItemCount(): Int = results.size

    override fun onChanged(value: List<ScanResult>) {



        val diff = DiffUtil.calculateDiff(DiffCallback(results, value))
        results = value
        diff.dispatchUpdatesTo(this)
    }

    class DiffCallback(
        private val oldList: List<ScanResult>,
        private val newList: List<ScanResult>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

    class ViewHolder(
        private val binding: ScanResultBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun apply(result: ScanResult) {

            Log.d("TAG", "apply")

            binding.resultValue.text = result.value
            binding.resultType.text = result.type
            binding.resultFormat.text = result.format
        }
    }
}