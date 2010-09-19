package za.vutshilalabs.bpelgen.core.translation;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eventb.core.IAxiom;
import org.eventb.core.IContextRoot;
import org.eventb.core.IMachineRoot;
import org.rodinp.core.RodinDBException;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import za.vutshilalabs.bpelgen.BpelgenPlugin;
import za.vutshilalabs.bpelgen.core.XMLtool;

/**
 * Create WSDL file from Event-B context
 * 
 * @author Mashele Ernest <mashern@tuks.co.za>
 * 
 */
public class WSDLwriter {

	private static final String MESSAGE_SUF = "MSG";
	private IContextRoot context;
	private Document document;
	private Element definitions;
	private Element portType;

	public WSDLwriter(IMachineRoot machine, IContextRoot context) {
		init(machine, context);
	}

	/**
	 * Create WSDL binding element
	 */
	private void createBindings() {
		Element binding = document.createElement("binding");
		Comment stub = document
				.createComment("TODO Auto-generated binding stub");
		binding.appendChild(stub);
		definitions.appendChild(binding);
	}

	/**
	 * Create the top-level WSDL element
	 * 
	 * @param name
	 *            - Service name
	 */
	private void createDefinitions(String name) {
		definitions = document.createElement("definitions");
		definitions.setAttribute("name", name);
		definitions.setAttribute("targetNamespace", "http://localhost");
		definitions.setAttribute("xmlns", "http://schemas.xmlsoap.org/wsdl/");
		definitions.setAttribute("xmlns:soap",
				"http://schemas.xmlsoap.org/wsdl/soap/");
		definitions.setAttribute("xmlns:xsd",
				"http://www.w3.org/2001/XMLSchema");
		Comment stub = document
				.createComment("TODO Auto-generated targetNamespace stub");
		document.appendChild(stub);
		document.appendChild(definitions);
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
			transformer.transform(source, result);
			file.refreshLocal(0, null);

		} catch (TransformerConfigurationException e) {
			BpelgenPlugin.logError(e, e.getMessage());
		} catch (TransformerException e) {
			BpelgenPlugin.logError(e, e.getMessage());
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Create WSDL messages
	 */
	private void createMessages() {

		// XXX review code

		IAxiom[] axioms;
		portType = document.createElement("portType");
		portType.setAttribute("name", "changeMe");

		HashTable<PredicateString> messageTable = new HashTable<PredicateString>();
		ArrayList<PredicateString> operations = new ArrayList<PredicateString>(
				0);
		ArrayList<String> messageNames = new ArrayList<String>(0);

		try {
			axioms = context.getAxioms();

			for (IAxiom axiom : axioms) {
				// operation ??? input ??? output
				String predicate = axiom.getPredicateString();
				PredicateString ps = new PredicateString();
				if (ps.createPredicate(predicate)) {
					if (ps.getOperation().endsWith(MESSAGE_SUF)) {
						String msgName = ps.getInput();
						messageTable.insert(msgName, ps);

						if (!messageNames.contains(msgName)) {
							messageNames.add(msgName);
						}

					} else {
						operations.add(ps);
					}
				}
			}
			// Create Messages

			for (String msgName : messageNames) {
				List<PredicateString> messages = messageTable
						.getHeapLocation(msgName);
				Element msg = document.createElement("message");
				msg.setAttribute("name", msgName);

				for (PredicateString p : messages) {
					Element part = document.createElement("part");
					part.setAttribute("name", p.getOperation());
					part.setAttribute("type", p.getOutput());

					msg.appendChild(part);
				}
				definitions.appendChild(msg);
			}

			// Create portType
			for (PredicateString ps : operations) {
				Element operation = document.createElement("operation");
				operation.setAttribute("name", ps.getOperation());
				// Operation input
				Element input = document.createElement("input");
				Element output = document.createElement("output");

				{
					String in = ps.getInput();
					input.setAttribute("message", in);

					{
						String out = ps.getOutput();
						output.setAttribute("message", out);
					}
					operation.appendChild(input);
					operation.appendChild(output);
					portType.appendChild(operation);

				}
			}

			definitions.appendChild(portType);
		} catch (RodinDBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create WSDL portTypes
	 */
	private void createPorttypes() {
		portType = document.createElement("portType");
		definitions.appendChild(portType);
	}

	/**
	 * Create WSDL service
	 */
	private void createService() {
		Element service = document.createElement("service");
		Comment stub = document
				.createComment("TODO Auto-generated service stub");
		service.appendChild(stub);
		definitions.appendChild(service);
	}

	/**
	 * Create supported types
	 */
	private void createTypes() {

		Element types = document.createElement("types");
		definitions.appendChild(types);
	}

	/**
	 * Initialise the components of WSDL file
	 */
	public void init(IMachineRoot machine, IContextRoot context) {

		XMLtool xml = new XMLtool(true, null);
		document = xml.getDocument();
		this.context = context;
		Comment generated = document
				.createComment("Generated by BPEL Generator plugin");
		document.appendChild(generated);

		createDefinitions(machine.getComponentName().concat("Service"));
		createTypes(); // ok
		createMessages(); // ok
		createPorttypes(); // ok
		createBindings(); // ok - end here
		createService();

	}
}
