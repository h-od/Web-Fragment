package com.hughod.webfragment

import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.fragment_web.*
import uk.co.alt236.webviewdebug.DebugWebViewClient

class WebFragment : Fragment() {

    companion object {
        private const val EXTRA_URL = "URL"
        private const val EXTRA_TITLE_RES = "TITLE_RES"
        private const val EXTRA_COLOR_RES = "COLOR_RES"
        private const val EXTRA_HAS_TOOLBAR = "HAS_TOOLBAR"
        private const val EXTRA_HAS_BACK_BUTTON = "HAS_BACK_BUTTON"
        private const val EXTRA_BACK_BUTTON_RES = "BACK_BUTTON_RES"
        private const val EXTRA_IS_DEBUG = "IS_DEBUG"

        fun getInstance(
                url: String,
                @StringRes titleRes: Int = -1,
                @ColorRes colorRes: Int = -1,
                includeToolbar: Boolean = false,
                hasBackButton: Boolean = false,
                @DrawableRes backButtonRes: Int = -1,
                isDebug: Boolean = false
        ) = WebFragment().withArguments(url, titleRes, colorRes, includeToolbar, hasBackButton, backButtonRes, isDebug)

        private fun WebFragment.withArguments(
                url: String,
                @StringRes titleRes: Int,
                @ColorRes colorRes: Int,
                includeToolbar: Boolean,
                hasBackButton: Boolean,
                @DrawableRes backButtonRes: Int,
                isDebug: Boolean): WebFragment {
            val bundle = Bundle()

            bundle.putString(EXTRA_URL, url)
            bundle.putInt(EXTRA_TITLE_RES, titleRes)
            bundle.putInt(EXTRA_COLOR_RES, colorRes)
            bundle.putBoolean(EXTRA_HAS_TOOLBAR, includeToolbar)
            bundle.putBoolean(EXTRA_HAS_BACK_BUTTON, hasBackButton)
            bundle.putInt(EXTRA_BACK_BUTTON_RES, backButtonRes)
            bundle.putBoolean(EXTRA_IS_DEBUG, isDebug)

            arguments = bundle

            return this
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_web, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val activity = this.activity.asAppCompatActivity() ?: return

        activity.setUpToolbar()
        webView.setUp()
        progressBar.setColour()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (webView != null) webView.destroy()
        super.onDestroy()
    }

    private fun FragmentActivity?.asAppCompatActivity(): AppCompatActivity? = this as AppCompatActivity

    private fun AppCompatActivity.setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back) // your drawable
        toolbar.setNavigationOnClickListener({ onBackPressed() })
        toolbar.setBackgroundColor(arguments.getColour())

        setTitle(arguments.getTitleRes())
    }

    private fun WebView.setUp() {
        val debugWebViewClient = DebugWebViewClient(Client(progressBar))
        debugWebViewClient.isLoggingEnabled = true

        webViewClient = debugWebViewClient //TODO is debug?

        loadUrl(arguments.getUrl())
    }

    private fun ProgressBar.setColour() {
        indeterminateDrawable.setColorFilter(arguments.getColour(), PorterDuff.Mode.MULTIPLY);
    }

    private fun Bundle?.getUrl(): String =
            this?.getString(EXTRA_URL)
                    ?: "https://stackoverflow.com/search?q=android+webview+not+loading"

    private fun Bundle?.getTitleRes(): Int = this?.getInt(EXTRA_TITLE_RES) ?: R.string.app_name

    private fun Bundle?.getColour(): Int {
        this ?: return android.R.color.transparent

        val colorId = (getInt(EXTRA_COLOR_RES))

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(colorId, activity!!.theme)
        } else {
            @Suppress("DEPRECATION") //for older versions
            resources.getColor(colorId)
        }
    }

    private fun Bundle?.hasToolbar(): Boolean = this?.getBoolean(EXTRA_HAS_TOOLBAR) ?: false

    private fun Bundle?.hasBackButton(): Boolean = this?.getBoolean(EXTRA_HAS_BACK_BUTTON) ?: false

    private fun Bundle?.backButtonRes(): Int = this?.getInt(EXTRA_BACK_BUTTON_RES) ?: -1

    private fun Bundle?.isDebug(): Boolean = this?.getBoolean(EXTRA_IS_DEBUG) ?: false
}

class Client(private val progressIndicator: View) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        progressIndicator.visibility = View.VISIBLE
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        progressIndicator.visibility = View.GONE
    }
}
