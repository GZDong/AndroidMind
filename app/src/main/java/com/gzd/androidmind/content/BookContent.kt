package com.gzd.androidmind.content

import com.gzd.androidmind.content.BookPage.Article
import com.gzd.androidmind.content.BookPage.Directory
import com.gzd.androidmind.content.ContentBlock.Link
import com.gzd.androidmind.content.ContentBlock.LinkButton
import com.gzd.androidmind.content.ContentBlock.Paragraph
import com.gzd.androidmind.content.framework.FrameworkDirectory
import com.gzd.androidmind.content.workskill.WorkSkillDirectory

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
object BookContent {
    val framework = FrameworkDirectory
    val workSkill = WorkSkillDirectory

    val roots: List<Directory> = listOf(framework, workSkill)

    val pagesById: Map<String, BookPage> = buildMap<String, BookPage> {
        fun register(page: BookPage) {
            val existing = get(page.id)
            if (existing != null) {
                require(existing === page) { "Duplicate page ID: ${page.id}" }
                return
            }
            put(page.id, page)
            when (page) {
                is Directory -> page.children.forEach(::register)
                is Article -> page.relatedPages.forEach(::register)
            }
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
