package com.websarva.wings.android.qrandbarcodereader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val iImages: ArrayList<Int>,
                private val iNames: ArrayList<String>):
    RecyclerView.Adapter<MyAdapter.ViewHolder>(){

    // Provide a reference to the type of views
    // that you are using (custom ViewHolder).
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val imageView: ImageView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.text_view)
            imageView = view.findViewById(R.id.image_view)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int):
            ViewHolder {
        // Create a new view, which defines the UI of the list item
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.my_text_view, viewGroup, false)
        return ViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.imageView.setImageResource(iImages.get(position))
        viewHolder.textView.text = iNames[position]

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = iNames.size

}
/*
class MyAdapter internal constructor(private val dataArray: List<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>{
    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView: TextView

        init {
            textView = view.findViewById<TextView>(R.id.text_view)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_text_view, parent, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = dataArray[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return dataArray.size
    }
}

 */