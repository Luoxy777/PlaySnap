package com.example.playsnapui.ui.home

import SharedData
import SharedData.deepLinkid
import kotlin.random.Random
import SharedData.userProfile
import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.playsnapui.data.Games
import com.example.playsnapui.R
import com.example.playsnapui.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerViewPopular: RecyclerView
    private lateinit var recyclerViewForYou: RecyclerView
    private lateinit var homeAdapterPopular: HomeAdapterPopular
    private lateinit var homeAdapterForYou: HomeAdapterForYou
    private lateinit var db: FirebaseFirestore
    private lateinit var gamesListPopular: ArrayList<Games>
    private lateinit var gamesListForYou: ArrayList<Games>
    private lateinit var viewModel: HomeViewModel
    private lateinit var tempListPopular : ArrayList<Games>
    private lateinit var tempListForYou : ArrayList<Games>
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


        userProfile?.let {
            binding.tvTitleName.text = it.username ?: "N/A"
        }
        Log.d("HomeFragment", "User Profile: ${userProfile?.username ?: "N/A"}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        Log.d("From Home Fragment", "$deepLinkid ????")
        if(deepLinkid != ""){
            fetchGameDetailsFromFirestore(deepLinkid!!)
        }

        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.visibility = View.VISIBLE

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        tempListForYou = arrayListOf()
        tempListPopular = arrayListOf()
        db = FirebaseFirestore.getInstance()
        gamesListPopular = arrayListOf()
        gamesListForYou = arrayListOf()

        setupRecyclerViews()
        loadGamesFromFirestore()
        setupListeners()
    }
    private fun fetchGameDetailsFromFirestore(gameId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("games").document(gameId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val gameDetails = document.toObject(Games::class.java)
                    Log.d("GameDetails", "Fetched game: $gameDetails")

                    // Store game details
                    SharedData.gameDetails = gameDetails

                    // Navigate to TutorialFragment
                    openTutorialFragment()
                } else {
                    Log.e("GameDetails", "Game not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("GameDetails", "Error fetching game details", e)
            }
    }

    private fun openTutorialFragment() {
        findNavController().navigate(R.id.action_PopularFragment_to_TutorialFragment)
    }


    private fun setupRecyclerViews() {
        homeAdapterPopular = HomeAdapterPopular(gamesListPopular, childFragmentManager)
        recyclerViewPopular = binding.recentRecyclerPopgame
        recyclerViewPopular.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerViewPopular.setHasFixedSize(true)
        recyclerViewPopular.adapter = homeAdapterPopular

        homeAdapterForYou = HomeAdapterForYou(gamesListForYou, childFragmentManager)
        recyclerViewForYou = binding.recentRecyclerForyou
        recyclerViewForYou.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewForYou.setHasFixedSize(true)
        recyclerViewForYou.isNestedScrollingEnabled = false
        recyclerViewForYou.adapter = homeAdapterForYou

        binding.recentRecyclerForyou.visibility = View.VISIBLE
        binding.recentRecyclerPopgame.visibility = View.VISIBLE
    }

    private fun setRecyclerViewHeightBasedOnItems(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter ?: return

        val itemCount = adapter.itemCount
        if (itemCount > 0) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

            val firstItemView = layoutManager.findViewByPosition(0) ?: return
            val itemHeight = firstItemView.measuredHeight

            // Calculate total height (items * item height)
            val totalHeight = itemHeight * itemCount + 425

            // Set the new height
            val layoutParams = recyclerView.layoutParams
            layoutParams.height = totalHeight
            recyclerView.layoutParams = layoutParams
        }
    }

    private fun setupListeners() {
        binding.btnFilterGame.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_filterFragment)
        }

        binding.btnScanObject.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_SnapFragment)
        }

        binding.btnTypeObject.setOnClickListener{
            SharedData.detectedObjects = emptyList()
            findNavController().navigate(R.id.action_HomeFragment_to_ObjectFragment)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        binding.helpButton.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_HelpFragment)
        }

        binding.etSearchGame.setOnClickListener{
            findNavController().navigate(R.id.action_HomeFragment_to_SearchByTitleFragment)
        }
    }

    private fun refreshData() {
        // Tambahkan logika refresh, misalnya memuat ulang data dari server atau database lokal
        loadGamesFromFirestore()
        Handler(Looper.getMainLooper()).postDelayed({
            swipeRefreshLayout.isRefreshing = false // Hentikan animasi refresh
        }, 2000) // Simulasi delay 2 detik
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadGamesFromFirestore() {
        val batasGames = 10

        db.collection("games").addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                return@addSnapshotListener
            }

            tempListPopular.clear()
            tempListForYou.clear()

            value?.documentChanges?.forEach { dc ->
                if (dc.type == DocumentChange.Type.ADDED) {
                    val game = dc.document.toObject(Games::class.java)
                    tempListPopular.add(game)
                    tempListForYou.add(game)
                    Log.d("Firestore", "Game added: ${game.namaPermainan}")
                }
            }

            gamesListForYou.clear()
            gamesListForYou.addAll(tempListForYou.shuffled().take(batasGames))
            Log.d("For you", "${gamesListForYou.size}")

            gamesListPopular.clear()
            gamesListPopular.addAll(tempListPopular.sortedByDescending { it.rating }.take(batasGames))
            Log.d("Popular", "${gamesListPopular.size}")


            if (::homeAdapterPopular.isInitialized && ::homeAdapterForYou.isInitialized) {
                Log.d("Firestore", "Notifying adapters...")
                requireActivity().runOnUiThread {
                    homeAdapterPopular.notifyDataSetChanged()
                    homeAdapterForYou.notifyDataSetChanged()
                }
            } else {
                Log.e("Firestore", "Adapter belum diinisialisasi!")
            }


            // Pastikan perubahan terjadi dalam UI thread
            if (_binding != null) {
                binding!!.recentRecyclerForyou.post {
                    setRecyclerViewHeightBasedOnItems(binding!!.recentRecyclerForyou)
                }
            }

            binding.swipeRefreshLayout.isRefreshing = false // Berhenti refresh setelah data dimuat

        }
    }

    override fun onResume() {
        super.onResume()
        userProfile?.let {
            binding.tvTitleName.text = it.username ?: "N/A"
        }
        loadGamesFromFirestore() // Pastikan data di-refresh saat kembali ke HomeFragment

    }


}
