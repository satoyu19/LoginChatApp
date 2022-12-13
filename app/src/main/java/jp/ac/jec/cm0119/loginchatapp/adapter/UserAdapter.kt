package jp.ac.jec.cm0119.loginchatapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.ac.jec.cm0119.loginchatapp.ui.ChatActivity
import jp.ac.jec.cm0119.loginchatapp.R
import jp.ac.jec.cm0119.loginchatapp.model.User
import jp.ac.jec.cm0119.loginchatapp.databinding.ItemProfileBinding

class UserAdapter(var context: Context, var userList: ArrayList<User>): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val binding = ItemProfileBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        var v = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        var user = userList[position]
        holder.binding.username.text = user.name
        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.ic_account)
            .into(holder.binding.profile)

            //トーク画面遷移
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)  //トーク相手のname
            intent.putExtra("image", user.profileImage) //同上
            intent.putExtra("uid", user.uid)    //同上
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = userList.size
}