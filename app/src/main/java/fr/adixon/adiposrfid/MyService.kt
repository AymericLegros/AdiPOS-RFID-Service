package fr.adixon.adiposrfid

import android.app.Service
import android.util.Log
import android.content.Intent
import android.os.*
import java.lang.Thread
import java.lang.InterruptedException
import android.widget.Toast
import java.util.*

class MyService : Service() {
    var randomNumber = 0
        private set
    private var mIsRandomGeneratorOn = false
    private val MIN = 0
    private val MAX = 100

    private inner class RandomNumberRequestHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                GET_RANDOM_NUMBER_FLAG -> {
                    val messageSendRandomNumber = Message.obtain(null, GET_RANDOM_NUMBER_FLAG)
                    messageSendRandomNumber.arg1 = randomNumber
                    try {
                        msg.replyTo.send(messageSendRandomNumber)
                    } catch (e: RemoteException) {
                        Log.i(TAG, "" + e.message)
                    }
                }
            }
            super.handleMessage(msg)
        }
    }

    private val randomNumberMessenger = Messenger(RandomNumberRequestHandler())
    override fun onBind(intent: Intent): IBinder? {
        return randomNumberMessenger.binder
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
    }

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRandomNumberGenerator()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        mIsRandomGeneratorOn = true
        Thread { startRandomNumberGenerator() }.start()
        return START_STICKY
    }

    private fun startRandomNumberGenerator() {
        while (mIsRandomGeneratorOn) {
            try {
                Thread.sleep(1000)
                if (mIsRandomGeneratorOn) {
                    randomNumber = Random().nextInt(MAX) + MIN
                    Log.i(TAG, "Random Number: $randomNumber")
                }
            } catch (e: InterruptedException) {
                Log.i(TAG, "Thread Interrupted")
            }
        }
    }

    private fun stopRandomNumberGenerator() {
        mIsRandomGeneratorOn = false
        Toast.makeText(applicationContext, "Service Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    companion object {
        private val TAG = MyService::class.java.simpleName
        const val GET_RANDOM_NUMBER_FLAG = 0
    }
}