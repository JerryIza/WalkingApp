package com.example.trailit.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.trailit.R
import com.example.trailit.other.TrackingUtility
import com.example.trailit.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.statistics_fragment.*
import java.lang.Math.round
import kotlin.math.roundToInt
import com.example.trailit.databinding.StatisticsFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@AndroidEntryPoint
class StatisticsFragment: Fragment() {

    private lateinit var binding: StatisticsFragmentBinding

    private val viewModel: StatisticsViewModel by viewModels()

    private var xAxisMax: Float = 0.0f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StatisticsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setUpBarChart()

        GlobalScope.launch(Dispatchers.Main) {
            xAxisMax = viewModel.getDbSize().toFloat()
            println(xAxisMax)
        }

    }

    private fun setUpBarChart() {
        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
        }
        binding.barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE

        }
        binding.barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
        }

    }

    private fun setupObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun

            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, {
            it?.let {
                val miles = it / 1609f
                val totalDistance = round(miles) / 10f
                val totalDistanceString = "${totalDistance}Miles"
                tvTotalDistance.text = totalDistanceString
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, {
            if (it!= null) {
                val avgSpeed = (it).roundToInt() / 10f
                val avgSpeedString = "${avgSpeed}Mph"
                tvAverageSpeed.text = avgSpeedString
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, {

            val totalCalories = "${it}kCal"
            tvAverageSpeed.text = totalCalories

        })
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, {
            it?.let {
                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInMPH)}
                val chartDataSet = BarDataSet(allAvgSpeeds, "Avg Speed").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                barChart.data = BarData(chartDataSet)
                }
                barChart.invalidate()

        })


    }

}