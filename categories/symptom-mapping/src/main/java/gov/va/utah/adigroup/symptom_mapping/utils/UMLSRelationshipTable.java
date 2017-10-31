/* Copyright (C) [2013] [The Symptom Mapping Project]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.va.utah.adigroup.symptom_mapping.utils;

import java.util.ArrayList;

/**
 * UMLSRelationshipTable 
 * @author Le-Thuy Tran {@code <ltran@cs.utah.edu>}
 * @version June 15, 2013
 */

public class UMLSRelationshipTable {
	private ArrayList<UMLSRelationshipTuple> table;
	
	public UMLSRelationshipTable(){
		table = new ArrayList<UMLSRelationshipTuple>();
	}
	
	public void addRelation(String pt, String ct, String r){
		int i =0;
		while( (i < table.size()) && ( table.get(i).getParentType().compareTo(pt) < 0)){
			i++;
		}
		while( (i < table.size()) && (table.get(i).getParentType().compareTo(pt) == 0) 
				&& (table.get(i).getrelation().compareTo(r) < 0) ){
			i++;
		}
		while( (i < table.size()) && (table.get(i).getParentType().compareTo(pt) == 0) 
				&& (table.get(i).getrelation().compareTo(r) == 0) 
				&& (table.get(i).getChildType().compareTo(ct) < 0)){
			i++;
		}
		if(i<table.size()){
			table.add(i, new UMLSRelationshipTuple(pt,ct,r));
		}else{
			table.add(new UMLSRelationshipTuple(pt,ct,r));
		}
	}
	
	public void printTable(){
		for(int i = 0; i < table.size(); i++){
			UMLSRelationshipTuple rel = table.get(i);
			System.out.println(rel.getParentType() + "\t" + rel.getChildType()+"\t"+rel.getrelation());
		}
	}
	
	   // This function is used in the method for traversing the concepts. 
		// In the method, if this function return 0, do nothing; return 1, add the concept and traverse the child path;
		// return 2, only add the concept and stop traversing
			
		public int hasRelation(String pt, String ct, String r){
			
			if(r.equals("part_of")){
				if(isAnatomicalPart(pt) && (isAnatomicalPart(ct) ||  ct.equals("Finding"))){
					return 1;
				} 
				return 0;
			}
			
			if(r.equals("systemic_part_of")){
				if(isAnatomicalPart(pt) && (isAnatomicalPart(ct)||ct.equals("Body Substance"))){
					return 1;
				}
				return 0;
			}
			
			if(r.equals("has_location")){ //**********
				if(isAnatomicalPart(pt)){
					if (isAnatomicalAbProblemAddSF(ct) || ct.equals("Clinical Attribute")
							|| isPhyandMenFinding(ct))
						return 1;
				}
				//if(isAnatomicalPart(pt) && isAnatomicalPart(ct)){
				//	return 1;
				//}
				if(pt.equals("Body Substance") && (ct.equals("Finding")||ct.equals("Sign or Symptom"))){
					return 1;
				}
				return 0;
			}
			
			if(r.equals("disease_has_primary_anatomic_site")){  //******
				if(isAnatomicalPart(pt)){ 
					if (isAnatomicalAbProblemAddSF(ct) || isAnamolies(ct) 
							|| isPhyandMenFinding(ct)|| ct.equals("Clinical Attribute"))
						return 1;
				}
				return 0;
			}
			
			if (r.equals("anatomic_structure_is_physical_part_of")) { //**************//
				if (isAnatomicalPart(pt) && (isAnatomicalPart(ct)||ct.equals("Body Substance"))){ // ct : Acquired Abnormality, Body Substance, Conginital Abnormality,                                                // Neoplastic Process, Organ or Tissue Function
					return 1;
				}
				return 0;
			}
			
			//if( r.equals("sign_or_symptom_of")){
			//	System.out.println("SIGN OR SYMPTOM OF " + pt + " : " + ct);
			//	return 2;
			//}
			
			//if(r.equals("clinically_associated_with")){
				//System.out.println("PARENT : " + pt + "CHILD : " + ct);
			//	if(isAnatomicalPart(pt)|| isAnatomicalAbProblemAddSF(pt) || isAnamolies(pt)){ 
			//		if ( isPhyandMenFinding(ct)){
						//System.out.println("PARENT : " + pt + "CHILD : " + ct);
			//			return 1;
			//		}
			//		return 0;
			//	}
			//	return 0;
			//}
			
			if(r.equals("isa")){
				if(isAnatomicalPart(pt)){
					if(	isAnatomicalPart(ct)){
						return 1;
					}else if( isAnatomicalAbProblemAddSF(ct)){
						return 1;
					}else if( isPhyandMenFinding(ct)){
						return 1;
					}
					return 0;
				} else if (isAnatomicalAbProblemAddSF(pt)||isAnamolies(pt)){
				    if (isAnatomicalAbProblemAddSF(ct) || isAnamolies(ct)){
						return 1;
				    }else if( isPhyandMenFinding(ct)){
				    	//System.out.println("Parent : " + pt + " Child : " + ct);
						return 1;
					}
					return 0;
				} else if(isPhyandMenFinding(ct) && isPhyandMenFinding(pt)){
					return 1;
				}
				else if (pt.equals("Body Substance") && ct.equals("Body Substance")){
					return 1;
				}
				return 0;
			}
			
			if(r.equals("mapped_to")){
				if(pt.equals("Sign or Symptom") || pt.equals("Finding")||isBehavior(pt)){
					if(ct.equals("Sign or Symptom") || ct.equals("Finding") || isBehavior(ct)){
						return 1;
					}
					return 0;
				}
				//if(isBehavior(pt) && isBehavior(ct)){
				//	return 1;
				//}
				//return 0;
			}
			
			//if(r.equals("clinically_associated_with")){
			//	if(isAnatomicalAbProblemAddSF(pt)){
			//		if(ct.equals("Sign or Symptom") || ct.equals("Finding")){
			//			return 2;
			//		}
			//		return 0;
			//	}
			//	return 0;
			//}
			
			if(r.equals("has_finding_site")){
				if(  isAnatomicalPart(pt)){
					if( isAnatomicalPart(ct)){
					   	return 1;				
					}else if (ct.equals("Body Substance")){
					   	return 1;
					}else if (isAnatomicalAbProblemAddSF(ct)){
					    return 1;
				    }else if (isPhyandMenFinding(ct)){
					    return 1;
				    }
					return 0;
				}else if (pt.equals("Body Substance")){
					if( ct.equals("Sign or Symptom") || ct.equals("Finding")){
					    return 1;
					}
			    	return 0;
			    }else if (isAnatomicalAbProblemAddSF(pt) || isAnamolies(pt)){
					if( isPhyandMenFinding(ct)){
					    return 1;
					}	
					return 0;
				}
				return 0;
			}
			
			return 0;
		}
		
		// Anomoly - as defined in the symptom mapping paper
		public boolean isAnamolies(String pt){
			    if( pt.equals("Acquired Abnormality") 
					|| pt.equals("Anatomical Abnormality") 
					|| pt.equals("Congenital Abnormality")){   
				return true;
			}
			return false;
		}
		
		// Physical or Mental Finding - as defined in the symptom mapping paper
		public boolean isPhyandMenFinding(String pt){
			if( pt.equals("Sign or Symptom") 
					|| pt.equals("Finding") 
					|| pt.equals("Mental Process") 
					|| pt.equals("Mental or Behavioral Dysfunction")){   
					return true;
				}
				return false;
			}
		
		// Anatomical Structure or Region - as defined in the symptom mapping paper
		public boolean isAnatomicalPart(String pt){
			if( pt.equals("Anatomical Structure") ||  pt.equals("Body System")  
			    || pt.equals("Embryonic Structure") || pt.equals("Fully Formed Anatomical Structure") 
			    ||  pt.equals("Body Part, Organ, or Organ Component")
			    || pt.equals("Body Location or Region") ||  pt.equals("Body Space or Junction")
			    || pt.equals("Spatial Concept") || pt.equals("Acquired Abnormality") 
			    || pt.equals("Anatomical Abnormality") || pt.equals("Congenital Abnormality")){    
				return true;
			}
			return false;
		}
		
		
		
		// Phenomenon or Process or Behavior - as defined in the symptom mapping paper
		public boolean isAnatomicalAbProblemAddSF(String pt){
			if(pt.equals("Disease or Syndrome") || pt.equals("Pathologic Function") 
			|| pt.equals("Neoplastic Process") || pt.equals("Mental or Behavioral Dysfunction")
			|| pt.equals("Injury or Poisoning") || pt.equals("Physiologic Function")
			|| pt.equals("Organism Function") || pt.equals("Mental Process")
			|| pt.equals("Organ or Tissue Function")
			|| pt.equals("Individual Behavior") || pt.equals("Social Behavior")){ 
				return true;
			}
			return false;
		}
		
		
		// Behavior - as defined in the symptom mapping paper
		public boolean isBehavior(String pt){
			if(pt.equals("Mental or Behavioral Dysfunction") || pt.equals("Mental Process")
					|| pt.equals("Individual Behavior") || pt.equals("Social Behavior")){
				return true;
			}
			return false;
		}
		
}
