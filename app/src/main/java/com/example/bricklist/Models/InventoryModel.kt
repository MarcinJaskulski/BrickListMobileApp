package com.example.bricklist.Models

import android.app.Activity

class InventoryModel(id: Int, name: String, active: Boolean, lastAccessed: String) {
    private val Id: Int = id;
    private val Name: String = name
    private val Active: Boolean = active
    private val LastAccessed: String = lastAccessed

    fun getId():Int{
        return Id
    }

    fun getName():String{
        return Name
    }

    fun getActive():Boolean{
        return Active
    }

    fun getLastAccessed():String{
        return LastAccessed
    }
}