package es.um.dis.tecnomod.oquo.endpoint;

import java.io.StringWriter;

import org.apache.jena.rdf.model.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.um.dis.tecnomod.oquo.dto.ObservationsInfoDTO;
import es.um.dis.tecnomod.oquo.service.InstanceCreator;

@RestController
public class Controller {
	@PostMapping(value="/toOquoObservation", produces = "text/turtle")
	public String toOquoObservation (@RequestParam("observationsInfo") ObservationsInfoDTO observationsInfo) {
		Model rdfModel = InstanceCreator.createObservations(observationsInfo);
		StringWriter writer = new StringWriter();
		rdfModel.write(writer, "TURTLE");
		return writer.toString();
	}
}
