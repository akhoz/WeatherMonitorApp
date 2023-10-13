package com.weathermonitor

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

import android.graphics.Color
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData


class MainActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference

    @SuppressLint("SuspiciousIndentation", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Firebase section----------------------------------------------------------
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        databaseReference = FirebaseDatabase.getInstance().reference

        //val textView = findViewById<TextView>(R.id.textView)

        val temperatureReport = mutableListOf<Any>()
        val celciusReport = mutableMapOf<String, Map<String, Any>>()
        val farenheitReport = mutableMapOf<String, Map<String, Any>>()


        val celciusMap = mutableMapOf<String, Any>()
        val farenheitMap = mutableMapOf<String, Any>()
        val temperatureKeys = mutableListOf<String>()
        val weatherMonitorReference = databaseReference.child("Temperature")

        weatherMonitorReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dateReference in snapshot.children) {
                    val value = dateReference.getValue()
                    val key = dateReference.key.toString()
                    key.let { temperatureKeys.add(it) }
                    value?.let { temperatureReport.add(it) }

                    for (hourReference in dateReference.children) {
                        val hourKey = hourReference.key.toString()
                        val hourCelcius = hourReference.child("celcius").getValue()
                        val hourFarenheit = hourReference.child("farenheit").getValue()
                        celciusMap[hourReference.key.toString()] = hourCelcius!!
                        farenheitMap[hourReference.key.toString()] = hourFarenheit!!

                    }
                    celciusReport[key] = celciusMap
                    farenheitReport[key] = farenheitMap
                }
                //textView.text = farenheitReport.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to read value.")
            }
        })

        //UI section----------------------------------------------------------
        val dayButton = findViewById<Button>(R.id.dayButton)
        val inRangeButton = findViewById<Button>(R.id.inRangeButton)

        val spinner = findViewById<Spinner>(R.id.spinner)

        val celciusEntries = ArrayList<Entry>()
        val farenheitEntries = ArrayList<Entry>()
        val lineChart = findViewById<LineChart>(R.id.lineChart)

        dayButton.setOnClickListener {

            //Spinner construction
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, temperatureKeys)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.visibility = View.VISIBLE

            //Spinner selection
            spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    println("Nothing selected")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedOption = temperatureKeys[position]

                    celciusEntries.clear()
                    farenheitEntries.clear()

                    val celciusData = celciusReport[selectedOption]
                    val farenheitData = farenheitReport[selectedOption]

                    for (hour in celciusData!!.keys) {
                        celciusEntries.add(Entry(hour.toFloat(), celciusData[hour].toString().toFloat()))
                    }

                    for (hour in farenheitData!!.keys) {
                        farenheitEntries.add(Entry(hour.toFloat(), farenheitData[hour].toString().toFloat()))
                    }

                    val celciusDataSet = LineDataSet(celciusEntries, "Celcius")
                    val farenheitDataSet = LineDataSet(farenheitEntries, "Farenheit")

                    celciusDataSet.color = Color.RED
                    farenheitDataSet.color = Color.BLUE

                    val lineData = LineData(celciusDataSet, farenheitDataSet)
                    lineChart.data = lineData
                    //lineChart.visibility = View.VISIBLE
                    lineChart.data.notifyDataChanged()
                    lineChart.notifyDataSetChanged()
                    lineChart.invalidate()

                }
            })


        }
    }
}
