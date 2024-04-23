package com.example.quickquery.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Environment
import android.util.Log
import android.view.Window
import android.webkit.WebView
import com.example.quickquery.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility class for various functions such as formatting JSON to HTML, capturing WebView as image, etc.
 */
object Utils {
    /**
     * Formats a JSON string to an HTML format for display in a WebView.
     *
     * @param json The JSON string to format.
     * @return A String containing HTML representation of the JSON.
     */
    fun formatJsonAsHtml(json: String): String {
        return try {
            val jsonArray = JSONArray(json)
            val htmlRows = buildHtmlTableRows(jsonArray)

            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Courier New', monospace; padding: 10px; background-color: #f5f5f5; color: #333; }
                    table { width: 100%; border-collapse: collapse; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f2; }
                    .collapsible {
                        cursor: pointer;
                        padding: 10px;
                        width: 100%;
                        border: none;
                        text-align: left;
                        outline: none;
                        font-size: 15px;
                    }
                    .active, .collapsible:hover {
                        background-color: #f9f9f9;
                    }
                    .content {
                        padding: 0 18px;
                        display: none;
                        overflow: hidden;
                        background-color: #f9f9f9;
                        transition: max-height 0.2s ease-out;
                    }
                </style>
            </head>
            <body>
                <h2>Country Data</h2>
                <button type="button" class="collapsible">Show Data</button>
                <div class="content">
                    <table>
                        <tr>
                            <th>Attribute</th>
                            <th>Value</th>
                        </tr>
                        $htmlRows
                    </table>
                </div>

                <script>
                    var coll = document.getElementsByClassName("collapsible");
                    for (var i = 0; i < coll.length; i++) {
                        coll[i].addEventListener("click", function() {
                            this.classList.toggle("active");
                            var content = this.nextElementSibling;
                            if (content.style.display === "block") {
                                content.style.display = "none";
                            } else {
                                content.style.display = "block";
                            }
                        });
                    }
                </script>
            </body>
            </html>
            """.trimIndent()
        } catch (e: JSONException) {
            Log.e("JSON", "Error formatting JSON", e)
            "<html><body><h1>Error</h1><p>Failed to parse JSON. Error: ${e.message}</p></body></html>"
        }
    }

    /**
     * Builds HTML table rows from a JSON Array.
     *
     * @param jsonArray The JSONArray to convert into HTML table rows.
     * @return A String containing HTML table rows.
     */
    private fun buildHtmlTableRows(jsonArray: JSONArray): String {
        val rows = StringBuilder()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            jsonObject.keys().forEach { key ->
                rows.append("<tr><td>$key</td><td>${formatValue(jsonObject.get(key))}</td></tr>")
            }
        }
        return rows.toString()
    }

    /**
     * Formats a JSON value for display in HTML, escaping special HTML characters.
     *
     * @param value The value to format.
     * @return A String formatted for HTML.
     */
    private fun formatValue(value: Any?): String {
        return when (value) {
            is JSONObject -> "<pre>${value.toString(4)}</pre>"
            is JSONArray -> "<pre>${value.toString(4)}</pre>"
            else -> value.toString()
        }.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    /**
     * Captures the content of a WebView as a Bitmap, saves it, and potentially displays it.
     *
     * @param webView The WebView to capture.
     * @param context The context used for accessing the file system.
     * @return The file path of the saved image or null if the save operation fails.
     */
    fun captureWebView(webView: WebView, context: Context): String? {
        val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        webView.draw(canvas)
        return saveBitmap(bitmap, context)

    }

    /**
     * Saves a Bitmap image to the device's external storage and returns the file path.
     *
     * @param bitmap The Bitmap to save.
     * @param context The context used for accessing the file system.
     * @return The file path of the saved image or null if the save operation fails.
     */
    private fun saveBitmap(bitmap: Bitmap, context: Context): String? {
        val filename = "${System.currentTimeMillis()}.png"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (storageDir != null && storageDir.exists()) {
            val file = File(storageDir, filename)
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    Log.d("WebViewCapture", "Saved image as: $filename")
                    Log.d("WebViewCapture", "File path: ${file.absolutePath}")
                }
                return file.absolutePath
            } catch (e: IOException) {
                Log.e("WebViewCapture", "Error saving image", e)
            }
        } else {
            Log.e("WebViewCapture", "Cannot write to external storage.")
        }
        return null
    }


    // Loading dialog management
    private var jarvisLoader: Dialog? = null

    /**
     * Shows the loading dialog.
     *
     * @param context The context on which the dialog is to be shown.
     * @param isCancelable Determines if the dialog can be canceled.
     */
    fun showDialog(context: Context, isCancelable: Boolean) {
        hideDialog() // Hide any existing dialog
        jarvisLoader = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.loader_layout) // Define this layout with your loading spinner
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            setCancelable(isCancelable)
            show()
        }
    }

    /**
     * Hides and clears the loading dialog.
     */
    fun hideDialog() {
        jarvisLoader?.dismiss()
        jarvisLoader = null // Set to null to avoid memory leaks
    }

    fun convertMillisToReadableDate(millis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    /**
     * Displays a static message in the WebView with enhanced formatting.
     *
     * @return A String containing HTML static message.
     */
    fun getStaticMessage(): String{
        return  """
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body {
                    font-family: 'Arial', sans-serif; /* Ensures the font is clean and readable */
                    text-align: center; /* Centers the text horizontally */
                    margin-top: 50px; /* Adds some space at the top of the page */
                    color: #333333; /* Sets a mild dark color for the text */
                    background-color: #f4f4f4; /* Light grey background for a little contrast */
                }
                p {
                    font-size: 24px; /* Sets a larger font size for better visibility */
                    width: 90%; /* Ensures the text does not span the whole width of the screen */
                    margin: auto; /* Centers the text block horizontally */
                }
            </style>
        </head>
        <body>
            <p>This is the WebView where I will display the data.</p>
        </body>
        </html>
    """.trimIndent()
    }
}