package com.gzd.androidmind.content

import com.gzd.androidmind.content.BookPage.Article
import com.gzd.androidmind.content.BookPage.Directory
import com.gzd.androidmind.content.ContentBlock.Link
import com.gzd.androidmind.content.ContentBlock.LinkButton
import com.gzd.androidmind.content.ContentBlock.Paragraph

/**
 * All reading content lives here. IDs are stable link targets; titles can change freely.
 * Directories can contain both articles and further directories, without a depth limit.
 *
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
object BookContent {
    val framework = Directory(
        id = "framework",
        title = "Framework",
        children = emptyList(),
    )

    val workSkill = Directory(
        id = "work-skill",
        title = "WorkSkill",
        children = emptyList(),
    )

    val roots = listOf(framework, workSkill)

    val pagesById: Map<String, BookPage> = buildMap {
        fun register(page: BookPage) {
            require(put(page.id, page) == null) { "Duplicate page ID: ${page.id}" }
            if (page is Directory) page.children.forEach(::register)
        }
        roots.forEach(::register)
    }.also { pages ->
        pages.values.filterIsInstance<Article>().forEach { article ->
            article.blocks.forEach { block ->
                if (block is Paragraph) {
                    block.links.forEach { (label, target) ->
                        require(label.isNotEmpty() && label in block.text) { "Missing inline link text: $label" }
                        require(target in pages) { "Unknown inline link target: $target" }
                    }
                }
                val target = when (block) {
                    is Link -> block.targetId
                    is LinkButton -> block.targetId
                    else -> null
                }
                require(target == null || target in pages) { "Unknown link target: $target" }
            }
        }
    }
}
