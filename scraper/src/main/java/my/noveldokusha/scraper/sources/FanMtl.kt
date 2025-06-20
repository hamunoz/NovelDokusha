package my.noveldokusha.scraper.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import my.noveldokusha.core.LanguageCode
import my.noveldokusha.core.PagedList
import my.noveldokusha.core.Response
import my.noveldokusha.network.NetworkClient
import my.noveldokusha.network.addPath
import my.noveldokusha.network.toDocument
import my.noveldokusha.network.toUrlBuilderSafe
import my.noveldokusha.network.tryConnect
import my.noveldokusha.scraper.R
import my.noveldokusha.scraper.SourceInterface
import my.noveldokusha.scraper.TextExtractor
import my.noveldokusha.scraper.domain.BookResult
import my.noveldokusha.scraper.domain.ChapterResult
import org.jsoup.nodes.Document
import okhttp3.FormBody
import okhttp3.Request

class FanMtl(
    private val networkClient: NetworkClient
) : SourceInterface.Catalog {
    override val id = "fanmtl"
    override val nameStrId = R.string.source_name_fanmtl
    override val baseUrl = "https://www.fanmtl.com/"
    override val catalogUrl = "https://www.fanmtl.com/list/all/all-newstime-1.html"
    override val language = LanguageCode.ENGLISH

    private suspend fun getPagesList(index: Int): Response<PagedList<BookResult>> =
        withContext(Dispatchers.Default) {
            tryConnect {
                val page = index + 1
                val url = baseUrl + "list/all/all-newstime-$page.html"
                val doc = networkClient.get(url).toDocument()
                val novels = doc.select(".novel-item").mapNotNull {
                    val aTag = it.selectFirst("a[href][title]") ?: return@mapNotNull null
                    //val cover = it.selectFirst("img")?.attr("src") ?: ""
                    val cover = it.selectFirst("img[src][data-src]")?: return@mapNotNull null
                    BookResult(
                        title = aTag.attr("title"),
                        url = baseUrl.removeSuffix("/") + aTag.attr("href"),
                      coverImageUrl = baseUrl.removeSuffix("/") + cover.attr("data-src")
                       //coverImageUrl = if (cover.startsWith("http")) cover else baseUrl.removeSuffix("/") + cover
                    )
                }
                val isLastPage = novels.isEmpty() || page >= 39
                PagedList(novels, index, isLastPage)
            }
        }

    override suspend fun getCatalogList(
        index: Int
    ): Response<PagedList<BookResult>> = getPagesList(index)

    // No override aquí, search es auxiliar
    suspend fun search(
        query: String,
        index: Int
    ): Response<PagedList<BookResult>> = withContext(Dispatchers.Default) {
        tryConnect {
            val formBody = FormBody.Builder()
                .add("show", "title")
                .add("tempid", "1")
                .add("tbname", "news")
                .add("keyboard", query)
                .build()
            val requestBuilder = Request.Builder()
                .url("https://www.fanmtl.com/e/search/index.php")
                .post(formBody)
            val response = networkClient.call(requestBuilder).toDocument()
            val novels = response.select(".novel-item").mapNotNull {
                val aTag = it.selectFirst("a[href][title]") ?: return@mapNotNull null
                //val cover = it.selectFirst(".cover-wrap img")?.attr("src") ?: ""
                val cover = it.selectFirst("img[src][data-src]")?: return@mapNotNull null
                BookResult(
                    title = aTag.attr("title"),
                    url = baseUrl.removeSuffix("/") + aTag.attr("href"),
                    //coverImageUrl = cover
                    coverImageUrl = baseUrl.removeSuffix("/") + cover.attr("data-src")
                )
            }
            PagedList(novels, 0, isLastPage = true)
        }
    }

    override suspend fun getCatalogSearch(index: Int, input: String): Response<PagedList<BookResult>> {
        return search(input, index)
    }

    override suspend fun getBookCoverImageUrl(bookUrl: String): Response<String?> =
        withContext(Dispatchers.Default) {
            tryConnect {
                val src = networkClient.get(bookUrl).toDocument()
                    //.selectFirst(".cover img")?.attr("data-src") //falta los atributos
             .selectFirst("img[src][data-src]")?.attr("data-src") ?: return@tryConnect null
            if (src.startsWith("http")) src else baseUrl.removeSuffix("/") + src
            }
        }

    override suspend fun getBookDescription(bookUrl: String): Response<String?> =
        withContext(Dispatchers.Default) {
            tryConnect {
                networkClient.get(bookUrl).toDocument()
                    .selectFirst(".summary .content")?.let { TextExtractor.get(it) }
            }
        }

    override suspend fun getChapterList(bookUrl: String): Response<List<ChapterResult>> =
        withContext(Dispatchers.Default) {
            tryConnect {
                val doc = networkClient.get(bookUrl).toDocument()
                doc.select("#chapters .chapter-list li").mapNotNull {
                    val a = it.selectFirst("a[href]")
                    val title = it.selectFirst(".chapter-title")?.text() ?: ""
                    if (a != null && title.isNotEmpty()) {
                        ChapterResult(
                            title = title,
                            url = baseUrl.removeSuffix("/") + a.attr("href")
                        )
                    } else null
                }
            }
        }

    override suspend fun getChapterTitle(doc: Document): String? =
        withContext(Dispatchers.Default) {
            doc.selectFirst(".titles h2")?.text()
        }

    override suspend fun getChapterText(doc: Document): String =
        withContext(Dispatchers.Default) {
            doc.select(".chapter-content p")
                .joinToString("\n") { it.text() }
        }
}
