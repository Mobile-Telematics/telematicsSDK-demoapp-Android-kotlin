package com.raxeltemematics.demoapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.list_item_trip.view.*

class TrackAdapter(val objects: List<TrackViewModel>, val selectedBlock: (trackId: String) -> Unit) :
    RecyclerView.Adapter<TrackAdapter.MyViewHolder>() {

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_trip, parent, false) as ViewGroup
        return MyViewHolder(textView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = objects[position]
        holder.view.departureAddressView.text = "Start address:\n${item.addressStart}"
        holder.view.destinationAddressView.text = "End address:\n${item.addressEnd}"
        holder.view.departureDateView.text = "Date start:\n${item.startDate}"
        holder.view.destinationDateView.text = "Date end:\n${item.endDate}"
        holder.view.mileageView.text = String.format("Mileage: %.1f km", item.distance)
        holder.view.totalTimeView.text = String.format("Total time: %d mins", item.duration.toInt())
        holder.view.setOnClickListener {
            selectedBlock(item.trackId!!)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = objects.size
}