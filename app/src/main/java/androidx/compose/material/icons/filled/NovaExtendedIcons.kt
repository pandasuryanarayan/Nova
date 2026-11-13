package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

private fun vec(name: String, path: String): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = PathParser().parsePathString(path).toNodes(),
        fill = SolidColor(Color.Black)
    ).build()

val Icons.Filled.AdsClick: ImageVector by lazy {
    vec("AdsClick", "M11,2v2.07C7.38,4.53 4.53,7.38 4.07,11H2v2h2.07c0.46,3.62 3.31,6.47 6.93,6.93V22h2v-2.07c3.62,-0.46 6.47,-3.31 6.93,-6.93H22v-2h-2.07C19.47,7.38 16.62,4.53 13,4.07V2h-2zM12,8c2.21,0 4,1.79 4,4s-1.79,4 -4,4 -4,-1.79 -4,-4 1.79,-4 4,-4z")
}

val Icons.Filled.Block: ImageVector by lazy {
    vec("Block", "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM4,12c0,-4.42 3.58,-8 8,-8 1.85,0 3.55,0.63 4.9,1.69L5.69,16.9C4.63,15.55 4,13.85 4,12zM12,20c-1.85,0 -3.55,-0.63 -4.9,-1.69L18.31,7.1C19.37,8.45 20,10.15 20,12c0,4.42 -3.58,8 -8,8z")
}

val Icons.Filled.Bookmark: ImageVector by lazy {
    vec("Bookmark", "M17,3H7c-1.1,0 -1.99,0.9 -1.99,2L5,21l7,-3 7,3V5c0,-1.1 -0.9,-2 -2,-2z")
}

val Icons.Filled.BookmarkBorder: ImageVector by lazy {
    vec("BookmarkBorder", "M17,3H7c-1.1,0 -1.99,0.9 -1.99,2L5,21l7,-3 7,3V5c0,-1.1 -0.9,-2 -2,-2zM17,18l-5,-2.18L7,18V5h10v13z")
}

val Icons.Filled.Bookmarks: ImageVector by lazy {
    vec("Bookmarks", "M19,18l2,1V3c0,-1.1 -0.9,-2 -2,-2H8.99C7.89,1 7,1.9 7,3h10c1.1,0 2,0.9 2,2v13zM15,5H5c-1.1,0 -2,0.9 -2,2v16l7,-3 7,3V7c0,-1.1 -0.9,-2 -2,-2z")
}

val Icons.Filled.Code: ImageVector by lazy {
    vec("Code", "M9.4,16.6L4.8,12l4.6,-4.6L8,6l-6,6 6,6 1.4,-1.4zM14.6,16.6l4.6,-4.6 -4.6,-4.6L16,6l6,6 -6,6 -1.4,-1.4z")
}

val Icons.Filled.ContentCopy: ImageVector by lazy {
    vec("ContentCopy", "M16,1H4c-1.1,0 -2,0.9 -2,2v14h2V3h12V1zM19,5H8c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h11c1.1,0 2,-0.9 2,-2V7c0,-1.1 -0.9,-2 -2,-2zM19,21H8V7h11v14z")
}

val Icons.Filled.DarkMode: ImageVector by lazy {
    vec("DarkMode", "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4 -0.44,-0.06 -0.9,-0.1 -1.36,-0.1z")
}

val Icons.Filled.DeleteSweep: ImageVector by lazy {
    vec("DeleteSweep", "M15,16h4v2h-4zM15,8h7v2h-7zM15,12h6v2h-6zM3,18c0,1.1 0.9,2 2,2h6c1.1,0 2,-0.9 2,-2V8H3v10zM14,5h-3l-1,-1H6L5,5H2v2h12z")
}

val Icons.Filled.DesktopWindows: ImageVector by lazy {
    vec("DesktopWindows", "M21,2H3c-1.1,0 -2,0.9 -2,2v12c0,1.1 0.9,2 2,2h7v2H8v2h8v-2h-2v-2h7c1.1,0 2,-0.9 2,-2V4c0,-1.1 -0.9,-2 -2,-2zM21,16H3V4h18v12z")
}

val Icons.Filled.Download: ImageVector by lazy {
    vec("Download", "M5,20h14v-2H5v2zM19,9h-4V3H9v6H5l7,7 7,-7z")
}

val Icons.Filled.ErrorOutline: ImageVector by lazy {
    vec("ErrorOutline", "M11,15h2v2h-2zM11,7h2v6h-2zM11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8z")
}

val Icons.Filled.FindInPage: ImageVector by lazy {
    vec("FindInPage", "M20,19.59V8l-6,-6H6c-1.1,0 -1.99,0.9 -1.99,2L4,20c0,1.1 0.89,2 1.99,2H18c0.45,0 0.85,-0.15 1.19,-0.4l-4.43,-4.43c-0.8,0.52 -1.74,0.83 -2.76,0.83 -2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5c0,1.02 -0.31,1.96 -0.83,2.75L20,19.59zM9,13c0,1.66 1.34,3 3,3s3,-1.34 3,-3 -1.34,-3 -3,-3 -3,1.34 -3,3z")
}

val Icons.Filled.FolderOpen: ImageVector by lazy {
    vec("FolderOpen", "M20,6h-8l-2,-2H4c-1.1,0 -1.99,0.9 -1.99,2L2,18c0,1.1 0.9,2 2,2h16c1.1,0 2,-0.9 2,-2V8c0,-1.1 -0.9,-2 -2,-2zM20,18H4V8h16v10z")
}

val Icons.Filled.Headphones: ImageVector by lazy {
    vec("Headphones", "M12,3c-4.97,0 -9,4.03 -9,9v7c0,1.1 0.9,2 2,2h4v-8H5v-1c0,-3.87 3.13,-7 7,-7s7,3.13 7,7v1h-4v8h4c1.1,0 2,-0.9 2,-2v-7c0,-4.97 -4.03,-9 -9,-9z")
}

val Icons.Filled.HighQuality: ImageVector by lazy {
    vec("HighQuality", "M19,4H5c-1.11,0 -2,0.9 -2,2v12c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V6c0,-1.1 -0.9,-2 -2,-2zM11,15H9.5v-2h-2v2H6V9h1.5v2.5h2V9H11v6zM18,14c0,0.55 -0.45,1 -1,1h-0.75v1.5h-1.5V15H14c-0.55,0 -1,-0.45 -1,-1v-4c0,-0.55 0.45,-1 1,-1h3c0.55,0 1,0.45 1,1v4zM14.5,13.5h2v-3h-2v3z")
}

val Icons.Filled.History: ImageVector by lazy {
    vec("History", "M13,3c-4.97,0 -9,4.03 -9,9H1l3.89,3.89 0.07,0.14L9,12H6c0,-3.87 3.13,-7 7,-7s7,3.13 7,7 -3.13,7 -7,7c-1.93,0 -3.68,-0.79 -4.94,-2.06l-1.42,1.42C8.27,19.99 10.51,21 13,21c4.97,0 9,-4.03 9,-9s-4.03,-9 -9,-9zM12,8v5l4.28,2.54 0.72,-1.21 -3.5,-2.08V8H12z")
}

val Icons.Filled.Language: ImageVector by lazy {
    vec("Language", "M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM18.92,8h-2.95c-0.32,-1.25 -0.78,-2.45 -1.38,-3.56 1.84,0.63 3.37,1.91 4.33,3.56zM12,4.04c0.83,1.2 1.48,2.53 1.91,3.96h-3.82c0.43,-1.43 1.08,-2.76 1.91,-3.96zM4.26,14C4.1,13.36 4,12.69 4,12s0.1,-1.36 0.26,-2h3.38c-0.08,0.66 -0.14,1.32 -0.14,2 0,0.68 0.06,1.34 0.14,2H4.26zM5.08,16h2.95c0.32,1.25 0.78,2.45 1.38,3.56 -1.84,-0.63 -3.37,-1.9 -4.33,-3.56zM8.03,8H5.08c0.96,-1.66 2.49,-2.93 4.33,-3.56C8.81,5.55 8.35,6.75 8.03,8zM12,19.96c-0.83,-1.2 -1.48,-2.53 -1.91,-3.96h3.82c-0.43,1.43 -1.08,2.76 -1.91,3.96zM14.34,14H9.66c-0.09,-0.66 -0.16,-1.32 -0.16,-2 0,-0.68 0.07,-1.35 0.16,-2h4.68c0.09,0.65 0.16,1.32 0.16,2 0,0.68 -0.07,1.34 -0.16,2zM14.59,19.56c0.6,-1.11 1.06,-2.31 1.38,-3.56h2.95c-0.96,1.65 -2.49,2.93 -4.33,3.56zM16.36,14c0.08,-0.66 0.14,-1.32 0.14,-2 0,-0.68 -0.06,-1.34 -0.14,-2h3.38c0.16,0.64 0.26,1.31 0.26,2s-0.1,1.36 -0.26,2h-3.38z")
}

val Icons.Filled.Pause: ImageVector by lazy {
    vec("Pause", "M6,19h4V5H6v14zM14,5v14h4V5h-4z")
}

val Icons.Filled.PlayCircleFilled: ImageVector by lazy {
    vec("PlayCircleFilled", "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,16.5v-9l6,4.5 -6,4.5z")
}

val Icons.Filled.Policy: ImageVector by lazy {
    vec("Policy", "M21,5l-9,-4 -9,4v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12V5zM12,7c2.76,0 5,2.24 5,5 0,0.65 -0.13,1.27 -0.35,1.85l-1.45,-1.45c0.19,-0.79 -0.02,-1.65 -0.63,-2.26 -0.9,-0.9 -2.32,-0.96 -3.28,-0.17L9.82,8.5C10.48,7.58 11.2,7 12,7zM12,17c-2.76,0 -5,-2.24 -5,-5 0,-0.65 0.13,-1.27 0.35,-1.85l1.45,1.45c-0.19,0.79 0.02,1.65 0.63,2.26 0.9,0.9 2.32,0.96 3.28,0.17l1.47,1.47C13.52,16.42 12.8,17 12,17z")
}

val Icons.Filled.Remove: ImageVector by lazy {
    vec("Remove", "M19,13H5v-2h14v2z")
}

val Icons.Filled.Shield: ImageVector by lazy {
    vec("Shield", "M12,1L3,5v6c0,5.55 3.84,10.74 9,12 5.16,-1.26 9,-6.45 9,-12V5l-9,-4z")
}

val Icons.Filled.Tab: ImageVector by lazy {
    vec("Tab", "M21,3H3c-1.1,0 -2,0.9 -2,2v14c0,1.1 0.9,2 2,2h18c1.1,0 2,-0.9 2,-2V5c0,-1.1 -0.9,-2 -2,-2zM21,19H3V5h10v4h8v10z")
}

val Icons.Filled.Terminal: ImageVector by lazy {
    vec("Terminal", "M20,4H4C2.89,4 2,4.9 2,6v12c0,1.1 0.89,2 2,2h16c1.1,0 2,-0.9 2,-2V6C22,4.9 21.11,4 20,4zM20,18H4V8h16v10zM18,17h-6v-2h6V17zM7.5,17l-1.41,-1.41L8.67,13l-2.59,-2.59L7.5,9l4,4L7.5,17z")
}

val Icons.Filled.TextFields: ImageVector by lazy {
    vec("TextFields", "M2.5,4v3h5v12h3V7h5V4h-13zM21.5,9h-9v3h3v7h3v-7h3V9z")
}

val Icons.Filled.Videocam: ImageVector by lazy {
    vec("Videocam", "M17,10.5V7c0,-0.55 -0.45,-1 -1,-1H4c-0.55,0 -1,0.45 -1,1v10c0,0.55 0.45,1 1,1h12c0.55,0 1,-0.45 1,-1v-3.5l4,4v-11l-4,4z")
}

val Icons.Filled.VisibilityOff: ImageVector by lazy {
    vec("VisibilityOff", "M12,7c2.76,0 5,2.24 5,5 0,0.65 -0.13,1.26 -0.36,1.83l2.92,2.92c1.51,-1.26 2.7,-2.89 3.43,-4.75 -1.73,-4.39 -6,-7.5 -11,-7.5 -1.4,0 -2.74,0.25 -3.98,0.7l2.16,2.16C10.74,7.13 11.35,7 12,7zM2,4.27l2.28,2.28 0.46,0.46C3.08,8.3 1.78,10.02 1,12c1.73,4.39 6,7.5 11,7.5 1.55,0 3.03,-0.3 4.38,-0.84l0.42,0.42L19.73,22 21,20.73 3.27,3 2,4.27zM7.53,9.8l1.55,1.55c-0.05,0.21 -0.08,0.43 -0.08,0.65 0,1.66 1.34,3 3,3 0.22,0 0.44,-0.03 0.65,-0.08l1.55,1.55c-0.67,0.33 -1.41,0.53 -2.2,0.53 -2.76,0 -5,-2.24 -5,-5 0,-0.79 0.2,-1.53 0.53,-2.2z")
}
