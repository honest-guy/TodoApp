package com.example.kotlintodo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlintodo.model.TodoItem
import com.example.kotlintodo.R
import com.example.kotlintodo.viewModels.TodoListViewModel

import com.example.kotlintodo.databinding.DialogLayoutBinding
import com.example.kotlintodo.databinding.TodoItemLayoutBinding


class TodoItemAdapter(
    private var tasks: List<TodoItem>,
    private val viewModel: TodoListViewModel,
    private val context: Context
) : RecyclerView.Adapter<TodoItemAdapter.TodoViewHolder>() {

    inner class TodoViewHolder(private val binding: TodoItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TodoItem, viewModel: TodoListViewModel) {
            binding.task = task
            binding.viewModel = viewModel
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                updateDialog(task)
            }
        }

         private fun updateDialog(task: TodoItem) {
             val builder = AlertDialog.Builder(context)
             val inflater = LayoutInflater.from(context)
             val dialogBinding = DataBindingUtil.inflate<DialogLayoutBinding>(
                 inflater, R.layout.dialog_layout, null, false
             )

             val editTextTitle = dialogBinding.root.findViewById<EditText>(R.id.editTextTitle)
             val checkBoxCompleted = dialogBinding.root.findViewById<CheckBox>(R.id.checkBoxCompleted)

             editTextTitle.setText(task.todo)
             checkBoxCompleted.isChecked = task.completed

             builder.setView(dialogBinding.root)
             builder.setTitle("Update Task")

             builder.setPositiveButton("Update") { dialog, _ ->
                 // User clicked Update button
                 val newTitle = editTextTitle.text.toString()
                 val newIsCompleted = checkBoxCompleted.isChecked

                 // Update task using ViewModel
                 viewModel.updateTask(task.id, newTitle, newIsCompleted)
             }

             builder.setNegativeButton("Cancel") { dialog, _ ->
                 // User cancelled the dialog
             }

             // Create the AlertDialog
             val dialog = builder.create()

             // Show the dialog
             dialog.show()
         }
     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TodoItemLayoutBinding.inflate(inflater, parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task, viewModel)
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TodoItem>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

}
