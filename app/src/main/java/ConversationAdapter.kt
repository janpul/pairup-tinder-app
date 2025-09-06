package com.example.pairup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ConversationAdapter(private val messages: List<ConversationActivity.Message>) :
    RecyclerView.Adapter<ConversationAdapter.MessageViewHolder>() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Choose the layout depending on whether the message is from the sender or receiver
        val layoutRes = if (viewType == 0) {
            R.layout.chat_message_received  // Layout for receiver (left)
        } else {
            R.layout.chat_message_sent  // Layout for sender (right)
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        // Determine whether the message was sent by the current user or not
        val message = messages[position]
        return if (message.senderId == auth.currentUser?.uid) 1 else 0  // 1 for sender, 0 for receiver
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message_content)

        fun bind(message: ConversationActivity.Message) {
            messageTextView.text = message.message
        }
    }
}
