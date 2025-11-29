//package com.example.trackspend.data.remote
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//import java.time.format.DateTimeParseException
//
//import android.content.Context
//import android.os.Build
//import androidx.annotation.RequiresApi
//
///**
// * Smart hybrid parser:
// * 1. Detect tracking number
// * 2. Detect carrier
// * 3. Extract price
// * 4. Detect store from:
// *      a) sender domain (best)
// *      b) JSON store list (fallback)
// */
//object EmailParser {
//
//    private val knownCarriers = setOf(
//        "UPS", "FedEx", "USPS", "DHL", "DHL eCommerce",
//        "Canada Post", "Amazon Logistics", "China Post",
//        "Purolator", "Lasership", "Cainiao", "OnTrac",
//        "Royal Mail", "Hermes", "DPD", "GLS"
//    )
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun parse(context: Context, rawEmail: String): ParsedEmailInfo {
//        if (rawEmail.isBlank()) return ParsedEmailInfo()
//
//        // Normalize email body
//        val text = rawEmail.replace("\n", " ")
//
//        // Load store list FIRST (needed for many functions)
//        val storeList = StoreLoader.loadStoreList(context)
//
//        // Extract all pieces
//        val domain = extractSenderDomain(rawEmail)
//        val tracking = extractTracking(text)
//        val price = extractPrice(text)
//        val storeFromBody = detectStoreFromBody(text, storeList)
//        val itemName = extractItemName(text, storeList)
//        val orderDate = extractOrderDate(text)
//        val storeFromDomain = detectStoreFromDomain(domain)
//
//        // PRIORITY: Domain > Body
//        val finalStore = storeFromDomain ?: storeFromBody
//
//        // Detect carrier (uses tracking + store hint)
//        val carrier = detectCarrier(tracking, finalStore)
//
//        return ParsedEmailInfo(
//            trackingNumber = tracking,
//            carrier = carrier,
//            store = finalStore,
//            itemName = itemName,
//            price = price,
//            orderDate = orderDate,
//            senderDomain = domain
//        )
//    }
//
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun normalizeDate(raw: String): String? {
//        val cleaned = raw
//            .replace(",", "")
//            .replace("th", "", true)
//            .replace("st", "", true)
//            .replace("nd", "", true)
//            .replace("rd", "", true)
//            .trim()
//
//        val formats = listOf(
//            "yyyy-MM-dd",
//            "yyyy/MM/dd",
//            "MM/dd/yyyy",
//            "dd/MM/yyyy",
//            "MM-dd-yyyy",
//            "dd-MM-yyyy",
//            "MMM d yyyy",
//            "MMMM d yyyy"
//        )
//
//        for (pattern in formats) {
//            try {
//                val formatter = java.time.format.DateTimeFormatter.ofPattern(pattern)
//                val parsed = java.time.LocalDate.parse(cleaned, formatter)
//                return parsed.toString() // normalize to YYYY-MM-DD
//            } catch (_: Exception) { }
//        }
//
//        return null
//    }
//
//
//    // ----------------------------------------------
//    // Extract sender email domain
//    // ----------------------------------------------
//    private fun extractSenderDomain(text: String): String? {
//        val regex = Regex("""[A-Za-z0-9._%+-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})""")
//        val match = regex.find(text) ?: return null
//        return match.groupValues[1] // domain.com
//    }
//
//    // ----------------------------------------------
//    // Detect store from known domains
//    // ----------------------------------------------
//    private fun detectStoreFromDomain(domain: String?): String? {
//        domain ?: return null
//        val d = domain.lowercase()
//        return when {
//            "amazon" in d -> "Amazon"
//            "bestbuy" in d -> "Best Buy"
//            "walmart" in d -> "Walmart"
//            "ebay" in d -> "eBay"
//            "etsy" in d -> "Etsy"
//            "aliexpress" in d -> "AliExpress"
//            "apple" in d -> "Apple"
//            "nike" in d -> "Nike"
//            "adidas" in d -> "Adidas"
//            "shein" in d -> "Shein"
//            "ikea" in d -> "Ikea"
//            else -> null
//        }
//    }
//
//    // ----------------------------------------------
//    // Fallback: detect store from email body
//    // ----------------------------------------------
//    private fun detectStoreFromBody(text: String, storeList: List<String>): String? {
//        val lower = text.lowercase()
//
//        return storeList.firstOrNull { store ->
//            val s = store.lowercase()
//
//            // Store must NOT be a known carrier
//            s in lower && !knownCarriers.contains(store)
//        }
//    }
//
//
//    // ----------------------------------------------
//    // Tracking number extraction
//    // ----------------------------------------------
//    private fun extractTracking(text: String): String? {
//        val cleaned = text.replace("\n", " ")
//            .replace("-", " ")
//            .replace(" ", "")
//
//        val patterns = listOf(
//            // UPS
//            Regex("""1Z[0-9A-Z]{16}""", RegexOption.IGNORE_CASE),
//
//            // Amazon Logistics
//            Regex("""TBA[0-9]{12,15}""", RegexOption.IGNORE_CASE),
//
//            // FedEx
//            Regex("""\b[0-9]{12}\b"""),
//            Regex("""\b[0-9]{15}\b"""),
//            Regex("""\b[0-9]{20}\b"""),
//
//            // USPS
//            Regex("""\b[0-9]{20,22}\b"""),
//
//            // DHL
//            Regex("""\b[0-9]{10}\b"""),
//
//            // Canada Post
//            Regex("""[A-Z]{2}[0-9]{9}[A-Z]{2}""")
//        )
//
//        for (pattern in patterns) {
//            val match = pattern.find(cleaned)
//            if (match != null) return match.value
//        }
//
//        return null
//    }
//
//
//
//    // ----------------------------------------------
//    // Price extraction
//    // ----------------------------------------------
//    private fun extractPrice(text: String): Double? {
//        val lower = text.lowercase()
//
//        // Patterns in priority order:
//        val patterns = listOf(
//            // GRAND TOTAL 46.65
//            Regex("""grand\s*total[^0-9]*([0-9]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE),
//
//            // TOTAL 46.65 / Total: 46.65
//            Regex("""total[^0-9]*([0-9]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE),
//
//            // AMOUNT PAID: 46.65
//            Regex("""(amount paid|paid)[^0-9]*([0-9]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE),
//
//            // SUBTOTAL 54.10
//            Regex("""subtotal[^0-9]*([0-9]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE),
//
//            // $46.65 OR CAD 46.65 OR USD 46.65
//            Regex("""(?:\$|cad|usd)\s*([0-9]+(?:\.[0-9]{2})?)""", RegexOption.IGNORE_CASE)
//        )
//
//        for (regex in patterns) {
//            val match = regex.find(text)
//            if (match != null) {
//                // If pattern has 2 capturing groups use the last one
//                return match.groupValues.lastOrNull()?.toDoubleOrNull()
//            }
//        }
//
//        return null
//    }
//
//
//
//    // ----------------------------------------------
//    // Carrier detection
//    // ----------------------------------------------
//    private fun detectCarrier(tracking: String?, store: String? = null): String? {
//        val t = tracking ?: return null
//        val code = t.uppercase().trim()
//
//        // ---------------------------
//        // LAYER 1 — Tracking Patterns
//        // ---------------------------
//
//        if (code.startsWith("1Z") && code.length in 18..22)
//            return "UPS"
//
//        if (code.startsWith("TBA"))
//            return "Amazon Logistics"
//
//        if (code.endsWith("CA") && code.length in 12..16)
//            return "Canada Post"
//
//        if (code.length == 10 && code.all { it.isDigit() })
//            return "DHL"
//
//        if ((code.length == 12 || code.length == 15 || code.length == 20) &&
//            code.all { it.isDigit() })
//            return "FedEx"
//
//        if (code.length in 20..22 && code.all { it.isDigit() })
//            return "USPS"
//
//        if (Regex("[A-Z]{2}[0-9]{9}[A-Z]{2}").matches(code))
//            return "China Post"
//
//        if (code.length in 14..40 && code.any { it.isLetter() })
//            return "DHL eCommerce"
//
//        // ---------------------------
//        // LAYER 2 — Store-Based Hints
//        // ---------------------------
//        val s = store?.lowercase()
//
//        if (s != null) {
//            if ("costco" in s) return "UPS"
//            if ("amazon" in s) return "Amazon Logistics"
//            if ("best buy" in s) return "Canada Post"
//            if ("walmart" in s) return "Canada Post"
//            if ("apple" in s) return "UPS"
//            if ("nike" in s) return "UPS"
//            if ("adidas" in s) return "DHL"
//            if ("h&m" in s) return "DHL"
//            if ("aliexpress" in s) return "China Post"
//            if ("shein" in s) return "DHL eCommerce"
//            if ("sephora" in s) return "Canada Post"
//            if ("zara" in s) return "DHL"
//        }
//
//        // ---------------------------
//        // LAYER 3 — Validation Rules
//        // ---------------------------
//        if (code.length < 6 || code.length > 40)
//            return null
//
//        if (code.length >= 26 && code.all { it.isDigit() })
//            return null
//
//        return "Unknown"
//    }
//
//
//
//
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun extractOrderDate(text: String): String? {
//        val lower = text.lowercase()
//
//        val dateKeywords = listOf(
//            "order date", "ordered on", "date placed", "placed on",
//            "purchase date", "payment date", "purchased on"
//        )
//
//        val datePatterns = listOf(
//            // January 12, 2024
//            Regex("""([A-Za-z]+ \d{1,2}, \d{4})"""),
//
//            // Jan 12 2024
//            Regex("""([A-Za-z]{3} \d{1,2} \d{4})"""),
//
//            // 2024-01-12
//            Regex("""(\d{4}-\d{2}-\d{2})"""),
//
//            // 12/01/2024 or 1/5/24
//            Regex("""(\d{1,2}/\d{1,2}/\d{2,4})""")
//        )
//
//        for (keyword in dateKeywords) {
//            val idx = lower.indexOf(keyword)
//            if (idx != -1) {
//                val substring = text.substring(idx)
//
//                for (pattern in datePatterns) {
//                    val match = pattern.find(substring)
//                    if (match != null) {
//                        return normalizeDate(match.groupValues[1])
//                    }
//                }
//            }
//        }
//
//        return null
//    }
//
//    private fun extractItemName(text: String, storeList: List<String>): String? {
//        val lines = text.split("\n")
//
//        val itemKeywords = listOf(
//            "item:", "product:", "description:", "you bought",
//            "ordered:", "order details", "item name:", "product name:"
//        )
//
//        for (line in lines) {
//            val lower = line.lowercase()
//
//            if (itemKeywords.any { lower.contains(it) }) {
//
//                // Get potential item name
//                val cleaned = line.substringAfter(":").trim()
//
//                // Validation rules
//                if (cleaned.length !in 3..60) continue
//                if (cleaned.any { it.isDigit() } && cleaned.filter { it.isDigit() }.length > 5) continue // rejects tracking numbers
//                if (cleaned.lowercase() in storeList.map { it.lowercase() }) continue
//                if (knownCarriers.contains(cleaned)) continue
//
//                return cleaned
//            }
//        }
//
//        return null
//    }
//
//
//}

package com.example.trackspend.data.remote

import android.R.attr.text
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Smart hybrid email parser:
 *
 * Extracts:
 *  - tracking number
 *  - carrier
 *  - store (from domain + text)
 *  - price (grand total / total / paid)
 *  - order date
 *  - item name (keyword-based)
 *
 * Goal: Autofill Add Package screen with minimal errors.
 */
object EmailParser {

    private val knownCarriers = setOf(
        "UPS", "FedEx", "USPS", "DHL", "DHL eCommerce",
        "Canada Post", "Amazon Logistics", "China Post",
        "Purolator", "Lasership", "Cainiao", "OnTrac",
        "Royal Mail", "Hermes", "DPD", "GLS"
    )

    // ============================================================
    // MAIN PARSE FUNCTION
    // ============================================================
    @RequiresApi(Build.VERSION_CODES.O)
    fun parse(context: Context, rawEmail: String): ParsedEmailInfo {
        if (rawEmail.isBlank()) return ParsedEmailInfo()

        val normalizedText = rawEmail.replace("\n", " ")

        val storeList = StoreLoader.loadStoreList(context)

        val domain = extractSenderDomain(rawEmail)
        val tracking = extractTracking(normalizedText)
        val price = extractPrice(normalizedText)
        val storeFromBody = detectStoreFromBody(normalizedText, storeList)
        val itemName = extractItemName(rawEmail, storeList)
        val orderDate = extractOrderDate(rawEmail)
        val storeFromDomain = detectStoreFromDomain(domain)

        val finalStore = storeFromDomain ?: storeFromBody
        val carrierFromBody = extractCarrierFromBody(normalizedText)
        val carrier = carrierFromBody ?: detectCarrier(tracking, finalStore)

        return ParsedEmailInfo(
            trackingNumber = tracking,
            carrier = carrier,
            store = finalStore,
            itemName = itemName,
            price = price,
            orderDate = orderDate,
            senderDomain = domain
        )
    }

    // ============================================================
    // SENDER DOMAIN
    // ============================================================
    private fun extractSenderDomain(text: String): String? {
        val regex = Regex("""[A-Za-z0-9._%+-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})""")
        return regex.find(text)?.groupValues?.get(1)
    }

    // ============================================================
    // STORE DETECTION — FROM DOMAIN
    // ============================================================
    private fun detectStoreFromDomain(domain: String?): String? {
        val d = domain?.lowercase() ?: return null
        return when {
            "amazon" in d -> "Amazon"
            "bestbuy" in d -> "Best Buy"
            "walmart" in d -> "Walmart"
            "ebay" in d -> "eBay"
            "etsy" in d -> "Etsy"
            "aliexpress" in d -> "AliExpress"
            "apple" in d -> "Apple"
            "nike" in d -> "Nike"
            "adidas" in d -> "Adidas"
            "shein" in d -> "Shein"
            "ikea" in d -> "Ikea"
            else -> null
        }
    }

    // ============================================================
    // STORE DETECTION — FROM BODY
    // ============================================================
    private fun detectStoreFromBody(text: String, stores: List<String>): String? {
        val lower = text.lowercase()
        return stores.firstOrNull { store ->
            val s = store.lowercase()
            s in lower && !knownCarriers.contains(store)
        }
    }

    // ============================================================
    // TRACKING EXTRACTION
    // ============================================================
    private fun extractTracking(text: String): String? {
        val cleaned = text.replace("\n", "").replace("-", "").replace(" ", "")

        val patterns = listOf(
            Regex("""1Z[0-9A-Z]{16}""", RegexOption.IGNORE_CASE),  // UPS
            Regex("""TBA[0-9]{12,15}""", RegexOption.IGNORE_CASE), // Amazon Logistics
            Regex("""\b[0-9]{12}\b"""),                            // FedEx 12
            Regex("""\b[0-9]{15}\b"""),                            // FedEx 15
            Regex("""\b[0-9]{20}\b"""),                            // FedEx 20
            Regex("""\b[0-9]{20,22}\b"""),                         // USPS
            Regex("""\b[0-9]{10}\b"""),                            // DHL
            Regex("""[A-Z]{2}[0-9]{9}CA""", RegexOption.IGNORE_CASE) // Canada Post
        )

        return patterns.firstNotNullOfOrNull { pattern ->
            pattern.find(cleaned)?.value?.takeIf { candidate ->
                candidate.any { it.isDigit() } &&
                        candidate.length in 8..40
            }
        }


    }

    // ============================================================
    // PRICE EXTRACTION
    // ============================================================
    private fun extractPrice(text: String): Double? {
        val lower = text.lowercase()

        val patterns = listOf(
            Regex("""grand\s*total[^0-9]*([0-9]+\.[0-9]{2})""", RegexOption.IGNORE_CASE),
            Regex("""total[^0-9]*([0-9]+\.[0-9]{2})""", RegexOption.IGNORE_CASE),
            Regex("""(amount paid|paid)[^0-9]*([0-9]+\.[0-9]{2})""", RegexOption.IGNORE_CASE),
            Regex("""subtotal[^0-9]*([0-9]+\.[0-9]{2})""", RegexOption.IGNORE_CASE),
            Regex("""(?:cad|usd|\$)\s*([0-9]+\.[0-9]{2})""", RegexOption.IGNORE_CASE)
        )

        for (regex in patterns) {
            val match = regex.find(lower)
            if (match != null) {
                return match.groupValues.last().toDoubleOrNull()
            }
        }

        return null
    }

    // ============================================================
    // CARRIER DETECTION
    // ============================================================
    private fun detectCarrier(tracking: String?, store: String?): String? {
        tracking ?: return null
        val code = tracking.uppercase()

        // Tracking-based
        return when {
            code.startsWith("1Z") -> "UPS"
            code.startsWith("TBA") -> "Amazon Logistics"
            code.endsWith("CA") && code.length in 12..16 -> "Canada Post"
            code.length == 10 && code.all { it.isDigit() } -> "DHL"
            (code.length == 12 || code.length == 15 || code.length == 20) && code.all { it.isDigit() } -> "FedEx"
            code.length in 20..22 && code.all { it.isDigit() } -> "USPS"
            Regex("[A-Z]{2}[0-9]{9}[A-Z]{2}").matches(code) -> "China Post"
            code.length in 14..40 && code.any { it.isLetter() } -> "DHL eCommerce"
            else -> {
                // Store-based fallback
                val s = store?.lowercase()
                when {
                    s == null -> "Unknown"
                    "costco" in s -> "UPS"
                    "amazon" in s -> "Amazon Logistics"
                    "best buy" in s -> "Canada Post"
                    "walmart" in s -> "Canada Post"
                    "apple" in s -> "UPS"
                    "nike" in s -> "UPS"
                    "adidas" in s -> "DHL"
                    "aliexpress" in s -> "China Post"
                    "shein" in s -> "DHL eCommerce"
                    else -> "Unknown"
                }
            }
        }
    }


    private fun extractCarrierFromBody(text: String): String? {
        val carriers = listOf(
            "FedEx", "FedEx Ground", "FedEx Express",
            "UPS", "United Parcel Service",
            "USPS", "Canada Post",
            "DHL", "DHL eCommerce",
            "Purolator", "OnTrac"
        )

        val lower = text.lowercase()
        return carriers.firstOrNull { c ->
            lower.contains(c.lowercase())
        }
    }


    // ============================================================
    // ORDER DATE EXTRACTION
    // ============================================================
    @RequiresApi(Build.VERSION_CODES.O)
    private fun extractOrderDate(text: String): String? {
        val lower = text.lowercase()

        val dateKeywords = listOf(
            "order date", "ordered on", "date placed", "placed on",
            "purchase date", "payment date", "purchased on"
        )

        val datePatterns = listOf(
            Regex("""([A-Za-z]+ \d{1,2}, \d{4})"""),
            Regex("""([A-Za-z]{3} \d{1,2} \d{4})"""),
            Regex("""(\d{4}-\d{2}-\d{2})"""),
            Regex("""(\d{1,2}/\d{1,2}/\d{2,4})""")
        )

        for (keyword in dateKeywords) {
            if (!lower.contains(keyword)) continue
            val index = lower.indexOf(keyword)
            val slice = text.substring(index)

            for (pattern in datePatterns) {
                val m = pattern.find(slice) ?: continue
                return normalizeDate(m.groupValues[1])
            }
        }

        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun normalizeDate(raw: String): String? {
        val cleaned = raw
            .replace(",", "")
            .replace("st", "", true)
            .replace("nd", "", true)
            .replace("rd", "", true)
            .replace("th", "", true)
            .trim()

        val formats = listOf(
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "MMM d yyyy",
            "MMMM d yyyy"
        )

        for (f in formats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(f)
                return LocalDate.parse(cleaned, formatter).toString()
            } catch (_: Exception) {
            }
        }

        return null
    }

    // ============================================================
    // ITEM NAME EXTRACTION
    // ============================================================
    private fun extractItemName(text: String, storeList: List<String>): String? {
        val lines = text.split("\n")

        val itemKeywords = listOf(
            "item:", "product:", "description:",
            "item name:", "product name:"
        )

        for (line in lines) {
            val lower = line.lowercase().trim()

            // Skip lines with no colon (like "Order details")
            if (!lower.contains(":")) continue

            if (itemKeywords.any { lower.startsWith(it.removeSuffix(":")) }) {

                val cleaned = line.substringAfter(":").trim()

                // Reject empty / invalid results
                if (cleaned.isBlank()) continue
                if (cleaned.length !in 3..60) continue
                if (cleaned.all { it.isDigit() }) continue   // reject numbers-only
                if (cleaned.lowercase() in storeList.map { it.lowercase() }) continue
                if (knownCarriers.contains(cleaned)) continue

                return cleaned
            }
        }

        return null
    }
}

