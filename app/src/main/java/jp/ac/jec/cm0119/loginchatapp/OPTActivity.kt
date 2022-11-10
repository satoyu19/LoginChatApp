package jp.ac.jec.cm0119.loginchatapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jp.ac.jec.cm0119.loginchatapp.databinding.ActivityOptactivityBinding

class OPTActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOptactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = binding.root

        setContentView(view)
    }
}