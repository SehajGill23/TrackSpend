package com.example.trackspend.data.remote

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * EmailParser performs intelligent extraction of order information
 * from raw email text. This acts as the "auto-fill brain" for the
 * Add Package screen.
 *
 * Responsibilities:
 *  - Detect tracking number using multiple carrier-specific patterns
 *  - Identify carrier using tracking patterns, keywords, or store fallback
 *  - Find store from sender domain or email body match
 *  - Extract price using flexible regex patterns
 *  - Detect order date in multiple formats and normalize it
 *  - Pull item name from product-related keywords
 *
 * This parser is intentionally fault-tolerant and aims for the
 * “best possible guess” instead of perfect accuracy.
 *
 * All parsing is done offline — no network calls.
 */
object EmailParser {

    private val knownCarriers = setOf(
        "UPS", "FedEx", "USPS", "DHL", "DHL eCommerce",
        "Canada Post", "Amazon Logistics", "China Post",
        "Purolator", "Lasership", "Cainiao", "OnTrac",
        "Royal Mail", "Hermes", "DPD", "GLS"
    )

    /**
     * Main entry point for parsing an email.
     *
     * Orchestrates all sub-parsers to extract:
     *  - tracking number
     *  - carrier
     *  - store (domain or body)
     *  - price
     *  - order date
     *  - item name
     *
     * @param context used to load the store list
     * @param rawEmail full email body and header text
     * @return ParsedEmailInfo containing all extracted fields
     */
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

    /**
     * Extracts the sender's domain from the email header section.
     *
     * Example:
     *   "order@nike.com" → "nike.com"
     *
     * @return domain string or null if none found
     */
    private fun extractSenderDomain(text: String): String? {
        val regex = Regex("""[A-Za-z0-9._%+-]+@([A-Za-z0-9.-]+\.[A-Za-z]{2,})""")
        return regex.find(text)?.groupValues?.get(1)
    }

    /**
     * Maps a sender domain to a known store (e.g., nike.com → nike).
     *
     * @param domain sender domain (may be null)
     * @return store name or null if the domain does not match any known store
     */
    private fun detectStoreFromDomain(domain: String?): String? {
        val d = domain?.lowercase() ?: return null
        return when {
            // Tech & Electronics
            "amazon" in d -> "Amazon"
            "apple" in d -> "Apple"
            "bestbuy" in d || "best buy" in d -> "Best Buy"
            "microsoft" in d -> "Microsoft"
            "samsung" in d -> "Samsung"
            "newegg" in d -> "Newegg"
            "bhphotovideo" in d || "b&h" in d -> "B&H Photo"
            "dell" in d -> "Dell"
            "hp store" in d -> "HP"

            // General Retail & Department Stores
            "walmart" in d -> "Walmart"
            "target" in d -> "Target"
            "costco" in d -> "Costco"
            "ebay" in d -> "eBay"
            "aliexpress" in d -> "AliExpress"
            "temu" in d -> "Temu"
            "dhgate" in d -> "DHGate"
            "rakuten" in d -> "Rakuten"
            "macys" in d || "macy's" in d -> "Macy's"
            "nordstrom" in d -> "Nordstrom"
            "kohls" in d || "kohl's" in d -> "Kohl's"
            "bloomingdales" in d -> "Bloomingdale's"

            // Fashion & Apparel
            "nike" in d -> "Nike"
            "adidas" in d -> "Adidas"
            "shein" in d -> "Shein"
            "zara" in d -> "Zara"
            "h&m" in d || "h & m" in d -> "H&M"
            "uniqlo" in d -> "Uniqlo"
            "asos" in d -> "ASOS"
            "lululemon" in d -> "Lululemon"
            "gucci" in d -> "Gucci"
            "louis vuitton" in d -> "Louis Vuitton"
            "victoria" in d && "secret" in d -> "Victoria's Secret"
            "gap" in d -> "Gap"
            "old navy" in d -> "Old Navy"
            "levi" in d -> "Levi's"
            "urban outfitters" in d -> "Urban Outfitters"
            "ssense" in d -> "SSENSE"
            "farfetch" in d -> "Farfetch"

            // Home & Furniture
            "ikea" in d -> "IKEA"
            "wayfair" in d -> "Wayfair"
            "home depot" in d -> "The Home Depot"
            "lowes" in d || "lowe's" in d -> "Lowe's"
            "pottery barn" in d -> "Pottery Barn"
            "west elm" in d -> "West Elm"
            "crate" in d && "barrel" in d -> "Crate & Barrel"
            "williams sonoma" in d -> "Williams Sonoma"

            // Beauty & Health
            "sephora" in d -> "Sephora"
            "ulta" in d -> "Ulta Beauty"
            "bath" in d && "body" in d -> "Bath & Body Works"
            "iherb" in d -> "iHerb"
            "glossier" in d -> "Glossier"

            // Handmade & Niche
            "etsy" in d -> "Etsy"
            "redbubble" in d -> "Redbubble"
            "stockx" in d -> "StockX"
            "chewy" in d -> "Chewy" // Pet supplies
            "petco" in d -> "Petco"
            "petsmart" in d -> "PetSmart"

            else -> null
        }
    }

    /**
     * Searches the email text for a store name by scanning the body
     * against a preloaded store list.
     *
     * @param text entire email body in lowercase
     * @param stores list of known store names loaded from assets
     * @return detected store or null
     */
    private fun detectStoreFromBody(text: String, stores: List<String>): String? {
        val lower = text.lowercase()
        return stores.firstOrNull { store ->
            val s = store.lowercase()
            s in lower && !knownCarriers.contains(store)
        }
    }

    /**
     * Extracts a tracking number using a series of carrier-specific
     * regex patterns (UPS, FedEx, USPS, DHL, Canada Post, Amazon Logistics).
     *
     * Cleans formatting (spaces, dashes) before scanning.
     *
     * @return first valid tracking number found or null
     */
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

    /**
     * Attempts to extract the order price by scanning for common
     * money patterns such as:
     *   - "Grand Total: $XX.XX"
     *   - "Paid: $XX.XX"
     *   - "$ XX.XX"
     *
     * @param text normalized lowercase email body
     * @return extracted price as Double or null
     */
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
    /**
     * Determines the carrier using:
     *  1. tracking number structure
     *  2. fallback: store name inference
     *
     * Example:
     *   "1Z..." → UPS
     *   "TBA..." → Amazon Logistics
     *
     * @param tracking cleaned tracking number candidate
     * @param store optional detected store
     * @return carrier name or "Unknown"
     */
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

    /**
     * Scans the email text for direct carrier mentions,
     * such as "FedEx", "UPS", "USPS", etc.
     *
     * @return carrier name if mentioned in the body, otherwise null
     */
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


    /**
     * Attempts to locate an "order date" using a list of
     * keywords and multiple date formats.
     *
     * Accepts styles like:
     *   - December 5, 2024
     *   - Dec 5 2024
     *   - 2024-12-05
     *   - 12/05/2024
     *
     * @return normalized ISO date (YYYY-MM-DD) or null
     */
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

    /**
     * Converts different date formats into a unified "YYYY-MM-DD" format.
     *
     * Strips suffixes (st, nd, rd, th) and parses with a set of formats.
     *
     * @param raw the date found in text
     * @return normalized ISO date or null if no format matched
     */
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

    /**
     * Attempts to extract the item name using product-related keywords
     * such as:
     *   - "Item:"
     *   - "Product:"
     *   - "Description:"
     *
     * Filters out invalid matches (store names, carriers, numbers only).
     *
     * @return a reasonable guess for the item name or null
     */
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

