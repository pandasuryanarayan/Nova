package androidx.compose.material.icons.automirrored.filled

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

private fun autoMirroredVec(name: String, path: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = true
    ).addPath(
        pathData = PathParser().parsePathString(path).toNodes(),
        fill = SolidColor(Color.Black)
    ).build()

val Icons.AutoMirrored.Filled.ListAlt: ImageVector by lazy {
    autoMirroredVec("ListAlt", "M19,5v14H5V5h14m1.1,-2H3.9c-0.5,0 -0.9,0.4 -0.9,0.9v16.2c0,0.4 0.4,0.9 0.9,0.9h16.2c0.4,0 0.9,-0.5 0.9,-0.9V3.9c0,-0.5 -0.5,-0.9 -0.9,-0.9zM11,7h6v2h-6V7zM11,11h6v2h-6v-2zM11,15h6v2h-6v-2zM7,7h2v2H7V7zM7,11h2v2H7v-2zM7,15h2v2H7v-2z")
}

val Icons.AutoMirrored.Filled.OpenInNew: ImageVector by lazy {
    autoMirroredVec("OpenInNew", "M19,19H5V5h7V3H5c-1.11,0 -2,0.9 -2,2v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2v-7h-2v7zM14,3v2h3.59l-9.83,9.83 1.41,1.41L19,6.41V10h2V3h-7z")
}
