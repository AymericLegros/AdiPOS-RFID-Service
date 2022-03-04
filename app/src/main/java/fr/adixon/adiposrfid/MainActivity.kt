package fr.adixon.adiposrfid

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var buttonStartService: Button? = null
    private var buttonStopService: Button? = null
    private var mContext: Context? = null
    private var serviceIntent: Intent? = null
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mContext = applicationContext
        buttonStartService = findViewById<Button>(R.id.buttonStartService)
        buttonStopService = findViewById<Button>(R.id.buttonStopService)
        buttonStartService?.setOnClickListener(this)
        buttonStopService?.setOnClickListener(this)
        serviceIntent = Intent(applicationContext, MyService::class.java)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonStartService -> {
                startService(serviceIntent)
                Toast.makeText(mContext, "Service Started", Toast.LENGTH_SHORT).show()
            }
            R.id.buttonStopService -> stopService(serviceIntent)
            else -> {}
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}