# oquo-instance-creator-endpoint
Endpoint to translate observations into OQUO-compliant RDF.

## Running

The following command will deploy the REST endpoint in the port 9000 (this can be changed as needed):


```
java -jar oquo-instance-creator-endpoint-0.0.1.jar --server.port=9000
```


## /toOquoObservation

This POST method translates a DTO representing a list of observations into OQUO-compliant RDF.


### Input JSON

The input is a DTO representing a list of observations. Next, an example is shown:

```
{
    "observationsInfoDTO": [
        {
            "sourceDocumentIRI": "http://purl.obolibrary.org/obo/go.owl",
            "featureOfInterestIRI": "http://purl.obolibrary.org/obo/go.owl",
            "featureOfInterestTypeIRI": "http://www.w3.org/2002/07/owl#Ontology",
            "metricUsedIRI": "http://infohub.siemens-energy.com/quality-metrics#QMO29",
            "scaleIRI": "http://infohub.siemens-energy.com/quality-metrics#QMO29RawScale",
            "scaleTypeIRI": "https://purl.org/oquo#RawScale",
            "rankingFunctionIRI": "http://purl.org/net/QualityModel#LowerBest",
            "value": 27,
            "timestamp": "2026-10-01T12:47:17.000+0000",
            "details": [
                "Detalle 1, Detalle 2"
            ],
            "issues": [
                {
                    "issueType": "http://infohub.siemens-energy.com/quality-metrics#ImportCycleIssue",
                    "message": "La ontologia tiene ciclos en sus imports",
                    "hostAsset": "http://purl.obolibrary.org/obo/go.owl"
                }
            ],
            "configurationDataList": [
                {
                    "configurationIRI": "http://infohub.siemens-energy.com/quality-metrics#Config1",
                    "serializedConfiguration": "Alpha < 0.05"
                }
            ]
        }
    ]
}
```

### Output RDF

The output is an OQUO-compliant RDF, exemplified next:

```
<http://purl.org/net/QualityModel#LowerBest>
        a       <http://purl.org/net/QualityModel#RankingFunction> .

<https://purl.org/oquo#metric-e13a76ff-0e8f-40a9-8e7b-ee6da388dddf>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Measurement> , <http://infohub.siemens-energy.com/quality-metrics#QMO29> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#hasValue>
                <https://purl.org/oquo#qualityValue-851fa417-e8db-4970-bf42-bf850349a987> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#measurementFor>
                <https://purl.org/oquo#observation-80214efb-f70d-4164-998b-9d15120326aa> ;
        <http://purl.org/net/QualityModel#hasScale>
                <http://infohub.siemens-energy.com/quality-metrics#QMO29RawScale> .

<https://purl.org/oquo#issue-2eae1355-795e-461d-82f0-c5bd1a39659f>
        a       <http://infohub.siemens-energy.com/quality-metrics#ImportCycleIssue> ;
        <http://www.w3.org/2000/01/rdf-schema#comment>
                "La ontologia tiene ciclos en sus imports" ;
        <http://purl.org/ipo/core#hasHostAsset>
                <http://purl.obolibrary.org/obo/go.owl> ;
        <http://purl.org/ipo/core#indicatedBy>
                <https://purl.org/oquo#observation-80214efb-f70d-4164-998b-9d15120326aa> .

<http://infohub.siemens-energy.com/quality-metrics#Config1>
        a       <https://purl.org/oquo#EvaluationConfigurationData> , <http://purl.org/net/EvaluationResult#EvaluationData> ;
        <https://www.omg.org/spec/Commons/TextDatatype/hasTextValue>
                "Alpha < 0.05" .

<http://infohub.siemens-energy.com/quality-metrics#QMO29RawScale>
        a       <https://purl.org/oquo#RawScale> ;
        <http://purl.org/net/QualityModel#hasRankigFunction>
                <http://purl.org/net/QualityModel#LowerBest> .

<https://purl.org/oquo#observation-80214efb-f70d-4164-998b-9d15120326aa>
        a       <http://purl.org/ipo/core#Symptom> , <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Observation> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#hasMeasurement>
                <https://purl.org/oquo#metric-e13a76ff-0e8f-40a9-8e7b-ee6da388dddf> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#ofEntity>
                <http://purl.obolibrary.org/obo/go.owl> ;
        <http://purl.org/ipo/core#indicates>
                <https://purl.org/oquo#issue-2eae1355-795e-461d-82f0-c5bd1a39659f> ;
        <http://www.w3.org/2006/time#hasTime>
                <https://purl.org/oquo#instant-d9a1c150-6f5f-47d1-a800-118b34834de6> .

<https://purl.org/oquo#instant-d9a1c150-6f5f-47d1-a800-118b34834de6>
        a       <http://www.w3.org/2006/time#Instant> ;
        <http://www.w3.org/2006/time#inXSDDateTime>
                "2026-10-01T12:47:17Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .

<https://purl.org/oquo#evaluation-d2972eb8-8b24-4e76-93b6-4f1f996dba3a>
        a       <http://purl.org/net/EvaluationResult#Evaluation> ;
        <http://purl.org/net/EvaluationResult#evaluatedSubject>
                <http://purl.obolibrary.org/obo/go.owl> ;
        <http://purl.org/net/EvaluationResult#inputData>
                <http://infohub.siemens-energy.com/quality-metrics#Config1> , <http://purl.obolibrary.org/obo/go.owl> ;
        <http://purl.org/net/EvaluationResult#producedQualityValue>
                <https://purl.org/oquo#qualityValue-851fa417-e8db-4970-bf42-bf850349a987> ;
        <http://www.w3.org/2006/time#hasTime>
                <https://purl.org/oquo#instant-d9a1c150-6f5f-47d1-a800-118b34834de6> ;
        <https://purl.org/oquo#hasDetectedIssue>
                <https://purl.org/oquo#issue-2eae1355-795e-461d-82f0-c5bd1a39659f> ;
        <https://purl.org/oquo#hasObservation>
                <https://purl.org/oquo#observation-80214efb-f70d-4164-998b-9d15120326aa> .

<https://purl.org/oquo#qualityValue-851fa417-e8db-4970-bf42-bf850349a987>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#MeasuredValue> , <http://purl.org/net/EvaluationResult#QualityValue> ;
        <http://www.w3.org/2000/01/rdf-schema#comment>
                "Detalle 1, Detalle 2" ;
        <http://purl.org/net/EvaluationResult#forMeasure>
                <https://purl.org/oquo#metric-e13a76ff-0e8f-40a9-8e7b-ee6da388dddf> ;
        <http://purl.org/net/EvaluationResult#hasLiteralValue>
                "27"^^<http://www.w3.org/2001/XMLSchema#int> ;
        <http://purl.org/net/EvaluationResult#isMeasuredOnScale>
                <http://infohub.siemens-energy.com/quality-metrics#QMO29RawScale> ;
        <http://purl.org/net/EvaluationResult#obtainedFrom>
                <https://purl.org/oquo#evaluation-d2972eb8-8b24-4e76-93b6-4f1f996dba3a> .

<http://purl.obolibrary.org/obo/go.owl>
        a       <http://purl.org/ipo/core#Asset> , <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Entity> , <https://purl.org/oquo#EvaluationInputData> , <http://purl.org/net/EvaluationResult#EvaluationData> , <http://www.w3.org/2002/07/owl#Ontology> , <http://purl.org/net/EvaluationResult#EvaluationSubject> ;
        <http://purl.org/ipo/core#hostAssetOf>
                <https://purl.org/oquo#issue-2eae1355-795e-461d-82f0-c5bd1a39659f> .


```