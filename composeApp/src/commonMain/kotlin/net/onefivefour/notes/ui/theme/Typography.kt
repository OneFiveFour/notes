package net.onefivefour.notes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import notes.composeapp.generated.resources.Res
import notes.composeapp.generated.resources.work_sans_bold
import notes.composeapp.generated.resources.work_sans_medium
import notes.composeapp.generated.resources.work_sans_regular
import notes.composeapp.generated.resources.work_sans_semibold
import org.jetbrains.compose.resources.Font

val WorkSansFontFamily: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.work_sans_regular, FontWeight.Normal),
        Font(Res.font.work_sans_medium, FontWeight.Medium),
        Font(Res.font.work_sans_semibold, FontWeight.SemiBold),
        Font(Res.font.work_sans_bold, FontWeight.Bold)
    )

val BeepMeTypography: Typography
    @Composable
    get() {
        val workSans = WorkSansFontFamily
        return Typography(
            titleLarge = TextStyle(
                fontFamily = workSans,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            titleSmall = TextStyle(
                fontFamily = workSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            labelMedium = TextStyle(
                fontFamily = workSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            labelSmall = TextStyle(
                fontFamily = workSans,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            ),
            bodySmall = TextStyle(
                fontFamily = workSans,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = workSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp
            )
        )
    }
