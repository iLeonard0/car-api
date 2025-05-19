package com.example.carapi


import android.app.Application
import com.example.carapi.database.DatabaseBuilder

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    /**
     * fun para inicializar nossas depêndencias através do nosso context
     */
    private fun init() {
        DatabaseBuilder.getInstance(this)
    }
}