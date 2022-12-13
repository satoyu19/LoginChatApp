package jp.ac.jec.cm0119.loginchatapp.model

data class Message(
    var messageId: String? = null,
    var message: String? = null,
    var senderId: String? = null,   //送り主？
    var imageUrl: String? = null,
    var timeStamp: Long = 0
)
