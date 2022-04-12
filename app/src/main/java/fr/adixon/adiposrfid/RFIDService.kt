package fr.adixon.adiposrfid

import android.app.Service
import android.content.Intent
import android.os.*
import com.module.interaction.ModuleConnector
import com.module.interaction.ReaderHelper
import com.nativec.tools.ModuleManager
import com.rfid.RFIDReaderHelper
import com.rfid.bean.MessageTran
import com.rfid.rxobserver.RXObserver
import com.rfid.rxobserver.bean.RXInventoryTag

// import com.rfid.RFIDReaderHelper;


private const val RFID_HELLO = 0
private const val RFID_INIT = 1
private const val RFID_TERMINATE = 2
private const val RFID_CONNECTOR_STATUS = 3
private const val RFID_SCAN_START = 4
private const val RFID_SCAN_STOP = 5


private const val TEST_SUCCESS = 100
private const val NETWORK_UNAVAILABLE = 101
private const val SERVICE_UNAVAILABLE = 102
private const val CONNECTION_READER_UNAVAILABLE = 103
private const val SCAN_UNAVAILABLE = 104

class RFIDService : Service() {
    private var bIntent: Intent = Intent("fr.adixon.adiposrfid.RFIDService.BROADCAST_ACTION")

    private var mConnector: ModuleConnector = Connector()
    private lateinit var mReaderHelper: RFIDReaderHelper;

    private lateinit var timerHandler: Handler
    private var timerDelay: Long = 1000
    private var tags: ArrayList<RFIDTag> = arrayListOf()
    private var loopScan: Boolean = false
    private lateinit var mData: Bundle
    private var testMode: Boolean = false
    private var mTimestamp: Long = 0

    private lateinit var mMessenger: Messenger
    private lateinit var rMessenger: Messenger

    private var rxObserver: RXObserver = object : RXObserver() {
        override fun onInventoryTag(tag: RXInventoryTag) {
//            println("------ tag.btAntId -------")
//            println(tag.btAntId)
            val tagFound = tags.find { it.code == tag.strEPC }

            if (tagFound != null) {
                tagFound.increment()
            } else {
                val newTag = RFIDTag(tag.strEPC)
                tags.add(newTag)
            }

        }

//        override fun onInventoryTagEnd(tagEnd: RXInventoryTagEnd?) {
//            if (loopScan) {
//                Thread { RFIDScan() }.start()
//            }
//        }

        override fun onFastSwitchAntInventoryTagEnd(tagEnd: RXInventoryTag.RXFastSwitchAntInventoryTagEnd) {
            if (loopScan) {
                Thread { RFIDScan() }.start()
            }
        }
    }

    private inner class IncomingHandler() : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            rMessenger = msg.replyTo
            mData = msg.data
            testMode = mData.getBoolean("test")
            mTimestamp = System.currentTimeMillis()
            when (msg.what) {
                RFID_SCAN_START -> RFIDInit()
                RFID_SCAN_STOP -> RFIDTerminate()
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun RFIDInit() {
        Thread {
            try {
                val ip: String? = mData.getString("ip")
                val port: Int = mData.getInt("port")
                if (mConnector.connect(ip, port)) {
                    println("--- CONNECTED \uD83D\uDFE2 ---")
                    ModuleManager.newInstance().uhfStatus = true


                    mReaderHelper = RFIDReaderHelper.getDefaultHelper()
                    println(mReaderHelper);
                    // mReaderHelper.setRXTXListener(mListener);
                    mReaderHelper.registerObserver(rxObserver)

//                 mReaderHelper.setWorkAntenna(0xFF.toByte(), 0x04.toByte()) // 1
//                mReaderHelper.setWorkAntenna(0xFF.toByte(), 0x01.toByte()) // 2
//                mReaderHelper.setWorkAntenna(0xFF.toByte(), 0x02.toByte()) // 3
//                mReaderHelper.setWorkAntenna(0xFF.toByte(), 0x03.toByte()) // 4
//                mReaderHelper.setWorkAntenna(0xFF.toByte(), 0x04.toByte())
//                mReaderHelper.setWorkAntenna(0xFF.toByte(), 0x05.toByte())
//                mReaderHelper.setWorkAntenna(0xFF.toByte(), 0x06.toByte())
//                mReaderHelper.setWorkAntenna(0xFF.toByte(), 0x07.toByte())
                    // println(mReaderHelper.getWorkAntenna(0xFF.toByte()))

                    RFIDScanStart()
                } else {
                    val newMsg = Message.obtain(null, CONNECTION_READER_UNAVAILABLE, 0, 0)
                    rMessenger.send(newMsg)
                }
            } catch(e: Exception) {
                println("---------- INIT -----------")
                e.printStackTrace()
            }
        }.start()
    }

    private fun RFIDScanStart() {
        println("----- RFIDScanStart -----")
        if (loopScan) return

        tags.clear()

        timerHandler = Handler(Looper.getMainLooper())
        timerHandler.postDelayed(sendTags, timerDelay)

        Thread {
            RFIDScan()
        }.start()

        loopScan = true
    }

    private fun RFIDScan() {
        fastSwitchAntInventory(0xFF.toByte(), 0x00.toByte(), 0x01.toByte(), 0x01.toByte(), 0x01.toByte(), 0x02.toByte(), 0x01.toByte(), 0x03.toByte(), 0x01.toByte(), 0x04.toByte(), 0x01.toByte(), 0x05.toByte(), 0x01.toByte(), 0x06.toByte(), 0x01.toByte(), 0x07.toByte(), 0x01.toByte(), 0x00.toByte(), 0x08.toByte());
        // mReaderHelper.fastSwitchAntInventory(0xFF.toByte(), 0x00.toByte(), 0x01.toByte(), 0x01.toByte(), 0x01.toByte(), 0x02.toByte(), 0x01.toByte(), 0x03.toByte(), 0x01.toByte(), 0x00.toByte(), 0x08.toByte());
        // mReaderHelper.realTimeInventory(0xFF.toByte(), 0x01.toByte())
    }

    private fun RFIDScanStop() {
        if (!loopScan) return
        println("------ RFIDScanStop ------")

        timerHandler.removeCallbacks(sendTags)

        loopScan = false

        tags.clear()
    }

    private fun RFIDTerminate() {
        Thread {
            try {
                if (loopScan) {
                    RFIDScanStop()
                }
                println("------ RFIDTerminate ------")
                mReaderHelper.unRegisterObserver(rxObserver)
                mConnector.disConnect()

                ModuleManager.newInstance().uhfStatus = false
                ModuleManager.newInstance().release()
                println("--- DISCONNECTED \uD83D\uDD34 ---")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private val sendTags = object : Runnable {
        override fun run() {
            val currTimestamp: Long = System.currentTimeMillis()
            val validTags = tags.filter { currTimestamp - it.timestamp < 1000 }
            val formattedTags = validTags.map { it.code } as ArrayList<String>
            bIntent.putStringArrayListExtra("fr.adixon.adiposrfid.AllTags", formattedTags)
            sendBroadcast(bIntent)

            timerHandler.postDelayed(this, timerDelay)

            // INTERRUPT FOR TEST
            if (testMode && currTimestamp - mTimestamp > 3000) {
                if (loopScan && formattedTags.size <= 0) {
                    val newMsg = Message.obtain(null, SCAN_UNAVAILABLE, 0, 0)
                    rMessenger.send(newMsg)
                } else {
                    val newMsg = Message.obtain(null, TEST_SUCCESS, 0, 0)
                    rMessenger.send(newMsg)
                }

                RFIDScanStop()
            }
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
        RFIDTerminate()
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
        super.onRebind(intent)
    }


    private fun fastSwitchAntInventory(
        btReadId: Byte,
        btA: Byte,
        btStayA: Byte,
        btB: Byte,
        btStayB: Byte,
        btC: Byte,
        btStayC: Byte,
        btD: Byte,
        btStayD: Byte,
        btE: Byte,
        btStayE: Byte,
        btF: Byte,
        btStayF: Byte,
        btG: Byte,
        btStayG: Byte,
        btH: Byte,
        btStayH: Byte,
        btInterval: Byte,
        btRepeat: Byte
    ): Int {
        val btCmd: Byte = -118
        val btAryData = byteArrayOf(btA, btStayA, btB, btStayB, btC, btStayC, btD, btStayD, btE, btStayE, btF, btStayF, btG, btStayG, btH, btStayH, btInterval, btRepeat)
        return sendMessage(btReadId, btCmd, btAryData)
    }


    private fun sendMessage(btReadId: Byte, btCmd: Byte, btAryData: ByteArray): Int {
        val msgTran = MessageTran(btReadId, btCmd, btAryData)
        return mReaderHelper.sendCommand(msgTran.aryTranData)
    }
}