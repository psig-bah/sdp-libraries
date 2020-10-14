/*
  Copyright © 2020 Booz Allen Hamilton. All Rights Reserved.
  This software package is licensed under the Booz Allen Public License. The license can be found in the License file or at http://boozallen.github.io/licenses/bapl
*/

void call(projectName, projectConfig){
  
  //get steps in order
  def stepOrder = ['update', 'clean', 'translate', 'scan', 'upload'] 
  def sortedSteps = projectConfig.steps.sort{ a,b -> 
                      stepOrder.indexOf(a) <=> stepOrder.indexOf(b) }
  
  //parameter <-> step associations
  def stepParameters = [
    'update':['updateServerURL','locale'],
    'clean':['buildID','maxHeap','addJVMOptions','debug','verbose','logFile'],
    'translate':['buildID','projectScanType','maxHeap','addJVMOptions',
                 'debug','verbose','logFile','excludeList','javaSrcFiles',
                 'javaVersion','javaClasspath','javaAddOptions',
                 'dotnetProject','dotnetAddOptions','dotnetFrameworkVersion',
                 'dotnetSrcFiles','dotnetLibdirs','mavenOptions','gradleTasks',
                 'useWrapper','gradleOptions','otherIncludesList',
                 'otherOptions','advOptions'],
    'scan':['buildID','maxHeap','addJVMOptions','resultsFile',
            'customRulepacks','addOptions','debug','verbose','logFile'],
    'upload':['appName','appVersion','resultsFile','filterSet',
              'failureCriteria','pollingInterval']
  ]

  def scanParameters = [
    'javaSrcFiles','javaVersion','javaClasspath','javaAddOptions',
    'dotnetProject','dotnetAddOptions','dotnetFrameworkVersion',
    'dotnetSrcFiles','dotnetLibdirs','mavenOptions','gradleTasks','useWrapper',
    'gradleOptions','otherIncludesList','otherOptions','advOptions'
  ]

  //run steps
  node{
    //get the built/compiled code
    unstash "workspace"

    sortedSteps.each{ step ->
      stage('Fortify: ' + projectName + '::' + step){
        
        String command = "fortify" + step.capitalize()
        String scanType = ""
        String scanParams = ""

        //add step-appropriate parameters
        projectConfig.each{ parameter, value ->
          if (stepParameters[step].contains(parameter)) {
            if (step == "translate") && (parameter == "projectScanType") {
              scanType = "${parameter}: ${value}"
            } else if (step == "translate") {
              scanParams += "${parameter}: '${value}', "
            } else {
              command += " ${parameter}: '${value}',"
            }
          }
        }

        //string cleanup
        if (step == "translate") {
          scanParams = scanParams[0..-2]
          command += " $scanType($scanParams)"
        } else if (command[-1] == ",") {
          command = command[0..-2]
        }
        
        //run it  
        evaluate command
      }
    }
  }
}