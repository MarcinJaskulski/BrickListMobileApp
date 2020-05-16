package com.example.bricklist

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class SettingsActivity : AppCompatActivity() {

    val fileName = "settings.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val data = readFile()
        val dataList = data.split(";")

        if(dataList[0] == "0") // nie pokazuj
            activeKitSwitch.isChecked = false
        else
            activeKitSwitch.isChecked = true

        urlPath.setText(dataList[1])

    }

    fun saveSettings(v: View){
        var isActive = activeKitSwitch.isChecked
        var pathUrl = urlPath.text

        var text = isActive.toInt().toString() + ";" + pathUrl

        val file = OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE))

        file.write(text)

        file.flush()
        file.close()

        Toast.makeText(this, "Ustawienia zapisane", Toast.LENGTH_SHORT).show()
    }

    fun readFile():String{
        val defaultValue = "0;http://fcds.cs.put.poznan.pl/MyWeb/BL/"
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
