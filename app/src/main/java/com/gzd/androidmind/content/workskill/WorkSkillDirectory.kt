package com.gzd.androidmind.content.workskill

import com.gzd.androidmind.content.BookPage
import com.gzd.androidmind.content.workskill.photocheating.PhotoCheatingOverviewPage

/**
 * @Author GZDong(A80788)
 * @Date 2026/11/2
 */
object WorkSkillDirectory : BookPage.Directory() {
    override val id = "work-skill"
    override val title = "WorkSkill"
    override val children: List<BookPage> = listOf(PhotoCheatingOverviewPage)
}
