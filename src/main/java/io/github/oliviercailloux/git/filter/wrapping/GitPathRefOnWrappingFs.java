package io.github.oliviercailloux.git.filter.wrapping;

import io.github.oliviercailloux.gitjfs.GitPathRef;
import org.eclipse.jgit.lib.ObjectId;

public class GitPathRefOnWrappingFs extends GitPathOnWrappingFs implements GitPathRef {

  protected GitPathRefOnWrappingFs(GitWrappingFs fs, GitPathRef delegate) {
    super(fs, delegate);
  }

  @Override
  GitPathRef delegate() {
    return (GitPathRef) super.delegate();
  }

  @Override
  public String getGitRef() {
    return delegate().getGitRef();
  }

  
}
