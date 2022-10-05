package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled

import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: com.udacity.project4.databinding.FragmentSelectLocationBinding

    private  lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var Poi : PointOfInterest? =null
    var lat : Double = 0.0
    var long : Double = 0.0
    var title :String? = null

    private lateinit var map: GoogleMap
    private  val REQUEST_LOCATION_PERMISSION = 1
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

       fusedLocationProviderClient = FusedLocationProviderClient(requireContext())

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync{
            map = it
            enableMyLocation(map)
            setMapStyle(map)
            setMapLongClick(map)
            setPoiClick(map)
        }
        binding.saveButton.setOnClickListener{
            if(title!= null){
                onLocationSelected()
            }else{
                Toast.makeText(requireContext(),"Please choose a location",Toast.LENGTH_SHORT).show()
            }

        }
//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected
//        TODO: call this function after the user confirms on the selected location
        return binding.root
    }



    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.latitude.value = lat
        _viewModel.longitude.value = long
        _viewModel.selectedPOI.value = Poi
        _viewModel.reminderSelectedLocationStr.value = title
        findNavController().popBackStack()

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mapFragment = parentFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync{
                googleMap ->
            googleMap.setOnMapLoadedCallback {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-3000.0,300.0), 10f))
            }
        }
    }


    private fun LocationPermissionApproved(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
//
//    @TargetApi(29)
//    private fun BackGroundLocationApproved():Boolean{
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_BACKGROUND_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            return true
//        } else {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
//                REQUEST_LOCATION_PERMISSION
//            )
//            Toast.makeText(requireContext(),"sds",Toast.LENGTH_SHORT).show()
//            return false
//        }
//    }


    private fun enableMyLocation(map1:GoogleMap){
        if(LocationPermissionApproved()){
            map1.setMyLocationEnabled(true)
            moveToUserLocation()
        }
        else{
            Toast.makeText(requireContext(),"Please enable the Location permission ",Toast.LENGTH_SHORT).show()
            requestLocationPermission()
        }
    }

    private fun moveToUserLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if(location!=null){
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 13f
                        )
                    )
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation(map)
                Log.e("SelectLocationFrag","enableMyLocation OnRequest")
            }
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        Log.e("SelectLocationFrag","onMapReady")
    }

    private fun setMapStyle(map:GoogleMap){
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e("SelectLocationFragment", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("SelectLocationFragment", "Can't find style. Error: ", e)
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            Poi = poi
            lat = poi.latLng.latitude
            long = poi.latLng.longitude
            title = poi.name
            poiMarker.showInfoWindow()
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener() {
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            val  poiMarker = map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
            )
            poiMarker.showInfoWindow()
            Poi = null
            lat = it.latitude
            long = it.longitude
            title = getString(R.string.dropped_pin)
        }
    }

    private fun requestLocationPermission(){
        requestPermissions(
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }
}
