package sh.sit.bonfire.networking

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking

class NetworkTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.network_test_activity)

        val text = findViewById<TextView>(R.id.logs)
        Thread {
            runBlocking {
                MobileProxyFactory.logs.collect { logs ->
                    Handler(Looper.getMainLooper()).post {
                        text.text = logs
                    }
                }
            }
        }.start()
    }
}
