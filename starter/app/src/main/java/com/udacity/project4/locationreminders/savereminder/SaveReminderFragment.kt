package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity
import android.app.Fragment
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private val GEOFENCE_RADIUS_IN_METERS = 500f

    private lateinit var dataItem: ReminderDataItem

    private var deviceLocationFlag = false

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
    private val REQUEST_LOCATION_PERMISSION = 1
    private val REQUEST_BACK_GROUND_LOCATION_PERMISSION = 2
    private val REQUEST_TURN_DEVICE_LOCATION_ON = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            dataItem = ReminderDataItem(title, description, location, latitude, longitude)

//            val reminder = ReminderDataItem(title, description, location, latitude, longitude)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            if (LocationPermissionApproved()) {
                if (runningQOrLater) {
                    if (BackGroundLocationPermissionApproved()) {
                        if (_viewModel.validateEnteredData(dataItem)) {
                            checkDeviceLocationSettingsAndStartGeofence(reminderDataItem = dataItem)

                        }
                    } else {
                        requestBackGroundLocationPermission()
                    }
                }else{ // before q
                    if (_viewModel.validateEnteredData(dataItem)) {
                        checkDeviceLocationSettingsAndStartGeofence(reminderDataItem = dataItem)
                    }
                }
            } else {
                requestLocationPermission()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    private fun addGeoFence(
        reminderDataItem: ReminderDataItem
    ) {
        val geofencePendingIntent: PendingIntent by lazy {
            val intent = Intent(mContext, GeofenceBroadcastReceiver::class.java)
            intent.action = "action_geofence"
            PendingIntent.getBroadcast(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        geofencingClient = LocationServices.getGeofencingClient(mContext)

        val geofence = Geofence.Builder()
            .setRequestId(reminderDataItem.id)
            .setCircularRegion(
                reminderDataItem.latitude!!,
                reminderDataItem.longitude!!,
                GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.e("SaveReminderFrag", "GeoFence Added")
            }
            addOnFailureListener {
                Log.e("SaveReminderFrag", "GeoFence NOT Added")
            }
        }
    }

    private fun LocationPermissionApproved(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
        Toast.makeText(
            requireContext(),
            "Please enable the Location permission ",
            Toast.LENGTH_SHORT
        ).show()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun BackGroundLocationPermissionApproved(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestBackGroundLocationPermission() {
        requestPermissions(
            arrayOf<String>(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_BACK_GROUND_LOCATION_PERMISSION
        )
        Toast.makeText(
            requireContext(),
            "Please enable the BackGround Location permission ",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true,reminderDataItem: ReminderDataItem){
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(exception.resolution.intentSender, REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(
                        "SaveRemindFrag",
                        "Error getting location settings resolution: " + sendEx.message
                    )
                }
            } else {
                Toast.makeText(requireContext(),R.string.location_required_error,Toast.LENGTH_SHORT).show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                deviceLocationFlag = true
                _viewModel.validateAndSaveReminder(reminderDataItem)
                addGeoFence(reminderDataItem)
                Log.e("SaveRemindFrag","addGeoFence")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_BACK_GROUND_LOCATION_PERMISSION) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if(!deviceLocationFlag){
                    checkDeviceLocationSettingsAndStartGeofence(true,dataItem)
                    Log.e("SaveReminderFrag","deviceLocationFlag not set")
                }
                Log.e("SaveReminderFrag","turn device location")
            }
        }
        if(requestCode == REQUEST_LOCATION_PERMISSION){
            if(grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                requestBackGroundLocationPermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkDeviceLocationSettingsAndStartGeofence(true,dataItem)
            Log.e("SveReminderFrag","onActivityResultCalled")
        }
    }

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        Log.e("SaveReminderFrag","onAttachCalled")
    }

    override fun onDetach() {
        super.onDetach()
        Log.e("SaveReminderFrag","onDeAttachCalled")
    }
}
