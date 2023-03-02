package com.example.mercados

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity(){

    companion object{
        const val REQUEST_CODE_LOCATION=0
    }
    private var activateGPS:Boolean=true
    private lateinit var map: GoogleMap
    private lateinit var localEdit:TextInputEditText
    private lateinit var activeE:CheckBox
    private lateinit var txtLatitud:TextView
    private lateinit var txtLongitud:TextView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var latitudeU=0.0
    var longitudeU=0.0
    var activo=0
    private lateinit var txtLocal:TextView
    private lateinit var txtMercado:TextView
    private lateinit var txtActivo:TextView
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        localEdit=findViewById(R.id.localEdit)
        activeE=findViewById(R.id.activeE)
        txtLatitud=findViewById(R.id.textLatitud)
        txtLongitud=findViewById(R.id.textLongitud)
        spinner=findViewById(R.id.spinnerM)

        txtMercado=findViewById(R.id.txtMercado)
        txtLocal=findViewById(R.id.txtLocal)
        txtActivo=findViewById(R.id.txtActivo)
        fusedLocationProviderClient=LocationServices.getFusedLocationProviderClient(this)

        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this, R.array.mercados, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun checkPermissions(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
        }else{
            getLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        fusedLocationProviderClient.lastLocation?.addOnSuccessListener {
            if(it==null){
                Toast.makeText(this,"Lo sentimos, no se puede obtener la ubicacion",Toast.LENGTH_LONG).show()
            }else it.apply{
                  latitudeU =it.latitude
                   longitudeU=it.longitude
                txtLatitud.text="Latitud: $latitude"
                txtLongitud.text="Latitud: $longitude"
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==1){
            if (grantResults.isNotEmpty()&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permiso concedido",Toast.LENGTH_LONG).show()
                    getLocation()
                }else{
                    Toast.makeText(this,"Permiso denegado", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun clickBtnInsertar(view: View){
        checkPermissions()
        if(activeE.isChecked){
             activo =1
        }else{
            activo=0
        }
        val url="http://mercadospuebla.com/Mercados/insertar.php"

        val queue = Volley.newRequestQueue(this)
        var resultadoPost= object : StringRequest(Request.Method.POST,url,
            Response.Listener { response ->
                Toast.makeText(this,"Local insertado correctamente",Toast.LENGTH_LONG).show()
            },Response.ErrorListener { error ->
                Toast.makeText(this,"Error $error",Toast.LENGTH_LONG).show()
            }){
            override fun getParams():MutableMap<String,String>{
                val parametros=HashMap<String,String>()
                parametros.put("mercado",spinner.selectedItem.toString())
                parametros.put("localmdo",localEdit?.text.toString())
                parametros.put("latitud",latitudeU.toString())
                parametros.put("longitud",longitudeU.toString())
                parametros.put("activo",activo.toString())
                return parametros
            }

        }


        val loc=localEdit?.text.toString()
        txtLocal.text="Local:$loc"
        val mdo=spinner.selectedItem.toString()
        txtMercado.text="Mercado: $mdo"
        if (activeE.isChecked){
            txtActivo.text="Activo economicamente: si"
        }else{
            txtActivo.text="Activo economicamente: no"
        }
        VolleySingleton.instance?.addToRequestQueue(resultadoPost)
    }

}