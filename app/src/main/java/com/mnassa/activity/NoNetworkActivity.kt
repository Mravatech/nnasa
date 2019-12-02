package com.mnassa.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mnassa.App
import com.mnassa.R
import kotlinx.android.synthetic.main.activity_no_network.*


class NoNetworkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_network)

        val connectivityManager =
                App.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo

        val isInternetConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected

        if (!isInternetConnected){

            Log.e("Internet Connection", "There is no internet connection(Activity)")

        }else{
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnExit.setOnClickListener {
            finish()
        }
//        btnSwitchOnConnection.setOnClickListener {
//            val intent = Intent()
//            intent.component = ComponentName("com.android.settings", "com.android.settings.Settings\$DataUsageSummaryActivity")
//            startActivity(intent)
//        }
    }


}
