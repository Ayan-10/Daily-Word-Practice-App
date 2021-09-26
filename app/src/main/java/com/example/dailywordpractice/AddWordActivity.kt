package com.example.dailywordpractice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity for entering a word.
 */

class AddWordActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_new_word)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val editWordView = findViewById<EditText>(R.id.edit_word)
        val editDefinitionView = findViewById<EditText>(R.id.edit_definition)

        val button = findViewById<Button>(R.id.button_save)
        button.setOnClickListener {
            val replyIntent = Intent()
            if (TextUtils.isEmpty(editWordView.text) || TextUtils.isEmpty(editDefinitionView.text)) {
                Toast.makeText(this, "Fields can't be empty", Toast.LENGTH_LONG).show()
            } else {
                val word = editWordView.text.toString()
                val definition = editDefinitionView.text.toString()
                replyIntent.putExtra(EXTRA_REPLY, word)
                replyIntent.putExtra(EXTRA_REPLY1, definition)

                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY"
        const val EXTRA_REPLY1 = "com.example.android.wordlistsql.REPLY1"
    }
}
