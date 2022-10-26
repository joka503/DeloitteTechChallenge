package com.example.techchallengedeloitte

import android.Manifest
import android.app.DownloadManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.techchallengedeloitte.constants.Constants
import com.example.techchallengedeloitte.data.DBHelper
import com.example.techchallengedeloitte.databinding.ActivityMainBinding
import java.io.File
import java.time.LocalDate


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var downloadID : Long = 0
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID === id) {
                fileDownloaded()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    override fun onResume() {
        super.onResume()

        when {
            let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } == PackageManager.PERMISSION_GRANTED -> {
                checkData()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showInContextUI()
            }
            else -> {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), 1
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    checkData()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.

                    showInContextUI()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }

    }

    /**
     * Show alert to user to explain why the app needs external storage access
     */
    private fun showInContextUI(){
        val dialogBuilder = let {
            AlertDialog.Builder(it).also {

                it.setMessage("The app needs access to the device storage to save data!")

                    .setPositiveButton("Accept", DialogInterface.OnClickListener(){ dialog, _ -> requestPermissions(
                        arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE), 1)
                    })

                    .setNegativeButton("Deny", DialogInterface.OnClickListener(){ dialog, _ -> dialog.cancel()
                    })
            }
        }
        val alert = dialogBuilder?.create()
        alert?.setTitle("Alert")
        alert?.show()
    }

    /**
     * Checks if app already has data from the csv file or if it needs to download it
     */
    private fun checkData(){
        val db = let { DBHelper(it, null) }
        val numRecords = db?.getNumData()
        if (numRecords != 0){

        }
        else{
            downloadFile()
        }
    }

    /**
     * Get the file
     */
    private fun downloadFile(){
        try {
            binding.progressBarDownload.visibility = View.VISIBLE
            binding.textDescriptionDownload.visibility = View.VISIBLE

            val request = DownloadManager.Request(Uri.parse(Constants.PostalCodesFile))
                .setTitle("Postal Codes")
                .setDescription("Downloading Postal Codes file....")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, File.separator + "PostalCodes_"+ LocalDate.now().toString() +".csv")

            val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

            registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            downloadID = dm.enqueue(request)
        }
        catch (e:java.lang.Exception){
            showAlert("Error downloading file!")
        }
    }

    /**
     * Open the file and store it's data
     */
    private fun fileDownloaded(){
        try {
            binding.progressBarDownload.visibility = View.GONE
            binding.textDescriptionDownload.visibility = View.GONE
        }
        catch (e:java.lang.Exception){
            showAlert("Error loading file!")
        }
    }

    /**
     * Show alert to user with required message
     */
    private fun showAlert(message : String){
        val dialogBuilder = let {
            AlertDialog.Builder(it).also {

                it.setMessage(message)

                    .setPositiveButton("Ok", DialogInterface.OnClickListener(){ dialog, _ ->
                    })
            }
        }
        val alert = dialogBuilder?.create()
        alert?.setTitle("Alert")
        alert?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onDownloadComplete);
    }
}