/**
 *
 */
package za.vutshilalabs.bpelgen.core.translation;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eventb.core.IAxiom;
import org.eventb.core.ICarrierSet;
import org.eventb.core.IContextRoot;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import za.vutshilalabs.bpelgen.core.IGlobalConstants;
import za.vutshilalabs.bpelgen.core.XMLtool;

/**
 * @author Ernest Mashele<mashern@tuks.co.za>
 * 
 */
public class OldContextTranslator {

	private IContextRoot context;
	private Element definitions;
	private Document document;
	private Element portType;

	/**
	 * 
	 * @param serviceName
	 */
	private void createDefinitions(String serviceName) {

		definitions = document.createElement("definitions");
		definitions.setAttribute("name", serviceName);
		// Todo targetNamespace should point to the file's address
		definitions.setAttribute("targetNamespace", "http://localhost");
		// default namespace
		definitions.setAttribute("xmlns", "http://schemas.xmlsoap.org/wsdl/");
		definitions.setAttribute("xmlns:soap",
				"http://schemas.xmlsoap.org/wsdl/soap/");
		definitions
				.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		// Todo add xsd file's namespace
		Comment stub = document
				.createComment("TODO Auto-generated targetNamespace stub");
		definitions.appendChild(stub);
		document.appendChild(definitions);
	}

	/**
	 * Creates xsd types corresponding to Event-B build-in types. All sets
	 * ending with Type are considered an a xsd ComplexType
	 */
	private boolean createElements(final String servName) {
		Comment generated = document.createComment(IGlobalConstants.GENERATED);
		document.appendChild(generated);
		createDefinitions(servName.concat("_Service"));

		Element types = document.createElement("types");
		Element schema = document.createElement("xs:schema");
		types.appendChild(schema);
		definitions.appendChild(types);

		ICarrierSet[] sets;
		IAxiom[] axioms;

		boolean noErrors = true;
		try {
			sets = context.getCarrierSets();
			axioms = context.getAxioms();

			for (ICarrierSet set : sets) {
				String setName = set.getIdentifierString();

				if (setName.endsWith(IGlobalConstants.TYPE)) {
					// Types
					Element complexType = document
							.createElement("xs:ComplexType");
					complexType.setAttribute("name", setName);
					int elementCount = 0;

					for (IAxiom axiom : axioms) {
						PredicateString ps = new PredicateString();
						if (ps.createPredicate(axiom.getPredicateString())) {
							if (ps.getInput().equals(setName)) {
								elementCount++;
								// Testing xsd types
								String type = "";
								for (int i = 0; i < IGlobalConstants.EVENTB_TYPES.length; i++) {
									if (IGlobalConstants.EVENTB_TYPES[i].equals(ps
											.getOutput())) {
										type = IGlobalConstants.XSD_TYPES[i];
										break;
									}
								}
								if (type.equals(""))
									noErrors = false;

								Element element = document
										.createElement("xs:element");
								element.setAttribute("name", ps.getOperation());
								element.setAttribute("type", type);
								complexType.appendChild(element);
							}
						} else {
							noErrors = false;
						}
					}
					if (elementCount > 0) {
						schema.appendChild(complexType);
					}

				} else if (setName.endsWith(IGlobalConstants.MESSAGE)) {
					// Messages
					Element message = document.createElement("message");
					message.setAttribute("name", setName);
					for (IAxiom axiom : axioms) {
						PredicateString ps = new PredicateString();
						if (ps.createPredicate(axiom.getPredicateString())) {
							if (ps.getInput().equals(setName)
									&& ps.getOutput().endsWith(IGlobalConstants.TYPE)) {

								Element part = document.createElement("part");
								part.setAttribute("name", ps.getOperation());
								part.setAttribute("type", ps.getOutput());
								message.appendChild(part);
							}
						} else {
							noErrors = false;
						}
					}
					definitions.appendChild(message);
				} else {
					noErrors = false;
				}
			}

			portType = document.createElement("portType");
			portType.setAttribute("name", servName.concat("_PortType"));

			// Operations
			for (IAxiom axiom : axioms) {
				PredicateString ps = new PredicateString();
				if (ps.createPredicate(axiom.getPredicateString())) {
					if (ps.getInput().endsWith(IGlobalConstants.MESSAGE)
							&& ps.getOutput().endsWith(IGlobalConstants.MESSAGE)) {

						Element operation = document.createElement("operation");
						operation.setAttribute("name", ps.getOperation());
						Element input = document.createElement("input");
						input.setAttribute("message", ps.getInput());
						operation.appendChild(input);
						Element output = document.createElement("output");
						output.setAttribute("message", ps.getOutput());
						operation.appendChild(output);
						portType.appendChild(operation);
					}
				} else {
					noErrors = false;
				}
			}

			definitions.appendChild(portType);

		} catch (RodinDBException e) {
			System.err.printf("failed loading context file: %s, exception: %s",
					context.getElementName(), e.getMessage());
			return false;
		}
		return noErrors;
	}

	/**
	 * Write WSDL data to file
	 * 
	 * @param file
	 */
	public void createFile(IFile file) {
		TransformerFactory factory;
		Source source;
		Transformer transformer;
		Result result;
		try {
			factory = TransformerFactory.newInstance();
			transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			source = new DOMSource(document);
			result = new StreamResult(file.getLocation().toFile());
			// result = new StreamResult(System.out);
			transformer.transform(source, result);
			file.refreshLocal(0, null);

		} catch (Exception ex) {
			System.err.printf("failed creating file, exception: %s",
					ex.getMessage());
		}

	}

	public void init(IMachineRoot machine, IContextRoot context) {

		XMLtool xml = new XMLtool(true, null);
		document = xml.getDocument();
		this.context = context;
		createElements(machine.getElementName());

	}
}