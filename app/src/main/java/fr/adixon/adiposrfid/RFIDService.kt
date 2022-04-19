package fr.adixon.adiposrfid

import android.app.Service
import android.content.Intent
import android.os.*
import com.module.interaction.ModuleConnector
import com.module.interaction.RXTXListener
import com.nativec.tools.ModuleManager
import com.rfid.RFIDReaderHelper
import com.rfid.bean.MessageTran
import com.rfid.rxobserver.bean.RXInventoryTag

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
private const val MISSING_ANTENNA = 105

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

    private var missingAnts: ArrayList<Int> = arrayListOf()

    private var rxObserver: MyRXObserver = object : MyRXObserver() {
        override fun onInventoryTag(tag: RXInventoryTag) {
            try {
                // if (tag.strRSSI.toInt() < 100) return
                val tagFound = tags.find { it.code == tag.strEPC }

                if (tagFound != null) {
                    if (tagFound.code == "E2 80 68 94 00 00 50 16 87 E5 1D 39") {
                        // println("--------- SPEED ---------");
                        println("SPEED:${tagFound.averageSpeed} DISTANCE:${tagFound.averageDistance}")
                        // println("SPEED:${tagFound.speed} COUNT:${tagFound.count} LAST:${tagFound.lastUpdate} DURATION:${tagFound.duration}");
                    }
                    tagFound.update(tag.strRSSI)
                } else {
                    val newTag = RFIDTag(tag.strEPC, tag.strRSSI)
                    tags.add(newTag)
                }
            } catch (e: Exception) {
                println("-------- onInventoryTag ---------")
                e.printStackTrace()
            }

        }

        override fun onFastSwitchAntInventoryTagEnd(tagEnd: RXInventoryTag.RXFastSwitchAntInventoryTagEnd) {
            if (loopScan) {
                Thread { RFIDScan() }.start()
            }
        }

        override fun onExeCMDStatus(cmd: Byte, status: Byte) {
            println("-------- onExeCMDStatus --------");

            println(cmd)
            // System.out.format("CDM:%s  Execute status:%S", String.format("%02X", cmd), String.format("%02x", status))
        }

        override fun onExeCMDStatus(cmd: Byte, btAryData: ByteArray) {
            if (btAryData[1] == 0x22.toByte()) {
                val ant = btAryData[0] + 1
                if (!missingAnts.contains(ant)) {
                    missingAnts.add(ant)
                }
            }
        }
    }

    private var mListener: RXTXListener = object : RXTXListener {
        override fun reciveData(p0: ByteArray) {
            // println("------------- reciveData -------------");
//            println(p0.toList())
//            p0.forEach { println(it); }
        }

        override fun sendData(p0: ByteArray?) {
            // TODO("Not yet implemented")
        }

        override fun onLostConnect() {
            // TODO("Not yet implemented")
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
                if (mConnector.connect(ip, 4001)) {
                    println("--- CONNECTED \uD83D\uDFE2 ---")
                    ModuleManager.newInstance().uhfStatus = true

                    mReaderHelper = RFIDReaderHelper.getDefaultHelper()
                    mReaderHelper.registerObserver(rxObserver)
                    // mReaderHelper.setRXTXListener(mListener)
                    mReaderHelper.getAntConnectionDetector(0xFF.toByte())

                    RFIDScanStart()
                } else {
                    val newMsg = Message.obtain(null, CONNECTION_READER_UNAVAILABLE, 0, 0)
                    rMessenger.send(newMsg)
                }
            } catch (e: Exception) {
                println("---------- ERROR: RFIDInit -----------")
                e.printStackTrace()
                RFIDTerminate()
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
        fastSwitchAntInventory(
            0xFF.toByte(),
            0x00.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x01.toByte(),
            0x02.toByte(),
            0x01.toByte(),
            0x03.toByte(),
            0x01.toByte(),
            0x04.toByte(),
            0x01.toByte(),
            0x05.toByte(),
            0x01.toByte(),
            0x06.toByte(),
            0x01.toByte(),
            0x07.toByte(),
            0x01.toByte(),
            0x00.toByte(),
            0x08.toByte()
        );
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
                if (loopScan) RFIDScanStop()
                println("------ RFIDTerminate ------")
                mReaderHelper.unRegisterObserver(rxObserver)
                mReaderHelper.setRXTXListener(null);
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
            tags.removeIf { currTimestamp - it.updatedAt > 1000 }
            val formattedTags = tags.map { it.code } as ArrayList<String>
            bIntent.putStringArrayListExtra("fr.adixon.adiposrfid.AllTags", formattedTags)
            sendBroadcast(bIntent)

            timerHandler.postDelayed(this, timerDelay)

            // INTERRUPT FOR TEST
            if (testMode && currTimestamp - mTimestamp > 3000) {
                println("---- SEND TEST RESULT ----")

                if (missingAnts.size > 0) {
                    val newMsg = Message.obtain(null, MISSING_ANTENNA, 0, 0)
                    val mData = Bundle()
                    mData.putIntegerArrayList("data", missingAnts)
                    newMsg.data = mData
                    rMessenger.send(newMsg)
                    return RFIDScanStop()
                }

                if (loopScan && formattedTags.size <= 0) {
                    val newMsg = Message.obtain(null, SCAN_UNAVAILABLE, 0, 0)
                    rMessenger.send(newMsg)
                    return RFIDScanStop()
                }

                val newMsg = Message.obtain(null, TEST_SUCCESS, 0, 0)
                rMessenger.send(newMsg)
                return RFIDScanStop()

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
        val btAryData = byteArrayOf(
            btA,
            btStayA,
            btB,
            btStayB,
            btC,
            btStayC,
            btD,
            btStayD,
            btE,
            btStayE,
            btF,
            btStayF,
            btG,
            btStayG,
            btH,
            btStayH,
            btInterval,
            btRepeat
        )
        return sendMessage(btReadId, btCmd, btAryData)
    }

    private fun sendMessage(btReadId: Byte, btCmd: Byte, btAryData: ByteArray): Int {
        val msgTran = MessageTran(btReadId, btCmd, btAryData)
        return mReaderHelper.sendCommand(msgTran.aryTranData)
    }
}