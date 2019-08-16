package eu.stamp_project.descartes.gitlab;

import java.util.Properties;

import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.MutationResultListenerFactory;

/**
 * Listener factory to provide GITLAB-ISSUES reporting to Descartes
 * To configure, add GITLAB-ISSUES to Descartes/pitest output (like for HTML or JSON).
 * @author Pierre-Yves Gibello - OW2
 *
 */
public class GitlabIssueListenerFactory implements MutationResultListenerFactory {

	public String description() {
		return "Mutation testing elements Gitlab Issue report plugin";
	}

	public MutationResultListener getListener(Properties props, ListenerArguments args) {
		/*
		 props can be set in pom.xml as follows, within <configuration> element:
		  <pluginConfiguration>
            <gitlab-url>value1</gitlab-url>
            <gitlab-token>value2</gitlab-token>
            <gitlab-project>value3</gitlab-project>
            < ... >
            <keyN>valueN</keyN>
          </pluginConfiguration>
         Which corresponds to properties:
          key1: value1
          ...
          keyN: valueN
		 */
		return new MutationReportListener(props, args);
	}

	public String name() {
		return "GITLAB-ISSUES";
	}

}
