/*
  Copyright © 2020 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(){
  
  def projects = [:]
  
  //get set of projects and their configs
  config.run_fortify_assessments.each{ projectName, projectConfig ->

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
  assert projectConfig.steps
  def stepList = ['update','clean','translate','scan','upload']
  projectConfig.steps.each{ step ->
    assert stepList.contains(step) 
  }

  //buildID
  if (projectConfig.steps.contains('clean') ||
      projectConfig.steps.contains('translate') ||
      projectConfig.steps.contains('scan')) {
        assert projectConfig.buildID
  }

  //projectScanType
  if (projectConfig.steps.contains('translate')) {
    assert projectConfig.projectScanType
    assert ['fortifyAdvanced','fortifyDevenv','fortifyDotnetSrc',
            'fortifyGradle','fortifyJava','fortifyMaven3','fortifyMSBuild',
            'fortifyOther'].contains(projectConfig.projectScanType)
  }

  //appName
  if (projectConfig.steps.contains('upload')) {
    assert projectConfig.appName
  }

  //appVersion
  if (projectConfig.steps.contains('upload')) {
    assert projectConfig.appVersion
  }

  //for different translations
  if (projectConfig.projectScanType) { 
    //Java      
    if (projectConfig.projectScanType == 'fortifyJava') {
      assert projectConfig.javaSrcFiles
    }

    //devenv/MSBuild
    if (['fortifyDevenv','fortifyMSBuild'].contains(projectConfig.projectScanType)) {
      assert projectConfig.dotnetProject
    }

    //.NET
    if (projectConfig.projectScanType == 'fortifyDotnetSrc') {
      assert projectConfig.dotnetFrameworkVersion
      assert projectConfig.dotnetSrcFiles
    }

    //Gradle
    if (projectConfig.projectScanType == 'fortifyGradle') {
      assert projectConfig.gradleTasks
    }

    //Other
    if (projectConfig.projectScanType == 'fortifyOther') {
      assert projectConfig.otherIncludesList
    }

    //Advanced
    if (projectConfig.projectScanType == 'fortifyAdvanced') {
      assert projectConfig.advOptions
    }
  }

  //Optional fields
  //locale
  if (projectConfig.locale) {
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
  projectConfig.each{ field, value ->
    assert fieldList.contains(field)
  }
}