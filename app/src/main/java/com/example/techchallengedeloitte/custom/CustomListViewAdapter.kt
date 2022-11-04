package com.example.techchallengedeloitte.custom

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.techchallengedeloitte.R

class CustomListViewAdapter(
    private val context: Activity,
    private val postCodes: List<PostalCodes>
) : ArrayAdapter<PostalCodes>(context, R.layout.row_item, postCodes) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.row_item, null, true)

        val postalCodeTextView = rowView.findViewById(R.id.textViewPostalCode) as TextView
        val locationTextView = rowView.findViewById(R.id.textViewLocation) as TextView
        val resultString =
            postCodes[position].num_cod_postal.toString() + "-" +
                    postCodes[position].ext_cod_postal.toString()
        postalCodeTextView.text = resultString
        locationTextView.text = postCodes[position].desig_postal

        return rowView
    }
}