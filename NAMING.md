# Naming Rules for this Project

This document attempts to clarify the naming rules for this project and the reasoning behind them.

## Guiding Principles

1. The internal organisation of PSI may change from time to time but our control software should be organised
   in such a way that such changes result in minimal alterations to the codebase.

       => It's ok for the external organisation name (PSI) to appear as part of our software 
          products naming strategy but the internal organisational names (eg names like 'GFA', 'AEK' 
          etc) should not (ideally) appear.

1. We should strive to name our software in such a way that encourages it to be deployed in the widest possible 
   context (including outside PSI). Sometimes, though, a piece of software will be targeted on a specific piece 
   of PSI infrastructure (eg inside SwissFEL, HIPA, SLS) and it will be hard to see how the supporting software 
   could be used elsewhere. In that situation it seems reasonable to use names that reflect the subdomain. 
    
       => Names like 'ch.psi.sf.resonantkicker', 'ch.psi.sls.3hc' etc (which would hypothetically support 
          one-off items in GFA's controls infrastructure) are ok as package names. But names  like 'ch.psi.gfa' 
          are not because they reflects PSI's internal organisational structure.. 
       
1. Whilst names associated with the *software source code* should be chosen for the widest possible degree of 
   applicability the names associated with *software deployment* (eg the names of web servers, machine names etc) 
   can be chosen rather more flexibly. Typically PSI's deployment infrastructure will change frequently as new 
   machines, container technologies etc come and go. Our naming practices need to be agile in the face of this 
   reality.
 
       => Names related to deployment can be chosen more flexibly and can include details related to
          our internal organisation (eg gfa-wica.psi.ch and gfa-autodeploy.psi.ch would be acceptable
          names for a web server). 
  
1. When looking at the control system deployment areas (eg the RHEL7 '/etc/systemd/system' systemctl area) it should
   be possible to easily distinguish between those artifacts that were developed at PSI and those which were 
   installed as part of the normal operating context. 
   
       => Deployment components developed at PSI could be prefixed with 'psi_XXX' or even 'gfa_XXX'. 
   
1. Where possible the project's naming convention should attempt to follow existing best practices. Where such 
   best practices are not clear the existing de-facto organisational practices can be used. This leads to the 
   following conclusions regarding the use of hyphens and underscores:

       => PSI GitLab Repository names will use underscores.
       => The maven groupId: will be 'ch.psi' unless the project underneath has multiple modules (in which 
          case the project name can be appended). 
       => The maven artifactId: will be the name of the project. This must be unique within PSI.
       => The Java package names will be valid Java identifiers (and therefore cannot contain hyphens). Underscores are
          acceptable.  
       => PSI Artifactory names will use hyphens.
       => PSI Docker Hub names will use hyphens.
       => Docker container names will use underscores.
       => RHEL service files will use hyphens.
   

## Implementation

### Sourcecode Artifacts

#### PSI GitLab Repository 

 * Group Name:   'controls_highlevel_applications' 
 * Project Name: 'ch.psi.wica2'

Note:
A quick look at the situation over at GitHub seems to indicate that far more repositories use hyphens than underscores 
as their name separator. For example on the SO issue [here](https://stackoverflow.com/questions/11947587/is-there-a-naming-convention-for-git-repositories) 250 
people upvoted the answer which suggested that a project's name uses hyphens rather than underscores or camelcase. 
Nevertheless in PSI's GitLab hosting environment underscore seems to be far more common in both group names and 
project names.


#### Maven Coordinates

 * groupId:    'ch.psi'
 * artifactId: 'wica'
 * version: eg '0.8.0-RELEASE'


#### Java Top Level Package

  * 'ch.psi.wica'

Note:
IntelliJ will not allow refactorings which use hyphens in package names. It will allow underscores,
and in the Oracle docs it is suggested that organisations with hyphens in their names are converted
to underscores. Ideally package names should be short.


### Deployment Artifacts
 
#### PSI Artifactory 

 * Releases:
     * location:   eg 'libs-releases-local/ch/psi/wica/0.8.1-RELEASE'
     * artifacts:  eg 'wica-0.8.1-RELEASE.jar'
 
 * Snapshots:
     * location:   eg 'libs-snapshots-local/ch/psi/wica/0.8.1-SNAPSHOT'
     * artifacts:  eg 'wica-0.8.1-SNAPSHOT.jar'


#### PSI Docker Hub

  * Releases:     'https://docker.psi.ch:5000/wica/wica-0.8.1-RELEASE.jar'
  * Snapshots: eg 'https://docker.psi.ch:5000/wica/wica-0.8.1-20181023.163837-16.jar'

#### PSI Ansible Playbook 


 * Install Script:   'install_gfa-wica'
 * Uninstall Script: 'uninstall_gfa-wica' 
 * Install Location: '/root/gfa-wica'

Note: 
For further details see the separate [gfa_ansible](https://git.psi.ch/controls_highlevel_applications/gfa_ansible) project.

 
#### RHEL7 Service Files
 
 * Service File Name:     'gfa-wica.service'
 * Service File Location: '/etc/systemd/system'
 
Note: 
For further details see the separate [gfa_ansible](https://git.psi.ch/controls_highlevel_applications/gfa_ansible) project.
 
 
#### Docker Container Deployment 

 * Container Name: 'gfa-wica'

Note:
Docker seems to accept both underscores and hyphens in container id names but when left to make its own 
automatic choice the docker service chooses underscores over hyphens.


#### Production Server Web URL

  * https://gfa-wica.psi.ch  
