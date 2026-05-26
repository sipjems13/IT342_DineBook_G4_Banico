package com.dinebook.mobile.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dinebook.mobile.R
import com.dinebook.mobile.auth.LoginActivity
import com.dinebook.mobile.booking.DiningRequest
import com.dinebook.mobile.booking.DiningRequestActivity
import com.dinebook.mobile.restaurant.Restaurant
import com.dinebook.mobile.shared.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var token: String
    private lateinit var userEmail: String
    private var activeTab = Tab.BROWSE

    private lateinit var tvUserEmail: TextView
    private lateinit var tvPageTitle: TextView
    private lateinit var tvPageHint: TextView
    private lateinit var tvMessage: TextView
    private lateinit var restaurantList: LinearLayout
    private lateinit var requestList: LinearLayout
    private lateinit var btnRefresh: Button
    private lateinit var drawerOverlay: View
    private lateinit var drawerPanel: View
    private lateinit var btnBrowseNav: TextView
    private lateinit var btnRequestsNav: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        token = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        userEmail = intent.getStringExtra("USER_EMAIL") ?: "Signed in"

        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvPageTitle = findViewById(R.id.tvPageTitle)
        tvPageHint = findViewById(R.id.tvPageHint)
        tvMessage = findViewById(R.id.tvMessage)
        restaurantList = findViewById(R.id.restaurantList)
        requestList = findViewById(R.id.requestList)
        btnRefresh = findViewById(R.id.btnRefresh)
        drawerOverlay = findViewById(R.id.drawerOverlay)
        drawerPanel = findViewById(R.id.drawerPanel)
        btnBrowseNav = findViewById(R.id.btnBrowseNav)
        btnRequestsNav = findViewById(R.id.btnRequestsNav)

        tvUserEmail.text = userEmail

        findViewById<Button>(R.id.btnMenu).setOnClickListener { openDrawer() }
        findViewById<Button>(R.id.btnCloseDrawer).setOnClickListener { closeDrawer() }
        drawerOverlay.setOnClickListener { closeDrawer() }
        btnBrowseNav.setOnClickListener { showBrowse() }
        btnRequestsNav.setOnClickListener { showRequests() }
        btnRefresh.setOnClickListener { refreshActiveTab() }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        showBrowse()
    }

    private fun showBrowse() {
        activeTab = Tab.BROWSE
        closeDrawer()
        tvPageTitle.text = "Browse restaurants"
        tvPageHint.text = "Tap a restaurant to submit a dining request."
        btnRefresh.text = "Refresh Restaurants"
        restaurantList.visibility = View.VISIBLE
        requestList.visibility = View.GONE
        setNavState()
        loadRestaurants()
    }

    private fun showRequests() {
        activeTab = Tab.REQUESTS
        closeDrawer()
        tvPageTitle.text = "My requests"
        tvPageHint.text = "Track the status of your submitted dining requests."
        btnRefresh.text = "Refresh Requests"
        restaurantList.visibility = View.GONE
        requestList.visibility = View.VISIBLE
        setNavState()
        loadMyRequests()
    }

    private fun refreshActiveTab() {
        if (activeTab == Tab.BROWSE) loadRestaurants() else loadMyRequests()
    }

    private fun loadRestaurants() {
        if (!hasToken()) return

        showMessage("Loading restaurants...", isError = false)
        btnRefresh.isEnabled = false
        restaurantList.removeAllViews()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.restaurantService.browseRestaurants("Bearer $token")
                }

                if (response.isSuccessful) {
                    val restaurants = response.body().orEmpty()
                    renderRestaurants(restaurants)
                    if (restaurants.isEmpty()) showMessage("No restaurants found yet.", false) else hideMessage()
                } else {
                    showMessage("Failed to load restaurants (${response.code()}).", true)
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}", true)
            } finally {
                btnRefresh.isEnabled = true
            }
        }
    }

    private fun loadMyRequests() {
        if (!hasToken()) return

        showMessage("Loading your requests...", isError = false)
        btnRefresh.isEnabled = false
        requestList.removeAllViews()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.diningRequestService.myRequests("Bearer $token")
                }

                if (response.isSuccessful) {
                    val requests = response.body().orEmpty()
                    renderRequests(requests)
                    if (requests.isEmpty()) showMessage("You have no dining requests yet.", false) else hideMessage()
                } else {
                    showMessage("Failed to load your requests (${response.code()}).", true)
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}", true)
            } finally {
                btnRefresh.isEnabled = true
            }
        }
    }

    private fun renderRestaurants(restaurants: List<Restaurant>) {
        restaurantList.removeAllViews()
        restaurants.forEach { restaurant ->
            val card = layoutInflater.inflate(R.layout.item_restaurant_card, restaurantList, false)
            card.findViewById<TextView>(R.id.tvRestaurantName).text = restaurant.name
            card.findViewById<TextView>(R.id.tvRestaurantLocation).text = restaurant.location
            card.findViewById<TextView>(R.id.tvRestaurantCuisine).text = restaurant.cuisine
            card.setOnClickListener {
                val intent = Intent(this, DiningRequestActivity::class.java).apply {
                    putExtra("ACCESS_TOKEN", token)
                    putExtra("USER_EMAIL", userEmail)
                    putExtra("RESTAURANT_ID", restaurant.id)
                }
                startActivity(intent)
            }
            restaurantList.addView(card)
        }
    }

    private fun renderRequests(requests: List<DiningRequest>) {
        requestList.removeAllViews()
        requests.forEach { request ->
            val card = layoutInflater.inflate(R.layout.item_request_card, requestList, false)
            card.findViewById<TextView>(R.id.tvRequestRestaurant).text = request.restaurantName
            card.findViewById<TextView>(R.id.tvRequestStatus).text = request.status
            card.findViewById<TextView>(R.id.tvRequestDetails).text =
                "${formatDate(request.requestedDateTime)} | ${request.guests} guest${if (request.guests > 1) "s" else ""}"
            card.findViewById<TextView>(R.id.tvRequestCreated).text = "Requested at ${formatDate(request.createdAt)}"
            requestList.addView(card)
        }
    }

    private fun formatDate(value: String): String {
        return try {
            val source = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val target = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
            target.format(source.parse(value.substringBefore("."))!!)
        } catch (_: Exception) {
            value
        }
    }

    private fun hasToken(): Boolean {
        if (token.isBlank()) {
            showMessage("Please log in again.", true)
            return false
        }
        return true
    }

    private fun openDrawer() {
        drawerOverlay.visibility = View.VISIBLE
        drawerPanel.visibility = View.VISIBLE
    }

    private fun closeDrawer() {
        drawerOverlay.visibility = View.GONE
        drawerPanel.visibility = View.GONE
    }

    private fun setNavState() {
        btnBrowseNav.isSelected = activeTab == Tab.BROWSE
        btnRequestsNav.isSelected = activeTab == Tab.REQUESTS
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

    private enum class Tab {
        BROWSE,
        REQUESTS
    }
}
