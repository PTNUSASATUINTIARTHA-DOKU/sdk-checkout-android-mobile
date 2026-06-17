import android.util.Base64
import android.util.Log
import android.util.Patterns
import com.doku.sdkcheckoutandroid.R
import com.doku.sdkcheckoutandroid.enum.PaymentMethodEnum
import com.doku.sdkcheckoutandroid.model.response.PaymentDetail
import com.doku.sdkcheckoutandroid.model.response.ShowPaymentMethodResponse
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class Helper {
    fun getIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun generateDokuSignature(
        clientId: String,
        requestId: String,
        requestTimestamp: String,
        requestTarget: String,
        requestBody: String,
        clientSecret: String
    ): String {
        val digest = if (requestBody.isNotEmpty()) generateDigest(requestBody) else ""
        val rawSignature = buildString {
            append("Client-Id:$clientId\n")
            append("Request-Id:$requestId\n")
            append("Request-Timestamp:$requestTimestamp\n")
            append("Request-Target:$requestTarget")
            if (digest.isNotEmpty()) append("\nDigest:$digest")
        }

        val hmac = hmacSha256(rawSignature, clientSecret)
        return "HMACSHA256=$hmac"
    }

    private fun generateDigest(body: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(body.trim().toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun hmacSha256(data: String, key: String): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(secretKeySpec)
        val rawHmac = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(rawHmac, Base64.NO_WRAP)
    }

    fun mappingCategoryPaymentMethod(category: String): PaymentMethodEnum {
        val method = when (category) {
            "QRIS" -> PaymentMethodEnum.QRIS
            "VIRTUAL_ACCOUNT" -> PaymentMethodEnum.BANK_TRANSFER
            "ONLINE_TO_OFFLINE" -> PaymentMethodEnum.CONVENIENCE_STORE
            "DANA" -> PaymentMethodEnum.DANA
            "SHOPEE_PAY" -> PaymentMethodEnum.SHOPEE_PAY
            "OVO" -> PaymentMethodEnum.OVO_NAVIGATE
            "LINKAJA" -> PaymentMethodEnum.LINK_AJA_NAVIGATE
            "DOKU" -> PaymentMethodEnum.DOKU_NAVIGATE
            "CREDIT_CARD" -> PaymentMethodEnum.CREDIT_CARD
            else -> PaymentMethodEnum.NONE
        }
        return method
    }

    fun updateCategoryName(category: String): String {
        return when (category) {
            "VIRTUAL_ACCOUNT" -> "Bank Transfer"
            "EMONEY" -> "e-Wallet"
            "PEER_TO_PEER" -> "Pay Later"
            "DIRECT_DEBIT" -> "Direct Debit"
            "QRIS" -> "QRIS"
            "CREDIT_CARD" -> "Credit Card"
            "ONLINE_TO_OFFLINE" -> "Convenience Store"
            else -> category
        }
    }

    fun updateCategoryIcon(category: String): Int {
        return when (category) {
            "VIRTUAL_ACCOUNT" -> R.drawable.ic_bank
            "EMONEY" -> R.drawable.ic_wallet
            "PEER_TO_PEER" -> R.drawable.ic_paylater
            "DIRECT_DEBIT" -> R.drawable.ic_direct_deb
            "QRIS" -> R.drawable.ic_qris
            "CREDIT_CARD" -> R.drawable.ic_card
            "ONLINE_TO_OFFLINE" -> R.drawable.ic_convinience
            else -> R.drawable.ic_wallet
        }
    }

    fun convertDateFormat(isoDate: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX", Locale.US)
            val outputFormat = java.text.SimpleDateFormat("dd MMMM yyyy, hh.mm a", Locale.US)

            val date = inputFormat.parse(isoDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            Log.d("DOKU_SDK", "Error Parse ${e.toString()}")
            "Invalid Date"
        }
    }

    fun isValidPhoneNumber(phone: String, prefix: String): Boolean {
        return phone.matches(Regex("^${prefix}[0-9]{8,15}$"))
    }

    fun isValidEmailOrPhone(input: String): Boolean {
        val isEmail = Patterns.EMAIL_ADDRESS.matcher(input).matches()

        val isPhone = input.matches(Regex("^[0-9]{8,15}$"))

        return isEmail || isPhone
    }

    fun loadPublicKey(publicKeyPem: String): PublicKey {
        val cleanKey = publicKeyPem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.decode(cleanKey, Base64.DEFAULT)
        val spec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(spec)
    }

    fun rsaEncrypt(data: String, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }
}

fun Double.asIDR(): String {
    val formatter = java.text.NumberFormat.getInstance().apply {
        maximumFractionDigits = 0
    }

    // Replace default separators to match Indonesian format
    val formatted = formatter.format(this)
        .replace(".", ",")   // grouping separator
        .replace(",", ".")   // decimal separator (not used since no decimals)

    return "IDR $formatted"
}

fun ShowPaymentMethodResponse.paymentDetail(
    category: String,
    keyword: String
): PaymentDetail? {

    val target = keyword.uppercase()

    return payment_method_type
        ?.firstOrNull { it.category_name == category }
        ?.detail
        ?.firstOrNull {
            it.payment_channel_id
                ?.uppercase()
                ?.contains(target) == true
        }
}