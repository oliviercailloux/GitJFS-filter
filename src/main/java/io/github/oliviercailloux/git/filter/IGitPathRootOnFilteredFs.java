package io.github.oliviercailloux.git.filter;

import io.github.oliviercailloux.gitjfs.GitPathRoot;

@SuppressWarnings("AbbreviationAsWordInName")
@Deprecated
sealed interface IGitPathRootOnFilteredFs extends IGitPathOnFilteredFs, GitPathRoot
    permits GitPathRootOnFilteredFs, GitPathRootRefOnFilteredFs, GitPathRootShaOnFilteredFs,
    GitPathRootShaCachedOnFilteredFs {
  @Override
  GitPathRoot delegate();
}
