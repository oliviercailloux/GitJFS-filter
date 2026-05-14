package io.github.oliviercailloux.git.filter;

import io.github.oliviercailloux.gitjfs.GitPath;

@SuppressWarnings("AbbreviationAsWordInName")
@Deprecated
sealed interface IGitPathOnFilteredFs extends GitPath permits IGitPathRootOnFilteredFs,
    GitPathOnFilteredFs, GitPathShaOnFilteredFs, GitPathRefOnFilteredFs {
  GitPath delegate();

  @Override
  GitFilteringFs getFileSystem();

  @Override
  IGitPathRootOnFilteredFs getRoot();
}
