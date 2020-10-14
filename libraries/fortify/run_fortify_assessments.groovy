/*
  Copyright © 2020 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(){
  
  def projects = [:]
  
  //get set of projects and their configs
  config.each{ projectName, projectConfig ->

    //validate config
    fortify_validation(projectConfig)

    //if we made it here, validation passed, so add it to the array
    projects[projectName] = { run_fortify_steps(projectName, projectConfig) }

  }

  //kick off assessments
  parallel projects

}

def fortify_validation(projectConfig){
  //(semi-)Required fields
  //steps
  assert projectConfig.hasVariable('steps')
  assert projectConfig.steps.size() > 0
  def stepList = ['update','clean','translate','scan','upload']
  projectConfig.steps.each{ step ->
    assert stepList.contains(step) 
  }

  //buildID
  if (projectConfig.steps.contains('clean') ||
      projectConfig.steps.contains('translate') ||
      projectConfig.steps.contains('scan')) {
        assert projectConfig.hasVariable('buildID')
  }

  //projectScanType
  if (projectConfig.steps.contains('translate')) {
    assert projectConfig.hasVariable('projectScanType')
    assert ['fortifyAdvanced','fortifyDevenv','fortifyDotnetSrc',
            'fortifyGradle','fortifyJava','fortifyMaven3','fortifyMSBuild',
            'fortifyOther'].contains(projectConfig.projectScanType)
  }

  //appName
  if (projectConfig.steps.contains('upload')) {
    assert projectConfig.hasVariable('appName')
  }

  //appVersion
  if (projectConfig.steps.contains('upload')) {
    assert projectConfig.hasVariable('appVersion')
  }

  //for different translations
  if (projectConfig.hasVariable('projectScanType') { 
    //Java      
    if (projectConfig.projectScanType == 'fortifyJava') {
      assert projectConfig.hasVariable('javaSrcFiles')
    }

    //devenv/MSBuild
    if (['fortifyDevenv','fortifyMSBuild'].contains(projectConfig.projectScanType)) {
      assert projectConfig.hasVariable('dotnetProject')
    }

    //.NET
    if (projectConfig.projectScanType == 'fortifyDotnetSrc') {
      assert projectConfig.hasVariable('dotnetFrameworkVersion')
      assert projectConfig.hasVariable('dotnetSrcFiles')
    }

    //Gradle
    if (projectConfig.projectScanType == 'fortifyGradle') {
      assert projectConfig.hasVariable('gradleTasks')
    }

    //Other
    if (projectConfig.projectScanType == 'fortifyOther') {
      assert projectConfig.hasVariable('otherIncludesList')
    }

    //Advanced
    if (projectConfig.projectScanType == 'fortifyAdvanced') {
      assert projectConfig.hasVariable('advOptions')
    }
  }

  //Optional fields
  //locale
  if (projectConfig.hasVariable('locale')) {
    assert ['en','zh_CN','zh_TW','pt_BR','ko','es'].contains(projectConfig.locale)
  }

  //No unexpected fields
  def fieldList = ['steps','buildID','projectScanType','appName','appVersion',
            'updateServerURL','locale','maxHeap','addJVMOptions','debug',
            'verbose','logfile','excludeList','resultsFile','customRulepacks',
            'addOptions','filterSet','failureCriteria','pollingInterval',
            'javaSrcFiles','javaVersion','javaClasspath','javaAddOptions',
            'dotnetProject','dotnetFrameworkVersion','dotnetSrcFiles','dotnetLibdirs',
            'dotnetAddOptions','mavenOptions','gradleTasks','useWrapper',
            'gradleOptions','otherIncludesList','otherOptions','advOptions']
  projectConfig.each{ field ->
    assert fieldList.contains(field)
  }
}