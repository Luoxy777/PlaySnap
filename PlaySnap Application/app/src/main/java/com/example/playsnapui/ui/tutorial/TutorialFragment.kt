package com.example.playsnapui.ui.tutorial

import SharedData.gameDetails
import TutorialViewModel
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.example.playsnapui.databinding.FragmentTutorialBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TutorialFragment : Fragment() {
    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!
    private var viewModel: TutorialViewModel? = null
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[TutorialViewModel::class.java]

        val thumbnailUrl = gameDetails?.squareThumb // Update this according to your data structure
        Log.d("Binding Check", "squareViewTutorial: ${binding.squareViewTutorial}")

        // Load the image using Glide into squareViewTutorial
        binding.squareViewTutorial.let {
            Log.d("Hm?", "Can you pass this?")
            Glide.with(requireContext())
                .load(thumbnailUrl)
                .placeholder(R.drawable.ic_display_game) // Make sure this image exists
                .into(it)
            Log.d("The thumbnail is:", "$thumbnailUrl")
        }

        // Inflate the layout using ViewBinding
        binding.bottomSheet.titleGameDescHeader.text = gameDetails?.namaPermainan ?: "NA"
        binding.bottomSheet.subtitleHeaderDesc.text = "${gameDetails?.jenisLokasi}, Usia ${gameDetails?.usiaMin} - ${gameDetails?.usiaMax} tahun"
        binding.bottomSheet.alatBermainContent.text = gameDetails?.properti ?: "NA"
        binding.bottomSheet.langkahBermainContent.text = Html.fromHtml(gameDetails?.tutorial ?: "NA", Html.FROM_HTML_MODE_LEGACY)
        binding.bottomSheet.numberPlayer.text = "${gameDetails?.pemainMin}-${gameDetails?.pemainMax}"


        val fullText = gameDetails?.deskripsi ?: "NA"
        val maxLength = 130
        val truncatedText: String
        val showMoreText: String

// Check if the string exceeds maxLength
        if (fullText.length > maxLength) {
            truncatedText = fullText.substring(0, maxLength) + "..."
            showMoreText = "baca_selengkapnya"
        } else {
            truncatedText = fullText
            showMoreText = ""
        }

        binding.bottomSheet.deskripsiContent.text = truncatedText
        binding.bottomSheet.bacaSelengkapnya.text = showMoreText

        binding.bottomSheet.bacaSelengkapnya.setOnClickListener {
            if (binding.bottomSheet.bacaSelengkapnya.text == "baca_selengkapnya") {
                // Show full text and change button text
                binding.bottomSheet.deskripsiContent.text = fullText
                binding.bottomSheet.bacaSelengkapnya.text = "kecilkan"
            } else {
                // Show truncated text and change button text back
                binding.bottomSheet.deskripsiContent.text = truncatedText
                binding.bottomSheet.bacaSelengkapnya.text = "baca_selengkapnya"
            }
        }

        playVideoFromGoogleDrive()

        // Fetch the bookmark status from Firestore based on user and game
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()

            // Check if a social interaction exists between the user and the game
            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("game_ID", gameDetails?.game_id)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // If no document, the game is not bookmarked yet
                        binding.bookmarkButtonTutorial.setBackgroundResource(R.drawable.ic_unbookmark)
                        binding.likeButtonTutorial.setBackgroundResource(R.drawable.ic_unlike)
                    } else {
                        // If document exists, set the bookmark icon based on the status
                        val doc = documents.first()
                        val bookmarkStatus = doc.getBoolean("bookmark_status") ?: false
                        if (bookmarkStatus) {
                            binding.bookmarkButtonTutorial.setBackgroundResource(R.drawable.ic_bookmark) // Bookmarked
                            binding.bookmarkButtonTutorial.tag = true
                        } else {
                            binding.bookmarkButtonTutorial.setBackgroundResource(R.drawable.ic_unbookmark) // Not bookmarked
                        }

                        val likeStatus = doc.getBoolean("like_status") ?: false
                        if (likeStatus) {
                            binding.likeButtonTutorial.setBackgroundResource(R.drawable.ic_like) // Bookmarked
                            binding.likeButtonTutorial.tag = true
                        } else {
                            binding.likeButtonTutorial.setBackgroundResource(R.drawable.ic_unlike)
                            binding.likeButtonTutorial.tag = false
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error checking bookmark status", exception)
                }
        }


        // Set up button listeners
        setUpListeners()
        return binding.root
    }

    private fun setUpListeners() {
        // Back button click listener
        binding.btnBack.setOnClickListener{
            findNavController().navigateUp()
        }


        // Handle bookmark button click to update the Firestore record
        binding.bookmarkButtonTutorial.setOnClickListener {
            // Toggle the bookmark status
            val currentBookmarkStatus = binding.bookmarkButtonTutorial.tag as? Boolean ?: false
            val newBookmarkStatus = !currentBookmarkStatus
            Log.d("Bookmark Status", "Current Bookmark Status: $newBookmarkStatus")  // Log the current state

            // Update Firestore
            gameDetails?.let { it1 -> updateBookmarkStatus(it1, newBookmarkStatus) }

            // Update the UI based on the new status
            if (newBookmarkStatus) {
                binding.bookmarkButtonTutorial.setBackgroundResource(R.drawable.ic_bookmark) // Bookmarked
            } else {
                binding.bookmarkButtonTutorial.setBackgroundResource(R.drawable.ic_unbookmark) // Not bookmarked
            }
            binding.bookmarkButtonTutorial.tag = newBookmarkStatus
        }

        // Like button click listener
        binding.likeButtonTutorial.setOnClickListener {
            // Toggle the bookmark status
            val currentLikeStatus = binding.likeButtonTutorial.tag as? Boolean ?: false
            val newLikeStatus = !currentLikeStatus
            Log.d("Bookmark Status", "Current Bookmark Status: $newLikeStatus")  // Log the current state

            // Update Firestore
            gameDetails?.let { it1 -> updateLikeStatus(it1, newLikeStatus) }

            // Update the UI based on the new status
            if (newLikeStatus) {
                binding.likeButtonTutorial.setBackgroundResource(R.drawable.ic_like) // Bookmarked
            } else {
                binding.likeButtonTutorial.setBackgroundResource(R.drawable.ic_unlike) // Not bookmarked
            }
            binding.likeButtonTutorial.tag = newLikeStatus
        }


        // Share button click listener
        binding.shareButtonTutorial.setOnClickListener { v -> viewModel?.toggleShare() }

        binding.bottomSheet.mainkanButtonTutorial.setOnClickListener {
            findNavController().navigate(R.id.action_TutorialFragment_to_FeedbackFragment)
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

    private var isFullscreen = false

    private fun playVideoFromGoogleDrive() {
        // Get the VideoView reference from binding
        val videoView: VideoView = binding.bottomSheet.videoTutorialContent

        // Video URL from Google Drive
        val videoUrl = gameDetails?.linkVideo

        // Convert to URI and set it to VideoView
        val uri: Uri = Uri.parse(videoUrl)
        videoView.setVideoURI(uri)

        // Set up MediaController for play/pause functionality
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Start the video when ready
        videoView.setOnPreparedListener {
            videoView.start()
        }

        // Handle errors
        videoView.setOnErrorListener { _, _, _ ->
            Log.e("TutorialFragment", "Error playing video")
            true
        }

        // Toggle fullscreen mode on tap (specifically horizontal fullscreen)
        videoView.setOnClickListener {
            toggleFullscreen(videoView)
        }
    }

    private fun toggleFullscreen(videoView: VideoView) {
        val layoutParams = videoView.layoutParams

        if (isFullscreen) {
            // Switch to normal size (keep the height fixed but reduce width)
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.video_width)
        } else {
            // Switch to horizontal fullscreen
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            // Optionally adjust height to maintain aspect ratio if needed
            val videoWidth = layoutParams.width
            val aspectRatio = 16f / 9f // For example, typical landscape aspect ratio
            layoutParams.height = (videoWidth / aspectRatio).toInt()
        }

        videoView.layoutParams = layoutParams
        isFullscreen = !isFullscreen
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding to prevent memory leaks
        _binding = null
    }
}