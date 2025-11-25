package com.example.newsapplication.data.remote

import android.util.Log
import com.example.newsapplication.BuildConfig
import com.example.newsapplication.R
import com.example.newsapplication.model.Article
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID
import java.util.function.Consumer

object SupabaseNewsRemoteDataSource {

    private const val TABLE_ARTICLES = "articles"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val supabaseClient: SupabaseClient? by lazy {
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_ANON_KEY.isBlank()) {
            null
        } else {
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_ANON_KEY
            ) {
                install(Postgrest)
            }
        }
    }

    @JvmStatic
    fun fetchBreakingNews(
        onSuccess: Consumer<List<Article>>,
        onError: Consumer<Throwable>
    ) = fetchArticles(limit = 10, onSuccess = onSuccess, onError = onError)

    @JvmStatic
    fun fetchPopularNews(
        onSuccess: Consumer<List<Article>>,
        onError: Consumer<Throwable>
    ) = fetchArticles(limit = 20, onSuccess = onSuccess, onError = onError)

    @JvmStatic
    fun fetchCategoryNews(
        category: String,
        onSuccess: Consumer<List<Article>>,
        onError: Consumer<Throwable>
    ) = fetchArticles(limit = 20, category = category, onSuccess = onSuccess, onError = onError)

    private fun fetchArticles(
        limit: Int,
        category: String? = null,
        onSuccess: Consumer<List<Article>>,
        onError: Consumer<Throwable>
    ) {
        val client = supabaseClient
        if (client == null) {
            onError.accept(IllegalStateException("Supabase credentials are missing"))
            return
        }

        scope.launch {
            try {
                val decoder = client.from(TABLE_ARTICLES)
                    .select()
                    .decodeList<SupabaseArticleDto>()

                val filteredByStatus = decoder.filter { article ->
                    (article.status?.equals("published", ignoreCase = true) == true) &&
                            article.deletedAt == null
                }

                val data = (if (category.isNullOrBlank()) {
                    filteredByStatus
                } else {
                    filteredByStatus.filter { dto ->
                        dto.metadataCategory?.equals(category, ignoreCase = true) == true
                    }
                })
                    .sortedByDescending { it.publishedAt }
                    .map { it.toDomainArticle() }
                    .take(limit)

                withContext(Dispatchers.Main) {
                    onSuccess.accept(data)
                }
            } catch (error: Throwable) {
                Log.e("SupabaseNewsRemote", "Unable to fetch articles", error)
                withContext(Dispatchers.Main) {
                    onError.accept(error)
                }
            }
        }
    }
}

@Serializable
private data class SupabaseArticleDto(
    val id: String? = null,
    @SerialName("channel_id")
    val channelId: Long? = null,
    val title: String? = null,
    val slug: String? = null,
    val summary: String? = null,
    val content: String? = null,
    @SerialName("source_url")
    val sourceUrl: String? = null,
    @SerialName("hero_image_url")
    val heroImageUrl: String? = null,
    val language: String? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    val status: String? = null,
    @SerialName("deleted_at")
    val deletedAt: String? = null,
    @SerialName("metadata")
    val metadata: Map<String, JsonElement>? = null
) {
    fun toDomainArticle(): Article {
        return Article(
            id ?: UUID.randomUUID().toString(),
            title ?: "Tin tức",
            summary ?: content?.take(140) ?: "",
            content ?: summary.orEmpty(),
            "Channel #${channelId ?: 0}",
            sourceUrl ?: "Supabase",
            metadataCategory ?: "Tin tức",
            heroImageUrl ?: "",
            R.drawable.placeholder_image,
            publishedAt?.substringBefore('T') ?: "",
            false
        )
    }

    val metadataCategory: String?
        get() = metadata?.get("category")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("Category")?.jsonPrimitive?.contentOrNull
}

