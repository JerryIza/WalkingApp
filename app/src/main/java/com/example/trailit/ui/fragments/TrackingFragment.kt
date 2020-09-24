package com.example.trailit.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.trailit.R
import com.example.trailit.db.Run
import com.example.trailit.other.Constants.ACTION_PAUSE_SERVICE
import com.example.trailit.other.Constants.ACTION_START_RESUME_SERVICE
import com.example.trailit.other.Constants.ACTION_STOP_SERVICE
import com.example.trailit.other.Constants.MAP_ZOOM
import com.example.trailit.other.Constants.TRAILLINE_COLOR
import com.example.trailit.other.Constants.TRAILLINE_WIDTH
import com.example.trailit.other.TrackingUtility
import com.example.trailit.services.TrackingService
import com.example.trailit.services.TrailLine
import com.example.trailit.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import kotlin.math.round


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false

    private var coordinatePoints = mutableListOf<TrailLine>()

    private var map: GoogleMap? = null

    private var currentTimeMillis = 0L

    private var menu: Menu? = null

    private var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnFinishRun.setOnClickListener {
            panViewAngleToWholeTrailAndCapture()
            endRunAndSaveToDb()
        }

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        mapView.getMapAsync {
            map = it
            addAllTrailLines()
        }

        setUpObservers()
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.get(0)?.isVisible = true
            commandToService(ACTION_PAUSE_SERVICE)
        } else {
            commandToService(ACTION_START_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.trail_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mCancelTrailMapping -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //improved dialog.
    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel trail Mapping")
            .setMessage("Are you sure you want to delete all of the current mapping data?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                stopMapping()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopMapping() {
        commandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun setUpObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.coordinatePoints.observe(viewLifecycleOwner, Observer {
            coordinatePoints = it
            addLatestTrailLine()
            panViewAngleToUser()
        })

        TrackingService.trailTimeInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeMillis, true)
            tvTimer.text = formattedTime
        })
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "Stop"
            menu?.get(0)?.isVisible = true

            btnFinishRun.visibility = View.GONE
        }
    }

    private fun panViewAngleToUser() {
        if (coordinatePoints.isNotEmpty() && coordinatePoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    coordinatePoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun panViewAngleToWholeTrailAndCapture() {
        val bounds = LatLngBounds.builder()
        for (trailLine in coordinatePoints) {
            for (pos in trailLine) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.height * 0.20f.toInt())
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (trailLine in coordinatePoints) {
                distanceInMeters += TrackingUtility.calculateTrailLineLength(trailLine).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1609f) / (currentTimeMillis / 1000f / 60 / 60) * 10) / 10
            val dateTimestamp = Calendar.getInstance().timeInMillis
            //we can pair with a smart watch, would be calculated much better
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(
                img = bmp,
                timestamp = dateTimestamp,
                avgSpeedInMPH = avgSpeed,
                distanceInMeters = distanceInMeters,
                timeInMillis = currentTimeMillis,
                caloriesBurned= caloriesBurned
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Trail saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopMapping()
        }
    }

    private fun addAllTrailLines() {
        for (trailLine in coordinatePoints) {
            val trailLineOptions = PolylineOptions()
                .color(TRAILLINE_COLOR)
                .width(TRAILLINE_WIDTH)
                .addAll(trailLine)
            map?.addPolyline(trailLineOptions)
        }
    }

    private fun addLatestTrailLine() {
        if (coordinatePoints.isNotEmpty() && coordinatePoints.last().size > 1) {
            val preLastLatLng = coordinatePoints.last()[coordinatePoints.last().size - 2]
            val lastLatLong = coordinatePoints.last().last()
            val trailLineOptions = PolylineOptions()
                .color(TRAILLINE_COLOR)
                .width(TRAILLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLong)
            map?.addPolyline(trailLineOptions)
        }
    }

    private fun commandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    //cache map so we don't load it every time we open.
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


}