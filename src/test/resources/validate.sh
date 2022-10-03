
curl --user admin:admin -X POST  -F "jenkinsfile=<jenkinsfile_valid" http://localhost:8080/pipeline-model-converter/validate

curl --user admin:admin -X POST  -F "jenkinsfile=<jenkinsfile_not_valid" http://localhost:8080/pipeline-model-converter/validate
