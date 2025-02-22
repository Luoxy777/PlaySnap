package com.example.playsnapui.ui.home

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
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
import org.w3c.dom.Text

class HomeAdapterPopular(internal val gameList: ArrayList<Games>) : RecyclerView.Adapter<HomeAdapterPopular.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.layout_popgame_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var game: Games = gameList[position]
        holder.gamesName.text = game.namaPermainan

        var countLike = game.totalLike
        var countShare = game.totalShare

        holder.likeCount.text = "${game.totalLike}"
        holder.shareCount.text = "${game.totalShare}"

        // Fetch the thumbnail URL for the game (assuming it's stored in game.thumbnailUrl)
        val thumbnailUrl = game.landThumb // Update this according to your data structure

        // Load the image into the squareView (ImageView) using Glide
        Glide.with(holder.itemView.context)
            .load(thumbnailUrl) // URL for the thumbnail image
            .into(holder.landView)


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
                        holder.likeButton.setImageResource(R.drawable.ic_unlike)
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

                        val likeStatus = doc.getBoolean("like_status") ?: false
                        if (likeStatus) {
                            holder.likeButton.setImageResource(R.drawable.ic_like) // Bookmarked
                            holder.likeButton.tag = true
                        } else {
                            holder.likeButton.setImageResource(R.drawable.ic_unlike) // Not bookmarked
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

        holder.likeButton.setOnClickListener {
            // Toggle the bookmark status
            val currentLikeStatus = holder.likeButton.tag as? Boolean ?: false
            val newLikeStatus = !currentLikeStatus
            Log.d("Bookmark Status", "Current Bookmark Status: $newLikeStatus")  // Log the current state

            // Update the UI based on the new status
            if (newLikeStatus) {
                countLike += 1
                holder.likeButton.setImageResource(R.drawable.ic_like) // Liked
            } else {
                countLike -= 1
                holder.likeButton.setImageResource(R.drawable.ic_unlike) //  unliked
            }

            holder.likeCount.text = "$countLike"

            holder.likeButton.tag = newLikeStatus

            // Update Firestore after UI change
            Handler(Looper.getMainLooper()).postDelayed({
                updateLikeStatus(game, newLikeStatus) { success ->
                    if (success) {
                        Log.d("Cikcikperiuk", "Auwo")
                        updateLikeCount(game, countLike)  // Sync the like count to Firestore after a short delay
                        Log.d("Cikcikperiuk", "Auwo")
                        game.totalLike = countLike
                    } else {
                        // Revert UI changes if Firestore update fails
                        Log.d("u ii a", "Loh")
                        if (newLikeStatus) {
                            countLike -= 1
                            holder.likeButton.setImageResource(R.drawable.ic_unlike)
                        } else {
                            countLike += 1
                            holder.likeButton.setImageResource(R.drawable.ic_like)
                        }
                        holder.likeCount.text = "$countLike"
                    }
                }
            }, 500)  // Sync Firestore after 500ms delay



        }

        holder.itemView.setOnClickListener {
            // Save the game details in SharedData
            SharedData.gameDetails = game

            // Perform the navigation
            it.findNavController().navigate(R.id.action_PopularFragment_to_TutorialFragment)
        }
    }

    private fun updateLikeCount(game: Games, countLike: Int){
        val db = FirebaseFirestore.getInstance()
        Log.d("CountLike: ", "$countLike")

        db.collection("games")
            .document(game.game_id)
            .update("totalLike", countLike)
            .addOnSuccessListener{
                Log.d("Firestore", "Total likes updated successfully!")
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
                            val likeStatus = document.getBoolean("like_status")
                            if (!bookmarkStatus && !likeStatus!! && (dateVisit == "null" || dateVisit == null)) {
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

    private fun updateLikeStatus(game: Games, likeStatus: Boolean,  callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            Log.d("Masuk pak", "hehe")
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
                            "like_status" to likeStatus,
                            "bookmark_status" to false, // Default bookmark status
                            "rating" to 0 // Default rating
                        )
                        db.collection("social_interaction").add(socialInteraction).addOnSuccessListener { callback(true) }
                    } else {
                        // If document exists, check the bookmark status
                        for (document in documents) {
                            val dateVisit = document.getString("date_visit") // Get the visit date from the document
                            val bookmarkStatus = document.getBoolean("bookmark_status")
                            if (!bookmarkStatus!! && !likeStatus && (dateVisit == "null" || dateVisit == null)) {
                                // If unbookmarking and no date visit, delete the document
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        callback(true)                                     }
                                    .addOnFailureListener { exception ->
                                        callback(false)                                     }
                            } else {
                                // Update the bookmark status
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .update("like_status", likeStatus).addOnSuccessListener{ callback(true)}
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    callback(false)                 }
        }
    }


    override fun getItemCount(): Int {
        return gameList.size
    }


    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val gamesName: TextView = itemView.findViewById(R.id.title_game_pop)
        val bookmarkButton: ImageButton =
            itemView.findViewById(R.id.bookmark_pop) // Bookmark button
        val likeButton: ImageButton = itemView.findViewById(R.id.btn_like_pop)
        val landView: ImageView = itemView.findViewById(R.id.display_game_pop)
        val likeCount: TextView = itemView.findViewById(R.id.count_like_pop)
        val shareCount: TextView = itemView.findViewById(R.id.count_share_pop)

    }
}