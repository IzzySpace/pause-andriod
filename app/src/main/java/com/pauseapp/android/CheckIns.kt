package com.pauseapp.android

import android.content.SharedPreferences
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

internal val resetActions = listOf(
    "Get up and take a short walk.",
    "Put your phone down and stretch.",
    "Get a glass of water.",
)

private val triggers = listOf("Boredom", "Stress", "Late-night scrolling", "Something else")
private val responses = listOf("Took a break", "Moved around", "Changed what I was doing", "Still figuring it out")
private const val MAX_CHECK_INS = 30

internal data class CheckIn(val time: Long, val trigger: String, val response: String)

internal class CheckInStore(private val preferences: SharedPreferences) {
    fun resetAction(): String = preferences.getString("reset_action", null)
        ?.takeIf { it in resetActions } ?: resetActions.first()

    fun saveResetAction(action: String) {
        preferences.edit { putString("reset_action", action) }
    }

    fun read(): List<CheckIn> {
        val records = JSONArray(preferences.getString("entries", null) ?: "[]")
        return List(records.length()) { index ->
            val item = records.getJSONObject(index)
            CheckIn(item.getLong("time"), item.getString("trigger"), item.getString("response"))
        }
    }

    fun add(trigger: String, response: String): List<CheckIn> {
        val entries = (listOf(CheckIn(System.currentTimeMillis(), trigger, response)) + read())
            .take(MAX_CHECK_INS)
        val records = JSONArray()
        entries.forEach { entry ->
            records.put(JSONObject().apply {
                put("time", entry.time)
                put("trigger", entry.trigger)
                put("response", entry.response)
            })
        }
        preferences.edit { putString("entries", records.toString()) }
        return entries
    }

    fun clear() {
        preferences.edit { remove("entries") }
    }
}

@Composable
internal fun CheckInScreen(store: CheckInStore) {
    var trigger by rememberSaveable { mutableStateOf("") }
    var response by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var entries by remember(store) { mutableStateOf(store.read()) }
    val context = LocalContext.current

    ScreenTitle("Private check-in")
    Text("Pause stores your last $MAX_CHECK_INS check-ins on this phone.")
    CheckInChoices("What brought it on?", triggers, trigger) {
        trigger = it
        message = ""
    }
    CheckInChoices("What did you do?", responses, response) {
        response = it
        message = ""
    }
    Button(
        enabled = trigger.isNotEmpty() && response.isNotEmpty(),
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            entries = store.add(trigger, response)
            trigger = ""
            response = ""
            message = "Check-in saved."
        },
    ) { Text("Save check-in") }
    if (message.isNotEmpty()) Text(message)

    if (entries.isNotEmpty()) {
        Text("Recent check-ins", style = MaterialTheme.typography.titleLarge)
        val dateFormat = DateFormat.getMediumDateFormat(context)
        val timeFormat = DateFormat.getTimeFormat(context)
        entries.forEach { entry ->
            val date = Date(entry.time)
            Surface(
                color = Lavender,
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(entry.trigger, style = MaterialTheme.typography.titleMedium)
                    Text(entry.response)
                    Text("${dateFormat.format(date)} · ${timeFormat.format(date)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        TextButton(onClick = { confirmClear = true }) { Text("Clear check-in history") }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear check-in history?") },
            text = { Text("All saved check-ins will be deleted from this phone.") },
            confirmButton = {
                TextButton(onClick = {
                    store.clear()
                    entries = emptyList()
                    message = "Check-in history cleared."
                    confirmClear = false
                }) { Text("Clear history") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun CheckInChoices(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Surface(color = Lavender, contentColor = Ink, shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    FilterChip(
                        selected = selected == option,
                        onClick = { onSelect(option) },
                        label = { Text(option) },
                    )
                }
            }
        }
    }
}
