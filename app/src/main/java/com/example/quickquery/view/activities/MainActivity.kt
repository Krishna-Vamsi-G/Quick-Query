package com.example.quickquery.view.activities

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.quickquery.MyApp
import com.example.quickquery.databinding.ActivityMainBinding
import com.example.quickquery.model.response.Country
import com.example.quickquery.utils.Utils
import com.example.quickquery.viewmodel.MainViewModel
import com.example.quickquery.viewmodel.ViewModelFactory
import com.google.gson.Gson
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.quickquery.R
import com.example.quickquery.databinding.DialogViewScreenshotBinding
import com.example.quickquery.model.data.FragmentName
import com.example.quickquery.view.fragments.CacheFragment
import com.example.quickquery.view.fragments.DatabaseFragment
import com.example.quickquery.view.fragments.ScreenshotView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dialogBinding: DialogViewScreenshotBinding


    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory((application as MyApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dialogBinding = DialogViewScreenshotBinding.inflate(layoutInflater)


        initializeUI()
    }

    /**
     * Initializes user interface components and setups initial UI state.
     */
    private fun initializeUI() {
        setupToolbar()
        setupActionIcon()
        setupWebView()
        setupObservers()
        setupClickListeners()
        displayStaticMessage()
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
        setupActionIcon()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setupToolbar()
        setupActionIcon()

    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        setupActionIcon()
        setupToolbar()
    }


    /**
     * Sets up the toolbar with the appropriate settings.
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Quick Query"
        with(window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = android.graphics.Color.TRANSPARENT
            }
        }
    }

    /**
     * Establishes observers on LiveData objects to react to changes.
     */
    private fun setupObservers() {
        viewModel.countryData.observe(this) { countries ->
            if (!countries.isNullOrEmpty()) {
                displayDataInWebView(countries)
            } else {
                Log.e("MainActivity", "No countries available to display.")
                showErrorInView("No data available for the query")
            }
        }

        viewModel.dataSourceType.observe(this) { source ->
            source?.let {
                val sourceName = it.name.lowercase(Locale.ROOT).capitalize(Locale.ROOT)
                Log.d("MainActivity", "Data loaded from $sourceName")
                Toast.makeText(this, "Data loaded from $sourceName", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            Log.e("MainActivity", "Error occurred: $error")
            showErrorInView(error)
        }

        viewModel.dataLoading.observe(this) { isLoading ->
            if (isLoading) {
                Utils.showDialog(this, isCancelable = false)
            } else {
                Utils.hideDialog()
            }

        }
    }

    /**
     * Configures click listeners for buttons.
     */
    private fun setupClickListeners() {
        binding.blackButton.setOnClickListener {
            val query = binding.inputQuery.text.toString().trim().lowercase()
            if (query.isNotEmpty()) {
                viewModel.fetchCountryDetails(query)
            } else {
                binding.inputQuery.error = "Please enter a query"
            }
        }

        binding.actionsIcon.setOnClickListener {
            toggleFabState()
        }

        binding.screenshotIcon.setOnClickListener {
            val filePath = Utils.captureWebView(binding.webView, this)
            filePath?.let { path ->
                showConfirmationDialog(path)
            } ?: showErrorInView("Failed to capture screenshot.")
        }
        binding.screenshotIcon.setOnClickListener {
            val filePath = Utils.captureWebView(binding.webView, this)
            filePath?.let { path ->
                showConfirmationDialog(path)
            } ?: showErrorInView("Failed to capture screenshot.")
        }

        binding.viewCache.setOnClickListener {
            openFragment(CacheFragment(), FragmentName.CACHE)
        }
        binding.viewCacheIcon.setOnClickListener {
            openFragment(CacheFragment(), FragmentName.CACHE)
        }

        binding.viewDatabase.setOnClickListener {
            openFragment(DatabaseFragment(), FragmentName.DATABASE)
        }
        binding.viewDatabaseIcon.setOnClickListener {
            openFragment(DatabaseFragment(), FragmentName.DATABASE)
        }


    }

    /**
     * Displays a custom dialog with a screenshot preview and options to view or cancel.
     *
     * @param imagePath The file path of the screenshot to display.
     */
    private fun showConfirmationDialog(imagePath: String) {
        // Initialize the dialog binding instance
        dialogBinding = DialogViewScreenshotBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)  // Use the binding's root as the dialog view
            .create()

        setupDialogViews(imagePath, dialog)
        dialog.show()
        Log.d("DialogView", "Confirmation dialog displayed.")
    }

    /**
     * Configures the views within the screenshot confirmation dialog.
     *
     * @param imagePath The file path of the image to display.
     * @param dialog The dialog instance where the views are hosted.
     */
    private fun setupDialogViews(imagePath: String, dialog: AlertDialog) {
        try {
            // Attempt to decode the stored image file into a Bitmap object.
            val bitmap = BitmapFactory.decodeFile(imagePath)
            dialogBinding.imagePreview.setImageBitmap(bitmap)  // Set the bitmap to the ImageView.
            Log.d("DialogView", "Image set in dialog successfully.")
        } catch (e: Exception) {
            // Log and show error if the bitmap decoding fails.
            Log.e("DialogView", "Error decoding bitmap from file: $imagePath", e)
            showErrorInView("Failed to load image. Please try again.")
            dialog.dismiss()  // Dismiss the dialog as continuing would result in a lack of necessary image.
            return
        }

        // Configure the "View" button to open the image in a more suitable viewer and then dismiss the dialog.
        dialogBinding.buttonView.setOnClickListener {
            Log.d("DialogView", "View button clicked.")
            displayImage(imagePath)  // Display the image using your defined method.
            dialog.dismiss()  // Dismiss the dialog post-viewing.
        }

        // Configure the "Cancel" button to dismiss the dialog without further actions.
        dialogBinding.buttonCancel.setOnClickListener {
            Log.d("DialogView", "Cancel button clicked.")
            dialog.dismiss()  // Simply dismiss the dialog.
        }
    }


    /**
     * Displays the image in a full-screen image viewer or another appropriate activity.
     *
     * @param imagePath The path of the image file to display.
     */
    private fun displayImage(imagePath: String) {
        try {
            // fragment to display the image
            openFragment(ScreenshotView(imagePath), FragmentName.SCREENSHOT)
            Log.d("DialogView", "Image viewer opened for: $imagePath")
        } catch (e: Exception) {
            Log.e("DialogView", "Failed to open image viewer for: $imagePath", e)
            showErrorInView("Failed to open image.")
        }
    }


    /**
     * Replaces the current fragment displayed in the frame container with the specified fragment.
     * It also updates the toolbar title based on the fragment type and collapses the FAB menu.
     *
     * @param fragment The new fragment to display.
     * @param fragName The enum representing the fragment, used to set the toolbar title.
     */
    private fun openFragment(fragment: Fragment, fragName: FragmentName) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameContainer, fragment)
                .addToBackStack(null)
                .commit()
            Log.d("MainActivity", "Fragment ${fragName.title} opened.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to open fragment ${fragName.title}: ${e.message}")
        }
        collapseFabsAndHideLabels()
        editToolbar(fragName)
    }

    /**
     * Updates the toolbar title based on the specified fragment name.
     * This method adjusts the toolbar to reflect the context of the active fragment.
     *
     * @param fragName The enum constant representing the currently active fragment.
     */
    private fun editToolbar(fragName: FragmentName) {
        val title = when (fragName) {
            FragmentName.CACHE -> "Quick Query Cache"
            FragmentName.DATABASE -> "Quick Query Database"
            FragmentName.SCREENSHOT -> "Quick Query Screenshot"
        }
        binding.toolbar.title = title
        Log.d("MainActivity", "Toolbar title set to: $title")
    }


    /**
     * Handles the collapsing of FABs and hides their labels to simplify the UI when navigating away from the main screen.
     */
    private fun collapseFabsAndHideLabels() {
        try {
            binding.actionsIcon.shrink()
            binding.actionsIcon.text = ""// Collapses the main action button.
            binding.actionsIcon.isExtended = false
            hideFabWithLabel(
                binding.viewCacheIcon,
                binding.viewCache
            )  // Hides the cache view button and label.
            hideFabWithLabel(
                binding.viewDatabaseIcon,
                binding.viewDatabase
            )  // Hides the database view button and label.
            hideFabWithLabel(
                binding.screenshotIcon,
                binding.screenshot
            )  // Hides the screenshot button and label.
            binding.actionsIcon.visibility = View.GONE  // Makes the actions button fully invisible.
            Log.d("MainActivity", "All FABs collapsed and labels hidden.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error collapsing FABs: ${e.message}")
        }
    }

    /**
     * Sets up the action FAB with initial settings for text, visibility, and state.
     */
    private fun setupActionIcon() {
        try {
//            binding.actionsIcon.text = ""  // Clears text to mimic a collapsed state visually.
            binding.actionsIcon.shrink()// Programmatically collapses the FAB.
            binding.actionsIcon.visibility = View.VISIBLE  // Ensures the FAB is visible on screen.
            Log.d("MainActivity", "Action icon setup complete.")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting up action icon: ${e.message}")
        }
    }

    /**
     * Toggles the state of the action FAB between expanded and collapsed and adjusts visibility of related FABs and labels.
     */
    private fun toggleFabState() {
        try {
            if (binding.actionsIcon.isExtended) {
                binding.actionsIcon.shrink()  // Collapses the main action FAB.
                hideFabWithLabel(binding.screenshotIcon, binding.screenshot)
                hideFabWithLabel(binding.viewDatabaseIcon, binding.viewDatabase)
                hideFabWithLabel(binding.viewCacheIcon, binding.viewCache)
                Log.d("MainActivity", "FABs hidden.")
            } else {
                binding.actionsIcon.text = "Actions"
                binding.actionsIcon.extend()  // Expands the main action FAB.
                showFabWithLabel(binding.screenshotIcon, binding.screenshot)
                showFabWithLabel(binding.viewDatabaseIcon, binding.viewDatabase)
                showFabWithLabel(binding.viewCacheIcon, binding.viewCache)
                binding.viewCache.background =
                    ContextCompat.getDrawable(this, R.drawable.curved_border_bg)
                binding.viewDatabase.background =
                    ContextCompat.getDrawable(this, R.drawable.curved_border_bg)
                binding.screenshot.background =
                    ContextCompat.getDrawable(this, R.drawable.curved_border_bg)
                val density = resources.displayMetrics.density
                val paddingInPx = (5 * density).toInt()
                binding.viewCache.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
                binding.viewDatabase.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)
                binding.screenshot.setPadding(paddingInPx, paddingInPx, paddingInPx, paddingInPx)


                Log.d("MainActivity", "FABs displayed.")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error toggling FAB state: ${e.message}")
        }
    }

    /**
     * Hides a FloatingActionButton and its associated label, enhancing UI cleanliness during state transitions.
     */
    private fun hideFabWithLabel(fab: FloatingActionButton, label: TextView) {
        try {
            fab.hide()  // Animates the FAB out of view.
            label.visibility = View.INVISIBLE  // Instantly hides the label for cleanliness.
            Log.d("MainActivity", "Hid FAB with label for: ${label.text}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error hiding FAB and label: ${e.message}")
        }
    }

    /**
     * Shows a FloatingActionButton and its associated label, enhancing UI interactivity during state transitions.
     */
    private fun showFabWithLabel(fab: FloatingActionButton, label: TextView) {
        try {
            fab.show()  // Animates the FAB into view.
            label.visibility = View.VISIBLE  // Instantly shows the label for interactivity.
            Log.d("MainActivity", "Showed FAB with label for: ${label.text}")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error showing FAB and label: ${e.message}")
        }
    }


    /**
     * Displays fetched country data in the WebView.
     *
     * @param countries List of countries to display.
     */
    private fun displayDataInWebView(countries: List<Country>) {
        val jsonData = Gson().toJson(countries)
        val htmlContent = Utils.formatJsonAsHtml(jsonData)
        loadHtmlContent(htmlContent)
    }

    /**
     * Displays a static message in the WebView at the start.
     */
    private fun displayStaticMessage() {
        val staticMessage = Utils.getStaticMessage()
        loadHtmlContent(staticMessage)
    }

    /**
     * Loads HTML content into the WebView.
     *
     * @param htmlContent HTML content to load.
     */
    private fun loadHtmlContent(htmlContent: String) {
        binding.webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    /**
     * Configures WebView settings for better user experience.
     */
    private fun setupWebView() {
        with(binding.webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Utils.hideDialog()
                super.onPageFinished(view, url)
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                error?.let {
                    Utils.hideDialog()
                    Log.e("MainActivity", "Web page failed to load: ${it.description}")
                    showErrorInView("Web page failed to load: ${it.description}")
                }
            }
        }
    }

    /**
     * Displays an error message in a Toast.
     *
     * @param message The message to display.
     */
    private fun showErrorInView(message: String) {
        Utils.hideDialog()
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
