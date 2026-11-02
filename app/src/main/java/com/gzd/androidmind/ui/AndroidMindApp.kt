package com.gzd.androidmind.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gzd.androidmind.content.BookContent
import com.gzd.androidmind.content.BookPage
import com.gzd.androidmind.content.ContentBlock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidMindApp() {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    // Separate reading histories preserve the position of each book when switching tabs.
    var frameworkHistory by rememberSaveable { mutableStateOf(listOf("framework")) }
    var workHistory by rememberSaveable { mutableStateOf(listOf("work-skill")) }
    val history = if (selectedTab == 0) frameworkHistory else workHistory
    val page = BookContent.pagesById.getValue(history.last())
    val stateHolder = rememberSaveableStateHolder()
    fun updateHistory(value: List<String>) {
        if (selectedTab == 0) frameworkHistory = value else workHistory = value
    }
    fun goBack() {
        stateHolder.removeState("$selectedTab:${history.size - 1}:${history.last()}")
        updateHistory(history.dropLast(1))
    }
    val navigate: (String) -> Unit = { id ->
        if (id != history.last()) updateHistory(history + id)
    }
    BackHandler(enabled = history.size > 1) { goBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (history.size == 1) "AndroidMind" else page.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        )
                        if (history.size == 1) {
                            Text(
                                "学习有迹，经验成书",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (history.size > 1) {
                        Box(
                            modifier = Modifier.size(48.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(role = Role.Button) { goBack() },
                            contentAlignment = Alignment.Center,
                        ) {
                            BookIcon(IconKind.Back, "返回")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (history.size == 1) {
                NavigationBar(
                    modifier = Modifier.selectableGroup(),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 0.dp,
                ) {
                    BookContent.roots.forEachIndexed { index, root ->
                        val selected = selectedTab == index
                        Column(
                            modifier = Modifier.weight(1f)
                                .selectable(
                                    selected = selected,
                                    role = Role.Tab,
                                    onClick = { selectedTab = index },
                                )
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else androidx.compose.ui.graphics.Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            ) {
                                Box(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                                    BookIcon(
                                        if (index == 0) IconKind.Framework else IconKind.Work,
                                        null,
                                    )
                                }
                            }
                            Text(
                                root.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            ReadingPage(
                page = page,
                stateHolder = stateHolder,
                stateKey = "$selectedTab:${history.size - 1}:${page.id}",
                navigate = navigate,
            )
        }
    }
}

@Composable
private fun ReadingPage(
    page: BookPage,
    stateHolder: SaveableStateHolder,
    stateKey: String,
    navigate: (String) -> Unit,
) {
    stateHolder.SaveableStateProvider(stateKey) {
        val listState = rememberLazyListState()
        when (page) {
            is BookPage.Directory -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                        Text(
                            page.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            when (page.id) {
                                BookContent.framework.id -> "源自自学"
                                BookContent.workSkill.id -> "源自工作中的经历"
                                else -> "${page.children.size} 个条目"
                            },
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(page.children, key = { it.id }) { child ->
                    Card(
                        onClick = { navigate(child.id) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                    ) {
                        Text(
                            child.title,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                if (page.children.isEmpty()) {
                    item {
                        Text(
                            "这一章，留给下一次收获。",
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            is BookPage.Article -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item {
                    Text(
                        page.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(page.blocks) { block -> ArticleBlock(block, navigate) }
            }
        }
    }
}

@Composable
private fun ArticleBlock(block: ContentBlock, navigate: (String) -> Unit) {
    when (block) {
        is ContentBlock.Heading -> Text(
            block.text,
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        is ContentBlock.Paragraph -> SelectionContainer {
            val linkColor = MaterialTheme.colorScheme.primary
            Text(
                buildAnnotatedString {
                    append(block.text)
                    block.links.forEach { (label, target) ->
                        var start = block.text.indexOf(label)
                        while (start >= 0) {
                            addLink(
                                LinkAnnotation.Clickable(
                                    tag = target,
                                    styles = TextLinkStyles(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
                                    linkInteractionListener = { navigate(target) },
                                ),
                                start,
                                start + label.length,
                            )
                            start = block.text.indexOf(label, start + label.length)
                        }
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 30.sp,
            )
        }
        is ContentBlock.Code -> Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SelectionContainer {
                Text(
                    block.text,
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(16.dp),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        is ContentBlock.FlowDiagram -> Column(modifier = Modifier.fillMaxWidth()) {
            block.steps.forEachIndexed { index, step ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        step,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 28.sp,
                    )
                }
                if (index < block.steps.lastIndex) {
                    Text(
                        "↓",
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
        }
        is ContentBlock.Link -> Text(
            block.text,
            modifier = Modifier.fillMaxWidth().clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClickLabel = "打开${block.text}",
            ) { navigate(block.targetId) }.padding(vertical = 12.dp),
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.bodyLarge,
        )
        is ContentBlock.LinkButton -> Button(onClick = { navigate(block.targetId) }, shape = RoundedCornerShape(4.dp)) {
            Text(block.text)
        }
    }
}

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
private enum class IconKind { Framework, Work, Back }

@Composable
private fun BookIcon(kind: IconKind, description: String?) {
    val color = LocalContentColor.current
    Canvas(
        Modifier.size(24.dp).then(
            if (description == null) Modifier else Modifier.semantics { contentDescription = description },
        ),
    ) {
        val unit = size.width / 24
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
            drawLine(color, Offset(x1 * unit, y1 * unit), Offset(x2 * unit, y2 * unit), 1.8f * unit, StrokeCap.Round)
        }
        when (kind) {
            IconKind.Back -> {
                line(19f, 12f, 5f, 12f)
                line(5f, 12f, 12f, 5f)
                line(5f, 12f, 12f, 19f)
            }
            IconKind.Framework -> {
                drawRect(color, Offset(7 * unit, 7 * unit), androidx.compose.ui.geometry.Size(10 * unit, 10 * unit), style = Stroke(1.8f * unit))
                for (position in listOf(9f, 15f)) {
                    line(position, 3f, position, 7f)
                    line(position, 17f, position, 21f)
                    line(3f, position, 7f, position)
                    line(17f, position, 21f, position)
                }
            }
            IconKind.Work -> {
                line(4f, 5f, 10f, 5f)
                line(10f, 5f, 12f, 7f)
                line(12f, 7f, 14f, 5f)
                line(14f, 5f, 20f, 5f)
                line(4f, 5f, 4f, 19f)
                line(20f, 5f, 20f, 19f)
                line(4f, 19f, 10f, 19f)
                line(10f, 19f, 12f, 21f)
                line(12f, 21f, 14f, 19f)
                line(14f, 19f, 20f, 19f)
                line(12f, 7f, 12f, 21f)
            }
        }
    }
}
