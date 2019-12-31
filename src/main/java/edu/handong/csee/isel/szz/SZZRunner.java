package edu.handong.csee.isel.szz;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.collections4.IterableUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class SZZRunner {

	public static void main(String[] args) {
		/**
		 * gitDir and BFCCommit are test cases. 
		 * Be aware of changing directory and change bfc commit unless you use SukJinKim/DataForSZZ github repo.
		 */
		File gitDir = new File("/Users/kimsukjin/git/DataForSZZ"); 
		String BFCCommit = "768b0df07b2722db926e99a8f917deeb5b55d628";
		
		Git git;
		Repository repo;
		
		try {
			git = Git.open(gitDir);
			repo = git.getRepository();
			
			Iterable<RevCommit> walk = git.log().call();
			
			List<RevCommit> commitList = IterableUtils.toList(walk);
			
			for(RevCommit rev : commitList) {
				if(rev.getName().equals(BFCCommit)) {
					//TODO Implement git diff BFC~1 BFC
					RevCommit parent = rev.getParent(0); //Get BFC pre-commit (i.e. BFC~1 commit)
					if(parent == null) {
						System.err.println("WARNING: Parent commit does not exist: " + rev.name() );
						break;
					}
		           
		            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE); 
					df.setRepository(repo);
					df.setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS));
					df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
					df.setDetectRenames(true);
					df.setPathFilter(PathSuffixFilter.create(".java"));
					
					List<DiffEntry> diffs = df.scan(parent.getTree(), rev.getTree());
		           
		            for (DiffEntry entry : diffs) {
		            	System.out.println("Ver 2 (using JC's)");
	                    System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
	                    try (DiffFormatter formatter = new DiffFormatter(System.out)) {
	                        formatter.setRepository(repo);
	                        formatter.format(entry);
	                    }
	                }
					
					
					break;
				}
			}
			
		} catch (IOException | GitAPIException e) {
			
			e.printStackTrace();
		}

	}
}
