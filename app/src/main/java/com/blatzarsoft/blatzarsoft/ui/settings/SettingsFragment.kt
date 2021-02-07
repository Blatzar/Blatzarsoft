package com.blatzarsoft.blatzarsoft.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.Intent.getIntent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.blatzarsoft.blatzarsoft.MainActivity
import com.blatzarsoft.blatzarsoft.R
import java.util.*

class ContextUtils(base: Context) : ContextWrapper(base) {

    companion object {

        fun updateLocale(c: Context, localeToSwitchTo: Locale): ContextWrapper {
            var context = c
            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(localeToSwitchTo)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                configuration.locale = localeToSwitchTo
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                context = context.createConfigurationContext(configuration)
            } else {
                resources.updateConfiguration(configuration, resources.displayMetrics)
            }
            return ContextUtils(context)
        }
    }
}

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
// get chosen language from shread preference
        val settingsManager = PreferenceManager.getDefaultSharedPreferences(newBase)
        val useEnglish = settingsManager.getBoolean("language", true)
        val language = if (useEnglish) "en" else "sv"
        val localeToSwitchTo = Locale(language)
        val localeUpdatedContext: ContextWrapper = ContextUtils.updateLocale(newBase, localeToSwitchTo)
        super.attachBaseContext(localeUpdatedContext)
    }

}

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        val autoDarkMode = findPreference("auto_dark_mode") as SwitchPreferenceCompat?
        val darkMode = findPreference("dark_mode") as SwitchPreferenceCompat?
        val languageSwitch = findPreference("language") as SwitchPreferenceCompat?
        darkMode?.isEnabled = autoDarkMode?.isChecked != true
        darkMode?.isChecked =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        autoDarkMode?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference, any: Any ->
                darkMode?.isEnabled = any != true
                if (any == true) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                    darkMode?.isChecked = isDarkMode == Configuration.UI_MODE_NIGHT_YES
                } else {
                    if (darkMode?.isChecked == true) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
                return@OnPreferenceChangeListener true
            }
        darkMode?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference, any: Any ->
                if (any == true) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                return@OnPreferenceChangeListener true
            }




        fun updateResources(context: Context, language: String): Context {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val res = context.resources
            val config = Configuration(res.configuration)
            config.setLocale(locale)
            config.locale = locale
            resources.updateConfiguration(config, resources.displayMetrics);
            return context.createConfigurationContext(config)
        }
        languageSwitch?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference, any: Any ->
                activity?.recreate()
                return@OnPreferenceChangeListener true
            }
    }
}