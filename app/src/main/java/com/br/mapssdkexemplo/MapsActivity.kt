package com.br.mapssdkexemplo

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.br.mapssdkexemplo.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.*
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    /* Responsável por "rastrear" eventos relacionados a geolocalização */
    private var locationListener: LocationListener? = null

    /* Reponsável por gerenciar / configurar o rastreamento da geolocalização */
    private var locationManager: LocationManager? = null

    /* Marcador de posição do dispositivo */
    private var usermaker: Marker? = null
    /* Verifica se o mapa está pronto ou não */
    private var isMapReady: Boolean = false

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

        /*
        /*=== Início do evento de clique longo  ===*/
        /* 4 - Clique longo retorna: a latitude e longitude do local do clique em um Toast */
        mMap.setOnMapLongClickListener { LatLng ->
            val myLatitude = LatLng.latitude
            val myLongitude = LatLng.longitude
            Toast.makeText(
                applicationContext,
                "Latitude: $myLatitude e Longitude: $myLongitude",
                Toast.LENGTH_LONG
            ).show()

            /* 5 - Adiciona ícone de helicóptero ao clicar */
            mMap.addMarker(
                MarkerOptions()
                    .position(LatLng)
                    .title("Ponto adicional")
                    /* Adiciona ícone armazenado em drawable */
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.heliponto))
            )

            /* 6 - Adicionar Círculo nos Helipontos com raio de 2km */
            mMap.addCircle(
                CircleOptions().center(LatLng).radius(200.0).strokeWidth(5.0f)
                    .strokeColor(Color.WHITE).fillColor(Color.argb(110, 100, 200, 200))
            )

            /* 7 - Criar linhas entre a Puc Minas e o ponto do clique */
            val latitude = LatLng.latitude
            val longitude = LatLng.longitude
            val puc = LatLng(-19.9332786, -43.9371484)
            val polylineOptions = PolylineOptions()
            polylineOptions.add(LatLng)
            polylineOptions.add(puc)
            polylineOptions.color(Color.GREEN).width(20.0f)
            mMap.addPolyline(polylineOptions)

        /*=== Final do evento de clique longo  ===*/
        } */

        /* 9 -  Verificar distância entre pontos */
        mMap.setOnMapLongClickListener { latLng ->
            val latitude = latLng.latitude
            val longitude = latLng.longitude
            val puc = LatLng(-19.9332786, -43.9371484);

            val results = FloatArray(1)
            Location.distanceBetween(
                puc.latitude,
                puc.longitude,
                latLng.latitude,
                latLng.longitude,
                results
            )
            Toast.makeText(
                applicationContext,
                "Distancia do ponto até a PUC: ${results[0]}",
                Toast.LENGTH_LONG
            ).show()


            /* 8 - Criar polígonos após clique */
            val polygonOptions = PolygonOptions()
            polygonOptions.add(LatLng(-19.9327225, -43.9388156))
            polygonOptions.add(LatLng(-19.9310411, -43.9383305))
            polygonOptions.add(LatLng(-19.9313083, -43.9372501))
            polygonOptions.add(LatLng(-19.9329917, -43.9376885))
            polygonOptions.strokeWidth(5.0f).strokeColor(Color.WHITE)
                .fillColor(Color.argb(110, 100, 200, 200))
            mMap.addPolygon(polygonOptions)

            /* 14e - Teste do Reverse Geocoding */
            val reverseGeo: String? = reverseGeocoding(puc)
            if (reverseGeo != null) {
                Toast.makeText(
                    applicationContext,
                    "Reverse Geocoding: $reverseGeo",
                    Toast.LENGTH_LONG).show()
            }
        } /* Fim setOnMapLongClickListener */

        /* 10 - Adicionar Icone do drone ao digitar um endereço e retornar um Toast com lat/long */
        binding.btnADD.setOnClickListener {
            if (binding.etDestino.text.toString() != "") {
                var geoloc = geocoding(binding.etDestino.text.toString())
                if (geoloc != null) {
                    Toast.makeText(
                        applicationContext,
                        "Local encontrado: Latitude: ${geoloc.latitude}, Longitude: ${geoloc.longitude}",
                        Toast.LENGTH_LONG
                    ).show()
                    val droneLocal = LatLng(geoloc.latitude, geoloc.longitude)
                    setLinha(userLocal, droneLocal)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(geoloc)
                            .title("Ponto adicional")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.drone))
                    )
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Local não encontrado!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(applicationContext, "Digite o nome do local!", Toast.LENGTH_LONG)
                    .show()
            }
        } /* Fim setOnClickListener*/

        /* 11) Location Listener - Atualiza o marcador de geolocalização conforme posição geográfica do dispositivo */
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val userPosition = LatLng(location.latitude, location.longitude)

                if (usermaker != null) {
                    /* Remove marcadores anteriores */
                    usermaker!!.remove()
                }
                /* Adiciona marcadores */
                usermaker = mMap.addMarker(
                    MarkerOptions()
                        .position(userPosition)
                        .title("Minha localização")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.home))
                )
                /* Move camera para a posição no user */
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        userPosition,
                        15.0f
                    )
                )
            }
        }
        isMapReady = true
        checkPermission()
        setupLocation()

    } /* Fim onMapReady */

    /* 12 - Cria linha entre usuário e drone */
    fun setLinha(startPoint: LatLng, endPoint: LatLng) {
        // Criar linhas
        var polylineOptions = PolylineOptions()
        polylineOptions?.add(startPoint)
        polylineOptions?.add(endPoint)
        // Verificar distância entre pontos.
        val results = FloatArray(1)
        Location.distanceBetween(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude, results)
        if (results[0] <= 500.0f) {
            // Se a distância for inferior a 500m, configurar cor da linha para verde
            polylineOptions?.color(Color.GREEN)?.width(20.0f)
        } else {
            // Se a distância for inferior a 500m, configurar cor da linha para vermelha
            polylineOptions?.color(Color.RED)?.width(20.0f)
        }

        mMap.addPolyline(polylineOptions)
    }


    /* 13 - Geocoding → transformar endereço em coordenadas */
    fun geocoding(descricaoLocal: String): LatLng? {
        val geocoder = Geocoder(
            applicationContext,
            Locale.getDefault()
        ) /* Locale representa uma região específica */
        try {
            val local = geocoder.getFromLocationName(descricaoLocal, 1)
            if (local != null && local.size > 0) {
                var destino = LatLng(local[0].latitude, local[0].longitude)
                return destino
            }
        } catch (e: IOException) {
            e.message
        }
        return null
    }

    /* 14 - Transforma coordenadas em endereço ou descrição */
    fun reverseGeocoding(latlang: LatLng): String? {
        val geocoder = Geocoder(
            applicationContext,
            Locale.getDefault()
        )
        /* Locale representa uma região específica */
        try {
            val local = geocoder.getFromLocation(latlang.latitude, latlang.longitude, 1)
            if (local != null && local.size > 0) {
                return local[0].getAddressLine(0).toString()
            }
        } catch (e: IOException) {
            e.message
        }
        return null
    }

    /* Permissões: */

    /* A ) Validar permissões em tempo de execução (necessário para API 23 ou superior) */
    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissões Ativadas", Toast.LENGTH_SHORT).show()
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                /* Em uma IU educacional, explique ao usuário por que seu aplicativo requer esta permissão para um recurso específico se comportar conforme o
                esperado. Nesta IU, inclua um botão "cancelar" ou "não, obrigado" que permite ao usuário continue usando seu aplicativo sem conceder a permissão */
                alertaPermissaoNegada()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                /* Pedir permissão diretamente. O ActivityResultCallback registrado obtém o resultado desta solicitação (abaixo) */
            }
        }
    }
    /* B) Calback que exibe a janela de solicitação de permissão */
    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            /* Método acoplado para setar a localização */
            setupLocation()
        }
    }

    /* C) Exibir IU educacional (recomendação do Google) → caixas de alerta */
    fun alertaPermissaoNegada() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Permissões Requeridas")
        alert.setMessage("Para continuar utilizando todos os recursos do aplicativo, é altamente recomendado autorizar o acesso a sua localização.")
        /* Evita o cancelamento do alert ao clicar fora da caixa */
        alert.setCancelable(false)
        alert.setPositiveButton(
            "Corrigir"
        ) { dialog, which ->
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        alert.setNegativeButton(
            "Cancelar"
        ) { dialog, which ->
            Toast.makeText(getApplicationContext(), "Algumas das funcionalidades do app foram desabilitadas.", Toast.LENGTH_LONG).show();
            /* Nunca Utilizar o comendo finish(). Fechar o app é uma prática pouco recomendada */
        }
        val alertDialog = alert.create()
        alertDialog.show()
    }

    /* D) Configurar a Geolocalização */
    @SuppressLint("MissingPermission")
    fun setupLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            /* requestLocationUpdates(String provider, long minTimeMs, float minDistanceM, LocationListener listener)
            1) provider - String: um provedor listado por getAllProviders() Este valor não pode ser null.(LocationManager.GPS_PROVIDER neste caso)
            2) miTimeMs - long: Intervalo mínimo de tempo entre as atualizações de localização em milissegundos(1000 ms neste caso)
            3) minDistanceM: float: distância mínima entre atualizações de localização em metros
            4) listener: LocationListener: o ouvinte que receberá atualizações de localização Este valor não pode ser null.
            https://developer.android.com/reference/android/location/LocationManager#requestLocationUpdates(java.lang.String,%20long,%20float,%20android.location.LocationListener)*/
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 10f, locationListener!!
            )
        }
    }
}