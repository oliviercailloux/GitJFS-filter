package io.github.oliviercailloux.git.filter;

import io.github.oliviercailloux.gitjfs.GitPathRoot;

@SuppressWarnings("AbbreviationAsWordInName")
sealed interface IGitPathRootOnFilteredFs extends IGitPathOnFilteredFs
    permits GitPathRootOnFilteredFs, GitPathRootRefOnFilteredFs, GitPathRootShaOnFilteredFs,
    GitPathRootShaCachedOnFilteredFs {
  @Override
  GitPathRoot delegate();
}
