package edu.handong.csee.isel.patch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.runner.Input;
import edu.handong.csee.isel.utils.CSVmaker;
import edu.handong.csee.isel.utils.Utils;

public class BPatch {
//	final static String[] headers = { "Project", "fix-commit", "fix-shortMessage", "fix-date", "fix-author", "patch" };
	
	public static List<String> collect(RevCommit parent, RevCommit commit, Input input) throws IOException, GitAPIException {
		List<String> patches = new ArrayList<String>();
		int min = input.conditionMin;
		int max = input.conditionMax;
		Git git = input.git;
		
		int patchSize = 0;
		Repository repo = git.getRepository();
		final List<DiffEntry> diffs = git.diff()
				.setOldTree(Utils.prepareTreeParser(repo, parent.getId().name()))
				.setNewTree(Utils.prepareTreeParser(repo, commit.getId().name())).call();
		for (DiffEntry diff : diffs) {
			if(!diff.getNewPath().endsWith(".java") || diff.getNewPath().contains("test")) continue;
			String patch = getPatch(diff, repo);
			if (patch == null)
				continue;
			patches.add(patch);
			int numLines = Utils.parseNumOfDiffLine(patch);
			patchSize += numLines;
			if (patchSize > max) { // for speed
				return null;
			}
		}

		if (patchSize < min || patchSize > max) {
			return null;
		}
		return patches;
	}
	
	public static String getPatch(DiffEntry diff, Repository repository) throws IOException {

		String patch = null;
		if (!diff.getNewPath().endsWith(".java")) // only .java format
			return null;

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (DiffFormatter formatter = new DiffFormatter(output)) {
			formatter.setRepository(repository);
			formatter.format(diff);
		}
		output.flush();
		output.close();
		patch = output.toString("UTF-8");

		return patch;
	}
}
