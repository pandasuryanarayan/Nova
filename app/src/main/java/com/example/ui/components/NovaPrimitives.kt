package com.example.ui.components

import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import com.example.ui.icons.NovaIcons
import com.example.ui.theme.LocalContentColor
import com.example.ui.theme.MaterialTheme

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val resolvedColor = if (color != Color.Unspecified) color else LocalContentColor.current
    val mergedStyle = style.merge(
        TextStyle(
            color = resolvedColor,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            textAlign = textAlign ?: TextAlign.Unspecified
        )
    )
    BasicText(
        text = text,
        modifier = modifier,
        style = mergedStyle,
        overflow = overflow,
        maxLines = maxLines
    )
}

@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    val painter = rememberVectorPainter(imageVector)
    val semanticsModifier = if (contentDescription != null) {
        modifier.semantics { this.contentDescription = contentDescription }
    } else {
        modifier
    }
    Image(
        painter = painter,
        contentDescription = contentDescription,
        colorFilter = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null,
        modifier = semanticsModifier.size(24.dp)
    )
}

@Composable
fun IconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    ) {
        content()
    }
}

@Composable
fun Surface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val base = modifier
        .clip(shape)
        .background(color, shape)
        .let { if (border != null) it.border(border, shape) else it }
        .let { if (onClick != null) it.clickable(role = Role.Button, onClick = onClick) else it }
    Box(modifier = base) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

data class ButtonColors(
    val containerColor: Color,
    val contentColor: Color
)

object ButtonDefaults {
    @Composable
    fun buttonColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary
    ) = ButtonColors(containerColor, contentColor)
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .background(if (enabled) colors.containerColor else colors.containerColor.copy(alpha = 0.5f), shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(contentPadding)
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.contentColor) {
            content()
        }
    }
}

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(contentPadding)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            content()
        }
    }
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            content()
        }
    }
}

@Composable
fun HorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

@Composable
fun LinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val frac = progress().coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(CircleShape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(frac)
                .background(color)
        )
    }
}

@Composable
fun CircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier.size(18.dp)) {
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 250f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedTrackColor: Color = MaterialTheme.colorScheme.primary,
    checkedThumbColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val trackColor = if (checked) checkedTrackColor else MaterialTheme.colorScheme.surfaceVariant
    val thumbColor = if (checked) checkedThumbColor else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 50.dp, height = 32.dp)
            .clip(CircleShape)
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (checked) Arrangement.End else Arrangement.Start,
            modifier = Modifier
                .size(width = 48.dp, height = 26.dp)
                .clip(CircleShape)
                .background(trackColor)
                .border(1.dp, if (checked) checkedTrackColor else MaterialTheme.colorScheme.outline, CircleShape)
                .padding(horizontal = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(thumbColor, CircleShape)
            )
        }
    }
}

@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(role = Role.Checkbox) { onCheckedChange(!checked) }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(
                    width = 1.5.dp,
                    color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            if (checked) {
                Icon(
                    imageVector = NovaIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun NovaBottomSheet(
    onDismissRequest: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler(onBack = onDismissRequest)
    val noRipple = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.56f))
            .clickable(interactionSource = noRipple, indication = null, onClick = onDismissRequest),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            color = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .clickable(interactionSource = noRipple, indication = null) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.36f))
                    )
                }
                content()
            }
        }
    }
}

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (icon != null) {
                    Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        icon()
                    }
                }
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                    title()
                }
                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                    text()
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    confirmButton()
                }
            }
        }
    }
}

@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!expanded) return
    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            modifier = Modifier.widthIn(min = 140.dp, max = 220.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
        }
    }
}

@Composable
fun DropdownMenuItem(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(10.dp))
        }
        text()
    }
}

@Composable
fun NovaEditText(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    hintColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    imeAction: Int = EditorInfo.IME_ACTION_DONE,
    onImeAction: (() -> Unit)? = null,
    requestFocusOnMount: Boolean = false,
    monospace: Boolean = false,
    textSizeSp: Float = 14f
) {
    val latestOnValueChange by rememberUpdatedState(onValueChange)
    val latestOnImeAction by rememberUpdatedState(onImeAction)
    var lastText by remember { mutableStateOf(value) }

    Column(modifier = modifier) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(containerColor, shape)
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            AndroidView(
                factory = { ctx ->
                    EditText(ctx).apply {
                        background = null
                        setPadding(0, 8, 0, 8)
                        isSingleLine = true
                        maxLines = 1
                        gravity = Gravity.CENTER_VERTICAL
                        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                        this.imeOptions = imeAction
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
                        typeface = if (monospace) Typeface.MONOSPACE else Typeface.SANS_SERIF
                        hint = placeholder
                        setTextColor(textColor.toArgb())
                        setHintTextColor(hintColor.toArgb())
                        setText(value)
                        setSelection(value.length)
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                            override fun afterTextChanged(s: Editable?) {
                                val next = s?.toString() ?: ""
                                if (next != lastText) {
                                    lastText = next
                                    latestOnValueChange(next)
                                }
                            }
                        })
                        setOnEditorActionListener { _, actionId, _ ->
                            if (actionId == imeAction || actionId == EditorInfo.IME_ACTION_DONE ||
                                actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND
                            ) {
                                latestOnImeAction?.invoke()
                                true
                            } else {
                                false
                            }
                        }
                        if (requestFocusOnMount) {
                            post {
                                requestFocus()
                                val imm = ctx.getSystemService(InputMethodManager::class.java)
                                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                            }
                        }
                    }
                },
                update = { et ->
                    et.setTextColor(textColor.toArgb())
                    et.setHintTextColor(hintColor.toArgb())
                    if (et.hint?.toString() != placeholder) et.hint = placeholder
                    if (et.text?.toString() != value) {
                        lastText = value
                        et.setText(value)
                        et.setSelection(value.length)
                    }
                },
                modifier = Modifier.weight(1f)
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(4.dp))
                trailingIcon()
            }
        }
    }
}
