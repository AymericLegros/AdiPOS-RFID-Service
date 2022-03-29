package fr.adixon.adiposrfid

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.module.interaction.ModuleConnector
import com.module.interaction.RXTXListener
import com.nativec.tools.ModuleManager
import com.rfid.RFIDReaderHelper
import com.rfid.rxobserver.RXObserver
import com.rfid.rxobserver.bean.RXInventoryTag
import com.rfid.rxobserver.bean.RXInventoryTag.RXInventoryTagEnd

private const val MSG_SAY_HELLO = 0
private const val RFID_INIT = 1
private const val RFID_TERMINATE = 2
private const val RFID_CONNECTOR_STATUS = 3
private const val RFID_SCAN_START = 4
private const val RFID_SCAN_STOP = 5

class MyService : Service() {
    private lateinit var mMessenger: Messenger
    private var bIntent: Intent = Intent("fr.adixon.adiposrfid.MyService.BROADCAST_ACTION");

    private var mConnector: ModuleConnector = Connector()
    private var mReaderHelper: RFIDReaderHelper = RFIDReaderHelper.getDefaultHelper()

    private lateinit var timerHandler: Handler
    private var timerDelay: Long = 1000
    private var loopScan: Boolean = false;
    private var tags: ArrayList<RFIDTag> = arrayListOf();

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
                MSG_SAY_HELLO -> {
                    Toast.makeText(applicationContext, "Hello World!", Toast.LENGTH_SHORT).show()
                }
                RFID_INIT -> Thread { RFIDInit() }.start()
                RFID_SCAN_START -> RFIDScanStart()
                RFID_SCAN_STOP -> RFIDScanStop()
                RFID_TERMINATE -> Thread { RFIDTerminate() }.start()
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun RFIDInit() {
        try {
            if (mConnector.connect("192.168.1.178", 4001)) {
                ModuleManager.newInstance().uhfStatus = true
                println("--- CONNECTED \uD83D\uDFE2 ---")

                // mReaderHelper = RFIDReaderHelper.getDefaultHelper()
                mReaderHelper.setRXTXListener(mListener);
                mReaderHelper.registerObserver(rxObserver)
            } else {
                println("--- ERROR: Connection impossible! \uD83D\uDE25 ---")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun RFIDScanStart() {
        timerHandler = Handler(Looper.getMainLooper())
        timerHandler.postDelayed(sendTags, timerDelay)

        tags.clear()

        Thread{ RFIDScan() }.start()

        loopScan = true
    }

    private fun RFIDScan() {
        mReaderHelper.realTimeInventory(0xFF.toByte(), 0x01.toByte())
    }

    private fun RFIDScanStop() {
        timerHandler.removeCallbacks(sendTags)

        loopScan = false

        tags.clear()
    }

    private fun RFIDTerminate() {
        try {
            println("RFIDTerminate");
            println("mReaderHelper");
            mReaderHelper.unRegisterObserver(rxObserver)
            println("disConnect");
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
            val updatedTags = tags.filter { currTimestamp - it.timestamp < 5000 }
            tags = updatedTags as ArrayList<RFIDTag>


            bIntent.putParcelableArrayListExtra("fr.adixon.adiposrfid.AllTags", tags)
            sendBroadcast(bIntent);

            timerHandler.postDelayed(this, timerDelay)
        }
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        createNotification()
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

//    override fun onDestroy() {
//        // The service is no longer used and is being destroyed
//        super.onDestroy()
//    }
}