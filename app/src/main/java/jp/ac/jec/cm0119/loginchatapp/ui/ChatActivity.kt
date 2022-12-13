package jp.ac.jec.cm0119.loginchatapp.ui

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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
    var senderRoom: String? = null
    var receiverRoom: String? = null
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
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding.name.text = name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.ic_account)
            .into(binding.profile01)
        binding.imageView.setOnClickListener { finish() }
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
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
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this@ChatActivity, messages, senderRoom!!, receiverRoom!!)

        binding.recycleView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding.recycleView.adapter = adapter
        database.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val message: Message? = snapshot1.getValue(Message::class.java) //選択したクラスへマーシャルする
                        message!!.messageId = snapshot1.key
                        messages!!.add(message)
                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        binding.send.setOnClickListener {
            val messageTxt: String = binding.messageBox.text.toString()
            val date = Date()
            val message = Message(null, messageTxt, senderUid, null, date.time)

            binding.messageBox.setText("")
            val randomKey = database.reference.push().key
            val lastMsgObj = HashMap<String,Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time
            database.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
            database.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
            database.reference.child("chats")
                .child(senderRoom!!)
                .child("messages")
                .child(randomKey!!).setValue(message).addOnSuccessListener {
                    database.reference.child("chats")
                        .child(receiverRoom!!)
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {

                        }
            }
        }
    }
}