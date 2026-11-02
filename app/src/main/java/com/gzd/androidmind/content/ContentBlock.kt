package com.gzd.androidmind.content

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
sealed interface ContentBlock {
    data class Heading(val text: String) : ContentBlock
    data class Paragraph(
        val text: String,
        val links: Map<String, String> = emptyMap(),
    ) : ContentBlock
    data class Code(val text: String) : ContentBlock
    data class Link(val text: String, val targetId: String) : ContentBlock
    data class LinkButton(val text: String, val targetId: String) : ContentBlock
}
