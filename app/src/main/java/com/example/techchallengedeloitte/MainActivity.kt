package com.example.techchallengedeloitte

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.techchallengedeloitte.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        when {
            let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            } == PackageManager.PERMISSION_GRANTED -> {
                val fragment1 : Fragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as Fragment?
                val child: FirstFragment? = fragment1?.childFragmentManager?.fragments?.get(0) as FirstFragment?
                child?.checkData()

            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                val fragment1 : Fragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as Fragment?
                val child: FirstFragment? = fragment1?.childFragmentManager?.fragments?.get(0) as FirstFragment?
                child?.showInContextUI()
            }
            else -> {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
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
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                ) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    val fragment1 : Fragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as Fragment?
                    val child: FirstFragment? = fragment1?.childFragmentManager?.fragments?.get(0) as FirstFragment?
                    child?.checkData()
                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.

                    val fragment1 : Fragment? = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as Fragment?
                    val child: FirstFragment? = fragment1?.childFragmentManager?.fragments?.get(0) as FirstFragment?
                    child?.showInContextUI()
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
}