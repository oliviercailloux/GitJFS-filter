package io.github.oliviercailloux.git.filter.wrapping;

import com.google.common.collect.ImmutableList;
import io.github.oliviercailloux.gitjfs.Commit;
import io.github.oliviercailloux.gitjfs.GitPathRoot;
import io.github.oliviercailloux.gitjfs.GitPathRootSha;
import io.github.oliviercailloux.gitjfs.GitPathSha;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import org.eclipse.jgit.lib.ObjectId;

public class GitPathShaOnWrappingFs extends GitPathOnWrappingFs implements GitPathSha {

  protected GitPathShaOnWrappingFs(GitWrappingFs fs, GitPathSha delegate) {
    super(fs, delegate);
  }

  @Override
  GitPathSha delegate() {
    return (GitPathSha) super.delegate();
  }

  @Override
  public ObjectId getStaticCommitId() {
    return delegate().getStaticCommitId();
  }
  
}
