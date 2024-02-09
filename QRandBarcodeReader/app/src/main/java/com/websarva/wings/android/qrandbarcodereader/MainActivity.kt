package com.websarva.wings.android.qrandbarcodereader

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.google.android.material.button.MaterialButton
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.lang.Exception
import kotlin.jvm.internal.Ref.BooleanRef

class MainActivity : AppCompatActivity() {

    private lateinit var cameraBtn: MaterialButton
    private lateinit var galleryBtn: MaterialButton
    private lateinit var imageIv: ImageView
    private lateinit var scanBtn: MaterialButton
    private lateinit var resultTv: TextView

    companion object{
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101

        private const val TAG = "MAIN_TAG"
    }

    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>

    private var imageUri: Uri? = null

    private var barcodeScannerOptions: BarcodeScannerOptions? = null
    private var barcodeScanner: BarcodeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraBtn = findViewById(R.id.cameraBtn)
        galleryBtn = findViewById(R.id.galleryBtn)
        imageIv = findViewById(R.id.imageIv)
        scanBtn = findViewById(R.id.scanBtn)
        resultTv = findViewById(R.id.resultTv)

        cameraPermission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        barcodeScannerOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions!!)

        //カメラボタン押下
        cameraBtn.setOnClickListener(){


            if(checkCameraPermissions()){
                pickImageCamera()
            }else{
                requestCameraPermission()
            }

        }

        //ギャラリーボタン押下
        galleryBtn.setOnClickListener(){

            if(checkStoragePermission()){
                pickImageGallery()
            }else{
                requestStoragePermission()
            }

        }

        //スキャンボタン押下
        scanBtn.setOnClickListener(){
            if(imageUri == null){
                showToast("Pick image first")
            }else{
                detectResultFormImage()
            }
        }
    }

    private fun detectResultFormImage(){
        Log.d(TAG,"detectResultFromImage: ")
        try{
            val inputImage = InputImage.fromFilePath(this,imageUri!!)

            val barcodeResult = barcodeScanner!!.process(inputImage)
                .addOnSuccessListener {barcodes->
                extractBarcodeQrCodeInfo(barcodes)
            }
                .addOnFailureListener{e->
                    Log.e(TAG,"detectResultFromImage: ",e)
                    showToast("Failed scanning due to ${e.message}")
                }
        }catch(e:Exception){
            Log.e(TAG,"detectResultFromImage: ",e)
            showToast("Failed due to ${e.message}")
        }
    }

    private fun extractBarcodeQrCodeInfo(barcodes:List<Barcode>){
        for(barcode in barcodes){
            val bound = barcode.boundingBox
            val corners = barcode.cornerPoints

            val rawValue = barcode.rawValue
            Log.d(TAG, "extractBarcodeQrCodeInfo: rawValue: $rawValue ")

            val valueType = barcode.valueType
            when(valueType){
                Barcode.TYPE_WIFI ->{
                    val typeWifi = barcode.wifi

                    val ssid = "${typeWifi?.ssid}"
                    val password = "${typeWifi?.password}"
                    var encryptionType = "${typeWifi?.encryptionType}"

                    if(encryptionType == "1"){
                        encryptionType = "OPEN"
                    }
                    else if(encryptionType == "2"){
                        encryptionType = "WPA"
                    }
                    else if(encryptionType == "3"){
                        encryptionType = "WEP"
                    }

                    Log.d(TAG, "extractBarcodeQrCodeInfo: TYPE_WIFI")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: ssid: $ssid")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: password: $password")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: encryptionType: $encryptionType")

                    resultTv.text = "TYPE_WIFI \n ssid: $ssid : \n password:$password \n encryptionType: $encryptionType \n\n rawValue: $rawValue"
                }
                Barcode.TYPE_URL ->{
                    val typeUrl = barcode.url
                    val title = "${typeUrl?.title}"
                    val url = "${typeUrl?.url}"

                    Log.d(TAG, "extractBarcodeQrCodeInfo: TYPE_URL")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: title: $title")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: url: $url")

                    resultTv.text = "TYPE_URL \n title: $title \n url: $url \n\n rawValue: $rawValue"
                }
                Barcode.TYPE_EMAIL ->{
                    val typeEmail = barcode.email

                    val address = "${typeEmail?.address}"
                    val body = "${typeEmail?.body}"
                    val subject = "${typeEmail?.subject}"

                    Log.d(TAG, "extractBarcodeQrCodeInfo: TYPE_EMAIL")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: address: $address")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: body: $body")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: subject: $subject")

                    //resultTv.text = ""
                    resultTv.text = "TYPE_EMAIL \n address: $address \n body: $body \n subject: $subject  \n\n rawValue: $rawValue"
                }
                Barcode.TYPE_CONTACT_INFO->{
                    val typeContact = barcode.contactInfo

                    val title = "${typeContact?.title}"
                    val organization = "${typeContact?.organization}"
                    val name = "${typeContact?.name?.first} ${typeContact?.name?.last}"
                    val phone = "${typeContact?.name?.first} ${typeContact?.phones?.get(0)?.number}"

                    Log.d(TAG, "extractBarcodeQrCodeInfo: TYPE_CONTACT_INFO")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: title: $title")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: organization: $organization")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: name: $name")
                    Log.d(TAG, "extractBarcodeQrCodeInfo: phone: $phone")

                    resultTv.text = "TYPE_CONTACT_INFO \n title: $title \n organization: $organization \n name: $name \n phone: $phone \n\n rawValue: $rawValue"
                }
                else ->{
                    resultTv.text = "rawValue: $rawValue"
                }
            }
        }
    }


    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data

            imageUri = data?.data
            Log.d(TAG,"galleryActivityResultLauncher:imageUri: $imageUri")

            imageIv.setImageURI(imageUri)
        }else{
            showToast("Cancelled.....!!!!!!!!!!!!")
        }

    }

    private fun pickImageCamera(){
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE,"Sample Image")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Sample Image Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result->

        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data

            Log.d(TAG,"cameraActivityResultLauncher: imageUri: $imageUri")

            imageIv.setImageURI(imageUri)
        }

    }

    //ストレージ権限の確認
    private fun checkStoragePermission():Boolean{
        val result = (ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        return result
    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission, STORAGE_REQUEST_CODE)
    }

    //カメラ権限の確認
    private fun checkCameraPermissions():Boolean{
        val resultCamera = (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)
        val resultStorage = (ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)

        return resultCamera && resultStorage

    }

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if(grantResults.isNotEmpty()){
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if(cameraAccepted && storageAccepted){
                        pickImageCamera()
                    }else{
                        showToast("Camera & Storage permissions are required")
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {

                if(grantResults.isNotEmpty()){
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if(storageAccepted){
                        pickImageGallery()
                    }else{
                        showToast("Storage permission is required...")
                    }
                }
            }
        }
    }

    //トースト表示
    private fun showToast(message: String){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }
}