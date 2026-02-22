package net.onefivefour.echolist.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import echolist.composeapp.generated.resources.Res
import echolist.composeapp.generated.resources.work_sans_bold
import echolist.composeapp.generated.resources.work_sans_medium
import echolist.composeapp.generated.resources.work_sans_regular
import echolist.composeapp.generated.resources.work_sans_semibold
import org.jetbrains.compose.resources.Font

val WorkSansFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.work_sans_regular, FontWeight.Normal),
        Font(Res.font.work_sans_medium, FontWeight.Medium),
        Font(Res.font.work_sans_semibold, FontWeight.SemiBold),
        Font(Res.font.work_sans_bold, FontWeight.Bold)
    )

@Composable
fun material3Typography(): Typography {
    return Typography(
        titleLarge = TextStyle(
            fontFamily = WorkSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        ),
        titleSmall = TextStyle(
            fontFamily = WorkSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        ),
        labelMedium = TextStyle(
            fontFamily = WorkSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        ),
        labelSmall = TextStyle(
            fontFamily = WorkSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp
        ),
        bodySmall = TextStyle(
            fontFamily = WorkSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = WorkSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
    )
}

internal val LocalTypography = staticCompositionLocalOf<Typography> {
    error("Typography not provided")
}
