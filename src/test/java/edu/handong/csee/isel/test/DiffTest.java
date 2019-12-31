package edu.handong.csee.isel.test;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;

public class DiffTest {

	public static void main(String[] args) {
		File gitDir = new File("/Users/kimsukjin/git/DataForSZZ"); 
		String BFCCommit = "768b0df07b2722db926e99a8f917deeb5b55d628";
		
		try {
			Git git = Git.open(gitDir);
			Repository repo = git.getRepository();
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
