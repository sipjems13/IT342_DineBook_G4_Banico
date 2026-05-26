package com.dinebook.mobile.booking

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dinebook.mobile.R
import com.dinebook.mobile.restaurant.Restaurant
import com.dinebook.mobile.shared.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DiningRequestActivity : AppCompatActivity() {

    private lateinit var token: String
    private var restaurantId: Long = 0L
    private var selectedDate = ""
    private var selectedTime = ""

    private lateinit var tvMessage: TextView
    private lateinit var tvRestaurantName: TextView
    private lateinit var tvRestaurantLocation: TextView
    private lateinit var tvRestaurantCuisine: TextView
    private lateinit var btnDate: Button
    private lateinit var btnTime: Button
    private lateinit var etGuests: EditText
    private lateinit var btnSubmitRequest: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dining_request)

        token = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        restaurantId = intent.getLongExtra("RESTAURANT_ID", 0L)

        tvMessage = findViewById(R.id.tvMessage)
        tvRestaurantName = findViewById(R.id.tvRestaurantName)
        tvRestaurantLocation = findViewById(R.id.tvRestaurantLocation)
        tvRestaurantCuisine = findViewById(R.id.tvRestaurantCuisine)
        btnDate = findViewById(R.id.btnDate)
        btnTime = findViewById(R.id.btnTime)
        etGuests = findViewById(R.id.etGuests)
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest)

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        btnDate.setOnClickListener { showDatePicker() }
        btnTime.setOnClickListener { showTimePicker() }
        btnSubmitRequest.setOnClickListener { submitDiningRequest() }

        loadRestaurant()
    }

    private fun loadRestaurant() {
        if (token.isBlank() || restaurantId <= 0L) {
            showMessage("Choose a restaurant before submitting a dining request.", isError = true)
            btnSubmitRequest.isEnabled = false
            return
        }

        showMessage("Loading restaurant...", isError = false)
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.restaurantService.getRestaurant("Bearer $token", restaurantId)
                }

                if (response.isSuccessful && response.body() != null) {
                    renderRestaurant(response.body()!!)
                    hideMessage()
                } else {
                    showMessage("Failed to load restaurant details (${response.code()}).", isError = true)
                    btnSubmitRequest.isEnabled = false
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}", isError = true)
                btnSubmitRequest.isEnabled = false
            }
        }
    }

    private fun renderRestaurant(restaurant: Restaurant) {
        tvRestaurantName.text = restaurant.name
        tvRestaurantLocation.text = restaurant.location
        tvRestaurantCuisine.text = restaurant.cuisine
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
                btnDate.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                selectedTime = String.format(Locale.US, "%02d:%02d", hour, minute)
                btnTime.text = selectedTime
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun submitDiningRequest() {
        val guests = etGuests.text.toString().trim().toIntOrNull()
        if (selectedDate.isBlank() || selectedTime.isBlank() || guests == null || guests < 1) {
            showMessage("Select date, time and guests.", isError = true)
            return
        }

        btnSubmitRequest.isEnabled = false
        btnSubmitRequest.text = "Submitting..."
        hideMessage()

        lifecycleScope.launch {
            try {
                val request = CreateDiningRequest(
                    restaurantId = restaurantId,
                    requestedDateTime = "${selectedDate}T${selectedTime}:00",
                    guests = guests
                )
                val response = withContext(Dispatchers.IO) {
                    ApiClient.diningRequestService.createDiningRequest("Bearer $token", request)
                }

                if (response.isSuccessful) {
                    showMessage("Dining request submitted.", isError = false)
                    selectedDate = ""
                    selectedTime = ""
                    btnDate.text = "Choose date"
                    btnTime.text = "Choose time"
                    etGuests.setText("2")
                } else {
                    val details = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    val message = details ?: "Request was refused by the server."
                    showMessage("Failed to submit dining request (${response.code()}): $message", isError = true)
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}", isError = true)
            } finally {
                btnSubmitRequest.isEnabled = true
                btnSubmitRequest.text = "Confirm Dining Request"
            }
        }
    }

    private fun showMessage(text: String, isError: Boolean) {
        tvMessage.visibility = View.VISIBLE
        tvMessage.text = text
        val color = if (isError) R.color.error_bg else R.color.success_bg
        val textColor = if (isError) R.color.error_text else R.color.success_text
        tvMessage.setBackgroundColor(ContextCompat.getColor(this, color))
        tvMessage.setTextColor(ContextCompat.getColor(this, textColor))
    }

    private fun hideMessage() {
        tvMessage.visibility = View.GONE
    }
}
