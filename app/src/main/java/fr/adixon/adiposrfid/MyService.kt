package fr.adixon.adiposrfid

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import com.module.interaction.ModuleConnector
import com.module.interaction.RXTXListener
import com.nativec.tools.ModuleManager
import com.rfid.RFIDReaderHelper
import com.rfid.rxobserver.RXObserver
import com.rfid.rxobserver.bean.RXInventoryTag

private const val MSG_SAY_HELLO = 0
private const val RFID_START = 1
private const val RFID_TERMINATE = 2
private const val RFID_CONNECTOR_STATUS = 3
private const val RFID_SCAN = 4

class MyService : Service() {
    private lateinit var mMessenger: Messenger
    private var bIntent: Intent = Intent("fr.adixon.adiposrfid.MyService.BROADCAST_ACTION");

    private var mConnector: ModuleConnector = Connector()
    private lateinit var mReaderHelper: RFIDReaderHelper

    private var mListener : RXTXListener = object : RXTXListener {
        override fun reciveData(btAryReceiveData: ByteArray?) {
            // TODO Auto-generated method stub
            // Get data from RFID module
            // println("reciveData")
        }

        override fun sendData(btArySendData: ByteArray?) {
            // TODO Auto-generated method stub
            // Get data sending to RFID module
        }

        override fun onLostConnect() {
            // TODO Auto-generated method stub
            // This method will be called once lost connection.
            println("Connection lost! \uD83D\uDE25")
        }
    }
    private var rxObserver: RXObserver = object : RXObserver() {
        override fun onInventoryTag(tag: RXInventoryTag) {
            Log.d("TAG", tag.strEPC)
            bIntent.putExtra("data", "TAG: ${tag.strEPC}")
            sendBroadcast(bIntent);
        }
    }

    private inner class IncomingHandler() : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SAY_HELLO -> {
                    Toast.makeText(applicationContext, "Hello World!", Toast.LENGTH_SHORT).show()
                }
                RFID_SCAN -> Thread { RFIDScan() }.start()
                RFID_TERMINATE -> RFIDTerminate()
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun RFIDInit() {
        try {
            if (mConnector.connect("192.168.1.178", 4001)) {
                ModuleManager.newInstance().uhfStatus = true
                println("--- CONNECTÉ \uD83D\uDFE2 ---")

                mReaderHelper = RFIDReaderHelper.getDefaultHelper()
                mReaderHelper.setRXTXListener(mListener);
                mReaderHelper.registerObserver(rxObserver)
            } else {
                println("--- ERROR: Connection impossible! \uD83D\uDE25 ---")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun RFIDScan() {
        println("RFIDScan")
        mReaderHelper.realTimeInventory(0xFF.toByte(), 0x01.toByte())
    }

    private fun RFIDTerminate() {
        try {
            println("RFIDTerminate");
            if (mReaderHelper != null) {
                println("mReaderHelper");
                mReaderHelper.unRegisterObserver(rxObserver)
            }
            if (mConnector != null) {
                println("disConnect");
                mConnector.disConnect()
            }

            ModuleManager.newInstance().uhfStatus = false
            ModuleManager.newInstance().release()
            println("--- DÉCONNECTÉ \uD83D\uDD34 ---")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate() {
        super.onCreate()
        // The service is being created
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // The service is starting, due to a call to startService()
        Thread { RFIDInit() }.start()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // A client is binding to the service with bindService()
        mMessenger = Messenger(IncomingHandler())
        return mMessenger.binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // All clients have unbound with unbindService()
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        super.onRebind(intent)
    }

    override fun onDestroy() {
        // The service is no longer used and is being destroyed
        super.onDestroy()
        Thread { RFIDTerminate() }.start()
    }
}