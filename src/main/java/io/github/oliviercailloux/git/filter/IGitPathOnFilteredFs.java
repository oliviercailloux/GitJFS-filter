package io.github.oliviercailloux.git.filter;

import io.github.oliviercailloux.gitjfs.GitPath;

@SuppressWarnings("AbbreviationAsWordInName")
sealed interface IGitPathOnFilteredFs extends GitPath
    permits IGitPathRootOnFilteredFs, GitPathOnFilteredFs {
  GitPath delegate();
}
