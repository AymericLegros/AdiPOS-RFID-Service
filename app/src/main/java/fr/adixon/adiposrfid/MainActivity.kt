package fr.adixon.adiposrfid

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity

class MainActivity : Activity() {
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        Toast.makeText(applicationContext, "Hello........", Toast.LENGTH_LONG).show();
    }
}