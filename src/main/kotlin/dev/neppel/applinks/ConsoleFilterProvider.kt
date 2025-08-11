package dev.neppel.applinks

import com.intellij.execution.filters.ConsoleFilterProviderEx
import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.HyperlinkInfo
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.extensions.InternalIgnoreDependencyViolation
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

@InternalIgnoreDependencyViolation
private class ConsoleFilterProvider : ConsoleFilterProviderEx {
    override fun getDefaultFilters(project: Project): Array<Filter> {
        return getDefaultFilters(project, GlobalSearchScope.allScope(project))
    }

    override fun getDefaultFilters(project: Project, scope: GlobalSearchScope): Array<Filter> {
        return arrayOf(ConsoleFilter(project, scope))
    }
}

class ConsoleFilter(val project: Project, val scope: GlobalSearchScope) : Filter {
    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val message = pattern.find(line) ?: return null
        val attrs = EditorColorsManager.getInstance().globalScheme.getAttributes(CodeInsightColors.HYPERLINK_ATTRIBUTES)

        val offset = entireLength - line.length
        return Filter.Result(offset + message.range.first, offset + message.range.last + 1, AppLinkFileHyperlinkInfo(message.value), attrs)
    }

    companion object {
        private val pattern = Regex("""(chart://#/chart/.*/[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}/[STDWM](15|5|2|1)/[0-9]{0,3})""")
    }
}

class AppLinkFileHyperlinkInfo(val path: String) : HyperlinkInfo {
    override fun navigate(project: Project) {
        OpenUrlHyperlinkInfo(path).navigate(project)
    }
}
