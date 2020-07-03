import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;

/**
 * This program formats SFDC Logs
 * 
 * @author Maciej Szymczak
 * @version 2020.07.02
 */

public class SalesforceLogFormatter {
			
	
	private static FileReader fr;	
	private static void formatLogs (String sourceFile, String destFile, String displayImportantOnly) throws IOException {
		try {
			FileWriter writer = new FileWriter(destFile);
			
			fr = new FileReader(sourceFile);
			BufferedReader br = new BufferedReader(fr);
			String phisicalLine;
			String indent="";
	        
			while( (phisicalLine = br.readLine()) != null) {
				phisicalLine = phisicalLine.replace("{", "((");
				phisicalLine = phisicalLine.replace("}", "))");
			    //remove timestamp
				if (phisicalLine.contains("|"))
					phisicalLine = phisicalLine.substring(phisicalLine.indexOf("|")+1, phisicalLine.length());
				//indentation
				if (phisicalLine.contains("CODE_UNIT_STARTED|") || phisicalLine.contains("DML_BEGIN|") ) {
					phisicalLine = phisicalLine.replace("CODE_UNIT_STARTED|", "{CODE_UNIT_STARTED|");
					phisicalLine = phisicalLine.replace("DML_BEGIN|", "{DML_BEGIN|");
			    	indent = indent + "  ";
				}
				//set ***IMPORTANT*** 
				if (phisicalLine.contains("USER_DEBUG|") 
			     || phisicalLine.contains("trigger event") //trigger started
			     || phisicalLine.contains("WF_CRITERIA_BEGIN") //Workflow or process builder started	
			     || phisicalLine.contains("FLOW_CREATE_INTERVIEW") // process builder started	
						) {
					phisicalLine = "***IMPORTANT*** " + phisicalLine;
				}				
				if (phisicalLine.contains("DML_BEGIN|")  //field update: trigger (or apex block):      RecordId: NO  Field name: NO   New value: NO   Old value: NO
						 || phisicalLine.contains("WF_FIELD_UPDATE") //field update: workflow -        RecordId: YES Field name: YES  New value: YES  Old value: NO. Example: WF_FIELD_UPDATE|[MT: A0001 a127E0000032hpq]|Field:MT: f1|Value:XXX|Id=04Y7E0000007TKC|CurrentRule:MT1 (Id=01Q7E000000Lmr0)
						 || phisicalLine.contains("FlowRecordUpdate") //field update: Process builder- RecordId: NO  Field name: NO   New value: NO   Old value: NO
								) {
							phisicalLine = "***IMPORTANT*** FIELD_UPDATE-reexecutes triggers! " + phisicalLine;
						}				
			    if (phisicalLine.contains("CODE_UNIT_FINISHED|") || phisicalLine.contains("DML_END|")) {
					phisicalLine = phisicalLine + "}";
			    }
			    
			    //Display important only
			    if (displayImportantOnly=="Y") {
				    if (phisicalLine.contains("***IMPORTANT***")||phisicalLine.contains("{")||phisicalLine.contains("}")) {
				        writer.append(indent+phisicalLine.replace("***IMPORTANT***", ""));
					    writer.append('\n');	
				    }
			    } else {
			        writer.append(indent+phisicalLine);
				    writer.append('\n');	
			    }
			    	
			    //revert indentation
			    if (phisicalLine.contains("CODE_UNIT_FINISHED|") || phisicalLine.contains("DML_END|")) {
			    	indent = indent.substring(0, indent.length()-2);			    	
			    }
			}
			
		    writer.flush();
		    writer.close();			
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	    
	}	
	
	public static void main(String[] args) throws Exception {
		formatLogs(args[0], args[1], "Y");		
	}	
}
