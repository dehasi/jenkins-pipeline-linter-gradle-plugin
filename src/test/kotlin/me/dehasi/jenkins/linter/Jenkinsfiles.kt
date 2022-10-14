package me.dehasi.jenkins.linter

object Jenkinsfiles {
    const val CORRECT_JENKINSFILE_CONTENT = "" +
            "pipeline {\n" +
            "  agent any\n" +
            "    stages {\n" +
            "      stage ('Initialize') {\n" +
            "      steps {\n" +
            "        echo 'Placeholder.'\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            ""

    const val INCORRECT_JENKINSFILE_CONTENT = "" +
            "pipeline {\n" +
            "  agent\n" + //  just removed  any
            "    stages {\n" +
            "      stage ('Initialize') {\n" +
            "      steps {\n" +
            "        echo 'Placeholder.'\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            ""
}