# oquo-instance-creator-endpoint

A REST service that turns the results of quality evaluations into RDF that follows the
[Ontology QUality Ontology (OQUO)](https://github.com/tecnomod-um/oquo).

You send it a JSON description of what you measured (which entity, with which metric, on
which scale, what value you got and when) and it returns the same information as
OQUO-compliant RDF in Turtle. You do not need to know the structure of the ontology or
write any triples by hand.

## Why would I use it?

[OQUO](https://github.com/tecnomod-um/oquo) is an ontology for representing **quality
models** (metrics, characteristics and subcharacteristics) and the **results of quality
evaluations**. Describing evaluation results with OQUO lets you:

- store results from different tools and frameworks in a single, shared vocabulary;
- query them with SPARQL (for example, "which classes of this ontology got the worst score
  for this metric?");
- compare evaluations over time, since every result carries its timestamp;
- link each measured value to the issues it reveals, the input that was evaluated and the
  configuration that was used.

The catch is that a single measurement in OQUO is spread across several linked resources
(an observation, a measurement, a quality value, an evaluation, a time instant, and
optionally issues and configuration data). Writing that by hand is slow and error-prone.
This service does it for you.

A public instance is available at
`https://semantics.inf.um.es/oquo-instance-creator/toOquoObservation`, so you can try it
without installing anything (see [Quick start](#quick-start)).

Typical uses:

- **You have a tool that computes metrics** (for ontologies or anything else) and you want
  its output to be OQUO-compliant. Call this endpoint from your tool, in any language.
- **You have evaluation results in a spreadsheet, CSV or database** and want to publish them
  as linked data. Convert each row to the JSON below and send it to the endpoint.
- **You are extending OQUO** with a new quality model and want example instance data to
  test it.

If your application is written in Java, you can skip the REST layer and use the underlying
library, [oquo-instance-creator](https://github.com/tecnomod-um/oquo-instance-creator),
directly.

## How it works

```
 your tool / script                oquo-instance-creator-endpoint               RDF store,
 (any language)                                                                 file, SPARQL...
 ┌──────────────┐   POST JSON       ┌───────────────────────────┐   Turtle     ┌────────────┐
 │ metric value │ ────────────────▶ │  /toOquoObservation       │ ───────────▶ │ OQUO data  │
 │ per entity   │                   │  (Spring Boot + Jena)     │              │            │
 └──────────────┘                   └───────────────────────────┘              └────────────┘
```

For every observation in the request, the service creates:

| Resource created              | OQUO / reused class                          | What it represents                                   |
|-------------------------------|----------------------------------------------|------------------------------------------------------|
| `<ns>observation-<uuid>`      | `oboe:Observation`, `ipo:Symptom`            | The act of observing one entity                      |
| `<ns>metric-<uuid>`           | `oboe:Measurement` + your metric class       | The measurement taken with that metric               |
| `<ns>qualityValue-<uuid>`     | `eval:QualityValue`, `oboe:MeasuredValue`    | The value obtained, on the given scale               |
| `<ns>evaluation-<uuid>`       | `eval:Evaluation`                            | The evaluation that produced the value               |
| `<ns>instant-<uuid>`          | `time:Instant`                               | When the evaluation happened                         |
| `<ns>issue-<uuid>`            | your issue class                             | (Optional) problems detected by the observation      |

`<ns>` is the **data namespace**: by default `https://purl.org/oquo-data/`, or the one you
pass in the [`dataNamespace`](#query-parameters) parameter. The generated instances are data,
so they are kept apart from the namespaces of the OQUO vocabulary (`https://purl.org/oquo#`
for the core, and one namespace per module, such as `https://purl.org/oquo-huron#`).

IRIs are generated with random UUIDs, so **every call produces new resources**, even for the
same input.

## Quick start

### Use the public instance

The service is deployed at:

```
https://semantics.inf.um.es/oquo-instance-creator/toOquoObservation
```

You can use it right away, with nothing to install. Save the
[example request](#example-request) below as `observation.json` and run:

```bash
curl -X POST https://semantics.inf.um.es/oquo-instance-creator/toOquoObservation \
     -H "Content-Type: application/json" \
     -d @observation.json
```

You will get back Turtle like the [example response](#example-response).

To generate the instances in your own namespace instead of the default one, add the
`dataNamespace` parameter (see [Query parameters](#query-parameters)):

```bash
curl -X POST https://semantics.inf.um.es/oquo-instance-creator/toOquoObservation \
     --url-query "dataNamespace=https://example.org/my-evaluations/" \
     -H "Content-Type: application/json" \
     -d @observation.json
```

If you prefer to run the service on your own machine (for example, to process large
volumes of data or to work offline), follow the steps below.

### Run your own instance

#### Requirements

- Java 17 or later
- Maven 3 (only to build from source)

#### Download

The easiest way to get the service is to download the ready-to-run `.jar` from the
[Releases page](https://github.com/tecnomod-um/oquo-instance-creator-endpoint/releases)
and skip straight to [Run](#run).

#### Build from source (optional)

The endpoint depends on the
[oquo-instance-creator](https://github.com/tecnomod-um/oquo-instance-creator) library
(version `0.0.6`), which is not published in Maven Central. Install it in your local Maven
repository first:

```bash
git clone https://github.com/tecnomod-um/oquo-instance-creator
cd oquo-instance-creator
mvn install -DskipTests -Dmaven.compiler.release=17
cd ..
```

Then build the endpoint:

```bash
git clone https://github.com/tecnomod-um/oquo-instance-creator-endpoint
cd oquo-instance-creator-endpoint
mvn package
```

This produces `target/oquo-instance-creator-endpoint-0.0.2.jar`.

#### Run

```bash
java -jar oquo-instance-creator-endpoint-0.0.2.jar --server.port=9000
```

Use the path to the `.jar` you downloaded, or `target/oquo-instance-creator-endpoint-0.0.2.jar`
if you built it yourself.

The service is now listening on `http://localhost:9000`. Change `--server.port` to use a
different port.

To call your own instance, use the same `curl` command as above, replacing the public URL
with `http://localhost:9000/toOquoObservation`.

## API reference

### `POST /toOquoObservation`

- **Query parameters:** optional `dataNamespace` (see below).
- **Request body:** `application/json`, a list of observations (see below).
- **Response:** `text/turtle`, the OQUO-compliant RDF for all the observations in the request.
  If `dataNamespace` is not valid, the service returns `400 Bad Request`.

#### Query parameters

| Parameter       | Required | Description |
|-----------------|:--------:|-------------|
| `dataNamespace` | no  | Namespace in which the instances (observations, measurements, quality values, evaluations, instants and issues) are created. It must be an absolute `http` or `https` IRI ending in `/` or `#`, e.g. `https://example.org/my-evaluations/`. Defaults to `https://purl.org/oquo-data/`. |

The value must be URL-encoded (e.g. `https%3A%2F%2Fexample.org%2Fmy-evaluations%2F`). This is
especially important if the namespace ends in `#`, which must be sent as `%23`: otherwise it
is interpreted as a URL fragment and never reaches the server. `curl --url-query` (curl 7.87
or later), Python `requests` (`params=`) and Postman (*Params* tab) encode it for you.

Use the default namespace for testing. To publish your evaluation results, use a namespace
of your own.

#### Request body

The body is an object with a single field, `observationsInfoDTO`, holding an array of
observations. You can send one observation or many in the same request.

#### Observation fields

| Field                      | Required | Description |
|----------------------------|:--------:|-------------|
| `featureOfInterestIRI`     | yes | IRI of the entity that was evaluated (e.g. a class of an ontology). |
| `featureOfInterestTypeIRI` | yes | IRI of the type of that entity (e.g. `http://www.w3.org/2002/07/owl#Class`). |
| `metricUsedIRI`            | yes | IRI of the metric, as defined in an OQUO-based module (e.g. `https://purl.org/oquo-huron#NamesPerClassMetric`). |
| `scaleIRI`                 | yes | IRI of the scale the value is measured on. |
| `scaleTypeIRI`             | yes | IRI of the type of scale, e.g. `https://purl.org/oquo#RawScale`. |
| `rankingFunctionIRI`       | yes | Whether higher or lower values are better: `http://purl.org/net/QualityModel#HigherBest` or `http://purl.org/net/QualityModel#LowerBest`. |
| `value`                    | yes | The measured value. Numbers, booleans and strings are stored as typed literals (e.g. `0` becomes `"0"^^xsd:int`). |
| `timestamp`                | yes | When the evaluation took place, e.g. `2023-03-18T12:47:17.000+0000`. |
| `sourceDocumentIRI`        | no  | IRI of the document that was evaluated (e.g. the ontology). Stored as the evaluation's input data. |
| `details`                  | no  | List of free-text notes, stored as `rdfs:comment` on the quality value. |
| `issues`                   | no  | List of problems detected (see below). |
| `configurationDataList`    | no  | List of configuration settings used in the evaluation (see below). |

#### Issue fields

| Field            | Required | Description |
|------------------|:--------:|-------------|
| `issueType`      | yes | IRI of the issue class, as defined in an OQUO-based module (e.g. `https://purl.org/oquo-huron#ClassWithNoNameIssue`). Generic severities defined in the OQUO core are also available: `oquo:CriticalIssue`, `oquo:MajorIssue`, `oquo:MinorIssue` and `oquo:InfoIssue`. |
| `message`        | no  | Human-readable description, stored as `rdfs:comment`. |
| `hostAsset`      | no  | IRI of the entity where the issue is located. |
| `causativeAsset` | no  | IRI of the entity that causes the issue, if different from the host. |

#### Configuration fields

| Field                     | Required | Description |
|---------------------------|:--------:|-------------|
| `configurationIRI`        | yes | IRI that identifies this configuration. |
| `serializedConfiguration` | yes | The configuration as plain text. |

### Example request

One observation: the metric *names per class* was applied to the Gene Ontology class
`GO_0044208`, which got a value of `0`. The observation also reports that the class has no
name, and records which properties were considered names.

```json
{
  "observationsInfoDTO": [
    {
      "sourceDocumentIRI": "http://purl.obolibrary.org/obo/go.owl",
      "featureOfInterestIRI": "http://purl.obolibrary.org/obo/GO_0044208",
      "featureOfInterestTypeIRI": "http://www.w3.org/2002/07/owl#Class",
      "metricUsedIRI": "https://purl.org/oquo-huron#NamesPerClassMetric",
      "scaleIRI": "https://purl.org/oquo-huron#NamesPerClassMetricScale",
      "scaleTypeIRI": "https://purl.org/oquo#RawScale",
      "rankingFunctionIRI": "http://purl.org/net/QualityModel#HigherBest",
      "value": 0,
      "timestamp": "2023-03-18T12:47:17.000+0000",
      "details": ["Detail 1, Detail 2"],
      "issues": [
        {
          "issueType": "https://purl.org/oquo-huron#ClassWithNoNameIssue",
          "message": "The class does not have any name",
          "hostAsset": "http://purl.obolibrary.org/obo/GO_0044208"
        }
      ],
      "configurationDataList": [
        {
          "configurationIRI": "https://purl.org/oquo-data/namesPerClassMetricConfiguration",
          "serializedConfiguration": "names = rdfs:label, foaf:name"
        }
      ]
    }
  ]
}
```

### Example response

<details>
<summary>Show the full Turtle output</summary>

```turtle
<https://purl.org/oquo-data/qualityValue-d07535b6-64a9-4394-8be2-112a1cd344c3>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#MeasuredValue> , <http://purl.org/net/EvaluationResult#QualityValue> ;
        <http://www.w3.org/2000/01/rdf-schema#comment>
                "Detail 1, Detail 2" ;
        <http://purl.org/net/EvaluationResult#forMeasure>
                <https://purl.org/oquo-data/metric-2a37d659-28fc-4193-9642-dc6bd92d660b> ;
        <http://purl.org/net/EvaluationResult#hasLiteralValue>
                "0"^^<http://www.w3.org/2001/XMLSchema#int> ;
        <http://purl.org/net/EvaluationResult#isMeasuredOnScale>
                <https://purl.org/oquo-huron#NamesPerClassMetricScale> ;
        <http://purl.org/net/EvaluationResult#obtainedFrom>
                <https://purl.org/oquo-data/evaluation-7558a284-6cf1-4ac2-b838-28d886bc8a25> .

<http://purl.org/net/QualityModel#HigherBest>
        a       <http://purl.org/net/QualityModel#RankingFunction> .

<https://purl.org/oquo-data/observation-b5eed3e3-490d-4cc1-afd9-dbf4a9934469>
        a       <http://purl.org/ipo/core#Symptom> , <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Observation> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#hasMeasurement>
                <https://purl.org/oquo-data/metric-2a37d659-28fc-4193-9642-dc6bd92d660b> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#ofEntity>
                <http://purl.obolibrary.org/obo/GO_0044208> ;
        <http://purl.org/ipo/core#indicates>
                <https://purl.org/oquo-data/issue-e38522e0-0cb5-4d4c-9891-c44ea106cb33> ;
        <http://www.w3.org/2006/time#hasTime>
                <https://purl.org/oquo-data/instant-45299b22-78a9-4d9a-bce8-171dfe9df5b4> .

<https://purl.org/oquo-data/metric-2a37d659-28fc-4193-9642-dc6bd92d660b>
        a       <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Measurement> , <https://purl.org/oquo-huron#NamesPerClassMetric> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#hasValue>
                <https://purl.org/oquo-data/qualityValue-d07535b6-64a9-4394-8be2-112a1cd344c3> ;
        <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#measurementFor>
                <https://purl.org/oquo-data/observation-b5eed3e3-490d-4cc1-afd9-dbf4a9934469> ;
        <http://purl.org/net/QualityModel#hasScale>
                <https://purl.org/oquo-huron#NamesPerClassMetricScale> .

<https://purl.org/oquo-data/evaluation-7558a284-6cf1-4ac2-b838-28d886bc8a25>
        a       <http://purl.org/net/EvaluationResult#Evaluation> ;
        <http://purl.org/net/EvaluationResult#evaluatedSubject>
                <http://purl.obolibrary.org/obo/GO_0044208> ;
        <http://purl.org/net/EvaluationResult#inputData>
                <https://purl.org/oquo-data/namesPerClassMetricConfiguration> , <http://purl.obolibrary.org/obo/go.owl> ;
        <http://purl.org/net/EvaluationResult#producedQualityValue>
                <https://purl.org/oquo-data/qualityValue-d07535b6-64a9-4394-8be2-112a1cd344c3> ;
        <http://www.w3.org/2006/time#hasTime>
                <https://purl.org/oquo-data/instant-45299b22-78a9-4d9a-bce8-171dfe9df5b4> ;
        <https://purl.org/oquo#hasDetectedIssue>
                <https://purl.org/oquo-data/issue-e38522e0-0cb5-4d4c-9891-c44ea106cb33> ;
        <https://purl.org/oquo#hasObservation>
                <https://purl.org/oquo-data/observation-b5eed3e3-490d-4cc1-afd9-dbf4a9934469> .

<http://purl.obolibrary.org/obo/GO_0044208>
        a       <http://purl.org/ipo/core#Asset> , <http://ecoinformatics.org/oboe/oboe.1.2/oboe-core.owl#Entity> , <http://www.w3.org/2002/07/owl#Class> , <http://purl.org/net/EvaluationResult#EvaluationSubject> ;
        <http://purl.org/ipo/core#hostAssetOf>
                <https://purl.org/oquo-data/issue-e38522e0-0cb5-4d4c-9891-c44ea106cb33> .

<https://purl.org/oquo-data/issue-e38522e0-0cb5-4d4c-9891-c44ea106cb33>
        a       <https://purl.org/oquo-huron#ClassWithNoNameIssue> ;
        <http://www.w3.org/2000/01/rdf-schema#comment>
                "The class does not have any name" ;
        <http://purl.org/ipo/core#hasHostAsset>
                <http://purl.obolibrary.org/obo/GO_0044208> ;
        <http://purl.org/ipo/core#indicatedBy>
                <https://purl.org/oquo-data/observation-b5eed3e3-490d-4cc1-afd9-dbf4a9934469> .

<https://purl.org/oquo-data/instant-45299b22-78a9-4d9a-bce8-171dfe9df5b4>
        a       <http://www.w3.org/2006/time#Instant> ;
        <http://www.w3.org/2006/time#inXSDDateTime>
                "2023-03-18T12:47:17Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .

<https://purl.org/oquo-data/namesPerClassMetricConfiguration>
        a       <https://purl.org/oquo#EvaluationConfigurationData> , <http://purl.org/net/EvaluationResult#EvaluationData> ;
        <https://www.omg.org/spec/Commons/TextDatatype/hasTextValue>
                "names = rdfs:label, foaf:name" .

<https://purl.org/oquo-huron#NamesPerClassMetricScale>
        a       <https://purl.org/oquo#RawScale> ;
        <http://purl.org/net/QualityModel#hasRankigFunction>
                <http://purl.org/net/QualityModel#HigherBest> .

<http://purl.obolibrary.org/obo/go.owl>
        a       <https://purl.org/oquo#EvaluationInputData> , <http://purl.org/net/EvaluationResult#EvaluationData> , <http://www.w3.org/2002/07/owl#Ontology> .

```

</details>

> **Note:** the property `http://purl.org/net/QualityModel#hasRankigFunction` is spelled this
> way in the [Quality Model Ontology](http://purl.org/net/QualityModel), which OQUO reuses.
> It is not a typo in this service.

### Calling it from Python

```python
import requests

observation = {
    "featureOfInterestIRI": "http://purl.obolibrary.org/obo/GO_0044208",
    "featureOfInterestTypeIRI": "http://www.w3.org/2002/07/owl#Class",
    "metricUsedIRI": "https://purl.org/oquo-huron#NamesPerClassMetric",
    "scaleIRI": "https://purl.org/oquo-huron#NamesPerClassMetricScale",
    "scaleTypeIRI": "https://purl.org/oquo#RawScale",
    "rankingFunctionIRI": "http://purl.org/net/QualityModel#HigherBest",
    "value": 0,
    "timestamp": "2023-03-18T12:47:17.000+0000",
}

response = requests.post(
    "https://semantics.inf.um.es/oquo-instance-creator/toOquoObservation",
    params={"dataNamespace": "https://example.org/my-evaluations/"},  # optional
    json={"observationsInfoDTO": [observation]},
)
response.raise_for_status()

with open("observations.ttl", "w") as f:
    f.write(response.text)
```

Replace the URL with `http://localhost:9000/toOquoObservation` to use your own instance.

The resulting file can be loaded into any RDF store or library (Apache Jena, rdflib,
GraphDB, Virtuoso...) together with the OQUO ontology.

## Where do the metric and scale IRIs come from?

The IRIs for metrics, scales and issue types are defined in the OQUO modules that integrate
each evaluation framework, and each module uses its own namespace:

| Module | Namespace | Repository |
|---|---|---|
| OQUO core | `https://purl.org/oquo#` | [oquo](https://github.com/tecnomod-um/oquo) |
| HURON | `https://purl.org/oquo-huron#` | [huron](https://github.com/tecnomod-um/huron) |
| OQuaRE metrics | `https://purl.org/oquo-oquare#` | [oquare](https://github.com/tecnomod-um/oquare) |
| OntoEnrich | `https://purl.org/oquo-ontoenrich#` | [oquo-ontoenrich](https://github.com/tecnomod-um/oquo-ontoenrich) |

Generic classes, such as `oquo:RawScale` or the issue severities, come from the OQUO core.
You can also define your own metrics, scales and issue types in an OQUO-based module. The
[QASAR example](https://github.com/tecnomod-um/oquo/tree/main/oquo-qasar) shows how OQUO is
used to build a quality model for ontology quality assurance.

## Related projects

| Project | What it does |
|---------|--------------|
| [oquo](https://github.com/tecnomod-um/oquo) | The ontology itself, its modules and examples. |
| [oquo-instance-creator](https://github.com/tecnomod-um/oquo-instance-creator) | Java library that this endpoint wraps. Use it directly from Java applications. |
| [oquo-scale-conversion-creator](https://github.com/tecnomod-um/oquo-scale-conversion-creator) | Command-line tool to add scale conversions to OQUO-based modules. |

## License

Apache License 2.0. See [LICENSE](LICENSE).