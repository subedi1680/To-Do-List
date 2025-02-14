package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskAdapter(private val database: AppDatabase) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private var tasks: MutableList<Task> = mutableListOf()

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskTitle: TextView = view.findViewById(R.id.taskTitle)  // Ensure ID matches XML
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton) // Changed to ImageButton
        val taskCheckbox: CheckBox = view.findViewById(R.id.taskCheckbox)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTitle.text = task.title

        // Hide delete button initially
        holder.deleteButton.visibility = View.GONE

        // Handle checkbox state and update delete button visibility
        holder.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
            holder.deleteButton.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Ensure checkbox does not persist checked state when scrolling
        holder.taskCheckbox.isChecked = false

        // Delete button functionality
        holder.deleteButton.setOnClickListener {
            deleteTask(task, position)
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        val diffCallback = TaskDiffCallback(tasks, newTasks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tasks.clear()
        tasks.addAll(newTasks)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun deleteTask(task: Task, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            database.taskDao().deleteTask(task)

            // Remove item from list before updating UI
            val updatedTasks = tasks.toMutableList()
            updatedTasks.removeAt(position)

            withContext(Dispatchers.Main) {
                updateTasks(updatedTasks)  // Update RecyclerView safely
            }
        }
    }
}
