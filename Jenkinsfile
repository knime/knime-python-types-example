#!groovy
def BN = (BRANCH_NAME == 'master' || BRANCH_NAME.startsWith('releases/')) ? BRANCH_NAME : 'releases/2023-10'

library "knime-pipeline@todo/DEVOPS-2151-workflow-tests-default-mac-os-arm"

static final String DEFAULT_WF_TESTS_PYTHON_ENV = 'env_py39_kn47.yml'

properties([
    pipelineTriggers([
        upstream("knime-python/${BRANCH_NAME.replaceAll('/', '%2F')}")
    ]),
    parameters(workflowTests.getConfigurationsAsParameters()),
    buildDiscarder(logRotator(numToKeepStr: '5')),
    disableConcurrentBuilds()
])

try {
    // provide the name of the update site project
    knimetools.defaultTychoBuild('org.knime.update.pythontypeexample', 'maven && workflow-tests && java17')

    String envYml = "${DEFAULT_WF_TESTS_PYTHON_ENV}"

    withEnv([ "KNIME_WORKFLOWTEST_PYTHON_ENVIRONMENT=${envYml}" ]) {
        stage("Workflowtests with Python ${envYml}") {
            workflowTests.runTests(
                dependencies: [
                    repositories: [
                        'knime-pythontypeexample',
                        'knime-python',
                        'knime-scripting-editor',
                        'knime-python-legacy',
                        'knime-conda',
                        'knime-filehandling',
                        'knime-core-columnar',
                    ],
                    ius: [
                        'org.knime.features.core.columnar.feature.group',
                        'org.knime.features.pythontypeexample.feature.group'
                    ]
                ]
            )
        }
    }

    stage('Sonarqube analysis') {
        env.lastStage = env.STAGE_NAME
        workflowTests.runSonar()
    }
} catch (ex) {
    currentBuild.result = 'FAILURE'
    throw ex
} finally {
    notifications.notifyBuild(currentBuild.result);
}

/* vim: set shiftwidth=4 expandtab smarttab: */
