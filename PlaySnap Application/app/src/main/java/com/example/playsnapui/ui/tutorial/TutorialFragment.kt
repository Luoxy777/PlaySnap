package com.example.playsnapui.ui.tutorial

import SharedData.deepLinkid
import SharedData.gameDetails
import TutorialViewModel
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.VideoView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playsnapui.R
import com.example.playsnapui.data.Games
import com.example.playsnapui.databinding.FragmentTutorialBinding
import com.example.playsnapui.ui.home.ShareFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TutorialFragment : Fragment() {
    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!
    private var viewModel: TutorialViewModel? = null
    private var isFullscreen = false

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

        if(gameDetails?.step != ""){
            binding.bottomSheet.bahanProperti.text = Html.fromHtml(gameDetails?.bahanProperti)
            binding.bottomSheet.caraMembuatContent.text = Html.fromHtml(gameDetails?.step)
        }else{
            binding.bottomSheet.caraMembuatIcon.visibility = View.GONE
            binding.bottomSheet.caraMembuatTitle.visibility = View.GONE
            binding.bottomSheet.caraMembuatContent.visibility = View.GONE
            binding.bottomSheet.bahanProperti.visibility = View.GONE
        }

        val fullText = gameDetails?.deskripsi ?: "NA"
        val maxLength = 130
        val truncatedText: String
        val showMoreText: String
        deepLinkid = ""

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
        playVideoFromGoogleDrive()
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
        binding.shareButtonTutorial.setOnClickListener {
            gameDetails?.let { it1 -> createDynamicLink(it1) }
        }

        binding.bottomSheet.mainkanButtonTutorial.setOnClickListener {
            val currentTime = System.currentTimeMillis() // You can also use LocalDateTime if needed
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(currentTime))
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val db = FirebaseFirestore.getInstance()

// Query to find the document that matches the user_ID and game_ID
            db.collection("social_interaction")
                .whereEqualTo("user_ID", userId)
                .whereEqualTo("game_ID", gameDetails?.game_id)
                .get()
                .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // If a document exists, get the first document
                        val document = querySnapshot.documents[0]

                        // Get the document reference to update it
                        val documentReference: DocumentReference = db.collection("social_interaction").document(document.id)

                        // Update the "date_visit" field with the current date
                        documentReference.update("date_visit", formattedDate)
                            .addOnSuccessListener {
                                // Data updated successfully
                                Log.d("Firestore", "Date visit updated successfully: $formattedDate")
                            }
                            .addOnFailureListener { e ->
                                // Handle failure
                                Log.e("Firestore", "Error updating date visit: ", e)
                            }
                    } else {
                        // No document found, handle accordingly (you can choose to add a new document or show an error)
                        Log.d("Firestore", "No document found matching the query, add new document")
                        val socialInteraction = hashMapOf(
                            "user_ID" to userId,
                            "game_ID" to gameDetails?.game_id,
                            "date_visit" to formattedDate, // Initially set to null (or current date when visited)
                            "like_status" to false, // Or based on your logic
                            "bookmark_status" to false,
                            "rating" to 0 // Default rating
                        )
                        db.collection("social_interaction").add(socialInteraction)

                    }
                }
                .addOnFailureListener { e ->
                    // Handle query failure
                    Log.e("Firestore", "Error getting documents: ", e)
                }


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

    private fun playVideoFromGoogleDrive() {
        // Get the VideoView reference from binding
        val videoView: VideoView = binding.bottomSheet.videoTutorialContent

        // Video URL from Google Drive
        val videoUrl = gameDetails?.linkVideo

        // Convert to URI and set it to VideoView
        val uri: Uri = Uri.parse(videoUrl)
        videoView.setVideoURI(uri)

        // Set up MediaController for play/pause functionality (but it will be custom)
        // Initially hide your custom controls (they will be shown when the video is clicked)
        binding.bottomSheet.controlOverlay.visibility = View.GONE

        videoView.setOnPreparedListener { mediaPlayer ->
            videoView.start()
            binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24)

            // Update SeekBar
            binding.bottomSheet.seekBar.max = mediaPlayer.duration
            updateSeekBar()

            // Update time text
            val totalTime = formatTime(mediaPlayer.duration)
//            binding.bottomSheet.totalTime.text = totalTime
        }

        // Handle errors
        videoView.setOnErrorListener { _, _, _ ->
            Log.e("TutorialFragment", "Error playing video")
            true
        }

        // Show the custom control overlay when video is clicked
        videoView.setOnClickListener {
            toggleControlOverlayVisibility()
        }

        binding.bottomSheet.playPauseButton.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow_24)  // Set play button
            } else {
                videoView.start()
                binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24)  // Set pause button
            }
        }
        // Forward 10 seconds
        binding.bottomSheet.forwardButton.setOnClickListener {
            val currentPosition = videoView.currentPosition
            videoView.seekTo(currentPosition + 10000)  // Forward by 10 seconds
        }

// Backward 10 seconds
        binding.bottomSheet.backwardButton.setOnClickListener {
            val currentPosition = videoView.currentPosition
            videoView.seekTo(currentPosition - 10000)  // Backward by 10 seconds
        }

        binding.bottomSheet.fullscreenButton.setOnClickListener {
            isFullscreen = !isFullscreen
            println("Fullscreen status = $isFullscreen")
            toggleFullscreen(videoView)  // Call the toggleFullscreen function when the fullscreen button is clicked
        }

        // SeekBar listener
        binding.bottomSheet.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
//                    binding.bottomSheet.currentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                videoView.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                videoView.start()
                binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24)
            }
        })

    }

    // Update SeekBar while video is playing
    private fun updateSeekBar() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val videoView = binding.bottomSheet.videoTutorialContent
                if (videoView.isPlaying) {
                    binding.bottomSheet.seekBar.progress = videoView.currentPosition
//                    binding.bottomSheet.currentTime.text = formatTime(videoView.currentPosition)
                }
                handler.postDelayed(this, 500)
            }
        })
    }

    // Format time in MM:SS
    private fun formatTime(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = milliseconds / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    private fun toggleControlOverlayVisibility() {
        // Toggle visibility of the custom control overlay
        if (binding.bottomSheet.controlOverlay.visibility == View.GONE) {
            binding.bottomSheet.controlOverlay.visibility = View.VISIBLE
        } else {
            binding.bottomSheet.controlOverlay.visibility = View.GONE
        }
    }

    private fun toggleFullscreen(videoView: VideoView) {
        val layoutParams = videoView.layoutParams

        val constraintLayout = view?.findViewById<ConstraintLayout>(R.id.wrapped_video_tutorial_content)
        val constraintSet = ConstraintSet()
        val marginSetter = view?.findViewById<ConstraintLayout>(R.id.margin_setter)
        constraintSet.clone(constraintLayout)

        if (!isFullscreen) {
            // Minimize to vertical mode
            println("Check !isFullScreen")
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.video_width)
            layoutParams.height = resources.getDimensionPixelSize(R.dimen.video_height)
            videoView.layoutParams = layoutParams

            binding.bottomSheet.fullscreenButton.setBackgroundResource(R.drawable.baseline_fullscreen_24)  // Change to fullscreen icon
            // Restore other layout elements visibility
            restoreOtherLayoutElements()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            // Switch to fullscreen (horizontal)
            println("Check fullScreen")
            layoutParams.width = resources.getDimensionPixelSize(R.dimen.video_width_full)
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            val videoWidth = layoutParams.width

            val aspectRatio = 16f / 9f  // 16:9 aspect ratio
            layoutParams.height = (videoWidth / aspectRatio).toInt()
            println("Video width = $videoWidth height = ${layoutParams.height}")
            videoView.layoutParams = layoutParams

            binding.bottomSheet.fullscreenButton.setBackgroundResource(R.drawable.baseline_fullscreen_exit_24)  // Change to minimize icon
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            // Hide other layout elements for fullscreen mode
            hideOtherLayoutElements()
            println("Oops? ended.")
        }
    }

    private fun createDynamicLink(game: Games) {
        // Create a Dynamic Link
        val link = Uri.parse("https://playsnapgame.page.link/${game.game_id}") // Adjust the link as needed
        showDynamicLinkDialog(link.toString())
    }

    private fun showDynamicLinkDialog(dynamicLink: String) {
        val dialog = ShareFragment(dynamicLink)
        dialog.show(parentFragmentManager, "ShareFragment")
    }


    private fun hideOtherLayoutElements() {
        // Hide elements other than the video view and controls
//        binding.squareViewTutorial.visibility = View.GONE
//        binding.interactionButton.visibility = View.GONE
//        binding.btnBack.visibility = View.GONE
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
        // Restore visibility of the elements that were hidden in fullscreen mode
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

    override fun onResume() {
        super.onResume()
        // Ensure the orientation is portrait when the fragment resumes
        if (!isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    override fun onPause() {
        super.onPause()
        // Ensure the orientation is portrait when the fragment is paused or navigated away from
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding to prevent memory leaks
        _binding = null
    }
}