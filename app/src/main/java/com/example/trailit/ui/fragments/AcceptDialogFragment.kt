package com.example.trailit.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.trailit.R
import com.example.trailit.data.entitites.Run
import com.example.trailit.databinding.AcceptDialogBinding
import com.example.trailit.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.accept_dialog.*

@AndroidEntryPoint
class AcceptDialogFragment : DialogFragment() {

    private lateinit var binding: AcceptDialogBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = AcceptDialogBinding.inflate(LayoutInflater.from(context))

        val builder = AlertDialog.Builder(requireActivity())
        binding.textDialog.text = getString(R.string.delete_all)
        binding.btnYes.setOnClickListener { viewModel.deleteAllRuns()
            dismiss()
        }
        binding.btnNo.setOnClickListener {
            dismiss()
        }
        builder.setView(binding.root)
        return builder.show()
    }
}            