package edu.handong.csee.isel.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;

import edu.handong.csee.isel.bic.BIC;
import edu.handong.csee.isel.bic.BIChange;
import edu.handong.csee.isel.patch.BPatch;
import edu.handong.csee.isel.patch.Patch;
import edu.handong.csee.isel.runner.Input;
import edu.handong.csee.isel.utils.CSVmaker;

public class NoParser extends Parser {

	final static String[] Patchheaders = { "Project", "fix-commit", "fix-shortMessage", "fix-date", "fix-author",
			"patch" };
	final static String[] BICheaders = { "BIShal1", "BIpath", "fixPath", "fixShal1", "numLineBI", "numLinePrefix",
			"content" };

	public NoParser(Input input) {
		super(input);

	}

	public void parse() throws InvalidRemoteException, TransportException, GitAPIException, IOException {

		super.parse();
		Pattern bugMessagePattern = Pattern.compile("fix|bug|resolved", Pattern.CASE_INSENSITIVE);
		CSVmaker writer;
		final Pattern keyPattern = Pattern.compile("\\[?(\\w+\\-\\d+)\\]?");
		if (input.isBI) {
			writer = new CSVmaker(new File(input.outPath + "BIC_" + input.projectName + ".csv"), BICheaders);
			for (RevCommit commit : walk) {
				try {
					RevCommit parent = commit.getParent(0);
					Matcher m = bugMessagePattern.matcher(commit.getFullMessage());
					if (!m.find())
						continue;

					List<BIChange> bics = BIC.collect(parent, commit, input);
					if (bics == null)
						continue;
					for (BIChange bic : bics)
						writer.write(bic);

				} catch (ArrayIndexOutOfBoundsException e) {
					break;
				}
			}
		} else { // patch collect
			writer = new CSVmaker(new File(input.outPath + "BPatch_" + input.projectName + ".csv"), Patchheaders);
			for (RevCommit commit : walk) {
				try {
					RevCommit parent = commit.getParent(0);
					Matcher m = bugMessagePattern.matcher(commit.getFullMessage());
					if (!m.find())
						continue;

					List<String> patches = BPatch.collect(parent, commit, input);
					if (patches == null)
						continue;
					for (String patch : patches) {
						Patch data = new Patch(input.projectName, commit.name(), commit.getShortMessage(),
								commit.getAuthorIdent().getWhen(), commit.getAuthorIdent().getName(), patch);
						int len = data.patch.split("\n").length;
						if (len > 500) {
							continue;
						}
						writer.write(data);

					}
				} catch (ArrayIndexOutOfBoundsException e) {
					break;
				}
			}
		}
	}

}
