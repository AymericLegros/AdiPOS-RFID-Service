package fr.adixon.adiposrfid

import com.module.interaction.ModuleConnector
import com.nativec.tools.SerialPort
import com.rfid.RFIDReaderHelper
import com.rfid.ReaderDataPackageParser
import com.rfid.ReaderDataPackageProcess
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

import java.util.regex.Pattern

class Connector : ModuleConnector {
    private val HOSTNAME_REGEXP =
        "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$"
    private var mRFIDReaderHelper: RFIDReaderHelper? = null
    private var mSocket: Socket? = null
    private var mRemoteAddr: InetSocketAddress? = null
    private var mSerialPort: SerialPort? = null
    override fun connectCom(port: String, baud: Int): Boolean {
        disConnect()
        try {
            mRFIDReaderHelper = RFIDReaderHelper.getDefaultHelper()
            mSerialPort = SerialPort(File(port), baud, 0)
            if (mSerialPort == null) {
                return false
            }
        } catch (var5: Exception) {
            var5.printStackTrace()
            return false
        }
        return try {
            mRFIDReaderHelper!!.setReader(mSerialPort!!.inputStream, mSerialPort!!.outputStream, ReaderDataPackageParser(), ReaderDataPackageProcess())
            true
        } catch (var4: Exception) {
            var4.printStackTrace()
            false
        }
    }

    override fun connect(host: String, port: Int): Boolean {
        return if (IP_ADDRESS.matcher(host).matches()) {
            disConnect()
            try {
                mRFIDReaderHelper = RFIDReaderHelper.getDefaultHelper()
                mRemoteAddr = InetSocketAddress(host, port)
                mSocket = Socket()
            } catch (var6: Exception) {
                return false
            }
            try {
                mSocket!!.connect(mRemoteAddr, 4000)
            } catch (var5: Exception) {
                var5.printStackTrace()
                return false
            }
            try {
                mRFIDReaderHelper!!.setReader(
                    mSocket!!.inputStream,
                    mSocket!!.outputStream,
                    ReaderDataPackageParser(),
                    ReaderDataPackageProcess()
                )
                true
            } catch (var4: Exception) {
                var4.printStackTrace()
                false
            }
        } else {
            false
        }
    }

    override fun isConnected(): Boolean {
        return if (mRFIDReaderHelper != null) mRFIDReaderHelper!!.isAlive else false
    }

    private val IP_ADDRESS = Pattern.compile(
        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                + "|[1-9][0-9]|[0-9]))"
    )

    override fun disConnect() {
        println("Connector: disconnect")
        try {
            if (mRFIDReaderHelper != null) {
                mRFIDReaderHelper!!.signOut()
            }
            if (mSocket != null) {
                mSocket!!.close()
            }
            if (mSerialPort != null) {
                mSerialPort!!.close()
            }
        } catch (var2: Exception) {
        }
        mSocket = null
        mSerialPort = null
        mRFIDReaderHelper = null
    }
}