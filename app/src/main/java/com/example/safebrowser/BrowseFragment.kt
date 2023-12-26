package com.example.safebrowser

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.safebrowser.databinding.FragmentBrowseBinding


class BrowseFragment (private var urlNew:String) : Fragment() {


    lateinit var binding: FragmentBrowseBinding
    private var darkPatternFound = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_browse,container,false)
        binding= FragmentBrowseBinding.bind(view)

        return view
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()

        val mainActivityRef=requireActivity() as MainActivity
        binding.webView.apply {
            settings.javaScriptEnabled=true
            settings.setSupportZoom(true)
            settings.builtInZoomControls=true
            settings.displayZoomControls=false
            webViewClient= object: WebViewClient(){
                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let {
                        // Load the URL in the WebView
                        view?.loadUrl(url)
                        return true // Indicate that the URL has been handled
                    }
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    extractAndPassText()

//                    binding.webView.evaluateJavascript("document.body.textContent") { text ->
//
//                        processText(text)
//                    }



                }
            }
            webChromeClient= WebChromeClient()
            when{
                URLUtil.isValidUrl(urlNew)->loadUrl(urlNew)
                urlNew.contains(".com",ignoreCase = true)->loadUrl(urlNew)
                else->loadUrl("https://www.google.com/search?q=$urlNew")
            }

        }
    }

    private fun extractAndPassText(){
        binding.webView.evaluateJavascript("document.documentElement.innerText") { result ->
            // 'result' contains the text content
            // You can now pass this text to your model or perform any other operations
            // For example, you can log it or send it to another function
            processText(result)
        }

    }

    private fun processText(text: String) {
        val mainActivity = requireActivity() as MainActivity
        val maxChunkSize = 50

        val sentences = text.split("\\.".toRegex())

        for (sentence in sentences) {
            if (!darkPatternFound) { // Check if a dark pattern is already found
                val potentialChunk = "$sentence".trim()

                val result = mainActivity.performPrediction(potentialChunk)
                if (result == "Dark Pattern") {
                    darkPatternFound = true
                    Toast.makeText(requireContext(), "Predicted = $result", Toast.LENGTH_LONG).show()
                    break
                }
            } else {
                break // Dark pattern already found, no need to search further
            }
        }

        // Check if a dark pattern was found in the whole webpage
        if (!darkPatternFound) {
            Toast.makeText(requireContext(), "No Dark Pattern found in the whole webpage", Toast.LENGTH_LONG).show()
        }
    }





}