package jp.ac.jec.cm0119.loginchatapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import jp.ac.jec.cm0119.loginchatapp.databinding.ActivityVerificationBinding

/**
 * ログイン、サインイン
 * * **/
class VerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerificationBinding

    //firebase
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth!!.currentUser != null) {
            val intent = Intent(this@VerificationActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        supportActionBar?.hide()
        binding.editNumber.requestFocus()   //カーソルを当てる
        // TODO: 空の場合にボタンを無効にする, 電話番号は国番号の必要がある
        binding.continueBtn.setOnClickListener {
            val intent = Intent(this@VerificationActivity, OPTActivity::class.java)
            intent.putExtra("phoneNumber", binding.editNumber.text.toString())
            startActivity(intent)
        }
    }
}
