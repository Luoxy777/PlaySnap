package com.example.playsnapui.ui.accountSetting

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.playsnapui.R

class AccountSettingFragment : Fragment() {

    companion object {
        fun newInstance() = AccountSettingFragment()
    }

    private lateinit var viewModel: AccountSettingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account_setting, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AccountSettingViewModel::class.java)
        // TODO: Use the ViewModel
    }

}