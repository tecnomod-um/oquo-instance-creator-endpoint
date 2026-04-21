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
      "configurationDataList": [],
      "details": [],
      "featureOfInterestIRI": "http://www.example.com/A",
      "featureOfInterestTypeIRI": "http://www.w3.org/2002/07/owl#Ontology",
      "issues": [],
      "metricUsedIRI": "http://example.org/quality-metrics#QMO29",
      "rankingFunctionIRI": "http://purl.org/net/QualityModel#LowerBest",
      "scaleIRI": "http://example.org/quality-metrics#QMO29RawScale",
      "scaleTypeIRI": "https://purl.org/oquo#RawScale",
      "sourceDocumentIRI": "http://www.example.com/A",
      "timestamp": "2026-04-21T11:18:41.085+00:00",
      "value": true
    }
  ]
}
```

### Output RDF

The output is an OQUO-compliant RDF, exemplified next:

```
<http://purl.org/net/QualityModel#LowerBest>
        a       <http://purl.org/net/QualityModel#RankingFunction> .

<https://purl.org/oquo#observation-c7542d22-be66-4ecf-9f7d-2369f90bd162>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Observation> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#hasMeasurement>
                <https://purl.org/oquo#metric-227d9432-caec-4142-8174-de198bcde8c7> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#ofEntity>
                <http://www.example.com/A> ;
        <http://www.w3.org/2006/time#hasTime>
                <https://purl.org/oquo#instant-0ccd499f-d0a9-428b-bcd0-542fffb3299e> .

<http://example.org/quality-metrics#QMO29RawScale>
        a       <https://purl.org/oquo#RawScale> ;
        <http://purl.org/net/QualityModel#hasRankigFunction>
                <http://purl.org/net/QualityModel#LowerBest> .

<https://purl.org/oquo#qualityValue-7a1edfa1-85d2-44cb-ad37-69d8f2dcd6cd>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#MeasuredValue> , <http://purl.org/net/EvaluationResult#QualityValue> ;
        <http://purl.org/net/EvaluationResult#forMeasure>
                <https://purl.org/oquo#metric-227d9432-caec-4142-8174-de198bcde8c7> ;
        <http://purl.org/net/EvaluationResult#hasLiteralValue>
                true ;
        <http://purl.org/net/EvaluationResult#isMeasuredOnScale>
                <http://example.org/quality-metrics#QMO29RawScale> ;
        <http://purl.org/net/EvaluationResult#obtainedFrom>
                <https://purl.org/oquo#evaluation-e81d31fe-874b-4bf8-b2ef-d964479dcca2> .

<https://purl.org/oquo#instant-0ccd499f-d0a9-428b-bcd0-542fffb3299e>
        a       <http://www.w3.org/2006/time#Instant> ;
        <http://www.w3.org/2006/time#inXSDDateTime>
                "2026-04-21T11:18:41.085Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .

<https://purl.org/oquo#metric-227d9432-caec-4142-8174-de198bcde8c7>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Measurement> , <http://example.org/quality-metrics#QMO29> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#hasValue>
                <https://purl.org/oquo#qualityValue-7a1edfa1-85d2-44cb-ad37-69d8f2dcd6cd> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#measurementFor>
                <https://purl.org/oquo#observation-c7542d22-be66-4ecf-9f7d-2369f90bd162> ;
        <http://purl.org/net/QualityModel#hasScale>
                <http://example.org/quality-metrics#QMO29RawScale> .

<http://www.example.com/A>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Entity> , <https://purl.org/oquo#EvaluationInputData> , <http://purl.org/net/EvaluationResult#EvaluationData> , <http://www.w3.org/2002/07/owl#Ontology> , <http://purl.org/net/EvaluationResult#EvaluationSubject> .

<https://purl.org/oquo#evaluation-e81d31fe-874b-4bf8-b2ef-d964479dcca2>
        a       <http://purl.org/net/EvaluationResult#Evaluation> ;
        <http://purl.org/net/EvaluationResult#evaluatedSubject>
                <http://www.example.com/A> ;
        <http://purl.org/net/EvaluationResult#inputData>
                <http://www.example.com/A> ;
        <http://purl.org/net/EvaluationResult#producedQualityValue>
                <https://purl.org/oquo#qualityValue-7a1edfa1-85d2-44cb-ad37-69d8f2dcd6cd> ;
        <http://www.w3.org/2006/time#hasTime>
                <https://purl.org/oquo#instant-0ccd499f-d0a9-428b-bcd0-542fffb3299e> ;
        <https://purl.org/oquo#hasObservation>
                <https://purl.org/oquo#observation-c7542d22-be66-4ecf-9f7d-2369f90bd162> .

```