package jp.ac.jec.cm0119.loginchatapp.ui

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.loginchatapp.adapter.UserAdapter
import jp.ac.jec.cm0119.loginchatapp.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.loginchatapp.model.User

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var users: ArrayList<User>? = null
    var dialog: ProgressDialog? = null
    var userAdapter: UserAdapter? = null
    var user: User? = null
    private val database by lazy { FirebaseDatabase.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        dialog = ProgressDialog(this@MainActivity)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)

        users = ArrayList<User>()
        userAdapter = UserAdapter(this@MainActivity, users!!)
        val layoutManager = GridLayoutManager(this@MainActivity, 2)
        binding.mRec.layoutManager = layoutManager

        database.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)    //自分?
            .addValueEventListener(object :
                ValueEventListener {  //FirebaseAuth.getInstance().uid!!のデータが変更された時のリスナー
                override fun onDataChange(snapshot: DataSnapshot) { //snapshot→FirebaseDatabaseのデータ
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.mRec.adapter = userAdapter

        //usersに変化があったらrecycleViewを更新する
        database.reference.child("users")
            .addValueEventListener(object : ValueEventListener { //users全体
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("Test", "users/MainActivity")
                    users!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val user: User? = snapshot1.getValue(User::class.java)
                        //自分でなければusersに追加
                        if (!user!!.uid.equals(FirebaseAuth.getInstance().uid)) users!!.add(user)
                    }
                    // TODO: 要改善 https://qiita.com/toastkidjp/items/f6fffc44acbf4d3690fd
                    userAdapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.reference.child("presence").child(currentId!!).setValue("Offline")
    }
}

