package com.example.bricklist

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.bricklist.Models.InventoryModel
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.sql.SQLException


class MainActivity : AppCompatActivity() {

    val REQUEST_CODE = 10000
    val REQUEST_KIT_CODE = 30000
    val REQUEST_SETTINGS = 40000
    var kitList: MutableList<InventoryModel>? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myDbHelper = DatabaseHelper(this@MainActivity)
        try {
            myDbHelper.createDataBase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }
        try {
            myDbHelper.openDataBase()
        } catch (sqle: SQLException) {
            throw sqle
        }
//        Toast.makeText(this@MainActivity, "Successfully Imported", Toast.LENGTH_SHORT).show()
        var c = myDbHelper.query(
            "Parts",
            null,
            null,
            null,
            null,
            null,
            null
        )
        showData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        showData()
    }

    fun addKit(v: View){
        val i = Intent(this, AddItemActivity::class.java)
        i.putExtra("Parametr", "Twoje Dane") // Wysłanie danych
        startActivityForResult(i,REQUEST_CODE) // wysłanie kodu
    }

    fun settingsClick(v: View){
        val i = Intent(this, SettingsActivity::class.java)
        startActivityForResult(i,REQUEST_SETTINGS) // wysłanie kodu
    }

    fun showKit(id: Int, name:String){
        val i = Intent(this, KitActivity::class.java)
        i.putExtra("Id", id) // Dodanie danych
        i.putExtra("Name", name) // Dodanie danych
        startActivityForResult(i,REQUEST_KIT_CODE) // wysłanie kodu
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showData(){
        tebleKits.removeAllViews()
        kitList = mutableListOf()

        val data = readFile()
        val dataList = data.split(";")

        val myDbHelper = DatabaseHelper(this@MainActivity)
        kitList  = myDbHelper.getInventoriesList(dataList[0].toInt().toBoolean());

        if(kitList != null)
            showKitList(kitList!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showKitList(kits:List<InventoryModel>) {
        val leftRowMargin = 0
        val topRowMargin = 0
        val rightRowMargin = 0
        val bottomRowMargin = 0
        var mediumTextSize = 0

        mediumTextSize = resources.getDimension(R.dimen.font_size_medium).toInt()
        val rows = kits.count()

        for (i in 0..rows - 1) {
            var kitName: String = kits.get(i).getName()
            var kitId = kits.get(i).getId()

            val btn = Button(this)
            btn.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            btn.gravity = Gravity.CENTER
            btn.setOnClickListener{
                val myDbHelper = DatabaseHelper(this@MainActivity)
                myDbHelper.updateLastAccessed(kitId)

                showKit(kitId, kitName )
            }
            btn.setPadding(20, 15, 20, 15)
            run({
                btn.setBackgroundColor(Color.parseColor("#D3D3D3"))
                btn.setText(kitName)
                btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize.toFloat())
            })

            // add table row
            val tr = TableRow(this)
            tr.id = i + 1
            tr.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT)
            tr.setPadding(10, 0, 10, 0)


            val switch = Switch(this)
            val activity = kits.get(i).getActive()
            switch.isChecked = activity

            switch.setOnClickListener{
                val myDbHelper = DatabaseHelper(this@MainActivity)
                if(activity){
                    myDbHelper.updateActive(kits.get(i).getId(), false)

                    val data = readFile()
                    val dataList = data.split(";")
                    if(dataList[0].toInt() == 1)
                        tebleKits.removeView(tr)
                }
                else
                    myDbHelper.updateActive(kits.get(i).getId(), true)
            }

//            // ustawieni elementów
//            val verticalLinearLayout = LinearLayout(this)
//            verticalLinearLayout.orientation = LinearLayout.VERTICAL
//            verticalLinearLayout.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
//                TableRow.LayoutParams.WRAP_CONTENT, 1.0F)
//
//            val horizontalLinearLayoutLayout = LinearLayout(this)
//            horizontalLinearLayoutLayout.orientation = LinearLayout.HORIZONTAL
//            horizontalLinearLayoutLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//
//            horizontalLinearLayoutLayout.addView(imgV)
//            horizontalLinearLayoutLayout.addView(imgV)

            tr.addView(btn)
            tr.addView(switch)
            tebleKits.addView(tr)

            // linia separująca
            if (i > -1) {
                val trSep = TableRow(this)
                val trParamsSep = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT)
                trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin)

                trSep.layoutParams = trParamsSep
                val tvSep = TextView(this)
                val tvSepLay = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT)
                tvSepLay.span = 4
                tvSep.layoutParams = tvSepLay
                tvSep.setBackgroundColor(Color.parseColor("#000000"))
                tvSep.height = 10

                trSep.addView(tvSep)
                tebleKits.addView(trSep, trParamsSep)
            }
        }
    }


    fun readFile():String{
        val defaultValue = "1;http://fcds.cs.put.poznan.pl/MyWeb/BL/"
        val fileName = "settings.txt"
        try{
            if(FileExist(fileName)){
                val file = InputStreamReader(openFileInput((fileName)))
                val br = BufferedReader(file)

                var line = br.readLine()
                file.close()
                return line
            }
            else{
                val file = OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE))
                file.write(defaultValue)
                file.flush()
                file.close()
            }

        }catch (e: Exception){

        }
        return defaultValue
    }

    fun FileExist(path: String): Boolean{
        val file = baseContext.getFileStreamPath(path)
        return file.exists()
    }

    fun Boolean.toInt() = if (this) 1 else 0
    fun Int.toBoolean() = if (this==1) true else false
}
