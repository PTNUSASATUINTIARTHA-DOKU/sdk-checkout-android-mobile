package com.doku.sdkcheckoutandroid.enum

enum class PaymentLogo(val key: String, val relativePath: String) {

    AKULAKU("AKULAKU", "financing/akulaku.png"),
    ALFA("ALFA", "convenience/alfamart.png"),
    ALLO("ALLO", "bank/allo-bank.png"),
    BANK_CIMB("BANK_CIMB", "bank/cimb.png"),
    BANK_DANAMON("BANK_DANAMON", "bank/danamon.png"),
    BANK_MANDIRI("BANK_MANDIRI", "bank/mandiri.png"),
    BANK_MUAMALAT("BANK_MUAMALAT_INDONESIA", "bank/muamalat.png"),
    BANK_PERMATA("BANK_PERMATA", "bank/permata.png"),
    BANK_SYARIAH_MANDIRI("BANK_SYARIAH_MANDIRI", "bank/mandirisyariah.png"),
    BANK_SAHABAT_SAMPOERNA("BANK_SAHABAT_SAMPOERNA", "bank/bss-primary-black.png"),
    BANK_TABUNGAN_PENSIUNAN_NASIONAL("BANK_TABUNGAN_PENSIUNAN_NASIONAL", "bank/btpn.png"),
    BANK_BJB("BANK_BJB", "bank/bjb.png"),
    BCA("BCA", "bank/bca.png"),
    BNC("BNC", "bank/bnc.png"),
    BNI("BNI", "bank/bni.png"),
    BRI("BRI", "bank/bri.png"),
    BRI_CERIA("BRI_CERIA", "bank/briceria.png"),
    BSS("BSS", "bank/bss-primary-black.png"),
    BTN("BTN", "bank/btn.png"),
    BTPN("BTPN", "bank/btpn.png"),
    DANA("DANA", "ewallet/DANA.png"),
    DOKU("DOKU", "ewallet/DOKU.png"),
    INDODANA("INDODANA", "financing/indodana.png"),
    INDOMARET("INDOMARET", "convenience/indomaret.png"),
    KREDIVO("KREDIVO", "financing/kredivo.png"),
    LINK_AJA("LINKAJA", "ewallet/Linkaja.png"),
    ISAKU("ISAKU", "ewallet/isaku.png"),
    MAYBANK("MAYBANK", "bank/maybank.png"),
    MDRA("MDRA", "bank/mandiri.png"),
    OVO("OVO", "ewallet/OVO.png"),
    SHOPEE_PAY("SHOPEE_PAY", "ewallet/ShopeePay-Horizontal2_O.png"),
    SINARMAS("SINARMAS", "bank/sinarmas.png"),
    HSBC("HSBC", "bank/hsbc.png"),
    CITIBANK("CITIBANK", "bank/citi.png"),
    ALTO("ALTO", "QRIS/ALTO.png"),
    VISA("VISA", "card/visa.png"),
    MASTER("MASTER", "card/mastercard.png"),
    JCB("JCB", "card/jcb.png"),
    AMEX("AMEX", "card/amex.png"),
    FALLBACK("FALLBACK", "error/404.png");

    companion object {
        private const val BASE_URL =
            "https://cdn-dev.oss-ap-southeast-5.aliyuncs.com/doku-ui-framework/doku/img/"

        fun from(key: String): PaymentLogo {
            return values().firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?: FALLBACK
        }
    }

    val fullUrl: String
        get() = BASE_URL + relativePath
}
