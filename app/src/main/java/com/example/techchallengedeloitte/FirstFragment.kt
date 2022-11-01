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
import androidx.fragment.app.Fragment
import com.example.techchallengedeloitte.constants.Constants
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

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var downloadID: Long = 0
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
        binding.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(requireContext(), "Teste", Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Toast.makeText(requireContext(), "Teste", Toast.LENGTH_SHORT).show()
                return false
            }
        })
        **/
    }

    /**
     * Show alert to user to explain why the app needs external storage access
     */
    fun showInContextUI() {
        val dialogBuilder = let {
            AlertDialog.Builder(requireContext()).also {

                it.setMessage("The app needs access to the device storage to save data!")

                    .setPositiveButton("Accept", DialogInterface.OnClickListener() { dialog, _ -> dialog.cancel()
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

            val request = DownloadManager.Request(Uri.parse(Constants.PostalCodesFile))
                .setTitle("Postal Codes")
                .setDescription("Downloading Postal Codes file....")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    File.separator + "PostalCodes.csv"
                )

            val dm = requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

            activity?.registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            /*requireActivity().registerReceiver(
                onDownloadComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            );*/

            downloadID = dm.enqueue(request)

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
            alterView(true)

            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator +
                        "PostalCodes.csv"
            )
            val rows: List<List<String>> = csvReader().readAll(file)

            var listPostalCodes = listOf<PostalCodes>()

            for (item in rows) {

                // To ignore the file header
                if (item[0] != "cod_distrito") {
                    val searchString : String = item[3].lowercase() + " " + item[16].lowercase() + " " + item[14] + " " + item[15]
                    val postalCode = PostalCodes(
                        item[0].toInt(), item[1].toInt(), item[2].toInt(), item[3],
                        item[4], item[5], item[6], item[7], item[8], item[9], item[10], item[11],
                        item[12], item[13], item[14].toInt(), item[15].toInt(), item[16], changeQueryString(searchString)
                    )

                    listPostalCodes += postalCode
                }
            }

            val result = DBHelper(requireContext(),null).addData(listPostalCodes)
            if (result){
                showAlert("Data saved! \n The app is ready for search.")
            }
            else{
                showAlert("Error saving the data from the file!")
            }
        } catch (e: java.lang.Exception) {
            showAlert("Error loading file!")
        } finally {
            alterView(false)
        }
    }

    /**
     * Replace all special characters
     */
    private fun changeQueryString(stringQuery : String) : String{
        var stringFinal : String = ""
        try {
            stringFinal = stringQuery
                .replace("ã","a")
                .replace("ç","c")
                .replace("á","a")
                .replace("à","a")
                .replace("ó","o")
                .replace("ò","o")
                .replace("é","e")
                .replace("è","e")
                .replace("ì","i")
                .replace("í","i")
                .replace("ú","u")
                .replace("ù","u")
        }
        catch (e: java.lang.Exception){
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
        val alert = dialogBuilder?.create()
        alert?.setTitle("Alert")
        alert?.show()
    }

    /**
     * Show or hide different layout elements
     */
    private fun alterView(change : Boolean){
        if(change){
            requireActivity().runOnUiThread(){
                binding.progressBarDownload.visibility = View.VISIBLE
                binding.textDescriptionDownload.visibility = View.VISIBLE
                binding.listviewPostCodes.visibility = View.GONE
                binding.searchView.visibility = View.GONE
            }
        }
        else{
            requireActivity().runOnUiThread(){
                binding.progressBarDownload.visibility = View.GONE
                binding.textDescriptionDownload.visibility = View.GONE
                binding.listviewPostCodes.visibility = View.VISIBLE
                binding.searchView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        requireActivity().unregisterReceiver(onDownloadComplete);
    }
}
