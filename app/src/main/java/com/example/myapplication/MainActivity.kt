package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize database
        database = AppDatabase.getDatabase(this)

        // Initialize RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        taskAdapter = TaskAdapter(database)
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load tasks from database
        loadTasks()

        // Set up Add Task button
        findViewById<Button>(R.id.addButton).setOnClickListener {
            addTask()
        }

        // Set up About button
        findViewById<Button>(R.id.aboutButton).setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadTasks() {
        lifecycleScope.launch {
            val tasks = database.taskDao().getAllTasks()
            taskAdapter.updateTasks(tasks)
        }
    }

    private fun addTask() {
        val taskInput = findViewById<EditText>(R.id.taskInput)
        val taskText = taskInput.text.toString().trim() // Remove extra spaces

        if (taskText.isNotEmpty()) {
            val task = Task(title = taskText)
            lifecycleScope.launch {
                database.taskDao().insertTask(task)
                loadTasks() // Refresh list after adding
            }
            taskInput.text.clear()
        } else {
            taskInput.error = "Task cannot be empty" // Show error if empty
        }
    }
}
