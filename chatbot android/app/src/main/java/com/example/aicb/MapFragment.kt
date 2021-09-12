package com.example.aicb

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.aicb.MainActivity.Companion.hospital
import com.example.aicb.SplashActivity.Companion.model
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pedro.library.AutoPermissions.Companion.loadAllPermissions
import kotlinx.android.synthetic.main.map_fragment.*
import kotlinx.android.synthetic.main.map_fragment.view.*

class MapFragment: Fragment() {
    var mainActivity: MainActivity? = null
    lateinit var mapFragment: SupportMapFragment
    lateinit var map: GoogleMap
    lateinit var gpsListener: GPSListener
    lateinit var cont: Context
    var myLocationMarker: MarkerOptions? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity?
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = layoutInflater.inflate(R.layout.map_fragment, container, false) as ViewGroup

        mapFragment = (this.childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        cont = rootView.context

        mapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
            Log.d("Map", "지도 준비 완료")
            map = googleMap
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                loadAllPermissions((context as Activity?)!!, 101)
                return@OnMapReadyCallback
            }
            map.setMyLocationEnabled(true)
            startLocationService()
        })

        rootView.back_button.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        return rootView
    }

    fun startLocationService() //내 좌표 검색
    {
        val manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            gpsListener = GPSListener()
            var location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            //manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,minTime,minDistance,gpsListener);
            while (location == null) {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 1f, gpsListener)
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            gpsListener.onLocationChanged(location)
        } catch (e: SecurityException) {
            Toast.makeText(context, "권한 부여 필요", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    inner class GPSListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude
            val message = "latitude: $latitude Longitude: $longitude"
            Log.d("현재 위치", message)

            val size: Int = hospital!!.size
            var min = 999999999.0
            var cal: Double

            model.apply{
                setShortest_name("")
                setShortest_latitude(0.0)
                setShortest_longitude(0.0)
                setShortest_address("")
                setShortest_number("")
            }
            /*model.setShortest_name("")
            model.setShortest_latitude(0.0)
            model.setShortest_longitude(0.0)
            model.setShortest_address("")
            model.setShortest_number("")*/

            //lat lon lat lon
            for (i in 0 until size) {
                cal = distance(latitude,longitude,hospital!!.get(i).latitdue,hospital!!.get(i).longitude)
                if (cal < min) {
                    min = cal
                    model.setShortest_name(hospital!!.get(i).name)
                    model.setShortest_latitude(hospital!!.get(i).latitdue)
                    model.setShortest_longitude(hospital!!.get(i).longitude)
                    model.setShortest_address("https://".plus(hospital!!.get(i).internet_address))
                    model.setShortest_number(hospital!!.get(i).number)
                }
            }

            showCurrentLocation(
                model.getShortest_name().value.toString(), model.getShortest_latitude().value!!,
                model.getShortest_longitude().value!!, model.getShortest_number().value.toString()
            )
        }

        private fun distance(
            my_latitude: Double,
            my_longitude: Double,
            h_latitude: Double,
            h_longitude: Double
        ): Double {
            val theta = my_longitude - h_longitude
            var distance = Math.sin(deg2rad(my_latitude)) * Math.sin(deg2rad(h_latitude)) +
                    Math.cos(deg2rad(my_latitude)) * Math.cos(deg2rad(h_latitude)) * Math.cos(
                deg2rad(theta)
            )
            distance = Math.acos(distance)
            distance = rad2deg(distance)
            distance = distance * 60 * 1.1515
            return distance
        }

        // This function converts decimal degrees to radians
        private fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }

        // This function converts radians to decimal degrees
        private fun rad2deg(rad: Double): Double {
            return rad * 180 / Math.PI
        }

        private fun showCurrentLocation(
            name: String,
            latitude: Double,
            logitude: Double,
            number: String
        ) {
            if (name == "") {
                Toast.makeText(cont, "알맞는 병원을 찾지 못하였습니다.", Toast.LENGTH_LONG).show()
                return
            }
            //경도와 위도의 값을 LatLng 객체로 만들어 지도위에 표시
            val curPoint = LatLng(latitude, logitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15f))
            showMyLocationMarker(name, curPoint, number)
        }

        private fun showMyLocationMarker(name: String, curPoint: LatLng, number: String) {
            if (myLocationMarker == null) {
                myLocationMarker = MarkerOptions()
                myLocationMarker?.apply{
                    position(curPoint)
                    title(name)
                    snippet(number)
                    icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_VIOLET
                    ))
                }
                myLocationMarker!!.position(curPoint)
                myLocationMarker!!.title(name)
                myLocationMarker!!.snippet(number)
                myLocationMarker!!.icon(
                    BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_VIOLET
                    )
                )
                map.addMarker(myLocationMarker)
            } else {
                myLocationMarker!!.position(curPoint)
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    override fun onResume() {
        super.onResume()

        //map을 다루기전, 허가가 필요
        if (ActivityCompat.checkSelfPermission(cont, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                cont, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadAllPermissions((context as Activity?)!!, 101)
        }
        //map.setMyLocationEnabled(true)
    }

    override fun onPause() {
        super.onPause()

        if (ActivityCompat.checkSelfPermission(cont, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                cont,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadAllPermissions((context as Activity?)!!, 101)
            return
        }

        map.setMyLocationEnabled(false)
    }
}