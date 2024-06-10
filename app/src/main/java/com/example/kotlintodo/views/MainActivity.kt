package com.example.kotlintodo.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlintodo.adapters.TodoItemAdapter
import com.example.kotlintodo.model.TodoItem
import com.example.kotlintodo.R
import com.example.kotlintodo.viewModels.TodoListViewModel
import com.example.kotlintodo.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.annotations.RealmModule

@RealmModule(classes = [TodoItem::class])
class MyRealmModule

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: TodoListViewModel

    private lateinit var binding: ActivityMainBinding

    private lateinit var addTaskButton: FloatingActionButton

    private lateinit var adapter: TodoItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .name("myrealm.realm")
            .schemaVersion(2) // Increment as necessary
            .modules(MyRealmModule())
            .deleteRealmIfMigrationNeeded()
            .allowWritesOnUiThread(true) // Preferably false to avoid UI thread operations
            .build()

        Realm.setDefaultConfiguration(config)

        viewModel = ViewModelProvider(this).get(TodoListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        adapter = TodoItemAdapter((emptyList()), viewModel, this)
        binding.todoList.layoutManager = LinearLayoutManager(this)
        binding.todoList.adapter = adapter

       /* viewModel.tasks.observe(this, Observer { taskList ->
            adapter.updateTasks(taskList)
        })*/

        viewModel.tasks.observe(this, Observer { tasks ->
            tasks?.let {
                adapter.updateTasks(it)
            }
        })

        viewModel.toastMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })

        val addTaskButton: FloatingActionButton = findViewById(R.id.add_task_fab)
        addTaskButton.setOnClickListener {
            val dialog = AddTaskDialogFragment()
            dialog.show(supportFragmentManager, "AddTaskDialogFragment")
        }

        viewModel.loadTasks()
    }






}