/*
 * Copyright 2017 Vestmark, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express 
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vestmark.bitbucket.plugin;

import java.util.List;
import java.util.LinkedList;

import org.apache.commons.lang3.math.NumberUtils;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.git.command.merge.conflict.GitMergeConflict;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.vestmark.bitbucket.plugin.VersionComparator;

/**
 * The MergeConflictDetector class stores the pull request instance and exposes details about it
 * through accessor methods that can be accessed through Soy.
 */
public class MergeConflictDetector 
{
  private final ApplicationUser user;
  private final PullRequest pullRequest;
  private final PullRequestRef fromBranch;
  private final PullRequestRef toBranch;
  private final Repository fromRepo;
  private final Repository toRepo;
  private final String fromBranchId;   // ID format: refs/head/master
  private final String toBranchId;
  private final String fromBranchName; // Display ID format: master
  private final String toBranchName;
  private final String compareUrlPrefix;
  private final List<MergeResult> mergeResults;

  public MergeConflictDetector(ApplicationUser user, PullRequest pullRequest, String hostUrl)
  {
    this.user = user;
    this.pullRequest = pullRequest;
    fromBranch = pullRequest.getFromRef();
    toBranch = pullRequest.getToRef();
    fromRepo = fromBranch.getRepository();
    toRepo = toBranch.getRepository();
    fromBranchId = fromBranch.getId();
    toBranchId = toBranch.getId();
    fromBranchName = fromBranch.getDisplayId();
    toBranchName = toBranch.getDisplayId();
    compareUrlPrefix = hostUrl + "/projects/" + fromRepo.getProject().getKey() + "/repos/" 
                               + fromRepo.getSlug() + "/compare/diff?sourceBranch=" + fromBranchId 
                               + "&targetBranch=";
    mergeResults = new LinkedList<MergeResult>();
  }

  public ApplicationUser getUser()
  {
    return user;
  }

  public PullRequest getPullRequest()
  {
    return pullRequest;
  }

  public PullRequestRef getFromBranch()
  {
    return fromBranch;
  }

  public PullRequestRef getToBranch()
  {
    return toBranch;
  }

  public Repository getFromRepo()
  {
    return fromRepo;
  }

  public Repository getToRepo()
  {
    return toRepo;
  }

  public String getFromBranchId()
  {
    return fromBranchId;
  }

  public String getToBranchId()
  {
    return toBranchId;
  }

  public String getFromBranchName()
  {
    return fromBranchName;
  }

  public String getToBranchName()
  {
    return toBranchName;
  }

  public String getCompareUrlPrefix()
  {
    return compareUrlPrefix;
  }

  public List<MergeResult> getMergeResults()
  {
    return mergeResults;
  }

  public void addResult(Branch toBranch, List<GitMergeConflict> mergeConflicts, 
                        List<String> messages, List<String> files)
  {
    mergeResults.add(new MergeResult(toBranch, mergeConflicts, messages, files));
  }

  public boolean isRelated(Branch toBranch)
  {
    String family = toBranchName.substring(toBranchName.lastIndexOf("/")).replaceAll("\\/([^\\.|-]*).*", "$1");
    String toBranchFamily = toBranch.getDisplayId().substring(toBranch.getDisplayId().lastIndexOf("/")).replaceAll("\\/([^\\.|-]*).*", "$1");
    if (NumberUtils.isNumber(family) && NumberUtils.isNumber(toBranchFamily)) {
      return true;
    }
    if (family.equals(toBranchFamily)) {
      return true;
    }
    return false;
  }
  
  public boolean isUpstreamBranch(Branch toBranch)
  {
    return VersionComparator.AS_STRING.compare(toBranch.getDisplayId(), toBranchName) >= 0 ? true : false;
  }

  /*
   * The MergeResult inner class must be public to fulfill JavaBeans requirements in order for the
   * accessor methods to be available in Soy.
   */
  public class MergeResult
  {
    private final Branch toBranch;
    private final List<GitMergeConflict> mergeConflicts;
    private final List<String> messages;
    private final List<String> files;

    public MergeResult(Branch toBranch, List<GitMergeConflict> mergeConflicts, 
                       List<String> messages, List<String> files)
    {
      this.toBranch = toBranch;
      this.mergeConflicts = mergeConflicts;
      this.messages = messages;
      this.files = files;
    }

    public Branch getToBranch()
    {
      return toBranch;
    }

    public List<GitMergeConflict> getMergeConflicts()
    {
      return mergeConflicts;
    }

    public List<String> getMessages()
    {
      return messages;
    }

    public List<String> getFiles()
    {
      return files;
    }
  }
}