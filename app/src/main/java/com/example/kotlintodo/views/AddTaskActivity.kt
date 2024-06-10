package com.example.kotlintodo.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.kotlintodo.R
import com.example.kotlintodo.viewModels.TodoListViewModel
import com.example.kotlintodo.databinding.ActivityAddTaskBinding
import io.realm.Realm

class AddTaskDialogFragment : DialogFragment() {
    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var viewModel: TodoListViewModel
    private lateinit var realm: Realm



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Set up data binding
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_add_task, container, false)
        viewModel = ViewModelProvider(this).get(TodoListViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.toastMessage.observe(this, Observer { message ->
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
}


    }
