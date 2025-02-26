package com.example.playsnapui.ui.home

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.FirebaseFirestore

class HomeAdapterForYou (private val gameList: ArrayList<Games>,     private val fragmentManager: FragmentManager // Pass FragmentManager here
) : RecyclerView.Adapter<HomeAdapterForYou.MyViewHolder>(){
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
        holder.likeCount.text = "${game.totalLike}"
        holder.shareCount.text = "${game.totalShare}"
        holder.rating.rating = game.rating
        holder.lokasiText.text = game.jenisLokasi
        holder.usiaText.text = "${game.usiaMin}-${game.usiaMax} tahun"
        val rangePemain = game.usiaMin..game.usiaMax
        var tempUsia1 : Int = 0
        var tempUsia2 : Int = 0
        var tempUsia3 : Int = 0
        var isFlag1 : Boolean = false
        var isFlag2 : Boolean = false
        var isFlag3 : Boolean = false
        var isFlagAlr : Boolean = false

        Log.d("Games", "Games = ${game.namaPermainan}, usia Min = ${game.usiaMin}, usia Max = ${game.usiaMax}")
//        for(usia in rangePemain){
//            if (usia < 6) {
//                isFlag1 = true
//            }
//            else if (usia >= 6 && usia <= 10) {
//                isFlag2 = true
//            }
//            else if (usia > 10) {
//                isFlag3 = true
//            }
//            if(isFlag1 && isFlag2 && isFlag3){
//                holder.usiaText.text = "Semua Umur"
//                isFlagAlr = true
//                break
//            }
//            else if(isFlag2 && isFlag3){
//                holder.usiaText.text = ">=6 tahun"
//                isFlagAlr = true
//                break
//            }
//        }
//        if(!isFlagAlr){
//            if(isFlag1 && isFlag2){
//                holder.usiaText.text = "<=10 tahun"
//            }
//            else if(isFlag1){
//                holder.usiaText.text = "<6 tahun"
//            }
//            else if(isFlag2){
//                holder.usiaText.text = "6-10 tahun"
//            }
//            else if(isFlag3){
//                holder.usiaText.text = ">10 tahun"
//            }
//        }

// Fetch the thumbnail URL for the game (assuming it's stored in game.thumbnailUrl)
        val thumbnailUrl = game.squareThumb // Update this according to your data structure

        // Load the image into the squareView (ImageView) using Glide
        Glide.with(holder.itemView.context)
            .load(thumbnailUrl) // URL for the thumbnail image
            .into(holder.squareView)

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

            // Update Firestore
            updateLikeStatus(game, newLikeStatus)

            // Update the UI based on the new status
            if (newLikeStatus) {
                holder.likeButton.setImageResource(R.drawable.ic_like) // Bookmarked
            } else {
                holder.likeButton.setImageResource(R.drawable.ic_unlike) // Not bookmarked
            }
            holder.likeButton.tag = newLikeStatus
        }

        holder.shareButton.setOnClickListener{
            createDynamicLink(game)
        }

        holder.itemView.setOnClickListener {
            // Save the game details in SharedData
            SharedData.gameDetails = game

            // Navigate to the tutorialFragment
            it.findNavController().navigate(R.id.action_PopularFragment_to_TutorialFragment)

        }
    }

    private fun createDynamicLink(game: Games) {
        // Create a Dynamic Link
        val link = Uri.parse("https://playsnapgame.page.link/game?id=${game.game_id}") // Adjust the link as needed

        FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setLink(link)
            .setDomainUriPrefix("https://playsnapgame.page.link")
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder("com.example.playsnapui")
                    .setMinimumVersion(24)
                    .build()
            )
            .buildShortDynamicLink() // FIX: Use this for async task
            .addOnSuccessListener { shortDynamicLink ->
                val dynamicLink = shortDynamicLink.shortLink.toString()
                showDynamicLinkDialog(dynamicLink)
            }
            .addOnFailureListener { e ->
                Log.e("DynamicLink", "Error creating dynamic link", e)
            }


    }

    private fun showDynamicLinkDialog(dynamicLink: String) {
        val dialog = ShareFragment(dynamicLink)
        dialog.show(fragmentManager, "ShareFragment")
    }



    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val gamesName: TextView = itemView.findViewById(R.id.title_game_foryou)
        val bookmarkButton: ImageButton = itemView.findViewById(R.id.bookmark_foryou) // Bookmark button
        val likeButton: ImageButton = itemView.findViewById(R.id.btn_like_foryou)
        val squareView: ImageView = itemView.findViewById(R.id.display_game_foryou)
        val likeCount: TextView = itemView.findViewById(R.id.count_like_foryou)
        val shareCount: TextView = itemView.findViewById(R.id.count_share_foryou)
        val rating : RatingBar = itemView.findViewById(R.id.rating_foryou)
        val lokasiText : TextView = itemView.findViewById(R.id.tv_game_foryou)
        val usiaText : TextView = itemView.findViewById(R.id.tv_player_foryou)
        val shareButton : ImageButton = itemView.findViewById(R.id.btn_share_foryou)

        // Fetch the bookmark status from Firestore based on user and game

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

    private fun updateLikeStatus(game: Games, likeStatus: Boolean) {
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
                            "like_status" to likeStatus,
                            "bookmark_status" to false, // Default bookmark status
                            "rating" to 0 // Default rating
                        )
                        db.collection("social_interaction").add(socialInteraction)
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
                                        Log.d("Firestore", "Document deleted because the game hasn't been visited.")
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w("Firestore", "Error deleting document", exception)
                                    }
                            } else {
                                // Update the bookmark status
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .update("like_status", likeStatus)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error fetching document for like status", exception)
                }
        }
    }

    fun updateGames(newGames: List<Games>) {
        gameList.clear()
        gameList.addAll(newGames)
        notifyDataSetChanged()
    }

}