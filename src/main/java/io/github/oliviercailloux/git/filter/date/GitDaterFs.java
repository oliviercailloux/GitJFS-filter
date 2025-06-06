package io.github.oliviercailloux.git.filter.date;

import io.github.oliviercailloux.git.filter.wrapping.GitWrappingFs;
import io.github.oliviercailloux.gitjfs.GitFileSystem;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathRootShaCached;
import java.time.Instant;
import java.util.function.Function;

public class GitDaterFs extends GitWrappingFs{

  private Function<GitPathRootShaCached, Instant> dateFunction;
  
    private GitDaterFs(GitFileSystem delegate, Function<GitPathRootShaCached, Instant> dateFunction) {
      super(delegate);
      this.dateFunction = dateFunction;
  }
}
