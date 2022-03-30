package fr.adixon.adiposrfid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.module.interaction.ModuleConnector
import com.module.interaction.RXTXListener
import com.nativec.tools.ModuleManager
import com.rfid.RFIDReaderHelper
import com.rfid.rxobserver.RXObserver
import com.rfid.rxobserver.bean.RXInventoryTag
import com.rfid.rxobserver.bean.RXInventoryTag.RXInventoryTagEnd

private const val RFID_INIT = 1
private const val RFID_TERMINATE = 2
private const val RFID_CONNECTOR_STATUS = 3
private const val RFID_SCAN_START = 4
private const val RFID_SCAN_STOP = 5

class RFIDService : Service() {
    private lateinit var mMessenger: Messenger
    private var bIntent: Intent = Intent("fr.adixon.adiposrfid.RFIDService.BROADCAST_ACTION");

    private var mConnector: ModuleConnector = Connector()
    private var mReaderHelper: RFIDReaderHelper = RFIDReaderHelper.getDefaultHelper()

    private lateinit var timerHandler: Handler
    private var timerDelay: Long = 1000
    private var loopScan: Boolean = false;
    private var tags: ArrayList<RFIDTag> = arrayListOf();
    private var process: Boolean = false;

    private var mListener: RXTXListener = object : RXTXListener {
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
            println("--- ERROR: Connection lost! \uD83D\uDE25 ---")
        }
    }

    private var rxObserver: RXObserver = object : RXObserver() {
        override fun onInventoryTag(tag: RXInventoryTag) {
            val tagFound = tags.find { it.code == tag.strEPC }

            if (tagFound != null) {
                tagFound.increment()
            } else {
                val newTag = RFIDTag(tag.strEPC);
                tags.add(newTag)
            }

        }

        override fun onInventoryTagEnd(tagEnd: RXInventoryTagEnd?) {
            if (loopScan) {
                Thread { RFIDScan() }.start()
            }
        }
    }

    private inner class IncomingHandler() : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                RFID_INIT -> Thread { RFIDInit() }.start()
                RFID_SCAN_START -> RFIDScanStart()
                RFID_SCAN_STOP -> RFIDScanStop()
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun RFIDInit() {
        if (mConnector.connect("192.168.1.178", 4001)) {
            println("--- CONNECTED \uD83D\uDFE2 ---")
            ModuleManager.newInstance().uhfStatus = true

            // mReaderHelper = RFIDReaderHelper.getDefaultHelper()
            mReaderHelper.setRXTXListener(mListener);
            mReaderHelper.registerObserver(rxObserver)
        } else {
            throw Exception("--- ERROR: Connection impossible! \uD83D\uDE25 ---")
        }
    }

    private fun RFIDScanStart() {
        println("----- RFIDScanStart -----")
        if (process) return
        process = true

        tags.clear()

        timerHandler = Handler(Looper.getMainLooper())
        timerHandler.postDelayed(sendTags, timerDelay)

        Thread { RFIDScan() }.start()

        loopScan = true
    }

    private fun RFIDScan() {
        mReaderHelper.realTimeInventory(0xFF.toByte(), 0x01.toByte())
    }

    private fun RFIDScanStop() {
        if (!process) return
        println("------ RFIDScanStop ------");
        process = false

        timerHandler.removeCallbacks(sendTags)

        loopScan = false

        tags.clear()
    }

    private fun RFIDTerminate() {
        try {
            if (process) RFIDScanStop()
            println("------ RFIDTerminate ------");
            mReaderHelper.unRegisterObserver(rxObserver)
            mConnector.disConnect()

            ModuleManager.newInstance().uhfStatus = false
            ModuleManager.newInstance().release()
            println("--- DISCONNECTED \uD83D\uDD34 ---")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val sendTags = object : Runnable {
        override fun run() {
            val currTimestamp: Long = System.currentTimeMillis()
            val validTags = tags.filter { currTimestamp - it.timestamp < 5000 }
            val formattedTag = validTags.map { it.code } as ArrayList<String>;
            bIntent.putStringArrayListExtra("fr.adixon.adiposrfid.AllTags", formattedTag)
            sendBroadcast(bIntent);

            timerHandler.postDelayed(this, timerDelay)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // A client is binding to the service with bindService()
        println("--------- onBind ---------")
        return try {
            mMessenger = Messenger(IncomingHandler())
            return mMessenger.binder
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onUnbind(intent: Intent): Boolean {
        // All clients have unbound with unbindService()
        println("--------- onUnbind ---------")
        Thread { RFIDTerminate() }.start()
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        super.onRebind(intent)
    }
}