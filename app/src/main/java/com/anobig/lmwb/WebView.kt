package com.anobig.lmwb

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader


class WebView : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.web_view)
        val webView = findViewById<WebView>(R.id.webView)

        // Local assets loader
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .setDomain("web")
            .build()

        // Override WebView client, and if request is to local file, intercept and serve local
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url);
            }
        }

        //Override the style of JS Alert like dialogs
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView,
                url: String?,
                message: String?,
                result: JsResult
            ): Boolean {
                AlertDialog.Builder(view.context)
                    .setTitle(R.string.alert)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface?, which: Int -> result.confirm() }
                    .setOnDismissListener { dialog: DialogInterface? -> result.confirm() }
                    .create()
                    .show()
                return true
            }

            override fun onJsConfirm(
                view: WebView,
                url: String?,
                message: String?,
                result: JsResult
            ): Boolean {
                AlertDialog.Builder(view.context)
                    .setTitle(R.string.confirm)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface?, which: Int -> result.confirm() }
                    .setNegativeButton(R.string.cancel) { dialog: DialogInterface?, which: Int -> result.cancel() }
                    .setOnDismissListener { dialog: DialogInterface? -> result.cancel() }
                    .create()
                    .show()
                return true
            }

            override fun onJsPrompt(
                view: WebView,
                url: String?,
                message: String?,
                defaultValue: String?,
                result: JsPromptResult
            ): Boolean {
                val input = EditText(view.context)
                input.inputType = InputType.TYPE_CLASS_TEXT
                input.setText(defaultValue)
                AlertDialog.Builder(view.context)
                    .setTitle(R.string.prompt)
                    .setMessage(message)
                    .setView(input)
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface?, which: Int ->
                        result.confirm(
                            input.text.toString()
                        )
                    }
                    .setNegativeButton(R.string.cancel) { dialog: DialogInterface?, which: Int -> result.cancel() }
                    .setOnDismissListener { dialog: DialogInterface? -> result.cancel() }
                    .create()
                    .show()
                return true
            }
        }

        // Enable JS
        webView.settings.javaScriptEnabled = true

        // Setting this off for security. Off by default for SDK versions >= 16.
        webView.settings.allowFileAccessFromFileURLs = false;

        // Off by default, deprecated for SDK versions >= 30.
        webView.settings.allowUniversalAccessFromFileURLs = false;

        // Keeping these off is less critical but still a good idea, especially if your app is not
        // using file:// or content:// URLs.
        webView.settings.allowFileAccess = false;
        webView.settings.allowContentAccess = false;

        webView.loadUrl("https://web/assets/www/about.html")

        //Handle webView back stack with standard android back button
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (webView.canGoBack())
                    webView.goBack()
                else
                    onBackPressedDispatcher.onBackPressed()
            }
        })
    }
}