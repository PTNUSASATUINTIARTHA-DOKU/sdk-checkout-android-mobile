# DOKUCheckout Android SDK

DOKU Checkout SDK provides a seamless payment integration for iOS merchant applications, supporting various payment channels including **Cards**, **Virtual Accounts (Bank Transfer)**, **E-Wallet**, **QRIS**, **Convenience Stores**, and **Paylater**.

## Requirements

- Android Gradle Plugin 8.4 or later
- Gradle Wrapper 9.0 or later

## Installation

### 1. Github Account Setup

Every developer (or CI/CD system) that needs to download this SDK will require a GitHub token with read permissions.

1. Go to your GitHub account -> Settings -> Developer settings -> Personal Access Tokens -> Tokens (classic).
2. Click Generate new token (classic).
3. Give it a descriptive name (for example: Read DOKU SDK).
4. Check the read:packages scope (read-only access).
5. Click Generate token at the bottom of the page and copy the generated token code.

### 2. Secure Credentials in local.properties
Open the local.properties file in the Android project that will consume this SDK (the target project), then paste your credentials at the very bottom line:
```
gpr.user=USERNAME_GITHUB
gpr.key=ghp_TOKEN_GENERATED
```

### 3. Register the Repository in ```settings.gradle.kts```
Open the settings.gradle.kts file (located in your project's root directory). We need to add the GitHub Packages Maven URL into the dependencyResolutionManagement block so Gradle knows where to look for the SDK.

```
maven {
      name = "DOKUCheckoutSDK"
      url = uri("https://maven.pkg.github.com/PTNUSASATUINTIARTHA-DOKU/sdk-checkout-android-mobile")

      val localProperties = java.util.Properties()
      val localPropertiesFile = rootProject.projectDir.resolve("local.properties")
      if (localPropertiesFile.exists()) {
          localPropertiesFile.inputStream().use { localProperties.load(it) }
      }

      credentials {
          username = localProperties.getProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
          password = localProperties.getProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
      }
  }
```

### 4. Add the SDK to the App Module ```build.gradle.kts```
Now that the repository is connected, open the build.gradle.kts file located inside the app folder (not the project's root directory), then add the following line inside the dependencies block:
```
dependencies {
  ...
  implementation("com.doku.sdk-checkout:doku-checkout:1.0.0")
  ...
}
```

### 5. Sync Project
Click "Sync Project with Gradle Files" (the elephant icon in the top right corner of Android Studio). Gradle will authenticate with GitHub using your token, download the .aar file of the DOKU SDK, and integrate it into your project.

## Usage

### 1. Import the SDK and Init SDK

```kotlin
...
import com.doku.sdkcheckoutandroid.*
...
DokuConfig.initDb(this)
```

### 2. Prepare the Checkout Request

Build the `CheckoutRequest` object containing order details, payment configuration, customer information, and addresses.

#### Order

```kotlin
val lineItems = listOf(
    LineItem(
        id = "001",
        name = "Sepatu",
        quantity = 1,
        price = 10000,
        sku = "",
        category = "alas kaki",
        url = "https://example.com/product/001",
        image_url = "https://example.com/product/001/image.jpg",
        type = "-"
    )
)

val invoiceNumber: String = "INV-SDK-4008"
val order = Order(
    amount = 10000,
    invoice_number = invoiceNumber,
    currency = "IDR",
    callback_url = "",
    callback_url_cancel = "",
    callback_url_result = "",
    language = "EN",
    auto_redirect = false,
    recover_abandoned_cart = false,
    expired_recovered_cart = 0,
    disable_retry_payment = false,
    line_items = lineItems
)
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `amount` | `Double` | ✅ | Total payment amount |
| `invoice_number` | `String` | ✅ | Unique invoice number for the transaction |
| `currency` | `String` | ✅ | Currency code (e.g., `"IDR"`) |
| `callback_url` | `String?` | ❌ | URL to redirect after payment |
| `language` | `String?` | ❌ | Language code (`"EN"` or `"ID"`) |
| `auto_redirect` | `Bool?` | ❌ | Auto redirect after payment |
| `disable_retry_payment` | `Bool?` | ❌ | Disable retry on failed payment |
| `line_items` | `[LineItem]?` | ❌ | List of purchased items |

#### Payment

```kotlin
val payment: Payment = Payment(
        payment_due_date = 4,
        type = "SALE"
    )
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `payment_due_date` | `Int` | ✅ | Payment expiry duration in minutes |
| `type` | `String?` | ❌ | Payment type (e.g., `"SALE"`) |
| `payment_method_types` | `[String]?` | ❌ | Filter specific payment methods |

#### Customer

```kotlin
val customer: Customer = Customer(
      id="doku022",
      name = "Passenger",
      last_name = "Passenger",
      phone = "6281716612",
      email = "reza@abc.com",
      address = "Jl Bahagia",
      postcode = "12121",
      state = "IDN",
      city = "JKT",
      country = "ID"
  )
```

| Parameter | Type | Required | Description |
|---|---|---|---|
| `id` | `String` | ✅ | Unique customer identifier |
| `name` | `String` | ✅ | Customer first name |
| `last_name` | `String?` | ❌ | Customer last name |
| `phone` | `String` | ✅ | Customer phone number |
| `email` | `String` | ✅ | Customer email address |
| `address` | `String?` | ❌ | Customer address |
| `postcode` | `String?` | ❌ | Postal code |
| `state` | `String?` | ❌ | State/Province |
| `city` | `String?` | ❌ | City |
| `country` | `String?` | ❌ | Country code (e.g., `"ID"`) |

#### Address (Shipping & Billing)

```kotlin
val address = Address(
    first_name = "Joe",
    last_name = "Doe",
    address = "Jalan DOKU no 15",
    city = "Jakarta",
    postal_code = "11923",
    phone = "081312345678",
    country_code = "ID"
)
```

#### Additional Info (Optional)

```kotlin
val additionalInfo = AdditionalInfo(
      allow_tenor = listOf(3, 6),
      doku_wallet_notify_url = "https://dw-notify.free.beeceptor.com",
      override_notification_url = "https://dw-notify.free.beeceptor.com"
  )
```

#### Assemble the Checkout Request

```kotlin
val checkoutRequest: CheckoutRequest = CheckoutRequest(
    order = order,
    payment = payment,
    customer = customer,
    shipping_address = address,
    additional_info = additionalInfo,
    billing_address = address,
)
```

### 3. Generate Signature

Before initializing the SDK, you need to generate a **Digest** and **Signature** for request authentication. This should ideally be done on your **backend server** to protect the `secretKey`.

> ⚠️ **Security Warning**: The example below generates the signature on the client side for demonstration purposes only. In production, **always generate the signature on your backend server** and pass it to the mobile app.

```kotlin
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
```

**Usage:**

```kotlin
val requestId = UUID.randomUUID().toString()
val timestamp = Helper().getIsoTimestamp()
val clientId = "YOUR_CLIENT_ID"
val secretKey = "YOUR_SECRET_KEY"
val requestTarget = "/checkout/v1/payment"
val gson = Gson()
val jsonBody = gson.toJson(checkoutRequest)
val signature = generateDokuSignature(
    clientId,
    requestId,
    timestamp,
    requestTarget,
    jsonBody,
    secretKey
)
```

### 4. Initialize & Present Checkout

```kotlin
// Configure the SDK
val sheet = com.doku.sdkcheckoutandroid.PaymentBottomSheet.newInstance(
    clientId = clientId,
    requestId = requestId,
    requestTimestamp = timestamp,
    invoiceNumber = invoiceNumber,
    checkoutRequest = checkoutRequest,
    signatureKey = signature,
    production = false,
    colorPallete = "#000000"
)

// Initialize and present
sheet.setOnSdkCompleteListener(object: OnSdkComplete {
    override fun onPaymentComplete(status: String) {
        Log.d("DOKU_MERCHANT_APP", "PAYMENT_STATUS: $status")
    }
})
sheet.show(parentFragmentManager, "PaymentBottomSheet")
```

#### SDKConfig Parameters

| Parameter | Type | Required | Description |
|---|---|---|---|
| `clientId` | `String` | ✅ | Merchant Client ID from DOKU |
| `requestId` | `String` | ✅ | Unique request identifier (UUID) |
| `requestTimestamp` | `String` | ✅ | ISO 8601 formatted timestamp |
| `signatureKey` | `String` | ✅ | HMAC-SHA256 signature |
| `invoiceNumber` | `String` | ✅ | Invoice number matching the order |
| `environment` | `Environment` | ✅ | `.sandbox` or `.production` |
| `colorPallete` | `String?` | ❌ | Custom primary color hex (e.g., `"#FF5733"`) |

#### Environment Options

| Value | Description |
|---|---|
| `.sandbox` | For testing and development (`api-sandbox.doku.com`) |
| `.production` | For live transactions (`api.doku.com`) |

## Complete Example

```kotlin
class NotificationsFragment : Fragment() {

    private var _binding: CheckoutFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CheckoutFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title =
            arguments?.getString("title") ?: getString(R.string.title_notifications)

        binding.btnProceedPayment.setOnClickListener {

            val lineItems = listOf(
                LineItem(
                    id = "001",
                    name = "Sepatu",
                    quantity = 1,
                    price = 10000,
                    sku = "",
                    category = "alas kaki",
                    url = "https://example.com/product/001",
                    image_url = "https://example.com/product/001/image.jpg",
                    type = "-"
                )
            )
            val invoiceNumber: String = "INV-SDK-4008"
            val order = Order(
                amount = 10000,
                invoice_number = invoiceNumber,
                currency = "IDR",
                callback_url = "",
                callback_url_cancel = "",
                callback_url_result = "",
                language = "EN",
                auto_redirect = false,
                recover_abandoned_cart = false,
                expired_recovered_cart = 0,
                disable_retry_payment = false,
                line_items = lineItems
            )

            val payment: Payment = Payment(
                payment_due_date = 4,
                type = "SALE"
            )

            val customer: Customer = Customer(
                id="doku022",
                name = "Passenger",
                last_name = "Passenger",
                phone = "6281716612",
                email = "reza@abc.com",
                address = "Jl Bahagia",
                postcode = "12121",
                state = "IDN",
                city = "JKT",
                country = "ID"
            )

            val address = Address(
                first_name = "Joe",
                last_name = "Doe",
                address = "Jalan DOKU no 15",
                city = "Jakarta",
                postal_code = "11923",
                phone = "081312345678",
                country_code = "ID"
            )

            val additionalInfo = AdditionalInfo(
                allow_tenor = listOf(3, 6),
                doku_wallet_notify_url = "https://dw-notify.free.beeceptor.com",
                override_notification_url = "https://dw-notify.free.beeceptor.com"
            )

            val checkoutRequest: CheckoutRequest = CheckoutRequest(
                order = order,
                payment = payment,
                customer = customer,
                shipping_address = address,
                additional_info = additionalInfo,
                billing_address = address,
            )
            val requestId = UUID.randomUUID().toString()
            val timestamp = Helper().getIsoTimestamp()
            val clientId = "YOUR_CLIENT_ID"
            val requestTarget = "/checkout/v1/payment"
            val gson = Gson()
            val jsonBody = gson.toJson(checkoutRequest)
            val signature = generateDokuSignature(
                clientId,
                requestId,
                timestamp,
                requestTarget,
                jsonBody,
                "YOUR_SECRET_KEY"
            )
            val sheet = com.doku.sdkcheckoutandroid.PaymentBottomSheet.newInstance(
                clientId = clientId,
                requestId = requestId,
                requestTimestamp = timestamp,
                invoiceNumber = invoiceNumber,
                checkoutRequest = checkoutRequest,
                signatureKey = signature,
                production = false,
                colorPallete = "#000000"
            )
            sheet.setOnSdkCompleteListener(object: OnSdkComplete {
                override fun onPaymentComplete(status: String) {
                    Log.d("DOKU_MERCHANT_APP", "PAYMENT_STATUS: $status")
                }
            })
            sheet.show(parentFragmentManager, "PaymentBottomSheet")
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

## Supported Payment Channels

| Category | Payment Channels |
|---|---|
| **Cards** | Visa, Mastercard, JCB |
| **Virtual Account** | BCA, BNI, BRI, Mandiri, and more |
| **E-Wallet** | OVO, DANA, ShopeePay, LinkAja, and more |
| **QRIS** | QRIS standard supported across e-wallets and banks |
| **Convenience Store** | Alfamart, Indomaret |
| **Paylater** | Akulaku, Kredivo, and more |

## License

DOKUCheckout is available under the MIT license. See the [LICENSE](LICENSE) file for more details.

---

© 2026 PT. Nusa Satu Inti Artha (DOKU). All rights reserved.
