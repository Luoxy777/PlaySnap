package com.example.playsnapui.ui.tutorial

import SharedData.deepLinkid
import SharedData.gameDetails
import TutorialViewModel
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.example.playsnapui.databinding.FragmentTutorialBinding
import com.example.playsnapui.ui.home.ShareFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TutorialFragment : Fragment() {
    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!
    private var viewModel: TutorialViewModel? = null
    private var isFullscreen = false
    private val seekBarHandler = Handler(Looper.getMainLooper())
    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            val videoView = binding.bottomSheet.videoTutorialContent
            if (videoView.isPlaying) {
                binding.bottomSheet.seekBar.progress = videoView.currentPosition
            }
            seekBarHandler.postDelayed(this, 500)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[TutorialViewModel::class.java]

        Glide.with(requireContext())
            .load(gameDetails?.squareThumb)
            .placeholder(R.drawable.ic_display_game)
            .into(binding.squareViewTutorial)

        binding.bottomSheet.titleGameDescHeader.text = gameDetails?.namaPermainan ?: "NA"
        binding.bottomSheet.subtitleHeaderDesc.text = "${gameDetails?.jenisLokasi}, Usia ${gameDetails?.usiaMin} - ${gameDetails?.usiaMax} tahun"
        binding.bottomSheet.alatBermainContent.text = gameDetails?.properti ?: "NA"
        binding.bottomSheet.langkahBermainContent.text = Html.fromHtml(gameDetails?.tutorial ?: "NA", Html.FROM_HTML_MODE_LEGACY)

        if(gameDetails?.pemainMin == gameDetails?.pemainMax){
            binding.bottomSheet.numberPlayer.text = "${gameDetails?.pemainMax}"
        }else{
            binding.bottomSheet.numberPlayer.text = "${gameDetails?.pemainMin}-${gameDetails?.pemainMax}"
        }

        if(gameDetails?.step != ""){
            binding.bottomSheet.bahanProperti.text = Html.fromHtml(gameDetails?.bahanProperti, Html.FROM_HTML_MODE_COMPACT)
            binding.bottomSheet.caraMembuatContent.text = Html.fromHtml(gameDetails?.step, Html.FROM_HTML_MODE_COMPACT)
        }else{
            binding.bottomSheet.caraMembuatIcon.visibility = View.GONE
            binding.bottomSheet.caraMembuatTitle.visibility = View.GONE
            binding.bottomSheet.caraMembuatContent.visibility = View.GONE
            binding.bottomSheet.bahanProperti.visibility = View.GONE
            binding.bottomSheet.tvBahan.visibility = View.GONE
            binding.bottomSheet.tvCara.visibility = View.GONE
        }

        val fullText = gameDetails?.deskripsi ?: "NA"
        val maxLength = 130
        val truncatedText = if (fullText.length > maxLength) fullText.substring(0, maxLength) + "..." else fullText
        val showMoreText = if (fullText.length > maxLength) "baca selengkapnya" else ""

        binding.bottomSheet.deskripsiContent.text = truncatedText
        binding.bottomSheet.bacaSelengkapnya.text = showMoreText

        binding.bottomSheet.bacaSelengkapnya.setOnClickListener {
            if (binding.bottomSheet.bacaSelengkapnya.text == "baca selengkapnya") {
                binding.bottomSheet.deskripsiContent.text = fullText
                binding.bottomSheet.bacaSelengkapnya.text = "kecilkan"
            } else {
                binding.bottomSheet.deskripsiContent.text = truncatedText
                binding.bottomSheet.bacaSelengkapnya.text = "baca selengkapnya"
            }
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val document = db.collection("social_interaction")
                    .whereEqualTo("user_ID", userId)
                    .whereEqualTo("game_ID", gameDetails?.game_id)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                binding.bookmarkButtonTutorial.setBackgroundResource(
                    if (document?.getBoolean("bookmark_status") == true) R.drawable.ic_bookmark else R.drawable.ic_unbookmark
                )
                binding.likeButtonTutorial.setBackgroundResource(
                    if (document?.getBoolean("like_status") == true) R.drawable.ic_like else R.drawable.ic_unlike
                )
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error fetching data", e)
            }
        }



        binding.btnBack.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.bookmarkButtonTutorial.setOnClickListener {
            val current = binding.bookmarkButtonTutorial.tag as? Boolean ?: false
            val newStatus = !current
            gameDetails?.let { it1 -> updateBookmarkStatus(it1, newStatus) }
            binding.bookmarkButtonTutorial.setBackgroundResource(if (newStatus) R.drawable.ic_bookmark else R.drawable.ic_unbookmark)
            binding.bookmarkButtonTutorial.tag = newStatus
        }

        binding.likeButtonTutorial.setOnClickListener {
            val current = binding.likeButtonTutorial.tag as? Boolean ?: false
            val newStatus = !current
            gameDetails?.let { it1 -> updateLikeStatus(it1, newStatus) }
            binding.likeButtonTutorial.setBackgroundResource(if (newStatus) R.drawable.ic_like else R.drawable.ic_unlike)
            binding.likeButtonTutorial.tag = newStatus
        }

        binding.shareButtonTutorial.setOnClickListener {
            gameDetails?.let { it1 -> createDynamicLink(it1) }
        }

        binding.bottomSheet.mainkanButtonTutorial.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(currentTime))
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("game_ID", gameDetails?.game_id)
                .get()
                .addOnSuccessListener { querySnapshot ->  // HAPUS viewLifecycleOwner
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        db.collection("social_interaction").document(document.id)
                            .update("date_visit", formattedDate)
                    } else {
                        val socialInteraction = hashMapOf(
                            "user_ID" to userId,
                            "game_ID" to gameDetails?.game_id,
                            "date_visit" to formattedDate,
                            "like_status" to false,
                            "bookmark_status" to false,
                            "rating" to 0
                        )
                        db.collection("social_interaction").add(socialInteraction)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Error fetching data", e)
                }

            findNavController().navigate(R.id.action_TutorialFragment_to_FeedbackFragment)
        }


        playVideoFromGoogleDrive()
        return binding.root
    }

    private fun playVideoFromGoogleDrive() {
        val videoView = binding.bottomSheet.videoTutorialContent
        val uri = Uri.parse(gameDetails?.linkVideo)
        videoView.setVideoURI(uri)

        binding.bottomSheet.controlOverlay.visibility = View.GONE

        videoView.setOnPreparedListener { mediaPlayer ->
            videoView.start()
            binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24)
            binding.bottomSheet.seekBar.max = mediaPlayer.duration
            updateSeekBar()
        }

        videoView.setOnClickListener { toggleControlOverlayVisibility() }

        binding.bottomSheet.playPauseButton.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow_24)
            } else {
                videoView.start()
                binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24)
            }
        }

        binding.bottomSheet.forwardButton.setOnClickListener {
            videoView.seekTo(videoView.currentPosition + 10000)
        }

        binding.bottomSheet.backwardButton.setOnClickListener {
            videoView.seekTo(videoView.currentPosition - 10000)
        }

        binding.bottomSheet.fullscreenButton.setOnClickListener {
            isFullscreen = !isFullscreen
            toggleFullscreen(videoView)
        }

        binding.bottomSheet.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) videoView.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) { videoView.pause() }
            override fun onStopTrackingTouch(seekBar: SeekBar?) { videoView.start() }
        })
    }

    private fun updateSeekBar() {
        seekBarHandler.post(updateSeekBarRunnable)
    }

    private fun toggleControlOverlayVisibility() {
        binding.bottomSheet.controlOverlay.visibility = if (binding.bottomSheet.controlOverlay.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    private fun toggleFullscreen(videoView: VideoView) {
        val layoutParams = videoView.layoutParams
        if (!isFullscreen) {
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.video_width)
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.video_height)
            restoreOtherLayoutElements()
            binding.bottomSheet.fullscreenButton.setBackgroundResource(R.drawable.baseline_fullscreen_24)
        } else {
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.video_width_full)
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            hideOtherLayoutElements()
            binding.bottomSheet.fullscreenButton.setBackgroundResource(R.drawable.baseline_fullscreen_exit_24)
        }
        videoView.layoutParams = layoutParams
    }

    private fun updateBookmarkStatus(game: Games, bookmarkStatus: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        lifecycleScope.launch(Dispatchers.IO) {  // Gunakan Coroutine
            try {
                val querySnapshot = db.collection("social_interaction")
                    .whereEqualTo("user_ID", userId)
                    .whereEqualTo("game_ID", game.game_id)
                    .get()
                    .await() // Convert ke coroutine

                if (querySnapshot.isEmpty) {
                    val socialInteraction = hashMapOf(
                        "user_ID" to userId,
                        "game_ID" to game.game_id,
                        "bookmark_status" to bookmarkStatus,
                        "like_status" to false,
                        "rating" to 0
                    )
                    db.collection("social_interaction").add(socialInteraction).await()
                } else {
                    for (document in querySnapshot.documents) {
                        if (!bookmarkStatus && document.getString("date_visit") == null) {
                            db.collection("social_interaction").document(document.id).delete().await()
                        } else {
                            db.collection("social_interaction").document(document.id)
                                .update("bookmark_status", bookmarkStatus).await()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error updating bookmark", e)
            }
        }
    }


    private fun updateLikeStatus(game: Games, likeStatus: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        lifecycleScope.launch(Dispatchers.IO) {  // Jalankan di background thread
            try {
                val querySnapshot = db.collection("social_interaction")
                    .whereEqualTo("user_ID", userId)
                    .whereEqualTo("game_ID", game.game_id)
                    .get()
                    .await() // Convert ke coroutine

                if (querySnapshot.isEmpty) {
                    val socialInteraction = hashMapOf(
                        "user_ID" to userId,
                        "game_ID" to game.game_id,
                        "like_status" to likeStatus,
                        "bookmark_status" to false,
                        "rating" to 0
                    )
                    db.collection("social_interaction").add(socialInteraction).await()
                } else {
                    for (document in querySnapshot.documents) {
                        if (!likeStatus && document.getString("date_visit") == null) {
                            db.collection("social_interaction").document(document.id).delete().await()
                        } else {
                            db.collection("social_interaction").document(document.id)
                                .update("like_status", likeStatus).await()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FirestoreError", "Error updating like status", e)
            }
        }
    }


    private fun createDynamicLink(game: Games) {
        val link = Uri.parse("https://playsnapgame.page.link/${game.game_id}")
        showDynamicLinkDialog(link.toString())
    }

    private fun showDynamicLinkDialog(link: String) {
        ShareFragment(link).show(parentFragmentManager, "ShareFragment")
    }

    private fun hideOtherLayoutElements() {
        binding.bottomSheet.dragIcon.visibility = View.GONE
        binding.bottomSheet.videoTutorialText.visibility = View.GONE
        binding.bottomSheet.gameDescHeaderWrapped.visibility = View.GONE
        binding.bottomSheet.deskripsiSection.visibility = View.GONE
        binding.bottomSheet.alatBermainSection.visibility = View.GONE
        binding.bottomSheet.titleTutorial.visibility = View.GONE
        binding.bottomSheet.langkahBermainTitle.visibility = View.GONE
        binding.bottomSheet.langkahBermainContent.visibility = View.GONE
        binding.bottomSheet.mainkanButtonTutorial.visibility = View.GONE
        binding.bottomSheet.caraMembuatIcon.visibility = View.GONE
        binding.bottomSheet.caraMembuatTitle.visibility = View.GONE
        binding.bottomSheet.caraMembuatContent.visibility = View.GONE
        binding.bottomSheet.bahanProperti.visibility = View.GONE
    }

    private fun restoreOtherLayoutElements() {
        binding.bottomSheet.dragIcon.visibility = View.VISIBLE
        binding.bottomSheet.videoTutorialText.visibility = View.VISIBLE
        binding.bottomSheet.gameDescHeaderWrapped.visibility = View.VISIBLE
        binding.bottomSheet.deskripsiSection.visibility = View.VISIBLE
        binding.bottomSheet.alatBermainSection.visibility = View.VISIBLE
        binding.bottomSheet.titleTutorial.visibility = View.VISIBLE
        binding.bottomSheet.langkahBermainTitle.visibility = View.VISIBLE
        binding.bottomSheet.langkahBermainContent.visibility = View.VISIBLE
        binding.bottomSheet.mainkanButtonTutorial.visibility = View.VISIBLE
        binding.bottomSheet.caraMembuatIcon.visibility = View.VISIBLE
        binding.bottomSheet.caraMembuatTitle.visibility = View.VISIBLE
        binding.bottomSheet.caraMembuatContent.visibility = View.VISIBLE
        binding.bottomSheet.bahanProperti.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        binding.bottomSheet.videoTutorialContent.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        seekBarHandler.removeCallbacks(updateSeekBarRunnable)
        binding.bottomSheet.videoTutorialContent.apply {
            stopPlayback()
            setOnPreparedListener(null)
            setOnClickListener(null)
            setOnErrorListener(null)
        }
        _binding = null
    }
}