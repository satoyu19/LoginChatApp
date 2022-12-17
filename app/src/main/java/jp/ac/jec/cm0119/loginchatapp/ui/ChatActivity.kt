package jp.ac.jec.cm0119.loginchatapp.ui

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.loginchatapp.R
import jp.ac.jec.cm0119.loginchatapp.adapter.MessagesAdapter
import jp.ac.jec.cm0119.loginchatapp.databinding.ActivityChatBinding
import jp.ac.jec.cm0119.loginchatapp.model.Message
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null  //自身のトーク履歴？
    var receiverRoom: String? = null    //相手のトーク履歴？
    var senderUid: String? = null   //自身
    var receiverUid: String? = null //相手
    var dialog: ProgressDialog? = null

    private val database by lazy { FirebaseDatabase.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        //name, profileは相手の情報
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding.name.text = name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.ic_account)
            .into(binding.profile01)
        binding.imageView.setOnClickListener { finish() }

        //相手のuidを変数に割り当て
        receiverUid = intent.getStringExtra("uid")

        //自分のuidを変数に割り当て
        senderUid = FirebaseAuth.getInstance().uid

        //相手のオンラインorオフラインの状態によりstatusを変更する
        database.reference.child("Presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Test", "Presence/${receiverUid}/ChatActivity")   //確認用
                    if (snapshot.exists()) {    //null値がふくまれていない場合
                        val status = snapshot.getValue(String::class.java)
                        if (status == "offline") {
                            binding.status.visibility = View.GONE
                        } else {
                            binding.status.text = status
                            binding.status.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        
        senderRoom = senderUid + receiverUid    //受け側(自分)のトークルーム
        receiverRoom = receiverUid + senderUid  //送信側(相手)のトークルーム

        adapter = MessagesAdapter(this@ChatActivity, messages, senderRoom!!, receiverRoom!!)

        //recycleView
        binding.recycleView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding.recycleView.adapter = adapter

        //todo: この辺で確認(こ    こでrecycleviewの更新を行なっているが、senderRoomのmessageに自分の送信したメッセージが更新されていない)
        //senderRoomのmessageに変更があれば(主に相手のメッセージ送信にともなって変更される)viewの更新
        database.reference.child("chats")
            .child(senderRoom!!)
            .child("message")   /**messages?**/
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children) {
                        // TODO: そもそもここが呼ばれていない 
                        Log.d("Test", "snapshot1.key/${snapshot1.key}")
                        val message: Message? = snapshot1.getValue(Message::class.java) //選択したクラスへマーシャルする
                        message!!.messageId = snapshot1.key
                        messages!!.add(message)
                    }
                    //
                    adapter!!.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}

            })
        //送信ボタン実装
        binding.send.setOnClickListener {
            val messageTxt: String = binding.messageBox.text.toString()
            val date = Date()
            val message = Message(null, messageTxt, senderUid, null, date.time)

            binding.messageBox.setText("")
            val randomKey = database.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time

            //特定の子キーを指定された値に変更する(最後のメッセージの更新)
            database.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
            database.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)

            //senderRoomとreceiverRoomにメッセージ追加
            database.reference.child("chats")
                .child(senderRoom!!)    //自身のトーク履歴？に追加
                .child("message")   /**変更点**/
                .child(randomKey!!).setValue(message).addOnSuccessListener {
                    database.reference.child("chats")
                        .child(receiverRoom!!)  //相手のトーク履歴？に追加
                        .child("message")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {}
                }
        }
        //ライブラリーボタンの実装
        binding.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        //todo: 要改善(https://www.binarydevelop.com/article/handler-57183)
        val handler = Handler()
        binding.messageBox.addTextChangedListener(object : TextWatcher {
            //文字列が修正される直前に呼び出される
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            //文字一つを入力した場合に呼び出される
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            //最後に呼び出される
            /**edit(messageBox)の値が帰られたら送信者をonline状態にする**/
            override fun afterTextChanged(p0: Editable?) {
                database.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")  //タイピング状態とする
                handler.removeCallbacksAndMessages(null)    //全てののコールバック及び送信ずみメッセージの投稿を削除
                handler.postDelayed(userStoppedTyping, 1000)    //指定した時間(1秒)の経過後runnableを実行
            }

            var userStoppedTyping = Runnable {
                database.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online") //タイピング状態をonline状態に戻す
            }
        })
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    /**ギャラリーからの結果を処理**/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                if (data.data != null) {
                    val selectedImage = data.data
                    val calender = Calendar.getInstance()
                    val refence = storage.reference.child("chats")
                        .child(calender.timeInMillis.toString() + "")
                    dialog!!.show()
                    refence.putFile(selectedImage!!).addOnCompleteListener { task ->
                        dialog!!.dismiss()
                        if (task.isSuccessful) {
                            refence.downloadUrl.addOnSuccessListener { uri ->   //成功
                                val filePath = uri.toString()
                                /**uri.result.toString()**/
                                val messageTxt = binding.messageBox.text.toString()
                                val date = Date()
                                val message = Message(null, "photo", senderUid, filePath, date.time)
                                binding.messageBox.setText("")
                                val randomKey = database.reference.push().key
                                val lastMsgObj = HashMap<String, Any>()
                                lastMsgObj["lastMsg"] = message.message!!
                                lastMsgObj["lastMsgTime"] = date.time
                                database.reference.child("chats")
                                    .updateChildren(lastMsgObj)
                                database.reference.child("chats").child(receiverRoom!!)
                                    .updateChildren(lastMsgObj)

                                database.reference.child("chats")
                                    .child(senderRoom!!)
                                    .child("messages")
                                    .child(randomKey!!).setValue(message).addOnSuccessListener {
                                        database.reference.child("chats")
                                            .child(receiverRoom!!)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message)
                                            .addOnSuccessListener {}
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("Presence").child(currentId!!).setValue("online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("Presence").child(currentId!!).setValue("offline")
    }
}