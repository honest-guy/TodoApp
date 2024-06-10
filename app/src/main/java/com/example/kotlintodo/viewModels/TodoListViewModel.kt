package com.example.kotlintodo.viewModels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlintodo.views.MainActivity
import com.example.kotlintodo.model.TodoItem
import com.example.kotlintodo.utils.RetrofitInstance
import com.example.kotlintodo.utils.TodoResponse
import io.realm.Realm
import io.realm.kotlin.executeTransactionAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TodoListViewModel : ViewModel() {
    private val realm: Realm = Realm.getDefaultInstance()

    val tasks: MutableLiveData<List<TodoItem>> = MutableLiveData()
    val toastMessage: MutableLiveData<String> = MutableLiveData()
    val taskTitle = MutableLiveData<String>()


    fun loadTasks() {
        // Load data from API
        RetrofitInstance.api.getTodoItems().enqueue(object : Callback<TodoResponse> {
            override fun onResponse(call: Call<TodoResponse>, response: Response<TodoResponse>) {
                if (response.isSuccessful) {
                    val apiTasks = response.body()?.todos
                    apiTasks?.let {
                        saveTasksToRealm(it)
                    }
                } else {
                    toastMessage.postValue("Failed to load tasks: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TodoResponse>, t: Throwable) {
                toastMessage.postValue("No network")
                Log.e("Failed to load tasks", t.toString())
            }
        })

        // Load data from Realm
        loadTasksFromRealm()
    }

    private fun saveTasksToRealm(tasks: List<TodoItem>) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransactionAsync({ transactionRealm ->
            transactionRealm.insertOrUpdate(tasks)
        }, {
            // Success
            loadTasksFromRealm()  // Refresh the local data
        }, { error ->
            toastMessage.postValue("Failed to save tasks: ${error.message}")
        })
    }

    private fun loadTasksFromRealm() {
        val realm = Realm.getDefaultInstance()
        viewModelScope.launch(Dispatchers.Main) {
            val results = realm.where(TodoItem::class.java).findAll()
            withContext(Dispatchers.Main) {
                tasks.value = realm.copyFromRealm(results)
            }
        }
    }


    private fun addTask(task: TodoItem) {
        val realm = Realm.getDefaultInstance()
        viewModelScope.launch(Dispatchers.Main) {
            try {
                realm.executeTransaction { transactionRealm ->
                    val currentIdNum: Number? = transactionRealm.where(TodoItem::class.java).max("id")
                    val nextId: Long = (currentIdNum?.toLong() ?: 0L) + 1L
                    val newTask = transactionRealm.createObject(TodoItem::class.java, nextId)
                    newTask.todo = task.todo
                    newTask.completed = task.completed
                }
                withContext(Dispatchers.Main) {
                    loadTasksFromRealm() // Refresh the tasks list
                    toastMessage.postValue("Task added successfully!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    toastMessage.postValue("Failed to add task: ${e.message}")
                    Log.e("Failed to add task:", e.toString())
                }
            } finally {
                realm.close()
            }
        }
    }

    fun onSaveTaskButtonClick(context: Context) {
        val title = taskTitle.value
        if (!title.isNullOrEmpty()) {
            val newTask = TodoItem().apply {
                this.todo = title // Set the title
                this.completed = false // Set the completed status
            } // Create a new TodoItem object
            addTask(newTask)
            toastMessage.postValue("Task added successfully!")
            // Navigate back to MainActivity
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        } else {
            toastMessage.postValue("Title cannot be empty")
        }
    }


    fun updateTask(taskId: Long, title: String, isCompleted: Boolean) {
        val realm = Realm.getDefaultInstance()
        viewModelScope.launch(Dispatchers.Main) {
            try {
                realm.executeTransactionAwait { transactionRealm ->
                    val task = transactionRealm.where(TodoItem::class.java).equalTo("id", taskId).findFirst()
                    task?.todo = title
                    task?.completed = isCompleted
                }
                loadTasksFromRealm() // Update the task list after updating
                toastMessage.postValue("Task updated successfully!")
            } catch (e: Exception) {
                toastMessage.postValue("Failed to update task: ${e.message}")
            }
        }
    }




    fun deleteTask(taskId: Long) {
        val realm = Realm.getDefaultInstance()
        viewModelScope.launch(Dispatchers.Main) {
            try {
                realm.executeTransactionAwait { transactionRealm ->
                    val task = transactionRealm.where(TodoItem::class.java).equalTo("id", taskId).findFirst()
                    task?.deleteFromRealm()
                }
                loadTasks() // Update the task list after deleting
                toastMessage.postValue("Task deleted!")
            } catch (e: Exception) {
                toastMessage.postValue("Failed to delete task: ${e.message}")
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        realm.close() // Close Realm when ViewModel is cleared
    }
}
