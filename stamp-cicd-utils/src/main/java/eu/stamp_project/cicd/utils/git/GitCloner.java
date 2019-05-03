package eu.stamp_project.cicd.utils.git;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.jgit.api.Git;

/**
 * Clone Git repository.
 * @author Pierre-Yves Gibello - OW2
 */
public class GitCloner {

    /**
     * Clone Git repository (git protocol, eg. github or gitlab)
     * @param uri The clone URI (generally https://)
     * @param destDir The destination directory
     * @param shortPath If true, project dir name is determined by the ".git" part of the URI. If false, it is the full URI path.
     * @return The directory where the project is cloned
     * @throws IOException
     */
    public static String cloneRepository(String uri, String destDir, boolean shortPath) throws IOException {
    	try {
    		URL url = new URL(uri);
    		String path = url.getPath();
    		if(path.endsWith(".git")) path = path.substring(0, path.length()-4);
    		if(shortPath) {
    			int pos = path.lastIndexOf("/");
    			if(pos >= 0) path = path.substring(pos+1);
    		}

    		String cloneDir = new String(destDir + File.separator + path);
    		Git.cloneRepository()
    			  .setURI(uri)
    			  .setDirectory(new File(cloneDir))
    			  .call();

    		return cloneDir;
    	} catch(Exception e) {
    		throw new IOException(e);
    	}
    }
}
