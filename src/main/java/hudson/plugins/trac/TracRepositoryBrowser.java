package hudson.plugins.trac;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.AbstractProject;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SubversionChangeLogSet.LogEntry;
import hudson.scm.SubversionChangeLogSet.Path;
import hudson.scm.SubversionRepositoryBrowser;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * {@link SubversionRepositoryBrowser} that produces Trac links.
 */
public class TracRepositoryBrowser extends SubversionRepositoryBrowser {
    @DataBoundConstructor
    public TracRepositoryBrowser() {
    }


    protected TracProjectProperty getTracProjectProperty(LogEntry changeSet) {
    	AbstractProject<?,?> p = (AbstractProject<?,?>)changeSet.getParent().build.getProject();
    	return p.getProperty(TracProjectProperty.class);
    }

    
    /**
     * Gets a URL for the {@link TracProjectProperty#tracWebsite} value
     * configured for the current project.
     */
    private URL getTracWebURL(LogEntry changeSet) throws MalformedURLException {
    	TracProjectProperty tpp = getTracProjectProperty(changeSet);
        if(tpp==null)   
        	return null;
        else
        	return new URL(tpp.tracWebsite);
    }

    private String getPath(Path path) {
        String pathValue = path.getValue();
        TracProjectProperty tpp = getTracProjectProperty(path.getLogEntry());
        if(tpp != null && tpp.tracStrippedFromChangesetPath != null && pathValue != null
            && pathValue.startsWith(tpp.tracStrippedFromChangesetPath))
            return pathValue.substring(tpp.tracStrippedFromChangesetPath.length());
        else
            return pathValue;
    }

    @Override
    public URL getDiffLink(Path path) throws IOException {
        if(path.getEditType()!= EditType.EDIT)
            return null;    // no diff if this is not an edit change
        URL baseUrl = getTracWebURL(path.getLogEntry());
        int revision = path.getLogEntry().getRevision();
        return new URL(baseUrl, "changeset/" + revision + getPath(path) + "#file0");
    }

    @Override
    public URL getFileLink(Path path) throws IOException {
        URL baseUrl = getTracWebURL(path.getLogEntry());
        return baseUrl == null ? null : new URL(baseUrl, "browser" + getPath(path) + "#L1");
    }

    @Override
    public URL getChangeSetLink(LogEntry changeSet) throws IOException {
        URL baseUrl = getTracWebURL(changeSet);
        return baseUrl == null ? null : new URL(baseUrl, "changeset/" + changeSet.getRevision());
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {
        public DescriptorImpl() {
            super(TracRepositoryBrowser.class);
        }

        public String getDisplayName() {
            return "Trac";
        }
    }
}
