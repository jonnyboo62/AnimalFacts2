package com.zybooks.animalfacts
import android.Manifest
import android.annotation.SuppressLint

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random
import androidx.activity.result.contract.ActivityResultContracts
import android.media.MediaPlayer
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import android.widget.Toast

const val TAG = "MainActivity"


/**
 * Class that is the start of the app, handles moving to the settings
 * and generates facts based on the complexity and what animal is selected
 */

class MainActivity : AppCompatActivity() {
    private lateinit var fact: TextView
    private lateinit var factButton: TextView
    private lateinit var animalFacts: List<String>
    private var currentAnimal = "Ape"
    private var currentComplexity = "Moderate"
    private lateinit var mediaPlayer: MediaPlayer




    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Primary App Functionality                                                                  //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Update the main page based on current complexity and animal selected including
     * button text and which animal file full of facts is read
     */
    fun updatePage() {
        factButton.text = "Produce ${currentComplexity} ${currentAnimal} Fact"
        animalFacts = readTextFileFromAssets("${currentAnimal.lowercase()}_facts.txt")

    }

    /**
     * Sets up all the variable inputs in the UI whether it is first initialization
     *  or reinitialization. Also update the page once.
     *
     * @param savedInstanceState contains the values of the app saved temporarily
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fact = this.findViewById(R.id.fact)
        factButton = this.findViewById(R.id.fact_button)
        registerForContextMenu(fact)
        updatePage()

        if (hasLocationPermission()) {
            findLocation()
            Toast.makeText(this, "Fun Fact: Some apes could be in your area, go to a zoo", Toast.LENGTH_SHORT).show()
        }

    }

    /**
     * Produce a fact from the list of available facts based on complexity and current animal selected
     *
     * @param view view containing the menu
     */
    fun produceFact(view: View) {
        var indexModifier = 0
        if (currentComplexity == "Moderate") {
            indexModifier = 10
        } else if (currentComplexity == "Complex") {
            indexModifier = 20
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Play a sound when a button is clicked                                                  //
        ////////////////////////////////////////////////////////////////////////////////////////////

        mediaPlayer = MediaPlayer.create(this, R.raw.fact)
        mediaPlayer.start()

        val randomIndex = Random().nextInt(10)
        val index = randomIndex + indexModifier
        fact.text = animalFacts[index] + "\n\n" + fact.text.toString()
    }

    /**
     * When a new animal is selected, a new file needs to be read in order to produce facts about the animal
     *
     * @param fileName name of the file to be read
     *
     * @return List<String> List of the lines in the form of strings
     */
    fun readTextFileFromAssets(fileName: String): List<String> {
        // Try to open and read the file, returning an empty list in case of exception
        return try {
            // Open the file from the assets folder
            val inputStream = assets.open(fileName)
            // Read the file content as a String
            val content = inputStream.bufferedReader().use { it.readText() }
            // Split the content into lines and return as a list
            content.split("\n").filter { it.isNotBlank() }
        } catch (e: Exception) {
            // Log the exception or handle it as needed
            emptyList()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Context Menu                                                                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the context menu for when copying the entirety of the facts produced
     *
     * @param menu the context menu that has been created
     * @param v the view containing the menu
     * @param menuInfo the context minue information that has been created
     */
    override fun onCreateContextMenu(menu: ContextMenu?,
                                     v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }

    /**
     * When an item in the context menu is selected an action is performed
     *
     * @param item the item selected from the context menu
     */
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.copy_facts -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("fact", fact.text.toString())
                clipboard.setPrimaryClip(clip)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Vertical Scroll that uses Touch Handling                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles touch movement scrolling of the text bos that contains the facts
     *
     * @param event touch event that happens
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = when (event.action) {
            MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
            MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
            MotionEvent.ACTION_UP -> "ACTION_UP"
            else -> "ACTION_CANCEL"
        }

        return true
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Settings Page                                                                              //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The value for starting the settings menu and changing the variables that get returned including animal and complexity
     */
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            currentComplexity = data?.getStringExtra("complexity") ?: currentComplexity
            currentAnimal = data?.getStringExtra("animal") ?: currentAnimal
            updatePage()
        }
    }

    /**
     * Open the settings page and return the changes that are made including an animal and complexity
     *
     * @param view the view containing the menu
     */
    fun settings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startForResult.launch(intent)

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Location Services                                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun hasLocationPermission(): Boolean {

        // Request fine location permission if not already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return false
        }
        return true
    }

    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // App can request location
        }
    }

    @SuppressLint("MissingPermission")
    private fun findLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)

        client.lastLocation
            .addOnSuccessListener(this) { location ->
                Log.d(TAG, "location = $location") }
    }


}