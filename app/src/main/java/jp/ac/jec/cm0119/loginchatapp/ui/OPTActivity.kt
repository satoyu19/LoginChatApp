package jp.ac.jec.cm0119.loginchatapp.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import jp.ac.jec.cm0119.loginchatapp.ui.SetupProfileActivity
import jp.ac.jec.cm0119.loginchatapp.databinding.ActivityOptactivityBinding
import java.util.concurrent.TimeUnit

class OPTActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOptactivityBinding
    private var verificationId: String? = null
    private var auth: FirebaseAuth? = null
    //todo: 非推奨なため、本番ではProgressBar等で代用する
    private var dialog: ProgressDialog? = null
    private lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOptactivityBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)

        //進捗率ダイアログバー
        dialog = ProgressDialog(this@OPTActivity)
        dialog!!.setMessage("認証番号　送信中...")
        dialog!!.setCancelable(false)
        dialog!!.show()

        auth = FirebaseAuth.getInstance()
        supportActionBar?.hide()    //アクションバー非表示

        val phoneNumber = intent.getStringExtra("phoneNumber")
        binding!!.phoneLabel.text = "Verify $phoneNumber"

//        todo: 電話認証(https://firebase.google.com/docs/auth/android/phone-auth)プロジェクトの設定
        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber(phoneNumber!!)
            .setTimeout(60L, TimeUnit.SECONDS)  //SMS 自動取得が完了するまで待機できる最大時間
            .setActivity(this@OPTActivity)
            .setCallbacks(object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){  //さまざまな電話認証イベントの登録済みコールバック
                //SMS が自動取得されるか、電話番号が即座に確認されたときにトリガーされます(上記URLに詳細あり)
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {}

                //電話番号の確認中にエラーが発生したときにトリガーされます
                // TODO: 電話番号認証中にエラーなら電話番号の再入力を求める 
                override fun onVerificationFailed(p0: FirebaseException) {
                    Toast.makeText(this@OPTActivity, "電話番号の確認中にエラーが発生", Toast.LENGTH_SHORT).show()
                    Log.d("error", p0.toString())
                }

                //SMS がユーザーの電話に送信されたときにトリガーされ、verificationIdとPhoneAuthProvider.ForceResendingTokenを含むことになります。
                override fun onCodeSent(verifyId: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    dialog!!.dismiss()
                    verificationId = verifyId
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager  //入力関係の制御を行う
                    //非推奨、showSoftInput() or hideSoftInputFromWindow()が代用
//                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
                    binding!!.otpView.requestFocus()    //フォーカスを当てる？

                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        binding!!.otpView.setOtpCompletionListener{ otp ->  //入力完了後？otp = 入力値
            val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
            auth!!.signInWithCredential(credential) //サインインを試みる
                .addOnCompleteListener { task ->    //結果の処理
                    if (task.isSuccessful) {
                        val intent = Intent(this@OPTActivity, SetupProfileActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }else {
                        Toast.makeText(this@OPTActivity, "失敗", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val inputMethodManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // エルビス演算子でViewを取得できなければ return false
        // focusViewには入力しようとしているのEditTextが取得されるはず
        val focusView = currentFocus ?: return false

        // このメソッドでキーボードを閉じる
        inputMethodManager.hideSoftInputFromWindow(
            focusView.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )

        return false
    }
}