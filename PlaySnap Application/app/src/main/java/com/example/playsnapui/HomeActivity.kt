package com.example.playsnapui

import SharedData.deepLinkid
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playsnapui.databinding.ActivityHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deepLinkid = intent.getStringExtra("gameId").orEmpty()

        // Get NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController ?: return

        binding.bottomNavigationView.setupWithNavController(navController)

        // Hide Bottom Navigation Bar in specific fragments
        val hiddenFragments = setOf(
            R.id.snapFragment, R.id.swipeGalleryFragment, R.id.scrollGalleryFragment, R.id.filterFragment, R.id.ObjectFragment,
            R.id.recommendGameFragment, R.id.tutorialFragment, R.id.historyFragment, R.id.reportFragment, R.id.reportSuccessFragment,
            R.id.editProfileFragment, R.id.accountSettingFragment, R.id.editPasswordFragment, R.id.feedbackFragment, R.id.helpFragmentReqSuccess,
            R.id.DeleteAccountFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility = if (destination.id in hiddenFragments) View.GONE else View.VISIBLE
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> navController.navigate(R.id.homeFragment)
                R.id.nav_profile -> navController.navigate(R.id.profileFragment)
                R.id.nav_bookmark -> navController.navigate(R.id.bookmarkFragment)
                R.id.nav_like -> navController.navigate(R.id.likeFragment)
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }
}
