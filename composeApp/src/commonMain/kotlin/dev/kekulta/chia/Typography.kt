package dev.kekulta.chia

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import chia.composeapp.generated.resources.Res
import chia.composeapp.generated.resources.manrope_variable
import org.jetbrains.compose.resources.Font

@Composable
fun getFont(weight: Int) =
    FontFamily(Font(Res.font.manrope_variable, weight = FontWeight(weight)))

val typography: Typography
    @Composable
    get() = Typography(
            displayLarge = TextStyle(
                fontFamily = getFont(750),
                fontSize = 57.sp
            ),
            displayMedium = TextStyle(
                fontFamily = getFont(900),
                fontSize = 45.sp
            ),
            displaySmall = TextStyle(
                fontFamily = getFont(600),
                fontSize = 22.sp
            ),
            headlineLarge = TextStyle(
                fontFamily = getFont(800),
                fontSize = 36.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = getFont(700),
                fontSize = 28.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = getFont(700),
                fontSize = 24.sp
            ),
            titleLarge = TextStyle(
                fontFamily = getFont(700),
                fontSize = 22.sp
            ),
            titleMedium = TextStyle(
                fontFamily = getFont(700),
                fontSize = 16.sp
            ),
            titleSmall = TextStyle(
                fontFamily = getFont(700),
                fontSize = 14.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = getFont(700),
                fontSize = 16.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = getFont(700),
                fontSize = 14.sp
            ),
            bodySmall = TextStyle(
                fontFamily = getFont(600),
                fontSize = 14.sp
            ),
            labelLarge = TextStyle(
                fontFamily = getFont(700),
                fontSize = 14.sp
            ),
            labelMedium = TextStyle(
                fontFamily = getFont(700),
                fontSize = 12.sp
            ),
            labelSmall = TextStyle(
                fontFamily = getFont(600),
                fontSize = 11.sp
            )
        )
