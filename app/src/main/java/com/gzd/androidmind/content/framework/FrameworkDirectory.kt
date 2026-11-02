package com.gzd.androidmind.content.framework

import com.gzd.androidmind.content.BookPage

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
object FrameworkDirectory : BookPage.Directory() {
    override val id = "framework"
    override val title = "Framework"
    override val children: List<BookPage> = emptyList()
}
