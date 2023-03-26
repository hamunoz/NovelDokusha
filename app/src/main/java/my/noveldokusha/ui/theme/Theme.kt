package my.noveldokusha.ui.theme

import androidx.annotation.StyleRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.mapNotNull
import my.noveldokusha.AppPreferences
import my.noveldokusha.R

@Composable
fun ColorScheme.isDark() = this.background.luminance() <= 0.5

private val light_ColorPalette = ColorScheme(
    primary = Grey25,
    onPrimary = Grey900,
    primaryContainer = Grey50,
    onPrimaryContainer = Grey800,
    inversePrimary = Grey900,
    secondary = Grey25,
    onSecondary = Grey900,
    secondaryContainer = Grey50,
    onSecondaryContainer = Grey800,
    tertiary = Grey25,
    onTertiary = Grey900,
    tertiaryContainer = Grey50,
    onTertiaryContainer = Grey800,
    background = Grey25,
    onBackground = Grey900,
    surface = Grey25,
    onSurface = Grey900,
    surfaceVariant = Grey25,
    onSurfaceVariant = Grey900,
    surfaceTint = Grey300,
    inverseSurface = Grey900,
    inverseOnSurface = Grey50,
    error = Error100,
    onError = Grey900,
    errorContainer = Error50,
    onErrorContainer = Grey800,
    outline = Grey1000,
    outlineVariant = Grey800,
    scrim = Grey300,
)

private val dark_ColorPalette = ColorScheme(
    primary = Grey900,
    onPrimary = Grey25,
    primaryContainer = Grey800,
    onPrimaryContainer = Grey100,
    inversePrimary = Grey25,
    secondary = Grey900,
    onSecondary = Grey25,
    secondaryContainer = Grey800,
    onSecondaryContainer = Grey50,
    tertiary = Grey900,
    onTertiary = Grey25,
    tertiaryContainer = Grey800,
    onTertiaryContainer = Grey50,
    background = Grey900,
    onBackground = Grey25,
    surface = Grey900,
    onSurface = Grey25,
    surfaceVariant = Grey900,
    onSurfaceVariant = Grey25,
    surfaceTint = Grey700,
    inverseSurface = Grey25,
    inverseOnSurface = Grey800,
    error = Error900,
    onError = Grey25,
    errorContainer = Error1000,
    onErrorContainer = Grey50,
    outline = Grey0,
    outlineVariant = Grey25,
    scrim = Grey800,
)

private val grey_ColorPalette = dark_ColorPalette
private val black_ColorPalette = dark_ColorPalette

enum class Themes {
    LIGHT,
    DARK,
    GREY,
    BLACK;

    companion object {
        fun fromIDTheme(@StyleRes id: Int) = when (id) {
            R.style.AppTheme_Light -> LIGHT
            R.style.AppTheme_BaseDark_Dark -> DARK
            R.style.AppTheme_BaseDark_Grey -> GREY
            R.style.AppTheme_BaseDark_Black -> BLACK
            else -> null
        }

        fun toIDTheme(theme: Themes) = when (theme) {
            LIGHT -> R.style.AppTheme_Light
            DARK -> R.style.AppTheme_BaseDark_Dark
            GREY -> R.style.AppTheme_BaseDark_Grey
            BLACK -> R.style.AppTheme_BaseDark_Black
        }

        val list = listOf(LIGHT, DARK, GREY, BLACK)
        val pairs = mapOf(
            LIGHT to "Light",
            DARK to "Dark",
            GREY to "Grey",
            BLACK to "Black"
        )
    }

    val isDark get() = this != LIGHT
}

@Composable
fun Theme(
    appPreferences: AppPreferences,
    wrapper: @Composable (fn: @Composable () -> @Composable Unit) -> Unit = { fn -> Surface(Modifier.fillMaxSize()) { fn() } },
    content: @Composable () -> @Composable Unit,
) {
    // Done so the first load is not undefined (visually annoying)
    val initialThemeFollowSystem by remember {
        mutableStateOf(appPreferences.THEME_FOLLOW_SYSTEM.value)
    }
    val initialThemeType by remember {
        mutableStateOf(Themes.fromIDTheme(appPreferences.THEME_ID.value) ?: Themes.LIGHT)
    }

    val themeFollowSystem by remember {
        appPreferences.THEME_FOLLOW_SYSTEM.flow()
    }.collectAsState(initialThemeFollowSystem)

    val themeType by remember {
        appPreferences.THEME_ID.flow().mapNotNull(Themes::fromIDTheme)
    }.collectAsState(initialThemeType)

    val isSystemThemeLight = !isSystemInDarkTheme()
    val isThemeLight by remember {
        derivedStateOf {
            themeType !in setOf(Themes.DARK, Themes.GREY, Themes.BLACK)
        }
    }

    val theme: Themes = when (themeFollowSystem) {
        true -> when {
            isSystemThemeLight && !isThemeLight -> Themes.LIGHT
            !isSystemThemeLight && isThemeLight -> Themes.DARK
            else -> themeType
        }
        false -> themeType
    }

    InternalTheme(
        theme = theme,
        content = content,
        wrapper = wrapper
    )
}

@Composable
fun InternalTheme(
    theme: Themes = if (isSystemInDarkTheme()) Themes.DARK else Themes.LIGHT,
    wrapper: @Composable (fn: @Composable () -> Unit) -> Unit = { fn -> Surface(Modifier.fillMaxSize()) { fn() } },
    content: @Composable () -> Unit
) {
    val palette = when (theme) {
        Themes.LIGHT -> light_ColorPalette
        Themes.DARK -> dark_ColorPalette
        Themes.GREY -> grey_ColorPalette
        Themes.BLACK -> black_ColorPalette
    }

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = palette.surface,
        darkIcons = theme.isDark
    )

    MaterialTheme(
        colorScheme = palette,
        typography = typography,
        shapes = shapes,
        content = { wrapper { content() } }
    )
}

// InternalTheme but for theming an object (wont try to fill all space)
@Composable
fun InternalThemeObject(
    theme: Themes = if (isSystemInDarkTheme()) Themes.DARK else Themes.LIGHT,
    wrapper: @Composable (fn: @Composable () -> Unit) -> Unit = { fn -> Surface { fn() } },
    content: @Composable () -> Unit
) {
    val palette = when (theme) {
        Themes.LIGHT -> light_ColorPalette
        Themes.DARK -> dark_ColorPalette
        Themes.GREY -> grey_ColorPalette
        Themes.BLACK -> black_ColorPalette
    }

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = palette.surface,
        darkIcons = theme.isDark
    )

    MaterialTheme(
        colorScheme = palette,
        typography = typography,
        shapes = shapes,
        content = { wrapper { content() } }
    )
}