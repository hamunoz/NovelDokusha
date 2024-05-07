package my.noveldokusha.features.sourceCatalog

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import my.noveldokusha.core.AppPreferences
import my.noveldokusha.tooling.local_database.BookMetadata
import my.noveldokusha.network.PagedListIteratorState
import my.noveldokusha.ui.composeViews.ToolbarMode

data class SourceCatalogScreenState(
    val sourceCatalogNameStrId: State<Int>,
    val searchTextInput: MutableState<String>,
    val fetchIterator: PagedListIteratorState<BookMetadata>,
    val toolbarMode: MutableState<ToolbarMode>,
    val listLayoutMode: MutableState<AppPreferences.LIST_LAYOUT_MODE>,
)