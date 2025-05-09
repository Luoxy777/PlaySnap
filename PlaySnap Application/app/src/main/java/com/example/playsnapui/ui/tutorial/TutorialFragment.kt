package com.example.playsnapui.ui.tutorial

import SharedData.gameDetails
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
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.VideoView
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
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TutorialFragment : Fragment() {

    private var _binding: FragmentTutorialBinding? = null
    private val binding get() = _binding!!
    private var isFullscreen = false

    private val handler = Handler(Looper.getMainLooper())
//    private val updateSeekBarRunnable = object : Runnable {
//        override fun run() {
//            updateSeekBar()
//            handler.postDelayed(this, 500)
//        }
//    }

    private lateinit var customWebChromeClient: WebChromeClient
    private lateinit var webView: WebView
    private var customView: View? = null
    private var customViewContainer: ViewGroup? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = binding.bottomSheet.videoTutorialContent
        setupWebView()
        val videoUri = gameDetails?.linkVideo

        val embedHtml = """
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
        <style>
            * { margin: 0; padding: 0; }
            html, body { width: 100%; height: 100%; overflow: hidden; }
            .video-container { 
                position: relative; 
                width: 100vw; 
                height: 56.25vw; /* 16:9 aspect ratio */ 
            }
            iframe { 
                position: absolute; 
                width: 100%; 
                height: 100%;
                transform: scale(1); /* Perbesar tampilan tombol */
                transform-origin: center; /* Pastikan scaling tetap rapi */
            }
        </style>
    </head>
    <body>
        <div class="video-container">
            <iframe 
                src="$videoUri"
                frameborder="0" 
                allowfullscreen 
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share">
            </iframe>
        </div>
    </body>
    </html>
""".trimIndent()

        webView.loadDataWithBaseURL(null, embedHtml, "text/html", "utf-8", null)
        initializeViews()
        setUpListeners()
//        playVideoFromGoogleDrive()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                return false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            private var customView: View? = null
            private var customViewCallback: CustomViewCallback? = null
            private var fullScreenContainer: FrameLayout? = null

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    onHideCustomView()
                    return
                }

                customView = view
                customViewCallback = callback

                fullScreenContainer = FrameLayout(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    addView(view)
                }

                requireActivity().window.decorView.findViewById<ViewGroup>(android.R.id.content)
                    .addView(fullScreenContainer)

                webView.visibility = View.GONE
            }

            override fun onHideCustomView() {
                fullScreenContainer?.removeView(customView)
                customView = null
                customViewCallback?.onCustomViewHidden()

                webView.visibility = View.VISIBLE
            }
        }

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.99 Mobile Safari/537.36" // Forces mobile mode
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    }

    @SuppressLint("SetTextI18n")
    private fun initializeViews() {
        // Load game thumbnail using Glide
        gameDetails?.squareThumb?.let { thumbnailUrl ->
            Glide.with(requireContext())
                .load(thumbnailUrl)
                .placeholder(R.drawable.ic_display_game)
                .into(binding.squareViewTutorial)
        }

        // Set game details
        binding.bottomSheet.apply {
            titleGameDescHeader.text = gameDetails?.namaPermainan ?: "NA"
            subtitleHeaderDesc.text = "${gameDetails?.jenisLokasi}, Usia ${gameDetails?.usiaMin} - ${gameDetails?.usiaMax} tahun"
            if(gameDetails?.properti?.isEmpty() == true){
                alatBermainSection.visibility = View.GONE
            }else{
                alatBermainContent.text = gameDetails?.properti ?: "NA"
            }
            langkahBermainContent.text = Html.fromHtml(gameDetails?.tutorial ?: "NA", Html.FROM_HTML_MODE_LEGACY)
            numberPlayer.text = if (gameDetails?.pemainMin == gameDetails?.pemainMax) {
                "${gameDetails?.pemainMax}"
            } else {
                "${gameDetails?.pemainMin}-${gameDetails?.pemainMax}"
            }

            if (gameDetails?.step.isNullOrEmpty()) {
                caraMembuatIcon.visibility = View.GONE
                caraMembuatTitle.visibility = View.GONE
                caraMembuatContent.visibility = View.GONE
                bahanProperti.visibility = View.GONE
                tvBahan.visibility = View.GONE
                tvCara.visibility = View.GONE
            } else {
                bahanProperti.text = Html.fromHtml(gameDetails?.bahanProperti, Html.FROM_HTML_MODE_COMPACT)
                caraMembuatContent.text = Html.fromHtml(gameDetails?.step, Html.FROM_HTML_MODE_COMPACT)
            }

            val fullText = gameDetails?.deskripsi ?: "NA"
            val maxLength = 130
            val truncatedText = if (fullText.length > maxLength) {
                fullText.substring(0, maxLength) + "..."
            } else {
                fullText
            }
            deskripsiContent.text = truncatedText
            bacaSelengkapnya.text = if (fullText.length > maxLength) "baca selengkapnya" else ""

            bacaSelengkapnya.setOnClickListener {
                if (bacaSelengkapnya.text == "baca selengkapnya") {
                    deskripsiContent.text = fullText
                    bacaSelengkapnya.text = "kecilkan"
                } else {
                    deskripsiContent.text = truncatedText
                    bacaSelengkapnya.text = "baca selengkapnya"
                }
            }
        }

        // Fetch bookmark and like status
        fetchSocialInteractionStatus()
    }

    private fun fetchSocialInteractionStatus() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val documents = db.collection("social_interaction")
                        .whereEqualTo("user_ID", userId)
                        .whereEqualTo("game_ID", gameDetails?.game_id)
                        .get()
                        .await()

                    withContext(Dispatchers.Main) {
                        if (documents.isEmpty) {
                            binding.bookmarkButtonTutorial.setBackgroundResource(R.drawable.ic_unbookmark)
                            binding.likeButtonTutorial.setBackgroundResource(R.drawable.ic_unlike)
                        } else {
                            val doc = documents.first()
                            val bookmarkStatus = doc.getBoolean("bookmark_status") ?: false
                            binding.bookmarkButtonTutorial.setBackgroundResource(
                                if (bookmarkStatus) R.drawable.ic_bookmark else R.drawable.ic_unbookmark
                            )
                            binding.bookmarkButtonTutorial.tag = bookmarkStatus

                            val likeStatus = doc.getBoolean("like_status") ?: false
                            binding.likeButtonTutorial.setBackgroundResource(
                                if (likeStatus) R.drawable.ic_like else R.drawable.ic_unlike
                            )
                            binding.likeButtonTutorial.tag = likeStatus
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Firestore", "Error fetching social interaction status", e)
                }
            }
        }
    }

    private fun setUpListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bookmarkButtonTutorial.setOnClickListener {
            val currentBookmarkStatus = binding.bookmarkButtonTutorial.tag as? Boolean ?: false
            val newBookmarkStatus = !currentBookmarkStatus
            gameDetails?.let { game -> updateBookmarkStatus(game, newBookmarkStatus) }
            binding.bookmarkButtonTutorial.setBackgroundResource(
                if (newBookmarkStatus) R.drawable.ic_bookmark else R.drawable.ic_unbookmark
            )
            binding.bookmarkButtonTutorial.tag = newBookmarkStatus
        }

        binding.likeButtonTutorial.setOnClickListener {
            val currentLikeStatus = binding.likeButtonTutorial.tag as? Boolean ?: false
            val newLikeStatus = !currentLikeStatus
            gameDetails?.let { game -> updateLikeStatus(game, newLikeStatus) }
            binding.likeButtonTutorial.setBackgroundResource(
                if (newLikeStatus) R.drawable.ic_like else R.drawable.ic_unlike
            )
            binding.likeButtonTutorial.tag = newLikeStatus
        }

        binding.shareButtonTutorial.setOnClickListener {
            gameDetails?.let { game -> createDynamicLink(game) }
        }

        binding.bottomSheet.mainkanButtonTutorial.setOnClickListener {
            updateVisitDate()
            findNavController().navigate(R.id.action_TutorialFragment_to_FeedbackFragment)
        }
    }



    private fun createDynamicLink(game: Games) {
        val link = Uri.parse("https://playsnapgame.page.link/${game.game_id}")
        showDynamicLinkDialog(link.toString())
    }

    private fun showDynamicLinkDialog(link: String) {
        ShareFragment(link).show(parentFragmentManager, "ShareFragment")
    }


    private fun updateBookmarkStatus(game: Games, bookmarkStatus: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val documents = db.collection("social_interaction")
                        .whereEqualTo("user_ID", userId)
                        .whereEqualTo("game_ID", game.game_id)
                        .get()
                        .await()
                    if (documents.isEmpty) {
                        val socialInteraction = hashMapOf(
                            "user_ID" to userId,
                            "game_ID" to game.game_id,
                            "date_visit" to null,
                            "like_status" to false,
                            "bookmark_status" to bookmarkStatus,
                            "rating" to 0
                        )
                        db.collection("social_interaction").add(socialInteraction).await()
                    } else {
                        for (document in documents) {
                            val dateVisit = document.getString("date_visit")
                            val likeStatus = document.getBoolean("like_status")
                            if (!bookmarkStatus && !likeStatus!! && (dateVisit == "null" || dateVisit == null)) {
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .delete()
                                    .await()
                            } else {
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .update("bookmark_status", bookmarkStatus)
                                    .await()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Firestore", "Error updating bookmark status", e)
                }
            }
        }
    }

    private fun updateLikeStatus(game: Games, likeStatus: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val documents = db.collection("social_interaction")
                        .whereEqualTo("user_ID", userId)
                        .whereEqualTo("game_ID", game.game_id)
                        .get()
                        .await()

                    if (documents.isEmpty) {
                        val socialInteraction = hashMapOf(
                            "user_ID" to userId,
                            "game_ID" to game.game_id,
                            "date_visit" to null,
                            "like_status" to likeStatus,
                            "bookmark_status" to false,
                            "rating" to 0
                        )
                        db.collection("social_interaction").add(socialInteraction).await()
                    } else {
                        for (document in documents) {
                            val dateVisit = document.getString("date_visit")
                            val bookmarkStatus = document.getBoolean("bookmark_status")
                            if (!bookmarkStatus!! && !likeStatus && (dateVisit == "null" || dateVisit == null)) {
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .delete()
                                    .await()
                            } else {
                                db.collection("social_interaction")
                                    .document(document.id)
                                    .update("like_status", likeStatus)
                                    .await()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Firestore", "Error updating like status", e)
                }
            }
        }
    }

    private fun updateVisitDate() {
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(currentTime))
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documents = db.collection("social_interaction")
                    .whereEqualTo("user_ID", userId)
                    .whereEqualTo("game_ID", gameDetails?.game_id)
                    .get()
                    .await()

                if (documents.isEmpty) {
                    val socialInteraction = hashMapOf(
                        "user_ID" to userId,
                        "game_ID" to gameDetails?.game_id,
                        "date_visit" to formattedDate,
                        "like_status" to false,
                        "bookmark_status" to false,
                        "rating" to 0
                    )
                    db.collection("social_interaction").add(socialInteraction).await()
                } else {
                    val document = documents.first()
                    db.collection("social_interaction")
                        .document(document.id)
                        .update("date_visit", formattedDate)
                        .await()
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error updating visit date", e)
            }
        }
    }

//    private fun playVideoFromGoogleDrive() {
//        val videoView: VideoView = binding.bottomSheet.videoTutorialContent
//        val videoUrl = gameDetails?.linkVideo ?: return
//
//        val uri = Uri.parse(videoUrl)
//        videoView.setVideoURI(uri)
//
//        videoView.setOnPreparedListener { mediaPlayer ->
//            videoView.start()
//            binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24)
//            binding.bottomSheet.seekBar.max = mediaPlayer.duration
//            handler.post(updateSeekBarRunnable)
//        }
//
//        videoView.setOnErrorListener { _, _, _ ->
//            Log.e("TutorialFragment", "Error playing video")
//            true
//        }
//
//        videoView.setOnClickListener {
//            toggleControlOverlayVisibility()
//        }
//
//        binding.bottomSheet.playPauseButton.setOnClickListener {
//            if (videoView.isPlaying) {
//                videoView.pause()
//                binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow_24)
//            } else {
//                videoView.start()
//                binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24)
//            }
//        }
//
//        binding.bottomSheet.forwardButton.setOnClickListener {
//            videoView.seekTo(videoView.currentPosition + 10000)
//        }
//
//        binding.bottomSheet.backwardButton.setOnClickListener {
//            videoView.seekTo(videoView.currentPosition - 10000)
//        }
//
//        binding.bottomSheet.fullscreenButton.setOnClickListener {
//            isFullscreen = !isFullscreen
//            toggleFullscreen(videoView)
//        }
//
//        binding.bottomSheet.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (fromUser) videoView.seekTo(progress)
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                videoView.pause()
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                videoView.start()
//                binding.bottomSheet.playPauseButton.setBackgroundResource(R.drawable.baseline_pause_24)
//            }
//        })
//    }
//
//    private fun updateSeekBar() {
//        val videoView = binding.bottomSheet.videoTutorialContent
//        if (videoView.isPlaying) {
//            binding.bottomSheet.seekBar.progress = videoView.currentPosition
//        }
//    }
//
//    private fun toggleControlOverlayVisibility() {
//        binding.bottomSheet.controlOverlay.visibility =
//            if (binding.bottomSheet.controlOverlay.visibility == View.GONE) View.VISIBLE else View.GONE
//    }
//
//    private fun toggleFullscreen(videoView: VideoView) {
//        if (isFullscreen) {
//            binding.bottomSheet.fullscreenButton.setBackgroundResource(R.drawable.baseline_fullscreen_exit_24)
//            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//            hideOtherLayoutElements()
//            adjustVideoViewLayout(true)
//        } else {
//            binding.bottomSheet.fullscreenButton.setBackgroundResource(R.drawable.baseline_fullscreen_24)
//            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//            restoreOtherLayoutElements()
//            adjustVideoViewLayout(false)
//
//        }
//    }
//
//    private fun adjustVideoViewLayout(isFullscreen: Boolean) {
//        val layoutParams = binding.bottomSheet.videoTutorialContent.layoutParams
//        if (isFullscreen) {
//            layoutParams.width = resources.getDimensionPixelSize(R.dimen.video_width_full)
//            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
//        } else {
//            layoutParams.width = resources.getDimensionPixelSize(R.dimen.video_width)
//            layoutParams.height = resources.getDimensionPixelSize(R.dimen.video_height)
//        }
//        binding.bottomSheet.videoTutorialContent.layoutParams = layoutParams
//    }
//
//    private fun hideOtherLayoutElements() {
//        binding.bottomSheet.apply {
//            dragIcon.visibility = View.GONE
//            videoTutorialText.visibility = View.GONE
//            gameDescHeaderWrapped.visibility = View.GONE
//            deskripsiSection.visibility = View.GONE
//            alatBermainSection.visibility = View.GONE
//            titleTutorial.visibility = View.GONE
//            langkahBermainIcon.visibility = View.GONE
//            langkahBermainTitle.visibility = View.GONE
//            langkahBermainContent.visibility = View.GONE
//            mainkanButtonTutorial.visibility = View.GONE
//            tvBahan.visibility = View.GONE
//            tvCara.visibility = View.GONE
//            caraMembuatIcon.visibility = View.GONE
//            caraMembuatTitle.visibility = View.GONE
//            caraMembuatContent.visibility = View.GONE
//            bahanProperti.visibility = View.GONE
//        }
//    }
//
//    private fun restoreOtherLayoutElements() {
//        binding.bottomSheet.apply {
//            dragIcon.visibility = View.VISIBLE
//            videoTutorialText.visibility = View.VISIBLE
//            gameDescHeaderWrapped.visibility = View.VISIBLE
//            deskripsiSection.visibility = View.VISIBLE
//            titleTutorial.visibility = View.VISIBLE
//            langkahBermainIcon.visibility = View.VISIBLE
//            langkahBermainTitle.visibility = View.VISIBLE
//            langkahBermainContent.visibility = View.VISIBLE
//            mainkanButtonTutorial.visibility = View.VISIBLE
//            if (gameDetails?.bahanProperti?.isNotEmpty() == true) {
//                tvBahan.visibility = View.VISIBLE
//                tvCara.visibility = View.VISIBLE
//                caraMembuatIcon.visibility = View.VISIBLE
//                caraMembuatTitle.visibility = View.VISIBLE
//                caraMembuatContent.visibility = View.VISIBLE
//                bahanProperti.visibility = View.VISIBLE
//            }
//            if (gameDetails?.properti?.isNotEmpty() == true) {
//                alatBermainSection.visibility = View.VISIBLE
//            }
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
//        handler.removeCallbacks(updateSeekBarRunnable)
//        if (binding.bottomSheet.videoTutorialContent.isPlaying) {
//            binding.bottomSheet.videoTutorialContent.stopPlayback()
//        }
//        if (customView != null) {
//            customWebChromeClient.onHideCustomView() // Use the stored reference
//        } else if (webView.canGoBack()) {
//            webView.goBack()
//        } else {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
        _binding = null
    }
}