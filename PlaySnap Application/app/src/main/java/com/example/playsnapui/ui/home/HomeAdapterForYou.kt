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
import com.bumptech.glide.request.RequestOptions
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeAdapterForYou(
    private val gameList: ArrayList<Games>,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<HomeAdapterForYou.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_foryougame_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount() = gameList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val game = gameList[position]
        holder.bind(game)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gamesName: TextView = itemView.findViewById(R.id.title_game_foryou)
        private val bookmarkButton: ImageButton = itemView.findViewById(R.id.bookmark_foryou)
        private val likeButton: ImageButton = itemView.findViewById(R.id.btn_like_foryou)
        private val squareView: ImageView = itemView.findViewById(R.id.display_game_foryou)
        private val likeCount: TextView = itemView.findViewById(R.id.count_like_foryou)
        private val shareCount: TextView = itemView.findViewById(R.id.count_share_foryou)
        private val rating: RatingBar = itemView.findViewById(R.id.rating_foryou)
        private val lokasiText: TextView = itemView.findViewById(R.id.tv_game_foryou)
        private val usiaText: TextView = itemView.findViewById(R.id.tv_player_foryou)
        private val shareButton: ImageButton = itemView.findViewById(R.id.btn_share_foryou)

        fun bind(game: Games) {
            gamesName.text = game.namaPermainan
            likeCount.text = game.totalLike.toString()
            shareCount.text = game.totalShare.toString()
            rating.rating = game.rating
            lokasiText.text = game.jenisLokasi
            usiaText.text = "${game.usiaMin}-${game.usiaMax} tahun"

            Glide.with(itemView.context)
                .load(game.squareThumb)
                .apply(RequestOptions().placeholder(R.drawable.maskot_bye_kanan).error(R.drawable.ic_maskot_bleh))
                .into(squareView)

            val userId = FirebaseAuth.getInstance().currentUser?.uid
            userId?.let { fetchSocialInteraction(it, game) }

            bookmarkButton.setOnClickListener { toggleBookmark(game) }
            likeButton.setOnClickListener { toggleLike(game) }
            shareButton.setOnClickListener { createDynamicLink(game) }

            itemView.setOnClickListener {
                SharedData.gameDetails = game
                it.findNavController().navigate(R.id.action_PopularFragment_to_TutorialFragment)
            }
        }

        private fun fetchSocialInteraction(userId: String, game: Games) {
            FirebaseFirestore.getInstance().collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("game_ID", game.game_id)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        bookmarkButton.setImageResource(R.drawable.ic_unbookmark)
                        likeButton.setImageResource(R.drawable.ic_unlike)
                    } else {
                        val doc = documents.first()
                        bookmarkButton.tag = doc.getBoolean("bookmark_status") == true
                        likeButton.tag = doc.getBoolean("like_status") == true
                        bookmarkButton.setImageResource(if (bookmarkButton.tag as Boolean) R.drawable.ic_bookmark else R.drawable.ic_unbookmark)
                        likeButton.setImageResource(if (likeButton.tag as Boolean) R.drawable.ic_like else R.drawable.ic_unlike)
                    }
                }
        }

        private fun toggleBookmark(game: Games) {
            val newStatus = !(bookmarkButton.tag as? Boolean ?: false)
            bookmarkButton.tag = newStatus
            bookmarkButton.setImageResource(if (newStatus) R.drawable.ic_bookmark else R.drawable.ic_unbookmark)
            updateFirestoreStatus(game, "bookmark_status", newStatus)
        }

        private fun toggleLike(game: Games) {
            val newStatus = !(likeButton.tag as? Boolean ?: false)
            likeButton.tag = newStatus
            likeButton.setImageResource(if (newStatus) R.drawable.ic_like else R.drawable.ic_unlike)
            updateFirestoreStatus(game, "like_status", newStatus)
        }

        private fun updateFirestoreStatus(game: Games, field: String, status: Boolean) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = FirebaseFirestore.getInstance()
            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("game_ID", game.game_id)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        db.collection("social_interaction").add(
                            mapOf("user_ID" to userId, "game_ID" to game.game_id, field to status)
                        )
                    } else {
                        db.collection("social_interaction").document(documents.first().id).update(field, status)
                    }
                }
        }

        private fun createDynamicLink(game: Games) {
            val link = Uri.parse("https://playsnapgame.page.link/${game.game_id}")
            showDynamicLinkDialog(link.toString())
        }

        private fun showDynamicLinkDialog(dynamicLink: String) {
            ShareFragment(dynamicLink).show(fragmentManager, "ShareFragment")
        }
    }

    fun updateGames(newGames: List<Games>) {
        gameList.clear()
        gameList.addAll(newGames)
        notifyDataSetChanged()
    }
}
