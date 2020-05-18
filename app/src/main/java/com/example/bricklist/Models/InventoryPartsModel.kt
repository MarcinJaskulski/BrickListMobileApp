package com.example.bricklist.Models

import android.app.Activity

class InventoryPartsModel(id: Int, inventoryID: Int, typeID: Int, itemID: Int,
                          quantityInSet: Int, quantityInStore: Int, colorID: Int, extra: Int, name: String) {
    private val Id: Int = id
    private val InventoryID: Int = inventoryID
    private val TypeID: Int = typeID
    private val ItemID: Int = itemID
    private val QuantityInSet: Int = quantityInSet // ile w zestawie
    private val QuantityInStore: Int = quantityInStore // ile się udało znaleźć
    private val ColorID: Int = colorID
    private val Extra: Int = extra
    private val Name: String = name

    private var ColorName: String? = null

    private var photo: ByteArray? = null

    fun setPhoto(value: ByteArray?){
        photo = value
    }
    fun getPhoto(): ByteArray?{
        return photo
    }

    fun setColorName(value: String?){
        ColorName = value
    }

    fun getColorName(): String?{
        return ColorName
    }


    fun getId():Int{
        return Id
    }

    fun getInventoryID(): Int{
        return InventoryID
    }

    fun getTypeID(): Int{
        return TypeID
    }

    fun getItemID(): Int{
        return  ItemID
    }

    fun getQuantityInSet(): Int{
        return QuantityInSet
    }

    fun getQuantityInStore(): Int{
        return QuantityInStore
    }

    fun getColorID(): Int{
        return ColorID
    }

    fun getExtra(): Int{
        return Extra
    }

    fun getName():String{
        return  Name
    }
}