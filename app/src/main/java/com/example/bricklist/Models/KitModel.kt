package com.example.bricklist.Models

class KitModel(id: Int, type: Int, qty: Int, colorId: Int, extra: String, alternate: String) {
    private val Id: Int? = id
    private val Type: Int? = type
    private val Qty: Int? = qty
    private val ColorId: Int? = colorId
    private val Extra: String? = extra
    private val Alternat: String? = alternate

    fun getId():Int?{
        return Id;
    }

    fun getType():Int?{
        return Type
    }

    fun getQty():Int?{
        return Qty
    }

    fun getColorId():Int?{
        return ColorId
    }

    fun getExtra():String?{
        return Extra
    }
    fun getAlternat():String?{
        return Alternat
    }

}