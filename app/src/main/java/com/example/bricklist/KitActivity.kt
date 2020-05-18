package com.example.bricklist

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
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
        tebleKits.removeAllViews()
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

            var inStore = items.get(i).getQuantityInStore()
            val inSet = items.get(i).getQuantityInSet()

            // image
            val imgV = ImageView(this)
            val dm = DisplayMetrics()
            imgV.minimumHeight = dm.heightPixels
            imgV.minimumWidth = dm.widthPixels

            val photo = items.get(i).getPhoto()
            var bm: Bitmap? = null
            if(photo != null)
                bm = BitmapFactory.decodeByteArray(photo, 0, photo!!.size)
            imgV.setImageBitmap(bm)
            imgV.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0F)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                imgV.foregroundGravity = Gravity.CENTER
            }



            // Nazwa
            val nameTV = TextView(this)
            val displayName = items.get(i).getName() + " " + items.get(i).getColorName()
            nameTV.setText(displayName)
            nameTV.layoutParams = LinearLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1.0F)
            nameTV.setPadding(20, 15, 20, 15)

            // Ilość
            val qtTV = TextView(this)
            qtTV.text = items.get(i).getQuantityInStore().toString() + "/" + items.get(i).getQuantityInSet().toString() // ile w zestawie
            qtTV.layoutParams = LinearLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1.0F)
            qtTV.id = View.generateViewId()
            qtTV.setPadding(20, 15, 20, 15)
            qtTV.gravity = Gravity.CENTER


            val tr = TableRow(this)
            tr.id = i + 1
            tr.layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT)

            val subIB = ImageButton(this)
            subIB.setOnClickListener{
                if(inStore -1 >= 0){
                    inStore--
                    qtTV.text = (inStore).toString() + "/" + inSet.toString()
                    subQuantity(items.get(i).getId(), inStore)
                }
                if(inStore != inSet){
                    tr.setBackgroundColor(Color.parseColor("#ffffff"))
                }
            }
            subIB.setImageResource(R.drawable.ic_sub)
            subIB.setBackgroundColor(255)
            subIB.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0F)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                subIB.foregroundGravity = Gravity.CENTER
            }

            val addIB = ImageButton(this)
            addIB.setOnClickListener{
                if(inStore + 1 <= inSet){
                    inStore++
                    qtTV.text = (inStore).toString() + "/" + inSet.toString()
                    addQuantity(items.get(i).getId(), inStore)
                }
                if(inStore == inSet){
                    tr.setBackgroundColor(Color.parseColor("#32CD32"))
                }
            }
            addIB.setImageResource(R.drawable.ic_add)
            addIB.setBackgroundColor(255)
            addIB.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0F)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                addIB.foregroundGravity = Gravity.CENTER
            }

            // ustawieni elementów
            val verticalLinearLayout = LinearLayout(this)
            verticalLinearLayout.orientation = LinearLayout.VERTICAL
            verticalLinearLayout.layoutParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT, 1.0F)

            val horizontalLinearLayoutLayout = LinearLayout(this)
            horizontalLinearLayoutLayout.orientation = LinearLayout.HORIZONTAL
            horizontalLinearLayoutLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            horizontalLinearLayoutLayout.addView(imgV)
            horizontalLinearLayoutLayout.addView(qtTV)
            horizontalLinearLayoutLayout.addView(subIB)
            horizontalLinearLayoutLayout.addView(addIB)
            horizontalLinearLayoutLayout.addView(nameTV)

            verticalLinearLayout.addView(horizontalLinearLayoutLayout)

            tr.addView(verticalLinearLayout)

            tebleKits.addView(tr)
            if(inStore == inSet){
                tr.setBackgroundColor(Color.parseColor("#32CD32"))
            }

            // linia separująca
            val trSep = TableRow(this)
            val trParamsSep = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT, 1.0F)
            trParamsSep.setMargins(leftRowMargin, topRowMargin, rightRowMargin, bottomRowMargin)

            trSep.layoutParams = trParamsSep
            val tvSep = TextView(this)
            val tvSepLay = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)
            tvSepLay.span = 1
            tvSep.layoutParams = tvSepLay
            tvSep.setBackgroundColor(Color.parseColor("#000000"))
            tvSep.height = 10

            trSep.addView(tvSep)
            tebleKits.addView(trSep, trParamsSep)
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
