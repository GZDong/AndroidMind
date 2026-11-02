package com.gzd.androidmind.content

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
sealed interface BookPage {
    val id: String
    val title: String

    /**
     * @Author GZDong(A80788)
     * @Date 2026/11/2
     */
    abstract class Directory : BookPage {
        abstract val children: List<BookPage>
    }

    /**
     * @Author GZDong(A80788)
     * @Date 2026/11/2
     */
    abstract class Article : BookPage {
        abstract val blocks: List<ContentBlock>

        /** Pages available through body links, without appearing in a directory list. */
        open val relatedPages: List<BookPage> = emptyList()
    }
}
