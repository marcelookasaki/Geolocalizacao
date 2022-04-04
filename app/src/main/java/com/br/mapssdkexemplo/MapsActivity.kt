package com.br.mapssdkexemplo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.br.mapssdkexemplo.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        /* 1 - Tipo de mapa: NONE, NORMAL, SATELLITE, TERRAIN, HYBRID  */
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID)

        /* 2 -  Marcador de PIN inicial e move camera */
        val userLocal = LatLng(-19.933701907118405, -43.93631614196182)
        mMap.addMarker(
            MarkerOptions()
                .position(userLocal)
                .title("PUC Minas")
                /* Apresenta Subtítulo ao clicar na home */
                .snippet("Unidade Praça da Liberdade")
                /* Ícone armazenado em drawable */
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.home)))

        /* 3 - Centralizar câmera e Configurar Zoom de 2 a 21 */
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocal, 19.0f))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocal))

    }
}