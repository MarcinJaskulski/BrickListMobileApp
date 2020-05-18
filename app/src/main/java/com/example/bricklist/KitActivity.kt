package com.example.bricklist

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.example.bricklist.Models.InventoryModel
import com.example.bricklist.Models.InventoryPartsModel
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class KitActivity : AppCompatActivity() {

    val REQUEST_CODE = 30000
    var itemList: MutableList<InventoryPartsModel>? = null
    var kitId: Int? = null
    var kitName: String? = "Default.xml"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kit)

        val extras = intent.extras ?: return
        val tempId = extras.getInt("Id") // odebranie info
        kitName = extras.getString("Name") + ".xml"

        if(tempId != null)
            kitId = tempId.toInt()

        showData()
    }

    fun mainListClick(v: View){
        val i = Intent(this, MainActivity::class.java)
        i.putExtra("Parametr", "Twoje Dane") // Wysłanie danych
        startActivityForResult(i,REQUEST_CODE) // wysłanie kodu
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showData(){
        tableCurrencies.removeAllViews()
        itemList = mutableListOf()

        val myDbHelper = DatabaseHelper(this@KitActivity)
        if(kitId != null)
            itemList  = myDbHelper.getInventoriesPartsList(kitId!!.toInt())

        if(itemList != null)
            showItemList(itemList!!)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun showItemList(items :List<InventoryPartsModel>) {
        val leftRowMargin = 0
        val topRowMargin = 0
        val rightRowMargin = 0
        val bottomRowMargin = 0
        var mediumTextSize = 0

        mediumTextSize = resources.getDimension(R.dimen.font_size_medium).toInt()
        val rows = items.count()

        // -1 oznacza nagłówek
        for (i in 0..rows - 1) {

            if(items.get(i).getItemID() == 0)
                continue

            var itemId: String? = null
            itemId = items.get(i).getItemID().toString()

            var inStore = items.get(i).getQuantityInStore()
            val inSet = items.get(i).getQuantityInSet()

            // image
            val imgV = ImageView(this)
            val photo = items.get(i).getPhoto()
            var bm: Bitmap? = null
            if(photo != null)
                bm = BitmapFactory.decodeByteArray(photo, 0, photo!!.size)
            imgV.setImageBitmap(bm)


            // itemId
            val tv = TextView(this)
            tv.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            tv.gravity = Gravity.CENTER
            tv.setPadding(20, 15, 20, 15)
            run({
                tv.setBackgroundColor(Color.parseColor("#32CD32"))
                tv.setText(items.get(i).getImageCode().toString())
//                tv.setText(itemId)
//                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mediumTextSize.toFloat())
            })

            // Nazwa
            val tv2 = TextView(this)
            tv2.setText(items.get(i).getName())
            tv2.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            tv2.setPadding(20, 15, 20, 15)

            // Ilość
            val tv3 = TextView(this)
            tv3.text = items.get(i).getQuantityInStore().toString() + "/" + items.get(i).getQuantityInSet().toString() // ile w zestawie
            tv3.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            tv3.id = View.generateViewId()
            tv3.setPadding(20, 15, 20, 15)


            val tr = TableRow(this)
            tr.id = i + 1
            tr.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT)

            val btn1 = ImageButton(this)
            btn1.setOnClickListener{
                if(inStore -1 >= 0){
                    inStore--
                    tv3.text = (inStore).toString() + "/" + inSet.toString()
                    subQuantity(items.get(i).getId(), inStore)
                }
                if(inStore != inSet){
                    tr.setBackgroundColor(Color.parseColor("#ffffff"))
                }
            }
            btn1.setImageResource(R.drawable.ic_sub)
            btn1.setBackgroundColor(255)

            val btn2 = ImageButton(this)
            btn2.setOnClickListener{
                if(inStore + 1 <= inSet){
                    inStore++
                    tv3.text = (inStore).toString() + "/" + inSet.toString()
                    addQuantity(items.get(i).getId(), inStore)
                }
                if(inStore == inSet){
                    tr.setBackgroundColor(Color.parseColor("#32CD32"))
                }
            }
            btn2.setImageResource(R.drawable.ic_add)
            btn2.setBackgroundColor(255)


            tr.addView(imgV)
//            tr.addView(tv)
            tr.addView(tv3)

            tr.addView(btn1)
            tr.addView(btn2)
            tr.addView(tv2)


            tableCurrencies.addView(tr)
            if(inStore == inSet){
                tr.setBackgroundColor(Color.parseColor("#32CD32"))
            }

            // linia separująca
            if (i > -1) {
                val trSep = TableRow(this)
                val trParamsSep = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT)
                trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin)

                trSep.layoutParams = trParamsSep
                val tvSep = TextView(this)
                val tvSepLay = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT)
                tvSepLay.span = 5
                tvSep.layoutParams = tvSepLay
                tvSep.setBackgroundColor(Color.parseColor("#000000"))
                tvSep.height = 10

                trSep.addView(tvSep)
                tableCurrencies.addView(trSep, trParamsSep)
            }
        }
    }

    fun subQuantity(id: Int, amount: Int){
        val myDbHelper = DatabaseHelper(this@KitActivity)
        myDbHelper.updateQuantity(id, amount)
    }

    fun addQuantity(id: Int, amount: Int){
        val myDbHelper = DatabaseHelper(this@KitActivity)
        myDbHelper.updateQuantity(id, amount)
    }


    fun btnExportXML(v: View){
        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()
        val rootElement: Element = doc.createElement("INVENTORY")

        val items = itemList!!
        val rows = items.count()

        // -1 oznacza nagłówek
        for (i in 0..rows - 1) {
//           var a = items.get(i).getName()
            val item: Element = doc.createElement("ITEM")

            val myDbHelper = DatabaseHelper(this@KitActivity)
            val itemTypeCode = myDbHelper.getItemTypeCodeById(items.get(i).getTypeID())
            val itemType: Element = doc.createElement("ITEMTYPE")
            itemType.appendChild(doc.createTextNode(itemTypeCode))
            item.appendChild(itemType)

            val itemID: Element = doc.createElement("ITEMID")
            itemID.appendChild(doc.createTextNode(items.get(i).getItemID().toString()))
            item.appendChild(itemID)

            val color: Element = doc.createElement("COLOR")
            color.appendChild(doc.createTextNode(items.get(i).getColorID().toString()))
            item.appendChild(color)

            val qtyFilled: Element = doc.createElement("QTYFILLED")
            val needQuantity = items.get(i).getQuantityInSet() - items.get(i).getQuantityInStore()
            qtyFilled.appendChild(doc.createTextNode(needQuantity.toString()))
            item.appendChild(qtyFilled)

            rootElement.appendChild(item)
        }

        doc.appendChild(rootElement)
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        val path =  this.filesDir
        val outDir = File(path, "BrickExport")
        outDir.mkdir()

        val filePath = File(outDir, kitName)
        transformer.transform(DOMSource(doc), StreamResult(filePath))


        Toast.makeText(this@KitActivity, "Wyeksportowano XML: " + filePath, Toast.LENGTH_LONG).show()
    }
}
