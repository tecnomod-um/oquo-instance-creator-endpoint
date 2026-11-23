package es.um.dis.tecnomod.oquo.endpoint;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import es.um.dis.tecnomod.oquo.dto.ObservationsInfoDTO;
import es.um.dis.tecnomod.oquo.service.InstanceCreator;
import es.um.dis.tecnomod.oquo.utils.Namespaces;

@RestController
public class Controller {
	@PostMapping(value = "/toOquoObservation", produces = "text/turtle")
	public String toOquoObservation(@RequestBody ObservationsInfoDTO observationsInfo,
			@RequestParam(name = "dataNamespace", required = false) String dataNamespace) {

		if (dataNamespace != null && !dataNamespace.isBlank()) {
			validateNamespace(dataNamespace);
		}

		Model rdfModel = InstanceCreator.createObservations(observationsInfo, dataNamespace);
		StringWriter writer = new StringWriter();
		rdfModel.write(writer, "TURTLE");
		return writer.toString();
	}

	@PostMapping(value = "/toOquoModel", produces = "text/turtle")
	public String toOquoModel(@RequestBody ObservationsInfoDTO observationsInfo,
			@RequestParam(name = "ontologyIRI", required = true) String ontologyIRI,
			@RequestParam(name = "dataNamespace", required = false) String dataNamespace) {
		if (dataNamespace != null && !dataNamespace.isBlank()) {
			validateNamespace(dataNamespace);
		}

		Model rdfModel = InstanceCreator.createObservationsAndApplyModel(observationsInfo, ontologyIRI, dataNamespace);
		StringWriter writer = new StringWriter();
		rdfModel.write(writer, "TURTLE");
		return writer.toString();
	}

	/**
	 * Applies an OQUO module to OQUO-compliant RDF: adds to it the module at
	 * ontologyIRI, with the ontologies it imports, and the quality values obtained
	 * by applying the scale conversions of the module to its quality values.
	 *
	 * @param rdf           the OQUO-compliant RDF, e.g. the results of a tool that
	 *                      already generates OQUO
	 * @param contentType   syntax of the RDF: text/turtle, application/rdf+xml or
	 *                      application/n-triples
	 * @param ontologyIRI   IRI of the OQUO module to apply, e.g.
	 *                      https://purl.org/oquo-huron
	 * @param dataNamespace namespace of the new quality values; the default one if
	 *                      not given
	 * @return the RDF received, with the module and the converted quality values,
	 *         in Turtle
	 */
	@PostMapping(value = "/applyModel", consumes = { "text/turtle", "application/rdf+xml",
			"application/n-triples" }, produces = "text/turtle")
	public String applyModel(@RequestBody String rdf, @RequestHeader("Content-Type") String contentType,
			@RequestParam String ontologyIRI, @RequestParam(required = false) String dataNamespace) {

		if (dataNamespace != null && !dataNamespace.isBlank()) {
			validateNamespace(dataNamespace);
		}
		MediaType mediaType = MediaType.parseMediaType(contentType);
		Lang lang = RDFLanguages.contentTypeToLang(mediaType.getType() + "/" + mediaType.getSubtype());
		Model oquoData = ModelFactory.createDefaultModel();
		oquoData.setNsPrefixes(Namespaces.getPrefixMap());
		try {
		    RDFDataMgr.read(oquoData, new StringReader(rdf), null, lang);
		} catch (RiotException e) {
		    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RDF: " + e.getMessage(), e);
		}
		try {
		    InstanceCreator.applyModel(oquoData, ontologyIRI, dataNamespace);
		} catch (IllegalArgumentException | RiotException | HttpException e) {
		    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
		            "Cannot apply the model " + ontologyIRI + ": " + e.getMessage(), e);
		}
		StringWriter writer = new StringWriter();
		oquoData.write(writer, "TURTLE");
		return writer.toString();

	}

	private static void validateNamespace(String ns) {
		try {
			URI uri = new URI(ns);
			boolean http = "http".equals(uri.getScheme()) || "https".equals(uri.getScheme());
			boolean validEnd = ns.endsWith("/") || ns.endsWith("#");
			if (uri.isAbsolute() && http && validEnd && !ns.matches(".*[\\s<>\"{}|\\\\^`].*")) {
				return;
			}
		} catch (URISyntaxException e) {
			// fall through
		}
		throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"dataNamespace must be an absolute http(s) IRI ending in '/' or '#'");
	}
}
