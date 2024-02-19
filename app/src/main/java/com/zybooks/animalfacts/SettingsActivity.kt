package com.zybooks.animalfacts

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import com.zybooks.animalfacts.R.drawable


/**
 * Class that is the start of the settings page and creates all the settings options
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var complexiity_radio_group: RadioGroup
    private lateinit var animal_spinner: Spinner

    /**
     * Sets up all the variable inputs in the UI whether it is first initialization
     *  or reinitialization. Also update the page once.
     *
     * @param savedInstanceState contains the values of the app saved temporarily
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_main)

        complexiity_radio_group = findViewById(R.id.complexiity_radio_group)
        animal_spinner = findViewById(R.id.animal_spinner)
        val animals = resources.getStringArray(R.array.Animals)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, animals)
        animal_spinner.adapter = adapter

        ////////////////////////////////////////////////////////////////////////////////////////////
        // Play Animation                                                                         //
        ////////////////////////////////////////////////////////////////////////////////////////////
        val partyTime: ImageView = findViewById(R.id.party_time)
        partyTime.setBackgroundResource(R.drawable.party_time)
        val apeAnimation = partyTime.background as AnimationDrawable
        apeAnimation.start()

    }


    /**
     * Return to the home page where facts can be generated
     *
     * @param view the view containing the settings menu
     */
    fun returnHome(view: View) {

        val complexity  = when (complexiity_radio_group.checkedRadioButtonId) {
            R.id.simple_radio_button -> "Simple"
            R.id.moderate_radio_button -> "Moderate"
            else -> "Complex"
        }
        val selectedAnimal = animal_spinner.selectedItem.toString()

        val intent = Intent().apply {
            putExtra("complexity", complexity)
            putExtra("animal", selectedAnimal)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}