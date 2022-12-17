package jp.ac.jec.cm0119.loginchatapp.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import jp.ac.jec.cm0119.loginchatapp.R
import jp.ac.jec.cm0119.loginchatapp.databinding.DeleteLayoutBinding
import jp.ac.jec.cm0119.loginchatapp.databinding.ReceiveMsgBinding
import jp.ac.jec.cm0119.loginchatapp.databinding.SendMsgBinding
import jp.ac.jec.cm0119.loginchatapp.model.Message

class MessagesAdapter(
    var context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    lateinit var messages: ArrayList<Message>
    private val ITEM_SEND = 1   //送り
    private val ITEM_RECEIVE = 2    //受け
    private val senderRoom: String
    private val receiverRoom: String

    init {
        if (messages != null) this.messages = messages
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }

    class SentMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    class ReceiveMsgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SEND) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.send_msg, parent, false)
            SentMsgHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.receive_msg, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId) { //自分
            ITEM_SEND
        } else {    //相手
            ITEM_RECEIVE
        }
    }

    // TODO: 呼ばれていない。
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        Log.d("Test", "message/ $message")
        //(if)送りか (else)受けか
        if (holder.javaClass == SentMsgHolder::class.java) {    //自分が送信したメッセージだったら
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")) {  //メッセージが写真の場合,画像を表示
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLiner.visibility = View.GONE
                Glide.with(context).load(message.imageUrl).placeholder(R.drawable.ic_image)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener {    //メッセージクリック時
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)   //自分のメッセージのIdを元にメッセージの内容を変更
                    }
                    message.messageId?.let { it2 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it2).setValue(message)   //相手のメッセージのIdを元にメッセージの内容を変更
                    }
                    dialog.dismiss()
                }
                binding.delete.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(null)   //メッセージのIdを元にメッセージの内容を変更(nullで削除扱い？)
                    }
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }  //キャンセル
                dialog.show()
                false
            }
        }
        else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLiner.visibility = View.GONE
                Glide.with(context).load(message.imageUrl).placeholder(R.drawable.ic_image)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener {    //メッセージクリック時
                val view = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)   //メッセージのIdを元にメッセージの内容を変更
                    }
                    message.messageId?.let { it2 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it2).setValue(message)   //メッセージのIdを元にメッセージの内容を変更
                    }
                    dialog.dismiss()
                } //全削除
                binding.delete.setOnClickListener {
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(null)   //メッセージのIdを元にメッセージの内容を変更(nullで削除扱い？)
                    }
                    dialog.dismiss()
                }   //対象のメッセージ削除？
                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }  //キャンセル
                dialog.show()
                false
            }
        }
    }

    override fun getItemCount(): Int = this.messages.size

}
