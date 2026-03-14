package com.kepler88d.diffplugin.git

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsException
import com.intellij.openapi.vfs.VirtualFile
import com.kepler88d.diffplugin.diff.DiffStats
import com.kepler88d.diffplugin.diff.DiffStatsFallbackCalculator
import com.kepler88d.diffplugin.diff.DiffStatsPathKey
import com.kepler88d.diffplugin.diff.DiffStatsRequest
import git4idea.GitUtil
import git4idea.commands.GitBinaryHandler
import git4idea.commands.GitCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal object GitDiffStatsReader {
    suspend fun load(project: Project, requests: List<DiffStatsRequest>): Map<String, DiffStats> =
        withContext(Dispatchers.IO) {
            val statsByKey = mutableMapOf<String, DiffStats>()
            val requestsByRoot = groupByRoot(project, requests)
            requestsByRoot.forEach { (root, rootRequests) ->
                val gitStats = loadGitStats(project, root, rootRequests)
                rootRequests.forEach { request ->
                    val stats = gitStats[request.key] ?: DiffStatsFallbackCalculator.calculate(request)
                    if (stats?.hasValue == true) statsByKey[request.key] = stats
                }
            }
            statsByKey
        }

    private fun groupByRoot(project: Project, requests: List<DiffStatsRequest>): Map<VirtualFile?, List<DiffStatsRequest>> {
        val pathsByKey = requests.groupBy { it.primaryPath.path }
        val rootsByPath = GitUtil.sortFilePathsByGitRootIgnoringMissing(project, requests.map { it.primaryPath })
        val requestsByRoot = linkedMapOf<VirtualFile?, MutableList<DiffStatsRequest>>()
        rootsByPath.forEach { (root, paths) ->
            val rootRequests = paths.flatMap { pathsByKey[it.path].orEmpty() }
            if (rootRequests.isNotEmpty()) requestsByRoot.getOrPut(root) { mutableListOf() }.addAll(rootRequests)
        }
        requests.filter { request -> rootsByPath.values.flatten().none { it.path == request.primaryPath.path } }
            .forEach { request -> requestsByRoot.getOrPut(null) { mutableListOf() }.add(request) }
        return requestsByRoot
    }

    private fun loadGitStats(project: Project, root: VirtualFile?, requests: List<DiffStatsRequest>): Map<String, DiffStats> {
        if (root == null) return emptyMap()
        return try {
            val bytes = GitBinaryHandler(project, root, GitCommand.DIFF).apply {
                setSilent(true)
                withNoTty()
                addParameters("--numstat", "-z", "--find-renames", "HEAD")
                endOptions()
                addRelativePaths(requests.map { it.primaryPath })
            }.run()
            GitNumstatParser.parse(bytes).associate { entry ->
                DiffStatsPathKey.fromRootRelativePath(root, entry.path) to entry.stats
            }
        } catch (exception: ProcessCanceledException) {
            throw exception
        } catch (_: VcsException) {
            emptyMap()
        }
    }
}
