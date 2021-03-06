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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestRef;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scm.git.command.merge.conflict.GitMergeConflict;
import com.atlassian.bitbucket.user.ApplicationUser;
import com.vestmark.bitbucket.plugin.VersionComparator;
import com.vestmark.bitbucket.plugin.MergeResult;

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
  private final VersionComparator mergeResultsComparator;

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
    mergeResultsComparator = new VersionComparator<MergeResult>(MergeResult::getToBranchDisplayId);
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

  public void addResult(MergeResult result) {
    mergeResults.add(result);
  }

  public void sortMergeResults() {
    Collections.sort(mergeResults, mergeResultsComparator);
  }

  public boolean isRelated(Branch otherBranch)
  {
    return VersionComparator.AS_STRING.getFamily(otherBranch.getDisplayId()).equals(VersionComparator.AS_STRING.getFamily(toBranchName));
  }
  
  public boolean isUpstreamBranch(Branch toBranch)
  {
    return VersionComparator.AS_STRING.compare(toBranch.getDisplayId(), toBranchName) >= 0 ? true : false;
  }

  public List<MergeResultsModel> getMergeResultsModelList() {
    List<MergeResultsModel> resultsModelList = new ArrayList<MergeResultsModel>();
    for (MergeResult result : mergeResults) {
      resultsModelList.add(new MergeResultsModel(result.getToBranch().getDisplayId(),
                                             result.getToBranch().getId(),
                                             result.getMergeConflictsTotal(),
                                             result.getMessages(),
                                             result.getFiles()));       
    }
    return resultsModelList;
  }
}
