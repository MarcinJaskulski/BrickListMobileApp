package com.example.bricklist

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.bricklist.Models.InventoryModel
import com.example.bricklist.Models.InventoryPartsModel
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.time.LocalDateTime

class DatabaseHelper(private val myContext: Context) : SQLiteOpenHelper(myContext, DB_NAME, null, 10) {
    var DB_PATH: String? = null
    private var myDataBase: SQLiteDatabase? = null
    @Throws(IOException::class)
    fun createDataBase() {
        val dbExist = checkDataBase()
        if (dbExist) {
        } else {
            this.readableDatabase
            try {
                copyDataBase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }
        }
    }

    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            val myPath = DB_PATH + DB_NAME
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: SQLiteException) {
        }
        checkDB?.close()
        return if (checkDB != null) true else false
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        val myInput =
            myContext.assets.open(DB_NAME)
        val outFileName = DB_PATH + DB_NAME
        val myOutput: OutputStream = FileOutputStream(outFileName)
        val buffer = ByteArray(10)
        var length: Int
        while (myInput.read(buffer).also { length = it } > 0) {
            myOutput.write(buffer, 0, length)
        }
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }

    @Throws(SQLException::class)
    fun openDataBase() {
        val myPath = DB_PATH + DB_NAME
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)
    }

    @Synchronized
    override fun close() {
        if (myDataBase != null) myDataBase!!.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (newVersion > oldVersion) try {
            copyDataBase()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun query(
        table: String?,
        columns: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        groupBy: String?,
        having: String?,
        orderBy: String?
    ): Cursor {
        return myDataBase!!.query(
            "Parts",
            null,
            null,
            null,
            null,
            null,
            null
        )
    }

    companion object {
        private const val DB_NAME = "BrickList.db"
    }

    init {
        DB_PATH = "/data/data/" + myContext.packageName + "/" + "databases/"
        Log.e("Path 1", DB_PATH)
    }

    // ----

    fun findPart(id: Int) : String?{
        val query = "SELECT Name FROM Parts WHERE id='$id'"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var name: String? = null

        if(cursor.moveToFirst()){
            name = cursor.getString(0)
            cursor.close()
        }
        db.close()
        return  name
    }

    fun findInventory() : String?{
        val query = "SELECT * FROM Inventories"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var name: String? = null

        if(cursor.moveToFirst()){
            name = cursor.getString(0)
            cursor.close()
        }
        db.close()
        return  name
    }

    // --

    fun addInventory(inventory: InventoryModel){
        val values = ContentValues()
        values.put("id", inventory.getId())
        values.put("Name", inventory.getName())
        values.put("Active", inventory.getActive())
        values.put("LastAccessed", inventory.getLastAccessed())
        val db = this.writableDatabase
        db.insert("Inventories", null, values)
        db.close()
    }

    fun addInventoryPart(inventoryPartModel: InventoryPartsModel){
        val itemID = inventoryPartModel.getItemID()
        val colorID = inventoryPartModel.getColorID()

        val values = ContentValues()
        values.put("id", inventoryPartModel.getId())
        values.put("InventoryID", inventoryPartModel.getInventoryID())
        values.put("TypeID", inventoryPartModel.getTypeID())
        values.put("ItemID", itemID)
        values.put("QuantityInSet", inventoryPartModel.getQuantityInSet())
        values.put("QuantityInStore", inventoryPartModel.getQuantityInStore())
        values.put("ColorID", colorID)
//        values.put("Extra", inventoryPartModel.getExtra())

        val db = this.writableDatabase
        db.insert("InventoriesParts", null, values)
        db.close()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun getInventoriesList(active: Boolean): MutableList<InventoryModel>{
        val inventoriesList = mutableListOf<InventoryModel>()
        val db = this.writableDatabase

        val query:String = if( active == true) "SELECT * FROM Inventories WHERE Active=1 ORDER BY LastAccessed desc"
        else "SELECT * FROM Inventories ORDER BY LastAccessed desc"

        val cursor = db.rawQuery(query,null)

        while(cursor.moveToNext()){
            val id = cursor.getInt(0)
            val name  = cursor.getString(1)
            val active = cursor.getInt(2)
            val activeBool: Boolean = active > 0
            val date = LocalDateTime.now().toString()
            val inventory  = InventoryModel(
                id,
                name,
                activeBool,
                date
            )
            inventoriesList.add(inventory)
        }
        cursor.close()
        return inventoriesList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getInventoriesPartsList(inventoryId: Int): MutableList<InventoryPartsModel>{
        val inventoryPartsList = mutableListOf<InventoryPartsModel>()
        val db = this.writableDatabase

        val query:String = "SELECT * FROM InventoriesParts WHERE InventoryID=$inventoryId ORDER BY ColorID desc"
        val cursor = db.rawQuery(query,null)

        while(cursor.moveToNext()){
            val id = cursor.getInt(0)
            val inventoryID  = cursor.getInt(1)
            val typeID = cursor.getInt(2)
            val itemID = cursor.getInt(3)
            val quantityInSet = cursor.getInt(4)
            val quantityInStore = cursor.getInt(5)
            val colorID = cursor.getInt(6)
            val extra = cursor.getInt(7)
            val name = getPartNameById(itemID)

            val inventoryParts  = InventoryPartsModel(
                id, inventoryID, typeID,
                itemID, quantityInSet, quantityInStore,
                colorID, extra, name
            )

            // dwie wartości potrzebne do pobrania obrazka
            val partCode = getPartCodeById(itemID)
            val codeCode = getCodeCodeByItemIdColorId(itemID, colorID)

            var photo = getImage(codeCode)
            // jeśli wczeniej nie było w bazie zdjęcia, to się teraz doda
            if(photo == null){
                val cd=ImgDownloader()
                cd.execute(codeCode.toString(),
                    "https://www.lego.com/service/bricks/5/2/" + codeCode,
                    "http://img.bricklink.com/P/" + codeCode + "/" + partCode + ".gif",
                    "https://www.bricklink.com/PL/" + partCode + ".jpg")
                photo = getImage(codeCode) // skoro synchronicznie jest pobrane zdjęcie, to to niekoniecznie coś przeczyta
            }
//            val b = a.get()
//            if(photo == null)
//                photo = b.toByteArray()
            inventoryParts.setPhoto(photo)

            inventoryPartsList.add(inventoryParts)
        }
        cursor.close()
        return inventoryPartsList
    }

    fun generateInventoryID(): Int {
        val query = "SELECT id FROM Inventories WHERE id = (SELECT MAX(id)  FROM Inventories)"
        return queryInDBInt(query)+1
    }

    fun generateInventoryPartID(): Int {
        val query = "SELECT id FROM InventoriesParts WHERE id = (SELECT MAX(id)  FROM InventoriesParts)"
        return queryInDBInt(query)+1
    }

    fun getItemTypeID(code:String):Int{
        val query = "SELECT id FROM ItemTypes WHERE Code='$code'"
        return queryInDBInt(query)
    }

    fun getPartID(code:String): Int {
        val query = "SELECT id FROM Parts WHERE  Code='$code'"
        return queryInDBInt(query)
    }

    fun getPartNameById(id: Int): String{
        val query = "SELECT Name FROM Parts WHERE id=$id"
        return queryInDBString(query)
    }

    fun getItemTypeCodeById(id: Int):String{
        val query = "SELECT Code FROM ItemTypes WHERE id=$id"
        return queryInDBString(query)
    }

    fun getPartCodeById(id:Int):String{
        val query = "SELECT Code FROM Parts WHERE id=$id"
        return queryInDBString(query)
    }

    fun getColorIdByCode(code:String): Int {
        val query = "SELECT id FROM Colors WHERE Code='$code'"
        return queryInDBInt(query)
    }


    fun getCodeCodeByItemIdColorId(itemId:Int, colorId:Int):Int{
        val query = "SELECT Code FROM Codes WHERE ItemID=$itemId AND ColorID=$colorId"
        return queryInDBInt(query)
    }



    fun queryInDBInt(query: String): Int{
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        var value:Int = 0
        if(cursor.moveToFirst()){
            value = Integer.parseInt(cursor.getString(0))
            cursor.close()
        }
        db.close()
        return value
    }

    fun queryInDBString(query: String): String{
        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        var value:String = ""
        if(cursor.moveToFirst()){
            value = cursor.getString(0)
            cursor.close()
        }
        db.close()
        return value
    }

    fun insertImage(code: String, img:ByteArray?){
        val values = ContentValues()
        values.put("Image",img)
        val strFilter = "Code='$code'"

        val db = this.writableDatabase
        db.update("Codes",values,strFilter,null)
        db.close()
    }

    fun updateQuantity(id: Int, amount: Int){
        val values = ContentValues()
        values.put("QuantityInStore",amount)
        val strFilter = "id=$id"

        val db = this.writableDatabase
        db.update("InventoriesParts",values,strFilter,null)
        db.close()
    }

    fun updateActive(id: Int, isActive: Boolean){
        val values = ContentValues()
        values.put("Active",isActive.toInt())
        val strFilter = "id=$id"

        val db = this.writableDatabase
        db.update("Inventories",values,strFilter,null)
        db.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLastAccessed(id: Int){
        val values = ContentValues()
        val date = LocalDateTime.now().toString()
        values.put("LastAccessed",date)
        val strFilter = "id=$id"

        val db = this.writableDatabase
        db.update("Inventories",values,strFilter,null)
        db.close()
    }

    // --------------------

    fun insertImage(code:Int,img:ByteArray?){
        val values = ContentValues()
        values.put("Image",img)
        val strFilter = "Code=$code"

        val db = this.writableDatabase
        db.update("Codes",values,strFilter,null)
        db.close()
    }

    fun getImage(code:Int): ByteArray?{
        var photo : ByteArray? = null
        val query = "SELECT Image FROM Codes WHERE  Code=$code"

        val db = this.writableDatabase
        val cursor = db.rawQuery(query,null)
        if(cursor.moveToFirst()){
            photo = cursor.getBlob(0)
            cursor.close()
        }
        db.close()
        if(photo==null)
            return null
        return photo
    }

    fun Boolean.toInt() = if (this) 1 else 0
    fun Int.toBoolean() = if (this==1) true else false


    // --------------------------------------------------------

    private inner class ImgDownloader: AsyncTask<String, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
        }

        override fun doInBackground(vararg params: String?): String {
            try{
                var photo: ByteArray? = null

                var url = URL("https://www.lego.com/service/bricks/5/2/4227395")
                var connection: HttpURLConnection? = null

                for (i in 1..3) {
                    url = URL(params[i])
                    connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        break
                    }
                }

                val lenghtOfFile = connection!!.contentLength
                val isStream = url.openStream() //val inputStream = connection.inputStream

                val fos = ByteArrayOutputStream();

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
                photo = fos.toByteArray()

                isStream.close()
                fos.close()

                // zapisa do bazy
                insertImage(params[0]!!.toInt(),photo)
//                return photo.toString()
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
}