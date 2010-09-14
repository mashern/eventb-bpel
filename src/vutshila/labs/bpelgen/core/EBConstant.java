/**
 * 
 */
package vutshila.labs.bpelgen.core;

/**
 * @author Ernest Mashele <mashern@tuks.co.za>
 * 
 */
public interface EBConstant {

	// RODIN XML string constants
	final String EVENTB_ACCURATE = "org.eventb.core.accurate";
	final String EVENTB_SEES_CONTEXT = "org.eventb.core.scSeesContext";
	final String EVENTB_TARGET = "org.eventb.core.scTarget";

	// Translation
	final String TYPE = "Type";
	final String MESSAGE = "Message";

	// Corresponding primitive types
	// integer, natural, real number
	final String EVENTB_TYPES[] = { "\u2124", "\u2115", "\u211D" };
	final String XSD_TYPES[] = { "xs:int", "xs:nonNegativeInteger", "xs:double" };

	// Comments
	final String GENERATED = "Generated by BPEL Generator plugin";

	// File extensions
	final String CONTEXT_EXTENSION = ".buc";
	final String MACHINE_EXTENSION = ".bum";
	final String BPEL_EXTENSION = ".bpel";
	final String WSDL_EXTENSION = ".wsdl";
	
	// Mathematical symbols
	final String MATH_ELEMENT = "\u2208";
	final String MATH_ARROW = "\u2192";
	final String CONFIGURATION = "org.eventb.core.fwd";
	final String PERIOD = ".";
}
