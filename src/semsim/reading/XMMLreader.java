package semsim.reading;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.semanticweb.owlapi.model.OWLException;

import JSim.util.Xcept;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.model.computational.Computation;
import semsim.model.computational.Event;
import semsim.model.computational.EventAssignment;
import semsim.model.computational.RelationalConstraint;
import semsim.model.computational.datastructures.DataStructure;
import semsim.model.computational.datastructures.Decimal;
import semsim.model.computational.datastructures.MMLchoice;
import semsim.model.computational.datastructures.SemSimInteger;
import semsim.model.computational.units.UnitFactor;
import semsim.model.computational.units.UnitOfMeasurement;
import semsim.utilities.SemSimUtil;


/**
 * Class for reading in information about a model that is serialized in JSim's
 * XMML format (an XML-based format for representing an MML model)
 * @author mneal
 *
 */
public class XMMLreader extends ModelReader {
	private Hashtable<String,String> discretevarsandconstraints = new Hashtable<String,String>();
	private Hashtable<String,Event> discretevarsandevents = new Hashtable<String,Event>();
	private Hashtable<String,String[]> discretevarsandeventtriggerinputs = new Hashtable<String,String[]>();
	private Set<String> realStatenames = new HashSet<String>();
	private Namespace mathmlns = Namespace.getNamespace("", "http://www.w3.org/1998/Math/MathML");
	private XMLOutputter xmloutputter = new XMLOutputter();
	private Set<Element> toolset = new HashSet<Element>(); 
	protected Document doc;
	private String mmlcode;
	
	
	/**
	 * Constructor
	 * @param modelaccessor Location of the MML file from which the XMML was generated
	 * @param doc JDOM Document representation of the XMML content
	 * @param mmlcode The raw MML model code
	 * @throws Xcept
	 */
	public XMMLreader(ModelAccessor modelaccessor, Document doc, String mmlcode) throws Xcept {
		super(modelaccessor);
		this.doc = doc;
		this.mmlcode = mmlcode;
	}
	
	// This is for reading from an actual XMML file (.xml), not an MML (.mod) file
	@Override
	public SemSimModel read() throws IOException, InterruptedException,
			OWLException, CloneNotSupportedException, XMLStreamException {
		
		File srcfile = modelaccessor.getFile();
		if(srcfile != null){
			doc = null;

			try {
				doc = new SAXBuilder().build(srcfile);
			} catch (JDOMException e) {
				e.printStackTrace();
			}
			return readFromDocument();
		}
		return null;
	}
	
	/**
	 * Read the contents of the XMML as stored in the JDOM Document into a {@link SemSimModel} object
	 * @return The {@link SemSimModel} corresponding to the XMML model 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OWLException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SemSimModel readFromDocument() throws IOException, InterruptedException, OWLException {
		int numdomains = 0;
		// Collect all tools into a set
		Iterator toolit = doc.getRootElement().getChild("toolList").getChildren().iterator();
		while(toolit.hasNext()) toolset.add((Element)toolit.next());
			
		Element varroot = doc.getRootElement().getChild("model").getChild("variableList");
		List varchildren = varroot.getChildren();
		Iterator variterator = varchildren.iterator();

		// Collect the units
		Iterator unitit = doc.getRootElement().getChild("model").getChild("unitList").getChildren().iterator();
		while(unitit.hasNext()){
			
			Element unitel = (Element) unitit.next();
			if(unitel.getName().equals("fundamentalUnit") || unitel.getName().equals("derivedUnit")){
				String unitname = unitel.getAttributeValue("id");
				String unittype = unitel.getAttributeValue("unitType");
				
				UnitOfMeasurement uom = new UnitOfMeasurement(unitname);
				uom.setUnitType(unittype);
				semsimmodel.addUnit(uom);
				
				if(unitel.getName().equals("fundamentalUnit")){
					uom.setFundamental(true);
				}
				else if(unitel.getName().equals("derivedUnit")){
					uom.setFundamental(false);
					// Process the realFactors (multipliers) and unit factors
					String multiplier = null;
					Iterator<?> realfactorit = unitel.getChildren("realFactor").iterator();
					
					// There should only be one realFactor, but we loop anyway
					while(realfactorit.hasNext()){
						Element realfactor = (Element) realfactorit.next();
						multiplier = realfactor.getAttributeValue("multiplier");
					}
										
					// Get the factors for the unit
					Iterator<?> unitfactorit = unitel.getChildren("unitFactor").iterator();
					
					while(unitfactorit.hasNext()){
						Element unitfactor = (Element) unitfactorit.next();
						String baseunits = unitfactor.getAttributeValue("unitID");
						// NOTE: there is no "prefix" attribute assigned to unit factors
						String exponent = unitfactor.getAttributeValue("exponent");
						UnitOfMeasurement baseuom = semsimmodel.getUnit(baseunits);
						
						// This assumes that all unit factors are fundamental units, 
						// which appears true based on my experience with XMML (-MLN)
						if(baseuom==null){
							baseuom = new UnitOfMeasurement(baseunits);
							baseuom.setFundamental(true);
							semsimmodel.addUnit(baseuom);
						}
						double exp = (exponent==null) ? 1.0 : Double.parseDouble(exponent);
						double mult = (multiplier==null) ? 1.0 : Double.parseDouble(multiplier);
						
						if(uom.getUnitFactors().size()==0)
							uom.addUnitFactor(new UnitFactor(baseuom, exp, null, mult)); // Only add multiplier to first unit factor
						else uom.addUnitFactor(new UnitFactor(baseuom, exp, null));  
					}
				}
			}
		}

		

		// Get the JSim "relations" constraints
		Iterator relit = doc.getRootElement().getChild("model").getChild("relationList").getChildren("relation").iterator();
		while(relit.hasNext()){
			Element rel = (Element) relit.next();
			String mmleq = rel.getChild("expression").getChildText("debug");
			String mathmleq = xmloutputter.outputString(rel.getChild("expression").getChild("math",mathmlns));
			semsimmodel.addRelationalConstraint(new RelationalConstraint(mmleq, mathmleq));
		}

		variterator = varchildren.iterator();
			
		// collect the codeword names and units
		while (variterator.hasNext()) {
			Element varchild = (Element) variterator.next();
			// if variable should go into SemSim
			if ( ! varchild.getAttributeValue("id").endsWith(".ct")) {
				String codeword =  varchild.getAttributeValue("id");
				
				DataStructure ds = null;

				// Store the data type attribute
				String vardatatype = varchild.getAttributeValue("dataType");
				if (vardatatype.equals("real"))  ds = new Decimal(codeword);
				else if (vardatatype.equals("int")) ds = new SemSimInteger(codeword);
				else if(vardatatype.equals("choice")) ds = new MMLchoice(codeword);
				
				semsimmodel.addDataStructure(ds);
				ds.setMetadataID(codeword);
								
				if(codeword.contains(":") || codeword.endsWith("__init")) ds.setDeclared(false);
				else ds.setDeclared(true);
				
				// Store the units attribute value
				if (varchild.getAttributeValue("unitID") != null) {
					String unitname = varchild.getAttributeValue("unitID");
					if(semsimmodel.containsUnit(unitname)) ds.setUnit(semsimmodel.getUnit(unitname));
					else{
						UnitOfMeasurement uom = new UnitOfMeasurement(unitname);
						semsimmodel.addUnit(uom);
						ds.setUnit(uom);
					}
				} 
				
				// Store the solution domain attribute in the array
				if(varchild.getChild("domainList")!=null){
					Iterator domit = varchild.getChild("domainList").getChildren("domain").iterator();
					while(domit.hasNext()){
						Element dom = (Element) domit.next();
						String domname = dom.getAttributeValue("domainID");
						DataStructure soldom;
						if(!domname.equals(codeword)){
							if(semsimmodel.hasSolutionDomainName(domname)){
								soldom = semsimmodel.getSolutionDomainByName(domname);
								ds.setSolutionDomain(soldom);
							}
							else{
								if(varchild.getAttributeValue("dataType").equals("real")){
									Decimal newsoldomain = new Decimal(domname);
									newsoldomain.setIsSolutionDomain(true);
									semsimmodel.addDataStructure(newsoldomain);
								}
								else if(varchild.getAttributeValue("dataType").equals("int")){
									SemSimInteger newsoldomain = new SemSimInteger(domname);
									newsoldomain.setIsSolutionDomain(true);
									semsimmodel.addDataStructure(newsoldomain);
								}
							}
						break;
						// When change to allow for multiple solution domains, need to edit this part
						}
					}
				}

				// Store the isDomain value in the array
				if (varchild.getAttributeValue("isDomain").equals("true")){
					ds.setIsSolutionDomain(true);
					numdomains++;
					
					// Catch multiple domains
					if(numdomains>1){
						semsimmodel.addError("The model contains multiple solution domains.\n" +
								"SemGen currently only supports single-domain models." );
						return semsimmodel;
					}
				}
				else ds.setIsSolutionDomain(false);
				
				// find the assignment constraint for the variable
				boolean stop = false;
				Computation computation = ds.getComputation();
				
				// Get the math assignments
				List<Element> tools = doc.getRootElement().getChild("toolList").getChildren("exprTool");
				Iterator exprit = tools.iterator();
				while (exprit.hasNext() && !stop) {
					Element expr = (Element) exprit.next();
					Iterator solvedvarsit = expr.getChild("solvedVariableList").getChildren("variableUsage").iterator();
					while(solvedvarsit.hasNext() && !stop){
						Element solvedvar = (Element) solvedvarsit.next();
						if(solvedvar.getAttributeValue("id").equals(codeword)){ //&& ds.isDeclared()){
							
							String mmlcodeex = expr.getChild("expression").getChildText("debug");
							String mathmlassignment = xmloutputter.outputString(expr.getChild("expression").getChild("math",mathmlns));
							
							computation.setComputationalCode(codeword + " = " + mmlcodeex);
							computation.setMathML(mathmlassignment);
							stop = true;
						}
					}
				}

				// If the codeword is solved with an ODE tool, get the state equation and initial condition from the ODE tool
				Iterator ODEtoolit = doc.getRootElement().getChild("toolList").getChildren("ODETool").iterator();
				stop = false;
				Element ODEtool = null;
				while (ODEtoolit.hasNext() && !stop) {
					ODEtool = (Element) ODEtoolit.next();
					Iterator varit = ODEtool.getChild("solvedVariableList").getChildren("variableUsage").iterator();
					while(varit.hasNext()){
						Element var = (Element) varit.next();
						if (var.getAttributeValue("id").equals(codeword) 
								&& !var.getAttributeValue("id").endsWith(".max)")){
								//&& ds.isDeclared()){
							String varid = codeword + ":" + ODEtool.getAttributeValue("timeDomainID");
							String steqid = ODEtool.getChild("stateEquation").getAttributeValue("toolID");
							Element steqtool = getToolByID(steqid);
							// Set the state equation
							if(steqtool.getName().equals("exprTool")){
								computation.setComputationalCode(codeword + ":" + ODEtool.getAttributeValue("timeDomainID") + " = " + steqtool.getChild("expression").getChild("debug").getText());
								computation.setMathML(xmloutputter.outputString(steqtool.getChild("expression").getChild("math",mathmlns)));
							}
							// if the state equation is part of an implicit tool, find the appropriate equation
							else if(steqtool.getName().equals("implicitTool")){
								Iterator zeroexit = steqtool.getChild("zeroExpressionList").getChildren("expression").iterator();
								while(zeroexit.hasNext()){
									Element ex = (Element) zeroexit.next();
									if(ex.getChild("debug").getText().startsWith(varid)){
										computation.setComputationalCode("0 = " + ex.getChild("debug").getText());
										computation.setMathML(xmloutputter.outputString(ex.getChild("math",mathmlns)));
									}
								}
							}
							ds.setStartValue(getIC(ODEtool, codeword));
						}
					}
				}
				
				// Get the implicit constraints
				Iterator imptools = doc.getRootElement().getChild("toolList").getChildren("implicitTool").iterator();
				Element imptool = null;
				while(imptools.hasNext()){
					imptool = (Element) imptools.next();
					Iterator<Element> solvedvars = imptool.getChild("solvedVariableList").getChildren("variableUsage").iterator();
					Iterator<Element> zeroeqs = imptool.getChild("zeroExpressionList").getChildren("expression").iterator();
					
					while(solvedvars.hasNext()){
						String solvedvar = solvedvars.next().getAttributeValue("id");
						Element zeroeq = zeroeqs.next();
						// If the variable is solved by the implicit tool
						if(solvedvar.equals(codeword)){
							// Get the zero expressions, find the one for the variable in question
							String mmleq = zeroeq.getChild("debug").getText();
							String mathmleq = xmloutputter.outputString(zeroeq.getChild("math",mathmlns));
							// Store in semsim model
							computation.setComputationalCode("0 = " + mmleq);
							computation.setMathML(mathmleq);
						}
					}
				}
			} // End of if statement that leaves out .ct vars
		} // End of variable iterator
		
		
		// Get the eventConstraints
		Iterator eventiterator = doc.getRootElement().getChild("model").getChild("eventList").getChildren("event").iterator();
		while (eventiterator.hasNext()) {
			
			Event ssevent = new Event();
			Element oneevent = (Element) eventiterator.next();
			String eventid = oneevent.getAttributeValue("id");
			ssevent.setName(eventid);

			Element trigger = oneevent.getChild("trigger");
			List<Element> triggerchildren = trigger.getChildren();
			Iterator triggerchildreniterator = triggerchildren.iterator();
			String triggertext = "event("+ oneevent.getChild("trigger").getChild("debug").getText() + ")";

			while (triggerchildreniterator.hasNext()) {
				Element triggerchild = (Element) triggerchildreniterator.next();
				
				if (triggerchild.getName().equals("math")) {
					Element triggermathel = trigger.getChild("math", mathmlns);
					ssevent.setTriggerMathML(xmloutputter.outputString(triggermathel));
				}
			}

			List stateactions = oneevent.getChild("actionList").getChildren("action");
			Iterator stateactionsiterator = stateactions.iterator();
			
			while (stateactionsiterator.hasNext()) {
				Element action = (Element) stateactionsiterator.next();
				EventAssignment ssea = new EventAssignment();
				String assignmentmathml = xmloutputter.outputString(action.getChild("expression").getChild("math",mathmlns));
				ssea.setMathML(assignmentmathml);
				String varstring = action.getAttributeValue("variableID");
				DataStructure outputds = semsimmodel.getAssociatedDataStructure(varstring);
				ssea.setOutput(outputds);
				outputds.getComputation().addEvent(ssevent);
				
				ssevent.addEventAssignment(ssea);
				
				String actioneq = varstring + " = " + action.getChild("expression").getChild("debug").getText();
				String fulltext = triggertext + "{ " + actioneq + "; }";
				discretevarsandconstraints.put(varstring, fulltext);
				discretevarsandevents.put(varstring, ssevent);
				realStatenames.add(varstring);

				// get the initial condition for the discrete realState variable (in some stateTool)
				outputds.setStartValue(getIC(getToolToSolveCodeword(outputds.getName()), outputds.getName()));
				
				// For now, store the MML code that triggers the event and makes the event assignment
				// Need to fix this hack once we can translate MathML to MML
				outputds.getComputation().setComputationalCode(fulltext);
				 
			}
			
			semsimmodel.addEvent(ssevent);
		}

		
		setCustomUnits();
		
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures())
			SemSimUtil.setComputationInputsForDataStructure(semsimmodel, ds, null);
		
		// Set hasInput/inputFor relationships for discrete variables and the data structures required for triggering them
		for(String dsx : discretevarsandeventtriggerinputs.keySet()){
			for(String inputx : discretevarsandeventtriggerinputs.get(dsx))
				semsimmodel.getAssociatedDataStructure(dsx).getComputation().addInput(semsimmodel.getAssociatedDataStructure(inputx));
		}
				
		// Add the model-level annotations
		semsimmodel.setSourceFileLocation(modelaccessor);
		semsimmodel.setSemSimVersion(sslib.getSemSimVersion());
		
		// If jsbatch couldn't parse the model code into an xmml file, log the error
		if(semsimmodel.getAssociatedDataStructures().isEmpty() && semsimmodel.getPhysicalModelComponents().isEmpty() && semsimmodel.getSubmodels().isEmpty()){
			semsimmodel.addError(modelaccessor.getModelName() + " model appears to be empty.");
		}
		
		// Set the semsimmodel name field
		semsimmodel.setName(modelaccessor.getModelName());
		
		return semsimmodel;
	}
	
	
	/**
	 * Deal with custom unit declarations
	 * @throws FileNotFoundException
	 * @throws OWLException
	 */
	private void setCustomUnits() throws FileNotFoundException, OWLException {
		Map<String,String> unitnamesandcustomdeclarations = new HashMap<String,String>();
		Scanner scnr = new Scanner(mmlcode);

		// This next part that attempts to account for custom unit declarations
		// is a bit of a hack
		// May need to account for multiple code lines in original .mod file
		// (blah...; blah....; blah.....;)
		// Can this be rewritten using XMML2?
		while (scnr.hasNextLine()) {
			String nextline = scnr.nextLine();
			String nextlinemod = nextline.replace(" ", "");
			nextlinemod = nextlinemod.replace("\t", "");
			if (nextlinemod.startsWith("unit") && nextlinemod.contains("=")) {
				String unitname = nextlinemod.substring(nextlinemod.indexOf("unit") + 4,nextlinemod.indexOf("="));
				unitnamesandcustomdeclarations.put(unitname, nextline);
			}
		}
		scnr.close();
		for(DataStructure ds : semsimmodel.getAssociatedDataStructures()){
			if(ds.hasUnits()){
				if(unitnamesandcustomdeclarations.containsKey(ds.getUnit().getName())){
					String customname = ds.getUnit().getName();
					semsimmodel.getUnit(customname).setCustomDeclaration(unitnamesandcustomdeclarations.get(customname));
				}
			}
		}
	}
	
	
	/**
	 * Look up the XMML tool element that has an input ID
	 * @param ID An input identifier
	 * @return The XMML tool Element in the XMML code that has the input ID
	 */
	private Element getToolByID(String ID){
		for(Element tool : toolset){
			if(tool.getAttributeValue("id").equals(ID)){
				return tool;
			} 
		}
		System.out.println("Couldn't find tool that matches ID " + ID);
		return null;
	}
	
	
	/**
	 * Find the XMML tool that is used to determine the numerical values of an
	 * input codeword
	 * @param cdwd An input codeword used in the XMML model
	 * @return The XMML tool Element that describes how to solve the codeword
	 */
	private Element getToolToSolveCodeword(String cdwd){
		for(Element tool : toolset){
			List<?> varlist = tool.getChild("solvedVariableList").getChildren("variableUsage");
			Iterator<?> varit = varlist.iterator();
			if(varlist.size() == 1){
				while(varit.hasNext()){
					Element var = (Element) varit.next();
					if(var.getAttributeValue("id").equals(cdwd)){
						return tool;
					}
				}
			}
		}
		System.out.println("Couldn't find tool to solve " + cdwd);
		return null;
	}
	
	
	/**
	 * Get the set of codewords that are used in the computations for
	 * a given XMML tool Element
	 * @param toolid The ID of the XMML tool Element
	 * @return Set of codewords that are required for the tool
	 */
	private Set<String> getRequiredVariablesForTool(String toolid){
		Element tool = getToolByID(toolid);
		Set<String> reqvars = new HashSet<String>();
		Iterator<?> reqvarit = tool.getChild("requiredVariableList").getChildren("variableUsage").iterator();
		while(reqvarit.hasNext()){
			Element reqvar = (Element) reqvarit.next();
			reqvars.add(reqvar.getAttributeValue("id"));
		}
		return reqvars;
	}
	
	
	/**
	 * Get the initial condition statement for a codeword in the model
	 * @param tool The parent tool Element that would contain the initial condition
	 * for the codeword
 	 * @param cdwd The codeword
	 * @return An expression indicating the initial condition of the codeword
	 */
	private String getIC(Element tool, String cdwd){
		// get the tool that sets the IC
		Iterator<?> ICit = tool.getChild("initialConditionList").getChildren("initialCondition").iterator();
		if(tool.getChild("initialConditionList").getChildren("initialCondition").size()>1){
			System.out.println("WARNING: Multiple initial conditions for variable " + cdwd);
			return null;
		}
		while(ICit.hasNext()){
			Element IC = (Element) ICit.next();
			String ICtoolid = IC.getAttributeValue("toolID");
			
			// if there are required variables, get them recursively
			if(getRequiredVariablesForTool(ICtoolid).contains(cdwd + "__init")){
				return getToolToSolveCodeword(cdwd + "__init").getChild("expression").getChild("debug").getText();
			}
			else{
				String eq = getToolByID(ICtoolid).getChild("expression").getChild("debug").getText();
				if(eq.contains("when (")){
					eq = eq.substring(eq.indexOf(")")+1,eq.length());
				}
				return eq;
			}
		}
		return null;
	}

}
