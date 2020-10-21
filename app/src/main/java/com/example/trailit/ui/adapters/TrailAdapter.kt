package com.example.trailit.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trailit.data.entitites.Run
import com.example.trailit.databinding.ItemRunBinding
import com.example.trailit.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class TrailAdapter : RecyclerView.Adapter<TrailAdapter.TrailViewHolder>() {

    val diffCallBack = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallBack)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrailViewHolder {
        val itemBinding = ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrailViewHolder(itemBinding)
    }

    override fun onBindViewHolder(
        holder: TrailViewHolder,
        position: Int
    ) {
        val run = differ.currentList[position]
        holder.bind(run)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    class TrailViewHolder(private val itemBinding: ItemRunBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(run: Run) {
                Glide.with(itemView).load(run.img).into(itemBinding.tvRunImage)
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }
                val dateFormat = SimpleDateFormat("MM.dd.yy", Locale.getDefault())
                itemBinding.tvDate.text = dateFormat.format(calendar.time)

                val avgSpeed = "${run.avgSpeedInMPH}mph"
                itemBinding.tvAvgSpeed.text = avgSpeed

                val distanceInMiles = "${run.distanceInMeters / 1609f / 10} Miles"
                itemBinding.tvDistance.text = distanceInMiles

                itemBinding.tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

                val caloriesBurned = "${run.caloriesBurned}kcal"
                itemBinding.tvCalories.text = caloriesBurned
        }
    }
}