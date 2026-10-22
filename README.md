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
            "featureOfInterestIRI": "http://purl.obolibrary.org/obo/GO_0044208",
            "featureOfInterestTypeIRI": "http://www.w3.org/2002/07/owl#Class",
            "metricUsedIRI": "https://purl.org/oquo#NamesPerClassMetric",
            "scaleIRI": "https://purl.org/oquo#NamesPerClassMetricScale",
            "scaleTypeIRI": "https://purl.org/oquo#RawScale",
            "rankingFunctionIRI": "http://purl.org/net/QualityModel#HigherBest",
            "value": 0,
            "timestamp": "2023-03-18T12:47:17.000+0000",
            "details": [
                "Detail 1, Detail 2"
            ],
            "issues": [
                {
                    "issueType": "https://purl.org/oquo#ClassWithNoNameIssue",
                    "message": "The class does not have any name",
                    "hostAsset": "http://purl.obolibrary.org/obo/GO_0044208"
                }
            ],
            "configurationDataList": [
                {
                    "configurationIRI": "https://purl.org/oquo#namesPerClassMetricConfiguration",
                    "serializedConfiguration": "names = rdfs:label, foaf:name"
                }
            ]
        }
    ]
}
```

### Output RDF

The output is an OQUO-compliant RDF, exemplified next:

```
<https://purl.org/oquo#observation-3946ded7-d25d-45f5-802e-e33ec57c175c>
        a       <http://purl.org/ipo/core#Symptom> , <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Observation> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#hasMeasurement>
                <https://purl.org/oquo#metric-0cf60840-f12c-4c99-87f7-8645848b336f> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#ofEntity>
                <http://purl.obolibrary.org/obo/GO_0044208> ;
        <http://purl.org/ipo/core#indicates>
                <https://purl.org/oquo#issue-17e48be3-b188-4a59-a823-636aec80e1cb> ;
        <http://www.w3.org/2006/time#hasTime>
                <https://purl.org/oquo#instant-3acd25f8-d0e0-42ac-8404-660f91dba0f9> .

<https://purl.org/oquo#qualityValue-e4e2572a-5f1c-40b7-acd2-e6c686c6b3a0>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#MeasuredValue> , <http://purl.org/net/EvaluationResult#QualityValue> ;
        <http://www.w3.org/2000/01/rdf-schema#comment>
                "Detail 1, Detail 2" ;
        <http://purl.org/net/EvaluationResult#forMeasure>
                <https://purl.org/oquo#metric-0cf60840-f12c-4c99-87f7-8645848b336f> ;
        <http://purl.org/net/EvaluationResult#hasLiteralValue>
                "0"^^<http://www.w3.org/2001/XMLSchema#int> ;
        <http://purl.org/net/EvaluationResult#isMeasuredOnScale>
                <https://purl.org/oquo#NamesPerClassMetricScale> ;
        <http://purl.org/net/EvaluationResult#obtainedFrom>
                <https://purl.org/oquo#evaluation-ba2a64aa-8aed-4d3c-b9f9-af87d4e3d8b2> .

<http://purl.org/net/QualityModel#HigherBest>
        a       <http://purl.org/net/QualityModel#RankingFunction> .

<https://purl.org/oquo#namesPerClassMetricConfiguration>
        a       <https://purl.org/oquo#EvaluationConfigurationData> , <http://purl.org/net/EvaluationResult#EvaluationData> ;
        <https://www.omg.org/spec/Commons/TextDatatype/hasTextValue>
                "names = rdfs:label, foaf:name" .

<http://purl.obolibrary.org/obo/GO_0044208>
        a       <http://purl.org/ipo/core#Asset> , <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Entity> , <http://www.w3.org/2002/07/owl#Class> , <http://purl.org/net/EvaluationResult#EvaluationSubject> ;
        <http://purl.org/ipo/core#hostAssetOf>
                <https://purl.org/oquo#issue-17e48be3-b188-4a59-a823-636aec80e1cb> .

<https://purl.org/oquo#evaluation-ba2a64aa-8aed-4d3c-b9f9-af87d4e3d8b2>
        a       <http://purl.org/net/EvaluationResult#Evaluation> ;
        <http://purl.org/net/EvaluationResult#evaluatedSubject>
                <http://purl.obolibrary.org/obo/GO_0044208> ;
        <http://purl.org/net/EvaluationResult#inputData>
                <https://purl.org/oquo#namesPerClassMetricConfiguration> , <http://purl.obolibrary.org/obo/go.owl> ;
        <http://purl.org/net/EvaluationResult#producedQualityValue>
                <https://purl.org/oquo#qualityValue-e4e2572a-5f1c-40b7-acd2-e6c686c6b3a0> ;
        <http://www.w3.org/2006/time#hasTime>
                <https://purl.org/oquo#instant-3acd25f8-d0e0-42ac-8404-660f91dba0f9> ;
        <https://purl.org/oquo#hasDetectedIssue>
                <https://purl.org/oquo#issue-17e48be3-b188-4a59-a823-636aec80e1cb> ;
        <https://purl.org/oquo#hasObservation>
                <https://purl.org/oquo#observation-3946ded7-d25d-45f5-802e-e33ec57c175c> .

<https://purl.org/oquo#issue-17e48be3-b188-4a59-a823-636aec80e1cb>
        a       <https://purl.org/oquo#ClassWithNoNameIssue> ;
        <http://www.w3.org/2000/01/rdf-schema#comment>
                "The class does not have any name" ;
        <http://purl.org/ipo/core#hasHostAsset>
                <http://purl.obolibrary.org/obo/GO_0044208> ;
        <http://purl.org/ipo/core#indicatedBy>
                <https://purl.org/oquo#observation-3946ded7-d25d-45f5-802e-e33ec57c175c> .

<https://purl.org/oquo#NamesPerClassMetricScale>
        a       <https://purl.org/oquo#RawScale> ;
        <http://purl.org/net/QualityModel#hasRankigFunction>
                <http://purl.org/net/QualityModel#HigherBest> .

<https://purl.org/oquo#metric-0cf60840-f12c-4c99-87f7-8645848b336f>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Measurement> , <https://purl.org/oquo#NamesPerClassMetric> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#hasValue>
                <https://purl.org/oquo#qualityValue-e4e2572a-5f1c-40b7-acd2-e6c686c6b3a0> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#measurementFor>
                <https://purl.org/oquo#observation-3946ded7-d25d-45f5-802e-e33ec57c175c> ;
        <http://purl.org/net/QualityModel#hasScale>
                <https://purl.org/oquo#NamesPerClassMetricScale> .

<https://purl.org/oquo#instant-3acd25f8-d0e0-42ac-8404-660f91dba0f9>
        a       <http://www.w3.org/2006/time#Instant> ;
        <http://www.w3.org/2006/time#inXSDDateTime>
                "2023-03-18T12:47:17Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .

<http://purl.obolibrary.org/obo/go.owl>
        a       <https://purl.org/oquo#EvaluationInputData> , <http://purl.org/net/EvaluationResult#EvaluationData> , <http://www.w3.org/2002/07/owl#Ontology> .

```