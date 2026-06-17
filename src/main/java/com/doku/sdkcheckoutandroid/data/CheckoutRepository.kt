import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.doku.sdkcheckoutandroid.helper.DokuConfig
import com.doku.sdkcheckoutandroid.model.request.CheckoutRequest
import com.doku.sdkcheckoutandroid.model.request.PaymentRequest
import com.doku.sdkcheckoutandroid.model.response.BinEligibilityResponse
import com.doku.sdkcheckoutandroid.model.response.CardPaymentResponse
import com.doku.sdkcheckoutandroid.model.response.CardTokenizeResponse
import com.doku.sdkcheckoutandroid.model.response.CheckPaymentStatusResponse
import com.doku.sdkcheckoutandroid.model.response.DeleteCardTokenizeResponse
import com.doku.sdkcheckoutandroid.model.response.GenerateCodeResponse
import com.doku.sdkcheckoutandroid.model.response.GenerateKeyResponse
import com.doku.sdkcheckoutandroid.model.response.JumpAppWalletResponse
import com.doku.sdkcheckoutandroid.model.response.QRISPaymentResponse
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import com.doku.sdkcheckoutandroid.model.response.ShowPromosResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2


class CheckoutRepository {

    private val client = OkHttpClient().newBuilder().followRedirects(false).build()

    fun generateHeaders(jsonBody: String, clientId: String, requestId: String, requestTimestamp: String,
                        signatureKey: String): Map<String, String> {

        return mapOf(
            "Client-Id" to clientId,
            "Request-Id" to requestId,
            "Request-Timestamp" to requestTimestamp,
            "Signature" to signatureKey
        )
    }

    fun doGetTokenId(checkoutRequest: CheckoutRequest, clientId: String,
                     requestId: String, requestTimestamp: String, signatureKey: String, onResult: (String?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/v1/payment"

        val gson = Gson()
        val jsonBody = gson.toJson(checkoutRequest)
        val headers = generateHeaders(jsonBody = jsonBody, clientId = clientId, requestId = requestId, requestTimestamp = requestTimestamp, signatureKey= signatureKey)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        Log.d("url doku",url)
        Log.d("DOKU_API", "REQUEST $jsonBody")

        val requestBuilder = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_API", "Error: ${e.message}")
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DOKU_API", "Response: $responseBody")
                val json = JSONObject(responseBody ?: "")
                try {
                    val tokenResponse = json
                        .getJSONObject("response")
                        .getJSONObject("payment")
                        .getString("token_id")

                    Log.d("DOKU_API", tokenResponse)
                    onResult(tokenResponse)

                } catch (e: Exception) {
                    Log.e("DOKU_API", "Failed to parse token_id: ${e.message}")
                    onResult("ERROR_DOKU;${json.getJSONArray("message").get(0)}")
                }
            }
        })
    }

    fun doShowPaymentMethod(tokenId: String, onResult: (ShowPaymentMethodResponse?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/v2/payment/show/$tokenId"
        Log.d("DOKU_API", "URL $url")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        Log.d("DOKU_API", "REQUEST $request")


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_SHOW_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DOKU_SHOW_API", "Response: $responseBody")

                try {
                    val gson = Gson()
                    val type = object : TypeToken<ShowPaymentMethodResponse>() {}.type
                    val response: ShowPaymentMethodResponse = gson.fromJson(responseBody, type)

                    onResult(response)

                } catch (e: Exception) {
                    Log.e("DOKU_API", "Parsing error: ${e.message}")

                }
            }
        })
    }

    fun doGenerateQRIS(request: PaymentRequest, onResult: (QRISPaymentResponse?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/v1/payment/${request.token_id}/generate-qris"
        val gson = Gson()
        Log.d("DOKU_API", "URL $url")
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        Log.d("DOKU_API", "REQUEST $jsonBody")


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DOKU_API", "Response: $responseBody")

                try {
                    if(response.code.toString().startsWith("20")) {
                        val gson = Gson()
                        val type = object : TypeToken<QRISPaymentResponse>() {}.type
                        val response: QRISPaymentResponse = gson.fromJson(responseBody, type)
                        Log.d("DOKU_API", "Response: $response")
                        onResult(response)
                    } else {
                        onResult(null)
                    }

                } catch (e: Exception) {
                    Log.e("DOKU_API", "Parsing error: ${e.message}")
                    onResult(null)
                }
            }
        })
    }

    fun doGenerateVa(request: PaymentRequest, onResult: (GenerateCodeResponse?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/v1/payment/${request.token_id}/generate-code"
        Log.d("DOKU_API", "URL $url")
        val gson = Gson()
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        Log.d("DOKU_API", "REQUEST $jsonBody")


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DOKU_API", "Response: $responseBody")

                try {
                    if(response.code.toString().startsWith("20")) {
                        val gson = Gson()
                        val type = object : TypeToken<GenerateCodeResponse>() {}.type
                        val response: GenerateCodeResponse = gson.fromJson(responseBody, type)
                        Log.d("DOKU_API", "Response: $response")

                        onResult(response)
                    } else {
                        onResult(null)
                    }

                } catch (e: Exception) {
                    onResult(null)
                    Log.e("DOKU_API", "Parsing error: ${e.message}")

                }
            }
        })
    }

    fun doShowPromos(tokenId: String, onResult: (ShowPromosResponse?) -> Unit) {
        Log.d("token",tokenId);
        val url = "${DokuConfig.baseUrl}/checkout/v2/promo/list/${tokenId}"
        Log.d("DOKU_API", "URL $url")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        Log.d("DOKU_API", "REQUEST $request")


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DOKU_API", "Response: $responseBody")

                try {
                    val gson = Gson()
                    val type = object : TypeToken<ShowPromosResponse>() {}.type
                    val response: ShowPromosResponse = gson.fromJson(responseBody, type)
                    Log.d("DOKU_API", "Response: $response")

                    onResult(response)

                } catch (e: Exception) {
                    Log.e("DOKU_API", "Parsing error: ${e.message}")

                }
            }
        })
    }

    fun doCheckStatusPayment(tokenId: String, onResult: (CheckPaymentStatusResponse?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/v1/payment/${tokenId}/check-status"
        Log.d("DOKU_API", "URL $url")
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        Log.d("DOKU_API", "REQUEST $request")


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DOKU_API", "Response: $responseBody")

                try {
                    val gson = Gson()
                    val type = object : TypeToken<CheckPaymentStatusResponse>() {}.type
                    val response: CheckPaymentStatusResponse = gson.fromJson(responseBody, type)
                    Log.d("DOKU_API", "Response: $response")

                    onResult(response)

                } catch (e: Exception) {
                    Log.e("DOKU_API", "Parsing error: ${e.message}")

                }
            }
        })
    }

    fun doPayJumpAppWallet(request: PaymentRequest, method: String, onResult: (JumpAppWalletResponse?) -> Unit) {
        var url = ""
        if (method == "emoney-doku") {
            url = "${DokuConfig.baseUrl}/checkout/v2/payment/$method/${request.token_id}"
        } else if(method == "emoney-shopeepay" || method == "emoney-dana" || method == "emoney-ovo") {
            url = "${DokuConfig.baseUrl}/checkout/v1/payment/${request.token_id}/$method"
        }

        Log.d("DOKU_API", "URL $url")
        val gson = Gson()
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        Log.d("DOKU_API", "REQUEST $jsonBody")
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DOKU_API", "Response: $responseBody")

                try {
                    if(response.code.toString().startsWith("20")) {
                        val gson = Gson()
                        val type = object : TypeToken<JumpAppWalletResponse>() {}.type
                        val response: JumpAppWalletResponse = gson.fromJson(responseBody, type)
                        Log.d("DOKU_API", "Response: $response")

                        onResult(response)
                    } else {
                        onResult(null)
                    }

                } catch (e: Exception) {
                    onResult(null)
                    Log.e("DOKU_API", "Parsing error: ${e.message}")

                }
            }
        })
    }

    fun doCardTokenization(tokenId: String, onResult: (List<CardTokenizeResponse>?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/h2h/tokenization/${tokenId}"
        val headers: Map<String, String> = mapOf(
            "Client-Id" to DokuConfig.clientId,
            "Request-Id" to DokuConfig.requestId,
        )
        Log.d("DOKU_API", "URL $url")
        Log.d("DOKU_API", "Header $headers")

        val requestBuilder = Request.Builder()
            .url(url)
            .get()
            .addHeader("Content-Type", "application/json")

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_TOKENIZE_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val gson = Gson()
                    Log.d("DOKU_API", "Response: $responseBody")
                    val type = object : TypeToken<List<CardTokenizeResponse>>() {}.type
                    val response: List<CardTokenizeResponse> = gson.fromJson(responseBody, type)

                    if(response.isNotEmpty()){
                        onResult(response)
                    } else {
                        onResult(null)
                    }

                } catch (e: Exception) {
                    Log.e("DOKU_API", "Parsing error: ${e.message}")
                    onResult(null)
                }
            }
        })
    }

    fun doDeleteCardTokenization(tokenId: String, tokenization: String, onResult: (DeleteCardTokenizeResponse?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/h2h/tokenization/${tokenId}/${tokenization}"
        val headers: Map<String, String> = mapOf(
            "Client-Id" to DokuConfig.clientId,
            "Request-Id" to DokuConfig.requestId,
            "accept" to "*/*"
        )
        Log.d("DOKU_API", "URL $url")
        Log.d("DOKU_API", "Header $headers")

        val requestBuilder = Request.Builder()
            .url(url)
            .delete()
            .addHeader("Content-Type", "application/json")

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val gson = Gson()
                    Log.d("DOKU_API", "Response: $responseBody")
                    val type = object : TypeToken<DeleteCardTokenizeResponse>() {}.type
                    val response: DeleteCardTokenizeResponse = gson.fromJson(responseBody, type)

                    onResult(response)

                } catch (e: Exception) {
                    Log.e("DOKU_API", "Parsing error: ${e.message}")
                    onResult(null)
                }
            }
        })
    }

    fun doGetBinInformation(tokenid: String, binNumber: String, onResult: (BinEligibilityResponse?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/h2h/installment/${binNumber}/$tokenid"
        val headers: Map<String, String> = mapOf(
            "Client-Id" to DokuConfig.clientId,
            "Request-Id" to DokuConfig.requestId,
            "accept" to "*/*"
        )
        Log.d("DOKU_API", "URL $url")
        Log.d("DOKU_API", "Header $headers")

        val requestBuilder = Request.Builder()
            .url(url)
            .get()
            .addHeader("Content-Type", "application/json")

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_TOKENIZE_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val gson = Gson()
                    Log.d("DOKU_API", "Response: $responseBody")
                    val type = object : TypeToken<BinEligibilityResponse>() {}.type
                    val response: BinEligibilityResponse = gson.fromJson(responseBody, type)

                    onResult(response)
                } catch (e: Exception) {
                    Log.e("DOKU_API", "Parsing error: ${e.message}")
                    onResult(null)
                }
            }
        })
    }

    fun doGeneratePublicKey(tokenid: String, onResult: (GenerateKeyResponse?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/h2h/generate/key/$tokenid"
        val headers: Map<String, String> = mapOf(
            "Client-Id" to DokuConfig.clientId,
            "Request-Id" to DokuConfig.requestId,
        )
        Log.d("DOKU_API", "URL $url")
        Log.d("DOKU_API", "Header $headers")

        val requestBuilder = Request.Builder()
            .url(url)
            .get()
            .addHeader("Content-Type", "application/json")

        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_TOKENIZE_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                try {
                    val gson = Gson()
                    Log.d("DOKU_API", "Response: $responseBody")
                    val type = object : TypeToken<GenerateKeyResponse>() {}.type
                    val response: GenerateKeyResponse = gson.fromJson(responseBody, type)

                    onResult(response)
                } catch (e: Exception) {
                    Log.e("DOKU_API", "Parsing error: ${e.message}")
                    onResult(null)
                }
            }
        })
    }

    fun doPayCard(request: PaymentRequest, onResult: (CardPaymentResponse?) -> Unit) {
        val url = "${DokuConfig.baseUrl}/checkout/h2h/payment"
        Log.d("DOKU_API", "URL $url")
        val gson = GsonBuilder()
            .disableHtmlEscaping()
            .create()
        val jsonBody = gson.toJson(request)
        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
        Log.d("DOKU_API", "REQUEST $jsonBody")
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()


        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("DOKU_API", "Error: ${e.message}")
                onResult(null)
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("DOKU_API", "Response: $responseBody")

                try {
                    if(response.code.toString().startsWith("20")) {
                        val gson = Gson()
                        val type = object : TypeToken<CardPaymentResponse>() {}.type
                        val response: CardPaymentResponse = gson.fromJson(responseBody, type)
                        Log.d("DOKU_API", "Response: $response")

                        onResult(response)
                    } else {
                        onResult(null)
                    }

                } catch (e: Exception) {
                    onResult(null)
                    Log.e("DOKU_API", "Parsing error: ${e.message}")

                }
            }
        })
    }
}