package jp.ac.jec.cm0119.loginchatapp.ui

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.loginchatapp.databinding.ActivitySetupProfileBinding
import jp.ac.jec.cm0119.loginchatapp.model.User
import jp.ac.jec.cm0119.loginchatapp.ui.MainActivity
import java.util.*
import kotlin.collections.HashMap

class SetupProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupProfileBinding
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    var selectedImage: Uri? = null
    var dialog: ProgressDialog? = null

//    private val galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            .
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)

        dialog = ProgressDialog(this@SetupProfileActivity)
        dialog!!.setMessage("Updating Profile...")
        dialog!!.setCancelable(false)

        supportActionBar?.hide()

        binding.imageView.setOnClickListener {
            //ギャラリーへのインテント
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            // TODO: 要改善
            startActivityForResult(intent, 45)  //ギャラリーを開く
        }

        binding.continueBtn02.setOnClickListener {
            val name = binding.nameBox.text.toString()
            if (name.isEmpty()) {
                binding.nameBox.error = "Please type a name"
            }
            dialog!!.show()
            if (selectedImage != null) {
                val reference = storage.reference.child("Profile")
                    .child(auth.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener{ task ->   //uriのアップロード task(成功　or 失敗)
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener { uri ->
//                            val imageUrl = uri.toString()
                            /**以下のコードに変更**/
                            val imageUrl = uri.result.toString()
                            val uid = auth.uid
                            val phone = auth.currentUser!!.phoneNumber
                            val name = binding.nameBox.text.toString()
                            val user = User(uid, name, phone, imageUrl)
                                //usersに登録
                            database.reference
                                .child("users")
                                .child(uid!!)
                                .setValue(user) //firebaseに登録
                                .addOnCompleteListener {
                                    dialog!!.dismiss()
                                    val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                        }
                    }
                }
            } else {
                val uid = auth.uid
                val phone = auth.currentUser!!.phoneNumber
                val name = binding.nameBox.text.toString()
                val user = User(uid, name, phone, "No Image")   //No Image用のuri準備
                database.reference
                    .child("users")
                    .child(uid!!)
                    .setValue(user)
                    .addOnCompleteListener {
                        dialog!!.dismiss()
                        val intent = Intent(this@SetupProfileActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
            }
        }
        setContentView(binding.root)
    }

    // TODO: StorageExceptionが発生している、要確認 →　storageのrule設定に問題があった
    //ギャラリーからの戻り処理
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data != null) {
            Log.d("LoginChatApp", "LoginChatApp")
            if (data.data != null) {    //uri
                val uri = data.data
                Log.d("Image", "data.data/${data.data}")
                val storage = FirebaseStorage.getInstance()
                val time = Date().time
                val reference = storage.reference
                    .child("Profile")
                    .child(time.toString())
                reference.putFile(uri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reference.downloadUrl.addOnCompleteListener {
                            val filePath = uri.toString()
                            val obj = HashMap<String, Any>()
                            obj["image"] = filePath
                            database.reference
                                .child("users")
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj).addOnCompleteListener {  }
                        }
                    }
                }
                Log.d("LoginChatApp", uri.toString())
                binding.imageView.setImageURI(data.data)
                selectedImage = data.data
            }
        }
    }
}