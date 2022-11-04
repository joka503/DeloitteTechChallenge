package com.example.techchallengedeloitte

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.techchallengedeloitte.constants.Constants
import com.example.techchallengedeloitte.custom.CustomListViewAdapter
import com.example.techchallengedeloitte.custom.PostalCodes
import com.example.techchallengedeloitte.data.DBHelper
import com.example.techchallengedeloitte.databinding.FragmentFirstBinding
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private var downloadID: Long = 0
    private val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID === id) {
                fileDownloaded()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    val finalString = changeQueryString(query.lowercase().replace(" ","* "))
                    getDataFromDatabase(finalString)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null || newText.isEmpty()) {
                    requireActivity().runOnUiThread() {
                        binding.listviewPostCodes.adapter = null
                    }
                }
                return false
            }
        })
    }

    /**
     * Show alert to user to explain why the app needs external storage access
     */
    fun showInContextUI() {
        val dialogBuilder = let {
            AlertDialog.Builder(requireContext()).also {

                it.setMessage("The app needs access to the device storage to save data!")

                    .setPositiveButton("Accept", DialogInterface.OnClickListener() { dialog, _ ->
                        dialog.cancel()
                    })

                    .setNegativeButton("Deny", DialogInterface.OnClickListener() { dialog, _ ->
                        dialog.cancel()
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
    fun checkData() {
        val db = let { DBHelper(requireContext(), null) }
        val numRecords = db?.getNumData()
        if (numRecords != 0) {

        } else {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator +
                        "PostalCodes.csv"
            )
            if (file.exists()) {
                fileDownloaded()
            } else {
                downloadFile()
            }
        }
    }

    /**
     * Get the file
     */
    private fun downloadFile() {
        try {
            alterView(true)
            activity?.registerReceiver(
                onDownloadComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )

            Thread {
                try {
                    val request = DownloadManager.Request(Uri.parse(Constants.PostalCodesFile))
                        .setTitle("Postal Codes")
                        .setDescription("Downloading Postal Codes file....")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setAllowedOverMetered(true)
                        .setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            File.separator + "PostalCodes.csv"
                        )

                    val dm =
                        requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                    downloadID = dm.enqueue(request)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()

        } catch (e: java.lang.Exception) {
            showAlert("Error downloading file!")
            alterView(false)
        }
    }

    /**
     * Open the file and store it's data
     */
    private fun fileDownloaded() {
        try {
            //alterView(true)

            requireActivity().runOnUiThread(){
                binding.textDescriptionDownload.text = getString(R.string.fileLoading)
            }

            Thread {
                try {
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator +
                                "PostalCodes.csv"
                    )
                    val rows = csvReader().readAll(file)

                    var listPostalCodes = arrayListOf<PostalCodes>()

                    for (item in rows) {
                        // To ignore the file header
                        if (item[0] != "cod_distrito") {
                            val searchString: String =
                                item[3].lowercase() + " " + item[16].lowercase() + " " + item[14] + " " + item[15]
                            val postalCode = PostalCodes(
                                item[3],
                                item[14].toInt(),
                                item[15].toInt(),
                                item[16],
                                changeQueryString(searchString)
                            )
                            listPostalCodes += postalCode

                            if (listPostalCodes.count() > 1000) {
                                val result =
                                    DBHelper(requireContext(), null).addData(listPostalCodes)
                                if (result) {
                                    listPostalCodes.clear()
                                }
                            }
                        }
                    }

                    if (listPostalCodes.isNotEmpty()) {
                        val result = DBHelper(requireContext(), null).addData(listPostalCodes)
                        if (result) {
                            showAlert("Data saved! \n The app is ready for search.")
                        } else {
                            showAlert("Error saving the data from the file!")
                        }
                    }

                    listPostalCodes.clear()
                    alterView(false)

                } catch (e: Exception) {
                    e.printStackTrace()
                    showAlert("Error saving the data from the file!")
                    alterView(false)
                }
            }.start()

        } catch (e: java.lang.Exception) {
            showAlert("Error loading file!")
            alterView(false)
        }
    }

    /**
     * Replace all special characters
     */
    private fun changeQueryString(stringQuery: String): String {
        var stringFinal: String = ""
        try {
            stringFinal = stringQuery
                .replace("ã", "a")
                .replace("ç", "c")
                .replace("á", "a")
                .replace("à", "a")
                .replace("ó", "o")
                .replace("ò", "o")
                .replace("é", "e")
                .replace("è", "e")
                .replace("ì", "i")
                .replace("í", "i")
                .replace("ú", "u")
                .replace("ù", "u")
        } catch (e: java.lang.Exception) {
            return ""
        }
        return stringFinal
    }

    /**
     * Show alert to user with required message
     */
    private fun showAlert(message: String) {
        val dialogBuilder = let {
            AlertDialog.Builder(requireContext()).also {

                it.setMessage(message)

                    .setPositiveButton("Ok", DialogInterface.OnClickListener() { dialog, _ ->
                    })
            }
        }
        requireActivity().runOnUiThread() {
            val alert = dialogBuilder?.create()
            alert?.setTitle("Alert")
            alert?.show()
        }
    }

    /**
     * Show or hide different layout elements
     */
    private fun alterView(change: Boolean) {
        if (change) {
            if (binding.progressBarDownload.visibility == View.GONE)
                requireActivity().runOnUiThread() {
                    binding.progressBarDownload.visibility = View.VISIBLE
                    binding.textDescriptionDownload.visibility = View.VISIBLE
                    binding.listviewPostCodes.visibility = View.GONE
                    binding.searchView.visibility = View.GONE
                }
        } else {
            if (binding.progressBarDownload.visibility == View.VISIBLE)
                requireActivity().runOnUiThread() {
                    binding.progressBarDownload.visibility = View.GONE
                    binding.textDescriptionDownload.visibility = View.GONE
                    binding.listviewPostCodes.visibility = View.VISIBLE
                    binding.searchView.visibility = View.VISIBLE
                }
        }
    }

    /**
     * Obtain data from database
     */
    private fun getDataFromDatabase(query: String) {
        try {
            Thread {
                val db = DBHelper(requireContext(), null)
                var list = db.getData(query)
                if (list != null) {
                    val adapter = CustomListViewAdapter(requireActivity(), list)
                    requireActivity().runOnUiThread() {
                        binding.listviewPostCodes.adapter = adapter
                    }
                }
            }.start()
        } catch (e: java.lang.Exception) {
            showAlert("Error obtaining data from database!")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().unregisterReceiver(onDownloadComplete);
    }
}
