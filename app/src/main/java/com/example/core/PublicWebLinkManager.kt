package com.example.core

import android.content.Context
import android.net.Uri

data class PublicSeoConfig(
    val title: String = PublicWebLinkManager.DEFAULT_SEO_TITLE,
    val description: String = PublicWebLinkManager.DEFAULT_SEO_DESCRIPTION,
    val keywords: String = PublicWebLinkManager.DEFAULT_SEO_KEYWORDS,
    val allowGoogleIndexing: Boolean = true
)

object PublicWebLinkManager {
    private const val PREFS_NAME = "noxra_public_web_link_prefs"
    private const val KEY_CUSTOM_WEB_URL = "custom_public_web_url"
    private const val KEY_SEO_TITLE = "seo_meta_title"
    private const val KEY_SEO_DESCRIPTION = "seo_meta_description"
    private const val KEY_SEO_KEYWORDS = "seo_meta_keywords"
    private const val KEY_ALLOW_INDEXING = "seo_allow_google_indexing"

    private const val LEGACY_PARENT_WEB_URL =
        "https://ais-pre-wfj6x5jtzdgkrvg32gkkh3-597285151758.asia-southeast1.run.app"

    const val DEFAULT_PUBLIC_WEB_URL =
        "https://ais-pre-ebyzwvrftdrahqkgejpaka-597285151758.asia-southeast1.run.app"

    const val DEV_PUBLIC_WEB_URL =
        "https://ais-dev-ebyzwvrftdrahqkgejpaka-597285151758.asia-southeast1.run.app"

    const val DEFAULT_SEO_TITLE =
        "Noxra AI — Smart English & Math Levels 1–50 | AI Tutor"

    const val DEFAULT_SEO_DESCRIPTION =
        "Master English & Math from Level 1 to Level 50 across Basic, Medium & Hard tiers with Noxra AI Tutor, AI Speaking Lab, and Global Ranked Leaderboards. Founded by Sachin Kumar."

    const val DEFAULT_SEO_KEYWORDS =
        "Noxra AI, Sachin Kumar, English Levels 1-50, Math Levels 1-50, AI Tutor, Speaking Lab, Student Ranked Leaderboard"

    fun getPublicWebUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_CUSTOM_WEB_URL, null)?.trim()
        return if (saved.isNullOrEmpty() || saved == LEGACY_PARENT_WEB_URL) {
            DEFAULT_PUBLIC_WEB_URL
        } else {
            saved
        }
    }

    fun savePublicWebUrl(context: Context, url: String): String {
        val cleaned = url.trim().let {
            if (it.isEmpty()) DEFAULT_PUBLIC_WEB_URL
            else if (!it.startsWith("http://") && !it.startsWith("https://")) "https://$it"
            else it
        }.trimEnd('/')
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_WEB_URL, cleaned)
            .apply()
        return cleaned
    }

    fun resetPublicWebUrl(context: Context): String {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_CUSTOM_WEB_URL)
            .apply()
        return DEFAULT_PUBLIC_WEB_URL
    }

    fun buildUserPublicProfileUrl(baseUrl: String, username: String?): String {
        val cleanBase = baseUrl.trim().ifEmpty { DEFAULT_PUBLIC_WEB_URL }.trimEnd('/')
        val cleanHandle = username?.trim()?.removePrefix("@")?.takeIf { it.isNotEmpty() }
            ?: return cleanBase
        return "$cleanBase/?u=${Uri.encode(cleanHandle)}"
    }

    fun buildPublicSearchQueryUrl(baseUrl: String, query: String): String {
        val cleanBase = baseUrl.trim().ifEmpty { DEFAULT_PUBLIC_WEB_URL }.trimEnd('/')
        val cleanQuery = query.trim()
        return if (cleanQuery.isEmpty()) cleanBase else "$cleanBase/?q=${Uri.encode(cleanQuery)}"
    }

    fun buildSitemapUrl(baseUrl: String): String {
        val cleanBase = baseUrl.trim().ifEmpty { DEFAULT_PUBLIC_WEB_URL }.trimEnd('/')
        return "$cleanBase/sitemap.xml"
    }

    fun buildGoogleSearchUrl(query: String): String {
        val searchTerms = query.trim().ifEmpty { "Noxra AI English Math Levels 1-50 Sachin Kumar" }
        return "https://www.google.com/search?q=${Uri.encode(searchTerms)}"
    }

    fun buildGoogleSiteIndexCheckUrl(publicUrl: String): String {
        val stripped = publicUrl.trim()
            .removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
            .ifEmpty { "ais-pre-ebyzwvrftdrahqkgejpaka-597285151758.asia-southeast1.run.app" }
        return "https://www.google.com/search?q=${Uri.encode("site:$stripped")}"
    }

    fun getSeoConfig(context: Context): PublicSeoConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return PublicSeoConfig(
            title = prefs.getString(KEY_SEO_TITLE, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_SEO_TITLE,
            description = prefs.getString(KEY_SEO_DESCRIPTION, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_SEO_DESCRIPTION,
            keywords = prefs.getString(KEY_SEO_KEYWORDS, null)?.takeIf { it.isNotBlank() } ?: DEFAULT_SEO_KEYWORDS,
            allowGoogleIndexing = prefs.getBoolean(KEY_ALLOW_INDEXING, true)
        )
    }

    fun saveSeoConfig(context: Context, config: PublicSeoConfig): PublicSeoConfig {
        val normalized = PublicSeoConfig(
            title = config.title.trim().take(90).ifBlank { DEFAULT_SEO_TITLE },
            description = config.description.trim().take(240).ifBlank { DEFAULT_SEO_DESCRIPTION },
            keywords = config.keywords.trim().take(240).ifBlank { DEFAULT_SEO_KEYWORDS },
            allowGoogleIndexing = config.allowGoogleIndexing
        )
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SEO_TITLE, normalized.title)
            .putString(KEY_SEO_DESCRIPTION, normalized.description)
            .putString(KEY_SEO_KEYWORDS, normalized.keywords)
            .putBoolean(KEY_ALLOW_INDEXING, normalized.allowGoogleIndexing)
            .apply()
        return normalized
    }

    fun generateHtmlSeoSnippet(
        publicUrl: String,
        seo: PublicSeoConfig,
        developerName: String = "Sachin Kumar"
    ): String {
        val robotsDirective = if (seo.allowGoogleIndexing) {
            "index, follow, max-image-preview:large, max-snippet:-1"
        } else {
            "noindex, nofollow"
        }
        val cleanUrl = publicUrl.trim().ifEmpty { DEFAULT_PUBLIC_WEB_URL }
        return """
            <!-- Google Search & Rich Result SEO Tags for Noxra AI -->
            <title>${escapeHtmlAttr(seo.title)}</title>
            <meta name="description" content="${escapeHtmlAttr(seo.description)}" />
            <meta name="keywords" content="${escapeHtmlAttr(seo.keywords)}" />
            <meta name="robots" content="$robotsDirective" />
            <meta name="googlebot" content="$robotsDirective" />
            <meta name="author" content="${escapeHtmlAttr(developerName)}" />
            <link rel="canonical" href="${escapeHtmlAttr(cleanUrl)}" />
            <meta property="og:type" content="website" />
            <meta property="og:site_name" content="Noxra AI" />
            <meta property="og:title" content="${escapeHtmlAttr(seo.title)}" />
            <meta property="og:description" content="${escapeHtmlAttr(seo.description)}" />
            <meta property="og:url" content="${escapeHtmlAttr(cleanUrl)}" />
            <script type="application/ld+json">
            {
              "@context": "https://schema.org",
              "@type": "EducationalApplication",
              "name": "Noxra AI",
              "url": "${escapeHtmlAttr(cleanUrl)}",
              "applicationCategory": "EducationalApplication",
              "operatingSystem": "Android, Web",
              "description": "${escapeHtmlAttr(seo.description)}",
              "founder": {
                "@type": "Person",
                "name": "${escapeHtmlAttr(developerName)}"
              }
            }
            </script>
        """.trimIndent()
    }

    private fun escapeHtmlAttr(raw: String): String {
        return raw
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }
}
