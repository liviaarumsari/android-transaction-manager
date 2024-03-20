package com.example.abe.ui.add_transaction

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.abe.R
import com.example.abe.databinding.ActivityFormTransactionBinding

class FormTransaction : AppCompatActivity() {
    private lateinit var binding: ActivityFormTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFormTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true);

        val categories = resources.getStringArray(R.array.Categories)
        val adapterItems = ArrayAdapter<String>(this, R.layout.list_item, categories)
        binding.categoryAutocomplete.setAdapter(adapterItems)
        binding.categoryAutocomplete.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val item = parent.getItemAtPosition(position).toString()
            }
        titleFocusListener()
        amountFocusListener()
        categoryFocusListener()
        locationFocusListener()
    }

    private fun titleFocusListener() {
        binding.formTitleEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.formTitleContainer.helperText =
                    if (binding.formTitleEditText.text.toString()
                            .isEmpty()
                    ) "Title is required" else null;
            }
        }
    }

    private fun amountFocusListener() {
        binding.formAmountEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.formAmountContainer.helperText =
                    if (binding.formAmountEditText.text.toString()
                            .isEmpty()
                    ) "Amount is required" else null;
            }
        }
    }

    private fun categoryFocusListener() {
        binding.categoryAutocomplete.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.formCategoryContainer.helperText =
                    if (binding.categoryAutocomplete.text.toString()
                            .isEmpty()
                    ) "Category is required" else null;
            }
        }
    }

    private fun locationFocusListener() {
        binding.formLocationEditText.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.formLocationContainer.helperText =
                    if (binding.formLocationEditText.text.toString()
                            .isEmpty()
                    ) "Location is required" else null;
            }
        }
    }
}