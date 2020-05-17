package com.example.bricklist

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.bricklist.Models.InventoryModel
import com.example.bricklist.Models.InventoryPartsModel
import com.example.bricklist.Models.KitModel
import kotlinx.android.synthetic.main.activity_add_item.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import javax.xml.parsers.DocumentBuilderFactory

class AddItemActivity : AppCompatActivity() {

    val REQUEST_CODE = 10000
    var prefixUrl = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
    var choosenKitNumber : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        val data = readFile()
        val dataList = data.split(";")
        prefixUrl = dataList[1]
    }

    fun addKitComplete(v: View){
        if(!kitNumber.text.isNullOrEmpty()){
            choosenKitNumber = kitNumber.text.toString().toInt()
            val fileUrl = prefixUrl + choosenKitNumber + ".xml"
            val filePath: File? = this.getExternalFilesDir(null)

            val cd=XMLDownloader()
            cd.execute()
        }
        else{
            Toast.makeText(applicationContext, "Nie podałeś numeru zestawu",Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadData(){
        val filename = choosenKitNumber.toString() + ".xml"
        val path = filesDir
        val inDir = File(path, "XML")

        if(inDir.exists()){
            val file =File(inDir, filename)
            if(file.exists()){

                // Dodanie Inventory
                val myDbHelper = DatabaseHelper(this@AddItemActivity)
                val inventoryId = myDbHelper.generateInventoryID()
                var kitName = kitName.text.toString()
                val inventoryModel = InventoryModel(inventoryId, kitName, true, LocalDateTime.now().toString())
                myDbHelper.addInventory(inventoryModel)


                val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
                xmlDoc.documentElement.normalize()

                val items: NodeList = xmlDoc.getElementsByTagName("ITEM")

                for(i in 0..items.length-1){
                    val itemNode: Node = items.item(i)
                    if(itemNode.getNodeType() == Node.ELEMENT_NODE){
                        val elem = itemNode as Element
                        val children = elem.childNodes

                        var itemId: String? = null
                        var type: String? = null
                        var qty:String? = null
                        var color:String? = null
                        var extra:String? = null
                        var alternate:String? = null

                        for(j in 0..children.length-1){
                            val node = children.item(j)
                            if(node is Element){
                                when(node.nodeName){
                                    "ITEMTYPE" -> {type = node.textContent}
                                    "ITEMID" -> {itemId = node.textContent}
                                    "QTY" -> {qty = node.textContent}
                                    "COLOR" -> {color = node.textContent}
                                    "EXTRA" -> {extra = node.textContent}
                                    "ALTERNATE" -> {alternate = node.textContent}
                                }
                            }
                        }
                        if(alternate == "N"){
                            // Dodanie do bazy danych elementu
                            val myDbHelper = DatabaseHelper(this@AddItemActivity)
                            val id = myDbHelper.generateInventoryPartID()

                            if(id != null && inventoryId != null && type != null && itemId != null
                                && qty != null && color != null && extra != null){
                                var itemIdInt: Int =myDbHelper.getPartID(itemId)
                                var typeInt: Int = myDbHelper.getItemTypeID(type)
                                var colorIdInt:Int = myDbHelper.getColorIdByCode(color)  //colorId.toInt()
//                                var extraInt:Int = extra.toInt()

                                val inventoryPartModel = InventoryPartsModel(id,
                                    inventoryId,
                                    typeInt,
                                    itemIdInt,
                                    qty.toInt(),
                                    0,
                                    colorIdInt,
                                    0,
                                    "")
                                myDbHelper.addInventoryPart(inventoryPartModel)
                            }
                        }
                    }
                }
            }
            Toast.makeText(applicationContext, "Dodano zestaw",Toast.LENGTH_SHORT).show()
            val i = Intent(this, MainActivity::class.java)
            startActivityForResult(i,REQUEST_CODE)
        }
        else
            Toast.makeText(applicationContext, "Zły URL",Toast.LENGTH_SHORT).show()
    }


    private inner class XMLDownloader: AsyncTask<String, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            loadData()
        }

        override fun doInBackground(vararg params: String?): String {
            try{
                val url = URL(prefixUrl + choosenKitNumber + ".xml")
                val connection = url.openConnection()
                connection.connect()
                val lenghtOfFile = connection.contentLength
                val isStream = url.openStream()
                val testDirectory = File("$filesDir/XML")
                if(!testDirectory.exists()) testDirectory.mkdir()
                val fos = FileOutputStream("$testDirectory/" + choosenKitNumber + ".xml")
                val data = ByteArray(1024)
                var count = 0
                var total: Long = 0
                var progress = 0
                count = isStream.read(data)
                while(count!=-1){
                    total += count.toLong()
                    val progress_temp = total.toInt()*100/lenghtOfFile
                    if(progress_temp % 10 ==0 && progress != progress_temp){
                        progress = progress_temp
                    }
                    fos.write(data,0,count)
                    count = isStream.read(data)
                }
                isStream.close()
                fos.close()
            }
            catch (e: MalformedURLException){
                return "Malformed URL"
            }
            catch (e: FileNotFoundException){
                return "File not found"
            }
            catch (e: IOException){
                return "IO Exception"
            }
            return "success"
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
}
