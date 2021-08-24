package com.example.generalfelicasimulator

import android.app.AlertDialog
import android.content.ComponentName
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.cardemulation.NfcFCardEmulation
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private var nfcFCardEmulation: NfcFCardEmulation? = null
    private var myComponentName: ComponentName? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION_NFCF)) {
            Log.e("GeneralFelicaSimulator", "HCE-F is not supported")
            AlertDialog.Builder(this).setTitle("Error").setMessage("HCE-F is not supported")
                .setOnDismissListener { exitProcess(-1) }.show()
            return
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcFCardEmulation = NfcFCardEmulation.getInstance(nfcAdapter)
        myComponentName = ComponentName(
            "com.example.generalfelicasimulator",
            "com.example.generalfelicasimulator.HCEFService"
        )

        val btn_update = findViewById<Button>(R.id.button_update)
        btn_update.setOnClickListener {
            val idm = findViewById<EditText>(R.id.editTextIDm).text.toString()
            val sys = findViewById<EditText>(R.id.editTextSys).text.toString()

            val result_idm = setIDm(idm)
            val result_sys = setSys(sys)

            if (result_idm && result_sys) {
                Toast.makeText(applicationContext, "Updated: $idm $sys", Toast.LENGTH_LONG).show()
            } else {
                if (!result_idm) {
                    Toast.makeText(applicationContext, "Error. Invalid IDm", Toast.LENGTH_LONG)
                        .show()
                }
                if (!result_sys) {
                    Toast.makeText(applicationContext, "Error. Invalid Sys", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    private fun setIDm(idm: String): Boolean {
        nfcFCardEmulation?.disableService(this)
        val result_idm = nfcFCardEmulation?.setNfcid2ForService(myComponentName, idm)
        nfcFCardEmulation?.enableService(this, myComponentName)
        return result_idm == true
    }

    private fun setSys(sys: String): Boolean {
        nfcFCardEmulation?.disableService(this)
        val result_sys = nfcFCardEmulation?.registerSystemCodeForService(myComponentName, sys)
        nfcFCardEmulation?.enableService(this, myComponentName)
        return result_sys == true
    }

    override fun onResume() {
        super.onResume()

        val org_sys = nfcFCardEmulation?.getSystemCodeForService(myComponentName)

        if (setSys("1234")) {
            findViewById<TextView>(R.id.textViewNoticeUnUnlocked).visibility = View.INVISIBLE
        }

        if (org_sys != null) {
            setSys(org_sys)
        }

        nfcFCardEmulation?.enableService(this, myComponentName)
    }

    override fun onPause() {
        super.onPause()
        nfcFCardEmulation?.disableService(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val idm = findViewById<EditText>(R.id.editTextIDm).text.toString()
        val sys = findViewById<EditText>(R.id.editTextSys).text.toString()

        outState.putString("IDm", idm)
        outState.putString("Sys", sys)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val idm = savedInstanceState.getString("IDm")
        val sys = savedInstanceState.getString("Sys")

        findViewById<EditText>(R.id.editTextIDm).setText(idm)
        findViewById<EditText>(R.id.editTextSys).setText(sys)
    }
}
