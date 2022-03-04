package fr.adixon.adiposrfid

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import java.util.*

class MyService : Service() {
    var randomNumber = 0
    private var mIsRandomGeneratorOn = false

    private lateinit var mMessenger: Messenger
    private var bIntent: Intent = Intent("fr.adixon.adiposrfid.MyService.BROADCAST_ACTION");

    private inner class IncomingHandler : Handler() {
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

    override fun onCreate() {
        super.onCreate()
        // The service is being created
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // The service is starting, due to a call to startService()
        mIsRandomGeneratorOn = true
        Thread { startRandomNumberGenerator() }.start()
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
        super.onDestroy()
        println("--------- onDestroy ---------")
        mIsRandomGeneratorOn = false
        // The service is no longer used and is being destroyed
    }


    private fun startRandomNumberGenerator() {
        while (mIsRandomGeneratorOn) {
            try {
                Thread.sleep(1000)
                if (mIsRandomGeneratorOn) {
                    val randomNumber: Int = Random().nextInt(100)
                    Log.i(TAG, "Random Number: $randomNumber")

                    bIntent.putExtra("data", "Random Number: $randomNumber")
                    sendBroadcast(bIntent);
                }
            } catch (e: InterruptedException) {
                Log.i(TAG, "Thread Interrupted")
            }
        }
    }


    companion object {
        private val TAG = MyService::class.java.simpleName
        const val GET_RANDOM_NUMBER_FLAG = 0
    }
}