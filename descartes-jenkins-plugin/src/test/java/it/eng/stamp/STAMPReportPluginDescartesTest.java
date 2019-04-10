package it.eng.stamp;


import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;

public class STAMPReportPluginDescartesTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
	JenkinsRule.WebClient wc;


    @Test
    public void testDefaultConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getPublishersList().add(new STAMPReportCollector());
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(new STAMPReportCollector(), project.getPublishersList().get(0));
    }
   

}