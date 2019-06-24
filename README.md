# descartes-issue-generator

This project extends the pitmp-maven-plugin, to generate Gitlab issues out of mutation testing:
to provide that, it adds a new output format, GITLAB-ISSUES, to PIT/Descartes.

The generated Gitlab issues will be overwritten (updated) if the plugin is run more than once,
so you can use it multiple times without generating tons of verbose issues...

To use it, just add some configuration to pitmp-maven-plugin, to declare:
- GITLAB-ISSUES as an output format (requires fullMutationMatrix=true so that list of succeding tests is provided by PIT)
- Gitlab configuration (optional): if you want Gitlab issues to be injected in your repository, specify the destination
(Gitlab url, project and token).

Note that, as fullMutationMatrix=true is required, PIT will only allow XML output in addition to GITLAB-ISSUES
(eg. no HTML nor JSON is allowed). This limitation is due to PIT/Descartes.

Example of Descartes plugin configuration to enable GITLAB-ISSUE output:

```
  <plugin>
    <groupId>eu.stamp-project</groupId>
    <artifactId>pitmp-maven-plugin</artifactId>
    <version>1.3.8-SNAPSHOT</version>
    <!-- All PIT's properties can be used. -->
    <dependencies>
      <dependency>
        <groupId>eu.stamp-project</groupId>
        <artifactId>descartes-issue-generator</artifactId>
        <version>1.0.0-SNAPSHOT</version>
      </dependency>
    </dependencies>
    <!--
      fullMutationMatrix=true required (so that list of succeding tests is provided by PIT) 
      With that option, PIT/Descartes only allows XML output (no HTML, JSON...)
      Output will be written in reportsDirectory (with same default as for Descartes),
      and optionally to Gitlab if appropriate configuration of destination is provided.
    -->
    <configuration>
      <mutationEngine>descartes</mutationEngine>
      <fullMutationMatrix>true</fullMutationMatrix>
      <reportsDirectory>/tmp/pit-reports</reportsDirectory>
      <outputFormats>
        <value>GITLAB-ISSUES</value>
        <value>XML</value>
      </outputFormats>
      <!-- Sample config of Gitlab destination (if provided, Gitlab issues will be generated) -->
      <pluginConfiguration>
        <gitlabUrl>http://localhost</gitlabUrl>
        <gitlabToken>i_rjH-MM3YuFsvAxCTsH</gitlabToken>
        <gitlabProject>2</gitlabProject>
      </pluginConfiguration>
    </configuration>
  </plugin>
```

