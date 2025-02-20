package com.example.playsnapui.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeAdapterForYou (private val gameList: ArrayList<Games>) : RecyclerView.Adapter<HomeAdapterForYou.MyViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HomeAdapterForYou.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_foryougame_list_item, parent, false)

        return HomeAdapterForYou.MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return gameList.size


    }

    override fun onBindViewHolder(holder: HomeAdapterForYou.MyViewHolder, position: Int) {
        val game : Games = gameList[position]
        holder.gamesName.text = game.namaPermainan

// Fetch the thumbnail URL for the game (assuming it's stored in game.thumbnailUrl)
        val thumbnailUrl = game.squareThumb // Update this according to your data structure

        // Load the image into the squareView (ImageView) using Glide
        Glide.with(holder.itemView.context)
            .load(thumbnailUrl) // URL for the thumbnail image
            .into(holder.squareView)

        // Fetch the bookmark status from Firestore based on user and game
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            // Check if a social interaction exists between the user and the game
            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("game_ID", game.game_id)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // If no document, the game is not bookmarked yet
                        holder.bookmarkButton.setImageResource(R.drawable.ic_unbookmark)
                    } else {
                        // If document exists, set the bookmark icon based on the status
                        val doc = documents.first()
                        val bookmarkStatus = doc.getBoolean("bookmark_status") ?: false
                        if (bookmarkStatus) {
                            holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark) // Bookmarked
                            holder.bookmarkButton.tag = true
                        } else {
                            holder.bookmarkButton.setImageResource(R.drawable.ic_unbookmark) // Not bookmarked
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error checking bookmark status", exception)
                }
        }

        // Handle bookmark button click to update the Firestore record
        holder.bookmarkButton.setOnClickListener {
            // Toggle the bookmark status
            val currentBookmarkStatus = holder.bookmarkButton.tag as? Boolean ?: false
            val newBookmarkStatus = !currentBookmarkStatus
            Log.d("Bookmark Status", "Current Bookmark Status: $newBookmarkStatus")  // Log the current state

            // Update Firestore
            updateBookmarkStatus(game, newBookmarkStatus)

            // Update the UI based on the new status
            if (newBookmarkStatus) {
                holder.bookmarkButton.setImageResource(R.drawable.ic_bookmark) // Bookmarked
            } else {
                holder.bookmarkButton.setImageResource(R.drawable.ic_unbookmark) // Not bookmarked
            }
            holder.bookmarkButton.tag = newBookmarkStatus
        }

        holder.itemView.setOnClickListener {
            // Save the game details in SharedData
            SharedData.gameDetails = game

            // Navigate to the tutorialFragment
            val action = HomeFragmentDirections.actionHomeFragmentToTutorialFragment()

            // Perform the navigation
            it.findNavController().navigate(action)
        }

    }

    private fun updateBookmarkStatus(game: Games, bookmarkStatus: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("game_ID", game.game_id)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // If no existing document, create a new one
                        val socialInteraction = hashMapOf(
                            "user_ID" to userId,
                            "game_ID" to game.game_id,
                            "date_visit" to null, // Initially set to null (or current date when visited)
                            "like_status" to false, // Or based on your logic
                            "bookmark_status" to bookmarkStatus,
                            "rating" to 0 // Default rating
                        )
                        db.collection("social_interaction").add(socialInteraction)
                    } else {
                        // If document exists, check the bookmark status
                        for (document in documents) {
                            val dateVisit = document.getString("date_visit") // Get the visit date from the document

                            if (!bookmarkStatus && (dateVisit == "null" || dateVisit == null)) {
                                // If unbookmarking and no date visit, delete the document
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Document deleted because the game hasn't been visited.")
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w("Firestore", "Error deleting document", exception)
                                    }
                            } else {
                                // Update the bookmark status
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .update("bookmark_status", bookmarkStatus)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error fetching document for bookmark status", exception)
                }
        }
    }
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gamesName: TextView = itemView.findViewById(R.id.title_game_foryou)
        val bookmarkButton: ImageButton = itemView.findViewById(R.id.bookmark_foryou) // Bookmark button
        val squareView: ImageView = itemView.findViewById(R.id.display_game_foryou)
    }

}