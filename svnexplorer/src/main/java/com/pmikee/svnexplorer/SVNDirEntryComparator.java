/**
 *
 */
package com.pmikee.svnexplorer;

import java.util.Comparator;
import java.util.regex.Pattern;

import org.tmatesoft.svn.core.SVNDirEntry;

/**
 * @author mpeteri
 *
 */
public class SVNDirEntryComparator implements Comparator<SVNDirEntry> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(SVNDirEntry version1, SVNDirEntry version2) {
		String[] v1 = version1.getName().split("-")[version1.getName().split("-").length - 1].split(Pattern.quote("."));
		String[] v2 = version2.getName().split("-")[version2.getName().split("-").length - 1].split(Pattern.quote("."));
		if (major(v1) == major(v2)) {
			if (minor(v1) != minor(v2)) {
				return minor(v1).compareTo(minor(v2));
			}
			if (harmadik(v1) != harmadik(v2)) {
				return harmadik(v1).compareTo(harmadik(v2));
			}
			if (negyedik(v1) != negyedik(v2)) {
				return negyedik(v1).compareTo(negyedik(v2));
			}
		} else {

			return major(v1).compareTo(major(v2));
		}
		return 0;

	}

	private Integer major(String[] version) {
		return Integer.parseInt(version[0]);
	}

	private Integer minor(String[] version) {
		return version.length > 1 ? Integer.parseInt(version[1]) : 0;
	}

	private Integer harmadik(String[] version) {
		return version.length > 2 ? Integer.parseInt(version[2]) : 0;
	}

	private Integer negyedik(String[] version) {
		return version.length > 3 ? Integer.parseInt(version[3]) : 0;
	}
}
