package com.example.trailit.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trailit.R
import com.example.trailit.databinding.AcceptDialogBinding
import com.example.trailit.databinding.TrailsFragmentBinding
import com.example.trailit.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.trailit.other.SortBy
import com.example.trailit.other.TrackingUtility
import com.example.trailit.ui.adapters.TrailAdapter
import com.example.trailit.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.trails_fragment.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class TrailsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: TrailsFragmentBinding

    //instantiating by using androidx.fragment:fragment-ktx: more concise
    private val viewModel: MainViewModel by viewModels()

    private lateinit var trailAdapter: TrailAdapter

    //val navController = Navigation.findNavController(requireActivity(), R.id.navHostFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = TrailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        setupRecyclerView()

        //spinner
        when(viewModel.sortBy){
            SortBy.DATE -> spinnerSort.setSelection(0)
            SortBy.RUNNING_TIME -> spinnerSort.setSelection(1)
            SortBy.DISTANCE -> spinnerSort.setSelection(2)
            SortBy.AVG_SPEED -> spinnerSort.setSelection(3)
            SortBy.CALORIES_BURNED -> spinnerSort.setSelection(4)
        }

        spinnerSort.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0 -> viewModel.sortRuns(SortBy.DATE)
                    1 -> viewModel.sortRuns(SortBy.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortBy.DISTANCE)
                    3 -> viewModel.sortRuns(SortBy.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortBy.CALORIES_BURNED)
                }
            }
        }
        binding.kebabBtn.setOnClickListener {
            showKebabMenu(binding.kebabBtn)

        }


        viewModel.runs.observe(viewLifecycleOwner, Observer {
            trailAdapter.submitList(it)
        })

    }

    private fun setupRecyclerView() = rvRuns.apply {
        trailAdapter = TrailAdapter()
        adapter = trailAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestPermission() {
        //requiredContext makes sure it is not equal to null
        //if permission are granted
        if (TrackingUtility.hasLocationPermission(requireContext())) {
            return
        }
        //if permissions have not been granted
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permission to use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun showKebabMenu(view: View){
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.kebab_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.deleteTrails -> {

                  findNavController().navigate(R.id.acceptDialogFragment)
                }
            }
            true
        }
        popup.show()

    }

}