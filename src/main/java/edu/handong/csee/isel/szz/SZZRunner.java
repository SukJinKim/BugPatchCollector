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
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.szz.utils.Utils;

public class SZZRunner {

	public static void main(String[] args) {
		/**
		 * gitDir and BFCCommit are test cases. 
		 * Be aware of changing directory and change bfc commit unless you use SukJinKim/DataForSZZ github repo.
		 */
		File gitDir = new File("/Users/kimsukjin/git/DataForSZZ");
				//new File("/Users/kimsukjin/git/PLOP2");
		String BFCCommit = //"768b0df07b2722db926e99a8f917deeb5b55d628"; //last commit (BFC)
				"4ec01ef1579b5fa724cf2df0876a1fddcb2b87b7"; //3rd commit
				//"80b3937067504a86582cb4316dc0fef8e2e7d6f4";
						
		Git git;
		Repository repo;
		
		try {
			git = Git.open(gitDir);
			repo = git.getRepository();
			
			Iterable<RevCommit> walk = git.log().call();
			
			List<RevCommit> commitList = IterableUtils.toList(walk);
			
			for(RevCommit rev : commitList) {
				if(rev.getName().equals(BFCCommit)) {
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
					
					//do diff
					List<DiffEntry> diffs = df.scan(parent.getTree(), rev.getTree());
					
					// check the change size in a patch
					int numLinesChanges = 0; // deleted + added
					String id =  rev.name() + "";
					
					for (DiffEntry diff : diffs) {
						String oldPath = diff.getOldPath();
						String newPath = diff.getNewPath();
						
						// ignore when no previous revision of a file, Test files, and non-java files.
						if(oldPath.equals("/dev/null") || newPath.indexOf("Test")>=0  || !newPath.endsWith(".java")) continue;

						// get preFixSource and fixSource without comments
						String prevFileSource=Utils.removeComments(Utils.fetchBlob(repo, id +  "~1", oldPath));
						String fileSource=Utils.removeComments(Utils.fetchBlob(repo, id, newPath));
						
						//TEST
						System.out.println("");
						System.out.println("Old path : " + oldPath);
						System.out.println(prevFileSource);
						System.out.println("New path : " + newPath);
						System.out.println(fileSource);
						
						// get line indices that are related to BI lines.
						EditList editList = Utils.getEditListFromDiff(prevFileSource, fileSource);
						for(Edit edit:editList){
							
							
							int beginA = edit.getBeginA();
							int endA = edit.getEndA();
							int beginB = edit.getBeginB();
							int endB = edit.getEndB();
							
							//TEST
							System.out.println("Type : " + edit.getType());
							System.out.println("beginA : " + beginA);
							System.out.println("endA : " + endA);
							System.out.println("beginB : " + beginB);
							System.out.println("endB : " + endB);
							System.out.println("");
							
							numLinesChanges += (endA-beginA) + (endB-beginB);
						}
						//TEST
						System.out.println("numLinesChanges : " + numLinesChanges);
					}
					
					break;
				}
			}
		} catch (IOException | GitAPIException e) {
			
			e.printStackTrace();
		}

	}
}
