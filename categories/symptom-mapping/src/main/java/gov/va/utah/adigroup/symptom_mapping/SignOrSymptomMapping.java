/**
 * Copyright (C) [2013] [The Symptom Mapping Project]
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

package gov.va.utah.adigroup.symptom_mapping;

import java.io.BufferedWriter;

import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.Statement;

import gov.va.utah.adigroup.symptom_mapping.utils.TXTFileReader;
import gov.va.utah.adigroup.symptom_mapping.utils.MySQLConnection;
import gov.va.utah.adigroup.symptom_mapping.utils.DistinctLinkedList;
import gov.va.utah.adigroup.symptom_mapping.utils.PushandPop;
import gov.va.utah.adigroup.symptom_mapping.utils.UMLSRelationshipTable;

import java.util.ArrayList;

/*
* @author Le-Thuy Tran {@code <ltran@cs.utah.edu>}
* @version June 15, 2013
*/
public class SignOrSymptomMapping {
	public static UMLSRelationshipTable umlsRelationTable;
	public static boolean DEBUGTREE = false;             // Set to true if want to print the traversed paths to the CUIs in DEBUGCUI list
	public static String  [] DEBUGCUI = {"C2752166"};    
	
	public static DistinctLinkedList generalSS = new  DistinctLinkedList(); // General well-being
	public static DistinctLinkedList ENTSS = new  DistinctLinkedList(); // ENT
	public static DistinctLinkedList EYESS = new  DistinctLinkedList(); // Visual System
	public static DistinctLinkedList integumentarySS = new  DistinctLinkedList(); // Integumentary
	public static DistinctLinkedList endocrineSS = new  DistinctLinkedList(); // Endocrine
	public static DistinctLinkedList digestiveSS = new  DistinctLinkedList(); // Digestive system
	public static DistinctLinkedList urinarySS = new  DistinctLinkedList(); // Urinary system
	public static DistinctLinkedList reproductiveSS = new  DistinctLinkedList(); // Reproductive system
	public static DistinctLinkedList genitourinarySS = new  DistinctLinkedList(); //Genitourinary system 
	public static DistinctLinkedList lymphaticSS = new  DistinctLinkedList(); // Lymphatic-Immune system
	public static DistinctLinkedList cardiovascularSS = new  DistinctLinkedList(); // Cardiovascular system
	public static DistinctLinkedList respiratorySS = new  DistinctLinkedList(); // Respiratory system
	public static DistinctLinkedList musculoskeletalSS = new  DistinctLinkedList(); // Musculoskeletal system
	public static DistinctLinkedList nervousSS = new  DistinctLinkedList(); // Nervous system
	public static DistinctLinkedList mentalHealthSS = new  DistinctLinkedList(); // Mental Health
	
	
	
	
	/*
	 * Check if a CUI belongs to a list of CUIs
	 */
	public static boolean isIn(String CUI, String [] CUIS){
		for(int i = 0; i < CUIS.length; i++){
			if(CUI.equals(CUIS[i]))
				return true;
		}
		return false;
	}
	
	/*
	 *  This is recursive process for traversing signs and symptoms of the organ system from the current 
	 *  parent CUI `parentCUI` of type `parentType`
	 *  @param mysqlConn: Connection to the database that stores the relationships in UMLS
	 *  @param level: traversed depth
	 *  @param parentCU: parent CUI
	 *  @param parentType: Type of parent CUI
	 *  @param SS: list of signs and symptoms  (LIST5 as referred in paper)
	 *  @param traversedCUIs: list of CUIs that have been traversed (LIST4 as referred in paper)
	 *  @param queue: a queue to trace the traversed path of the CUIs for debugging purpose
	 */
	public static void Maps(MySQLConnection mysqlConn, int level, String parentCUI, String parentType, DistinctLinkedList SS,
			DistinctLinkedList traversedCUIs, PushandPop<String> queue) throws Exception{
		
		
		// Select all relationships of interest (as described in the Table 2 of the paper) and the related CUIs for the current
		// traversed CUI 
		Statement s = mysqlConn.createStatement();
		ResultSet rs = mysqlConn.executeQuery(s, "SELECT CUI2, REL, RELA FROM umls.mrrel WHERE CUI1 = '" + parentCUI + "' AND "
				                                  + "((REL = 'CHD' AND RELA ='isa') OR "
				                                  + "(REL = 'RQ' AND RELA ='isa')  OR (REL='RN' AND RELA = 'isa')"
				                                  + "OR (RELA='systemic_part_of') OR (REL='RN' AND RELA='part_of') "
				                                  + "OR (REL='CHD' AND RELA='part_of') OR (RELA='has_finding_site') "
				                                  + "OR (RELA='disease_has_primary_anatomic_site') "				                         
				                                  + "OR ((REL ='RN' OR REL='RQ') AND RELA ='mapped_to') "				                                  
				                                  + "OR (RELA='has_location')  OR (RELA='anatomic_structure_is_physical_part_of'))");
		String CUI2;                        
		String RELA;
		String REL;
		String CUI2Type;
		int relationship;
		
		if( level > 30){
			System.out.println("Traversed depth : " + level);
		}
		
		if(DEBUGTREE){ // Set the variable DEBUGTREE to true to track the traversed path that includes DEBUGCUI
			Statement s1 = mysqlConn.createStatement();
			ResultSet rs1 = mysqlConn.executeQuery(s1, "SELECT STR FROM umls.mrconso where CUI='" + parentCUI +"'");
			String symptomDesc ="";
			if(rs1.next()){
				symptomDesc = rs1.getString("STR") + " | " + parentCUI + " | "+ parentType + "\n"; 
			}
			
			s1.close();
			rs1.close();									
			queue.push(symptomDesc);
		}
		
		while (rs.next()){
			CUI2 = rs.getString("CUI2");
			RELA = rs.getString("RELA");
			REL = rs.getString("REL");
			Statement s3 = mysqlConn.createStatement();			
			ResultSet rs3 = mysqlConn.executeQuery(s3, "SELECT STY FROM umls.mrsty where CUI='"+ CUI2+"'");
			while (rs3.next()){
				CUI2Type = rs3.getString("STY");
				relationship = umlsRelationTable.hasRelation(parentType, CUI2Type, RELA);
				if(relationship == 1){
					if(traversedCUIs.addWord(CUI2 + " " + CUI2Type)){
						if(CUI2Type.equals("Sign or Symptom") || CUI2Type.equals("Finding") ||
								CUI2Type.equals("Mental or Behavioral Dysfunction") || CUI2Type.equals("Mental Process")
								|| CUI2Type.equals("Social Behavior") || CUI2Type.equals("Individual Behavior")
								|| CUI2Type.equals("Injury or Poisoning")){	
							SS.addWord(CUI2+" "+ CUI2Type);
						}
						
						if ( (DEBUGTREE == true) && (isIn(CUI2,DEBUGCUI))){ 
							// if DEBUGTREE was set to true and the current traversed CUI is in the list of CUIs to debug  
							queue.print(); // Print out the traversed path
							Statement s1 = mysqlConn.createStatement();
							ResultSet rs1 = mysqlConn.executeQuery(s1, "SELECT STR FROM umls.mrconso where CUI='" + CUI2 +"'");
							String symptomDesc ="";
							if(rs1.next()){
								symptomDesc =  rs1.getString("STR");
							}
							s1.close();
							rs1.close();
							System.out.println(REL + "  " + RELA + "   "+ CUI2 + "("+symptomDesc+")");
							System.out.println("=====================================");
						}
						
						if(DEBUGTREE){ // Also push the relation into the queue 
							queue.push(REL + "  " + RELA);
						}
						
						// Call Maps to continue traversing the childs.
						Maps(mysqlConn, level+1, CUI2, CUI2Type, SS, traversedCUIs, queue);
						
						if(DEBUGTREE){ 
							queue.pop();
						}
					}
				}
			}
			s3.close();
			rs3.close();
		}
	
		if(DEBUGTREE){
			queue.pop();
		}
		s.close();
		rs.close();
	}
	
	/*
	 * blockDescendents: copy the parent CUI and all descendants into the list of traversed concepts
	 * to block those concepts from being traversed.
	 *  @param mysqlConn: Connection to the database that stores the relationships in UMLS
	 *  @param parentCU: parent CUI
	 *  @param parentType: Type of parent CUI
	 *  @param traversed: list of CUIs that have been traversed
	 */
	
	public static void blockDescendents(MySQLConnection mysqlConn, String parentCUI, String parentType, 
			DistinctLinkedList traversed) throws Exception{
		Statement s = mysqlConn.createStatement();
		ResultSet rs = mysqlConn.executeQuery(s, "SELECT CUI2, REL, RELA FROM umls.mrrel WHERE CUI1 = '" + parentCUI + "' AND "
				                                  + "((REL = 'CHD' AND RELA ='isa') OR "
				                                  + "(REL = 'RQ' AND RELA ='isa')  OR (REL='RN' AND RELA = 'isa')"
				                                  + "OR (RELA='systemic_part_of') OR (REL='RN' AND RELA='part_of') "
				                                  + "OR (REL='CHD' AND RELA='part_of') OR (RELA='has_finding_site') "
				                                  + "OR (RELA='disease_has_primary_anatomic_site') "				                         
				                                  + "OR ((REL ='RN' OR REL='RQ') AND RELA ='mapped_to') "				                                  
				                                  + "OR (RELA='has_location')  OR (RELA='anatomic_structure_is_physical_part_of'))");
		while (rs.next()){
			String CUI2 = rs.getString("CUI2");
			String RELA = rs.getString("RELA");
			Statement s3 = mysqlConn.createStatement();			
			ResultSet rs3 = mysqlConn.executeQuery(s3, "SELECT STY FROM umls.mrsty where CUI='"+ CUI2+"'");
			while (rs3.next()){
				String CUI2Type = rs3.getString("STY");
				int relationship = umlsRelationTable.hasRelation(parentType, CUI2Type, RELA);
				if(relationship == 1){
					if(traversed.addWord(CUI2 + " " + CUI2Type)){
						//System.out.println(CUI2);
						blockDescendents(mysqlConn, CUI2, CUI2Type, traversed);
					}
				}
			}
			s3.close();
			rs3.close();
		}
		s.close();
		rs.close();
	}
	
	
	/*
	 * copyTraversedSS: copy the the values from one DistinctLinkedList to the other
	 * @param traversedSS is the list to be copied
	 * @param traversed is the destination  
	 */
	public static void copyTraversedSS(DistinctLinkedList traversed, DistinctLinkedList traversedSS){
		for(int i = traversedSS.getSize()-1; i >= 0; i--){
			traversed.addWord(traversedSS.getValue(i));
		}
	}
	
	public static void MappingForOrganSystem(MySQLConnection mysqlConn, String organSystem, String [] parentCUIs, 
			String [] parentTypes, DistinctLinkedList SSList) throws Exception{
		
		DistinctLinkedList traversed = new DistinctLinkedList();
		
		PushandPop<String> queue = new PushandPop<String>();
		System.out.println("******************"+organSystem);
		
		if (organSystem.equals("ENT")){
			copyTraversedSS(traversed, generalSS);
		}else if (organSystem.equals("EYE")){
			copyTraversedSS(traversed, generalSS);
		}else if (organSystem.equals("Integumentary")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			blockDescendents(mysqlConn, "C2158560", "Anatomical Abnormality", traversed); 	
			//Integumentary system, NOS | C0037267 | Body System
			//RN  part_ofIntegument | C1123023 | Body System
			//RO  has_locationSKIN ABSCESS | C0149777 | Pathologic Function
			//CHD  isaAbscess of face | C0263097 | Disease or Syndrome
			//CHD  isaAbscess of jaw, NOS | C0266963 | Disease or Syndrome
			//CHD  isaDENTAL ABSCESS | C0518988 | Disease or Syndrome
			//CHD  isa   C2087774(tooth abscess in left upper central incisor)
			//=====================================
			//Integumentary system, NOS | C0037267 | Body System
			//CHD  isaEntire integumentary system (body structure) | C1284116 | Body System
			//RN  part_ofEntire skin AND subcutaneous tissue (body structure) | C1284137 | Body Part, Organ, or Organ Component
			//RN  part_ofMouth Mucosa | C0026639 | Body Part, Organ, or Organ Component
			//RN  part_ofGum | C0017562 | Body Part, Organ, or Organ Component
			//RO  has_locationDisease, Gingival | C0017563 | Disease or Syndrome
			//RN  isa   C2011928(mass of lingual mandibular gingiva on right)	
		}
		else if (organSystem.equals("Endocrine")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			
			//**********  Endocrine  **************
			//Endocrine System | C0014136 | Body System
			//RO  systemic_part_ofTestis | C0039597 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteTesticular observation | C0426322 | Finding
			//CHD  isaTESTICULAR PAIN | C0039591 | Sign or Symptom
			//CHD  isa   C2129031(testicular pain right)
			
			//systemic_part_of	LCH	C0029939	Ovaries	Body Part, Organ, or Organ Component 
		 	//CST	Endocrine System	RO	systemic_part_of	LCH	C0039597	Testis	Body Part, Organ, or Organ Component 
		 	//CST	Endocrine System	RO	systemic_part_of	RCD	C0227873	Right ovary	Body Part, Organ, or Organ Component 
		 	//CST	Endocrine System	RO	systemic_part_of	RCD	C0227874	Left ovary	Body Part, Organ, or Organ Component 
			blockDescendents(mysqlConn, "C0029939", "Body Part, Organ, or Organ Component", traversed); 
			blockDescendents(mysqlConn, "C0039597", "Body Part, Organ, or Organ Component", traversed); 
			
			//endocrine symptoms (symptom) | C2216346 | Sign or Symptom
			//CHD  isaMuscle weakness | C0151786 | Sign or Symptom
			//CHD  isaGeneralized muscle weakness | C0746674 | Sign or Symptom
			//RN  mapped_toLimb weakness | C0587246 | Sign or Symptom
			//CHD  isaMonoparesis - arm | C0751409 | Sign or Symptom
			//CHD  isa   C2202995(weakness of left arm)
			blockDescendents(mysqlConn, "C0151786", "Sign or Symptom", traversed); 
			
			//CHD	isa	MEDCIN	C2103402	endocrine disorders of female reproductive system (diagnosis)	Disease or Syndrome 
		 	//MSH	Disease, Endocrine	CHD	isa	MEDCIN	C2103403	endocrine disorders of male reproductive system (diagnosis)	Disease or Syndrome 
		 	//MSH	Disease, Endocrine	
			blockDescendents(mysqlConn, "C2103403", "Disease or Syndrome", traversed); 
			blockDescendents(mysqlConn, "C2103402", "Disease or Syndrome", traversed); 
			copyTraversedSS(traversed, integumentarySS);
			copyTraversedSS(traversed, EYESS);
			
			//**********  Endocrine  **************
			//Endocrine System | C0014136 | Body System
			//RO  has_finding_siteDisease, Endocrine | C0014130 | Disease or Syndrome
			//CHD  isaDISORDERS OF FLUID, ELECTROLYTE AND ACID-BASE BALANCE | C0267994 | Pathologic Function
			//CHD  isa   C0020649(Low blood pressure)
			blockDescendents(mysqlConn, "C0020649", "Finding", traversed); 
			//=====================================
			//Endocrine System | C0014136 | Body System
			//RO  systemic_part_ofExocrine and endocrine pancreas | C0030274 | Body Part, Organ, or Organ Component
			//RN  part_ofExocrine pancreas | C0553695 | Body Part, Organ, or Organ Component
			//RO  anatomic_structure_is_physical_part_ofPancreatic duct | C0030288 | Body Part, Organ, or Organ Component
			//CHD  isaPapilla of duodenum | C0227296 | Body Part, Organ, or Organ Component
			//RO  has_locationBiliary Tract Obstruction | C0400979 | Disease or Syndrome
			//CHD  isaBile stasis | C0008370 | Disease or Syndrome
			//CHD  isaBiliary Stasis, Extrahepatic | C0005398 | Disease or Syndrome
			//CHD  isa   C0239082(Common Bile Duct Obstruction)
			
		}else if (organSystem.equals("Digestive")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);
			copyTraversedSS(traversed, endocrineSS);
			
		    //UWDA	Mouth	RN	part_of	MSH	C0039493	Joint, Temporomandibular	Body Space or Junction 
			//UWDA	Mouth	RN	part_of	FMA	C0930688	Faucial part of mouth	Body Location or Region 
			blockDescendents(mysqlConn, "C0930688", "Body Location or Region", traversed);
			//UWDA	Mouth	RN	part_of	LCH	C0023759	Lips	Body Part, Organ, or Organ Component 
			blockDescendents(mysqlConn, "C0023759", "Body Part, Organ, or Organ Component", traversed);
			//UWDA	Mouth	RN	part_of	FMA	C0934689	Mandibular part of mouth	Body Location or Region 
			blockDescendents(mysqlConn, "C0934689", "Body Location or Region", traversed);
			//UWDA	Mouth	RN	part_of	FMA	C0930280	Maxillary part of mouth	Body Location or Region 
			blockDescendents(mysqlConn, "C0930280", "Body Location or Region", traversed);
			//UWDA	Mouth	RN	part_of	MSH	C0026639	Mouth Mucosa	Body Part, Organ, or Organ Component 
			blockDescendents(mysqlConn, "C0026639", "Body Part, Organ, or Organ Component", traversed);
			//UWDA	Mouth	RN	part_of	FMA	C0930248	Skin of mouth	Body Part, Organ, or Organ Component 
			blockDescendents(mysqlConn, "C0930248", "Body Part, Organ, or Organ Component", traversed);
			//UWDA	Mouth	RN	part_of	FMA	C0929153	Superficial fascia of mouth	Body Part, Organ, or Organ Component 
			blockDescendents(mysqlConn, "C0929153", "Body Part, Organ, or Organ Component", traversed);
			//UWDA	Mouth	RN	part_of	FMA	C0927199	Set of muscles of mouth	Body Part, Organ, or Organ Component 
			blockDescendents(mysqlConn, "C0927199", "Body Part, Organ, or Organ Component", traversed);
			
			//RO  has_locationDigestive System Disease | C0012242 | Disease or Syndrome
			//CHD  isaInfection of digestive system | C0729555 | Disease or Syndrome
			//CHD  isagastrointestinal infection | C1264613 | Disease or Syndrome
			//CHD  isaDiseases, Gastrointestinal | C0017178 | Disease or Syndrome
			//CHD  isaperitoneal and retroperitoneal disorders (diagnosis) | C0851922 | Disease or Syndrome
			//CHD  isaAbdominal Injuries | C0848377 | Injury or Poisoning
			//RN  isa   C2198679(tissue injury across lower abdomen (physical finding))
			//=====================================
			blockDescendents(mysqlConn, "C0848377", "Injury or Poisoning", traversed);
			
			
			//Digestive System | C0012240 | Body System
			//RN  part_ofMouth | C0230028 | Body Location or Region
			//RN  part_ofMouth | C0226896 | Body Space or Junction
			//CHD  isaOral soft tissues, NOS | C0447424 | Body Part, Organ, or Organ Component
			//CHD  isaLips | C0023759 | Body Part, Organ, or Organ Component
			//RN  isaUpper lip | C0458582 | Body Part, Organ, or Organ Component
			//RN  part_ofSkin of upper lip | C0222102 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteskin plaque of upper lip | C2131491 | Finding
			//CHD  isa   C2131492(skin plaque of right side of upper lip)
			blockDescendents(mysqlConn, "C0023759", "Body Part, Organ, or Organ Component", traversed);
			
			//Digestive System | C0012240 | Body System
			//RO  has_locationDigestive System Disease | C0012242 | Disease or Syndrome
			//CHD  isaInfection of digestive system | C0729555 | Disease or Syndrome
			//CHD  isagastrointestinal infection | C1264613 | Disease or Syndrome
			//CHD  isaDiseases, Gastrointestinal | C0017178 | Disease or Syndrome
			//CHD  isaDisease of liver | C0023895 | Disease or Syndrome
			//CHD  isaHepatomegaly | C0019209 | Sign or Symptom
			//CHD  isaHepatosplenomegaly | C0019214 | Sign or Symptom
			//RN  isa   C0038002(Splenomegaly)
			//PAR	inverse_isa	AIR	C0038002	Splenomegaly	Finding 
			//C0019214	Hepatosplenomegaly	Sign or Symptom
			//traversed.addWord("C0019214	Sign or Symptom");
			//SS.addWord("C0019214");
			//SS.addWord("C0019209"); //Hepatology
			//blockDescendents(mysqlConn, "C0038002", "Finding", traversed);
			blockDescendents(mysqlConn, "C0004604", "Sign or Symptom", traversed); // Back pain
			
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//RO  has_finding_siteDigestive system observation | C0426573 | Finding
			//CHD  isaGI Findings | C1261141 | Finding
			//CHD  isaGastrointestinal symptom NOS | C0426576 | Sign or Symptom
			//CHD  isaAbdominal Pain | C0000737 | Sign or Symptom
			//RQ  mapped_toABDOMINAL PAIN LOWER | C0232495 | Sign or Symptom
			//CHD  isaPelvic Pain | C0030794 | Sign or Symptom
			//CHD  isaGenitourinary pain | C0423703 | Sign or Symptom
			//CHD  isaPain in male genitalia | C0458104 | Sign or Symptom
			//CHD  isaTESTICULAR PAIN | C0039591 | Sign or Symptom
			//CHD  isa   C2129031(testicular pain right)
			blockDescendents(mysqlConn, "C0232495", "Sign or Symptom", traversed);
			//MSH	Abdominal Pain	CHD	isa	RCD	C0567085	Pain of uterus	Disease or Syndrome 
			blockDescendents(mysqlConn, "C0567085", "Disease or Syndrome", traversed);
		 	//MSH	Abdominal Pain	CHD	isa	RCD	C0575448	Lumbar spine - painful on move	Finding 
			blockDescendents(mysqlConn, "C0575448", "Finding", traversed);
			
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//RO  has_finding_siteDigestive system observation | C0426573 | Finding
			//CHD  isaGI Findings | C1261141 | Finding
			//CHD  isaGastrointestinal symptom NOS | C0426576 | Sign or Symptom
			//CHD  isa   C0033775(Pruritus Ani)
			blockDescendents(mysqlConn, "C0033775", "Sign or Symptom", traversed);
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//CHD  isaGastrointestinal tract | C0017189 | Body System
			//RO  systemic_part_ofPeritoneal sac | C0504217 | Body Part, Organ, or Organ Component
			//RN  part_ofCavity, Peritoneal | C1704247 | Body Space or Junction
			//CHD  isaRegion of peritoneal cavity | C0446603 | Body Location or Region
			//CHD  isaPelvic region of peritoneum | C0459689 | Body Location or Region
			//RN  part_ofSkin of part of genitalia | C0559556 | Body Part, Organ, or Organ Component
			//CHD  isaSkin part male ext genitalia | C0559557 | Body Part, Organ, or Organ Component
			//CHD  isaSkin of penis | C0222193 | Body Part, Organ, or Organ Component
			//RN  part_ofForeskin | C0227952 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteDisorders of prepuce | C2903046 | Disease or Syndrome
			//CHD  isa   C0426339(Foreskin deficient)
			blockDescendents(mysqlConn, "C0446603", "Body Location or Region", traversed);
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//CHD  isaGastrointestinal tract | C0017189 | Body System
			//RO  systemic_part_ofPeritoneal sac | C0504217 | Body Part, Organ, or Organ Component
			//RN  part_ofCavity, Peritoneal | C1704247 | Body Space or Junction
			//CHD  isaRegion of peritoneal cavity | C0446603 | Body Location or Region
			//CHD  isaPelvic region of peritoneum | C0459689 | Body Location or Region
			//RN  part_ofSkin of part of genitalia | C0559556 | Body Part, Organ, or Organ Component
			//CHD  isaSkin part male ext genitalia | C0559557 | Body Part, Organ, or Organ Component
			//CHD  isaSkin of scrotum | C0222198 | Body Part, Organ, or Organ Component
			//RO  has_finding_site   C2026018(cellulitis of right scrotum)
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//CHD  isaGastrointestinal tract | C0017189 | Body System
			//RO  systemic_part_ofPeritoneal sac | C0504217 | Body Part, Organ, or Organ Component
			//RN  part_ofCavity, Peritoneal | C1704247 | Body Space or Junction
			//CHD  isaRegion of peritoneal cavity | C0446603 | Body Location or Region
			//CHD  isaPelvic region of peritoneum | C0459689 | Body Location or Region
			//RN  part_ofSkin of part of genitalia | C0559556 | Body Part, Organ, or Organ Component
			//CHD  isaSkin part male ext genitalia | C0559557 | Body Part, Organ, or Organ Component
			//CHD  isaSkin of scrotum | C0222198 | Body Part, Organ, or Organ Component
			//RO  has_finding_site   C0406671(Burning scrotum)
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//CHD  isaGastrointestinal tract | C0017189 | Body System
			//RO  systemic_part_ofPeritoneal sac | C0504217 | Body Part, Organ, or Organ Component
			//RN  part_ofCavity, Peritoneal | C1704247 | Body Space or Junction
			//CHD  isaRegion of peritoneal cavity | C0446603 | Body Location or Region
			//CHD  isaPelvic region of peritoneum | C0459689 | Body Location or Region
			//RN  part_ofSkin of perianal area | C0222180 | Body Part, Organ, or Organ Component
			//RO  has_finding_site   C2009843(furuncle of perianal region (physical finding))
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//CHD  isaGastrointestinal tract | C0017189 | Body System
			//RO  systemic_part_ofPeritoneal sac | C0504217 | Body Part, Organ, or Organ Component
			//RN  part_ofCavity, Peritoneal | C1704247 | Body Space or Junction
			//CHD  isaRegion of peritoneal cavity | C0446603 | Body Location or Region
			//CHD  isaPelvic region of peritoneum | C0459689 | Body Location or Region
			//RN  part_ofSkin of perianal area | C0222180 | Body Part, Organ, or Organ Component
			//RO  has_finding_site   C0581344(Macerated perianal skin)
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//CHD  isaGastrointestinal tract | C0017189 | Body System
			//RO  systemic_part_ofPeritoneal sac | C0504217 | Body Part, Organ, or Organ Component
			//RN  part_ofCavity, Peritoneal | C1704247 | Body Space or Junction
			//CHD  isaRegion of peritoneal cavity | C0446603 | Body Location or Region
			//CHD  isaPelvic region of peritoneum | C0459689 | Body Location or Region
			//RN  part_ofSkin of anogenital region | C0459221 | Body Part, Organ, or Organ Component
			//CHD  isaSkin of part of anogenital region | C0559623 | Body Part, Organ, or Organ Component
			//CHD  isaSkin of external genitalia | C0222177 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteincision of genitalia (physical finding) | C2010996 | Finding
			//CHD  isaedges of incision of genitalia (physical finding) | C2011010 | Finding
			//CHD  isaincision of genitalia with approximation of postsuture edges | C2011022 | Finding
			//CHD  isa   C2010927(incision of genitalia with well approximated postsuture edges)
			
			
			/*Digestive System | C0012240 | Body System
				RO  has_locationDigestive System Disease | C0012242 | Disease or Syndrome
				CHD  isaInfection of digestive system | C0729555 | Disease or Syndrome
				CHD  isagastrointestinal infection | C1264613 | Disease or Syndrome
				CHD  isaDiseases, Gastrointestinal | C0017178 | Disease or Syndrome
				CHD  isaGastrointestinal fistula, NOS | C0341212 | Disease or Syndrome
				CHD  isaFistula, Intestinal | C0021833 | Anatomical Abnormality
				CHD  isaColonic fistula | C0341365 | Disease or Syndrome
				RN  isa   C2237753(barium enema fistulae of splenic flexure (procedure))
				=====================================
				Digestive System | C0012240 | Body System
				RO  has_locationDigestive System Disease | C0012242 | Disease or Syndrome
				CHD  isaInfection of digestive system | C0729555 | Disease or Syndrome
				CHD  isagastrointestinal infection | C1264613 | Disease or Syndrome
				CHD  isaDiseases, Gastrointestinal | C0017178 | Disease or Syndrome
				CHD  isadisorder of jejunum and ileum | C2103077 | Disease or Syndrome
				CHD  isaMalnutrition syndrome | C0162429 | Disease or Syndrome
				CHD  isaDeficiency of micronutrients | C0342917 | Pathologic Function
				CHD  isaMineral deficiency | C0687148 | Disease or Syndrome
				CHD  isaTrace element deficiency | C0342924 | Pathologic Function
				CHD  isaIodine deficiency | C0342199 | Disease or Syndrome
				CHD  isaSimple goiter | C0018022 | Disease or Syndrome
				CHD  isaUnspecified nontoxic nodular goiter | C1318500 | Disease or Syndrome
				CHD  isaNontoxic uninodular goiter | C0342115 | Disease or Syndrome
				CHD  isaNodule, Thyroid | C0040137 | Neoplastic Process
				RN  isathyroid nodule | C2116082 | Finding
				CHD  isasoft nodule of thyroid (physical finding) | C2116159 | Finding
				CHD  isa   C2116160(soft nodule of left lobe of thyroid (physical finding))
				=====================================
				Digestive System | C0012240 | Body System
				RO  has_locationDigestive System Disease | C0012242 | Disease or Syndrome
				CHD  isaInfection of digestive system | C0729555 | Disease or Syndrome
				CHD  isagastrointestinal infection | C1264613 | Disease or Syndrome
				CHD  isaDiseases, Gastrointestinal | C0017178 | Disease or Syndrome
				CHD  isadisorder of jejunum and ileum | C2103077 | Disease or Syndrome
				CHD  isaMalnutrition syndrome | C0162429 | Disease or Syndrome
				CHD  isaDeficiency of micronutrients | C0342917 | Pathologic Function
				CHD  isaMineral deficiency | C0687148 | Disease or Syndrome
				CHD  isaTrace element deficiency | C0342924 | Pathologic Function
				CHD  isaIodine deficiency | C0342199 | Disease or Syndrome
				CHD  isaSimple goiter | C0018022 | Disease or Syndrome
				CHD  isaUnspecified nontoxic nodular goiter | C1318500 | Disease or Syndrome
				CHD  isaNontoxic uninodular goiter | C0342115 | Disease or Syndrome
				CHD  isaNodule, Thyroid | C0040137 | Neoplastic Process
				RN  isathyroid nodule | C2116082 | Finding
				CHD  isa   C2116025(fixed nodule of thyroid (physical finding))
				=====================================
				*/
			
			//Pelvic swelling, NOS | C0347943 | Finding;
			blockDescendents(mysqlConn, "C0347943", "Finding", traversed);
			
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//RO  has_finding_siteReflux, NOS | C0232483 | Pathologic Function
			//CHD  isaVesico-ureteric reflux | C0042580 | Disease or Syndrome
			//CHD  isaPrimary vesicoureteric reflux | C0403622 | Disease or Syndrome
			//CHD  isaCongenital vesico-ureteric reflux | C0521554 | Congenital Abnormality
			//CHD  isa   C3494819(Primary left vesicoureteral reflux (disorder))
			blockDescendents(mysqlConn, "C0042580", "Disease or Syndrome", traversed);
			
			
			//RO  has_locationDigestive System Disease | C0012242 | Disease or Syndrome
			//CHD  isaInfection of digestive system | C0729555 | Disease or Syndrome
			//CHD  isagastrointestinal infection | C1264613 | Disease or Syndrome
			//CHD  isaDiseases, Gastrointestinal | C0017178 | Disease or Syndrome
			//CHD  isaperitoneal and retroperitoneal disorders (diagnosis) | C0851922 | Disease or Syndrome
			//CHD  isaHerniated tissue | C0019270 | Anatomical Abnormality
			//CHD  isaAbdominal Hernia | C0178282 | Anatomical Abnormality
			//CHD  isaO/E - hernia | C0555732 | Finding
			//CHD  isa   C0437008(O/E - reducible hernia)
			blockDescendents(mysqlConn, "C0019270", "Anatomical Abnormality", traversed);
			//=====================================
			//Digestive System | C0012240 | Body System
			//RO  has_locationDigestive System Disease | C0012242 | Disease or Syndrome
			//CHD  isaInfection of digestive system | C0729555 | Disease or Syndrome
			//CHD  isagastrointestinal infection | C1264613 | Disease or Syndrome
			//CHD  isaDiseases, Gastrointestinal | C0017178 | Disease or Syndrome
			//CHD  isaperitoneal and retroperitoneal disorders (diagnosis) | C0851922 | Disease or Syndrome
			//CHD  isaDisease of peritoneum | C0031142 | Disease or Syndrome
			//CHD  isaAbdominal Pregnancies | C0032984 | Disease or Syndrome
			//CHD  isaViable fetus in abdominal preg | C0404869 | Disease or Syndrome
			//CHD  isa   C0451792(Deliv viab fetus in abdom preg)
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofMouth | C0230028 | Body Location or Region
			//RN  part_ofMouth | C0226896 | Body Space or Junction
			//RO  has_finding_siteMouth observations | C0455788 | Finding
			//CHD  isaOral cavity problem | C0576978 | Finding
			//RQ  isa   C0747047(ORAL MASS)
			blockDescendents(mysqlConn, "C0226896", "Body Space or Junction", traversed);
			//=====================================
			
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofOropharynx | C0521367 | Body Location or Region
			//CHD  isaWall of oropharynx | C0926831 | Body Part, Organ, or Organ Component
			//CHD  isaLateral wall of oropharynx | C0227156 | Body Location or Region
			//CHD  isaEntire lateral wall of oropharynx (body structure) | C1284403 | Body Location or Region
			//RN  part_ofPalatine Tonsil | C0040421 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteTonsil observations | C0426506 | Finding
			//CHD  isaObs of appearance of tonsil | C0577000 | Finding
			//CHD  isaObservation of size of tonsil | C0578162 | Finding
			//CHD  isa   C0455901(Small tonsils)
			blockDescendents(mysqlConn, "C0521367", "Body Location or Region", traversed);
			//=====================================
			//Digestive System | C0012240 | Body System
			//RN  part_ofDigestive System | C0012240 | Body System
			//RO  has_finding_siteDigestive system observation | C0426573 | Finding
			//CHD  isaGI Findings | C1261141 | Finding
			//CHD  isaGastrointestinal symptom NOS | C0426576 | Sign or Symptom
			//CHD  isaOther symptoms involving abdomen and pelvis | C0159065 | Sign or Symptom
			//RN  mapped_toNoises in abdomen | C0426625 | Finding
			//CHD  isaABDOMINAL BRUIT | C0221755 | Sign or Symptom
			//CHD  isaRENAL ARTERY BRUIT | C0748263 | Finding
			//CHD  isa   C2170166(renal artery bruit heard in left flank (physical finding))
			blockDescendents(mysqlConn, "C0221755", "Sign or Symptom", traversed);			
			
			//Eating feeding drinking obs | C0559899 | Finding
			//CHD  isaEating observations | C0558129 | Finding
			//CHD  isaObservation of eating pattern | C0566554 | Finding
			//CHD  isaObs food aversion+cravings | C0566561 | Finding
			//CHD  isaCraving for non-food item | C0558173 | Finding
			//RN  isa   C2138383(craving cocaine)
			
		} else if(organSystem.equals("Urinary")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			
			traversed.addWord("C0014394 Sign or Symptom");
			blockDescendents(mysqlConn, "C0014394", "Sign or Symptom", traversed); 
			//Urinary system, NOS | C1508753 | Body System
			//RN  part_ofLower urinary tract | C0729866 | Body System
			//RO  has_finding_siteLower urinary tract finding (finding) | C1291662 | Finding
			//CHD  isaMicturition observations | C0429837 | Finding
			//CHD  isa   C0437454(O/E - micturition reflex)
			//traversed.addWord("C0437454 Finding");
			//blockDescendents(mysqlConn, "C0437454", "Finding", traversed);
			//=====================================
			//Urinary system, NOS | C1508753 | Body System
			//RO  has_finding_siteDisease, Urologic | C0042075 | Disease or Syndrome
			//CHD  isaDisease, Male Genital | C0017412 | Disease or Syndrome
			//CHD  isaEdema of male genital organs | C0156317 | Sign or Symptom
			//CHD  isa   C0156308(Edema of penis)
			traversed.addWord("C0156317 Sign or Symptom");
			blockDescendents(mysqlConn, "C0156317", "Sign or Symptom", traversed); 
			
			//Urinary system, NOS | C1508753 | Body System
			//RN  part_ofUpper urinary tract | C0729865 | Body Part, Organ, or Organ Component
			//RN  part_ofKidney | C0022646 | Body Part, Organ, or Organ Component
			//RO  has_locationKidney disease | C0022658 | Disease or Syndrome
			//CHD  isaVascular disorders of kidney | C0268790 | Disease or Syndrome
			//CHD  isaRenal artery embolism | C0341708 | Pathologic Function
			//CHD  isaThromboembolism of renal arteries | C0268791 | Disease or Syndrome
			//RN  isaEMBOLISM ARTERIAL | C0549124 | Pathologic Function
			//RN  isachest computerized tomography arterial embolus | C2046122 | Finding
			//CHD  isa   C2046169(chest computerized tomography embolus of left pulmonary artery)
			blockDescendents(mysqlConn, "C0549124", "Pathologic Function", traversed);
			//=====================================
			//Urinary system, NOS | C1508753 | Body System
			//RO  has_finding_siteUninary function finding | C1291658 | Finding
			//CHD  isaUrinary symptoms | C0426359 | Sign or Symptom
			//CHD  isaperineal symptoms | C2053301 | Sign or Symptom
			//CHD  isa[D] Perineal pain | C0240717 | Sign or Symptom
			//CHD  isaAnal pain | C0238637 | Sign or Symptom
			//CHD  isa   C2126223(constant anal pain (symptom))
			traversed.addWord("C0238637 Sign or Symptom");
			blockDescendents(mysqlConn, "C0240717", "Sign or Symptom", traversed);
			
			//Urinary system, NOS | C1508753 | Body System
			//RN  part_ofBladder | C0005682 | Body Part, Organ, or Organ Component
			//RO  has_locationBladder Disease | C0005686 | Disease or Syndrome
			//CHD  isaCystocele | C1394494 | Disease or Syndrome
			//CHD  isa   C2012463(grade of cystocele (1-4))
			//=====================================
			//Urinary system, NOS | C1508753 | Body System
			//RN  part_ofLower urinary tract | C0729866 | Body System
			//RO  has_finding_siteLower urinary tract finding (finding) | C1291662 | Finding
			//CHD  isaDysuria | C0013428 | Sign or Symptom
			//CHD  isa   C2921780(pain during urination (dysuria) seems related to menstrual cycle)
			//=====================================
			//Urinary system, NOS | C1508753 | Body System
			//RN  part_ofUpper urinary tract | C0729865 | Body Part, Organ, or Organ Component
			//RN  part_ofKidney | C0022646 | Body Part, Organ, or Organ Component
			//RO  has_locationHypertensive renal disease | C0848548 | Disease or Syndrome
			//CHD  isaObst.renal hypert.unspecified | C0341914 | Disease or Syndrome
			//RN  isapregnancy complicated by hypertension secondary to renal disease (diagnosis) | C2063016 | Finding
			//CHD  isa   C2047547(hypertension secondary to renal disease as complication in pregnancy postpartum condition or prior complication delivered)
			//=====================================
			//traversed.addWord("C0156648 Disease or Syndrome");//mapped_from	ICD9CM	C0156648	Renal hyperten preg-unsp	Disease or Syndrome 
			blockDescendents(mysqlConn, "C1314753", "Pathologic Function", traversed);
			
			//Urinary system, NOS | C1508753 | Body System
			//RO  has_finding_siteDisease, Urologic | C0042075 | Disease or Syndrome
			//CHD  isaDisease, Male Genital | C0017412 | Disease or Syndrome
			//CHD  isaDisease, Penile | C0030846 | Disease or Syndrome
			//CHD  isa   C0541940(Decreased erection of penis)
			blockDescendents(mysqlConn, "C0017412", "Disease or Syndrome", traversed);
			
			//Urinary system, NOS | C1508753 | Body System
			//RO  has_finding_siteUninary function finding | C1291658 | Finding
			//CHD  isaUrinary symptoms | C0426359 | Sign or Symptom
			//CHD  isagroin symptoms (symptom) | C2012610 | Sign or Symptom
			//CHD  isaPAIN GROIN | C0239783 | Sign or Symptom
			//CHD  isa   C2012602(swelling of lymph nodes in both sides of groin)
			blockDescendents(mysqlConn, "C0239783", "Sign or Symptom", traversed);
			
	    } else  if (organSystem.equals("Reproductive")){
	    	
			//RN  part_of|
			//Genital system | C0559522 | Body System
			//RN  isa|
			//Female genital system | C0700038 | Body System
			//RO  anatomic_structure_is_physical_part_of|
			//Female breast | C0222603 | Body Part, Organ, or Organ Component
			//RN  part_of|
			//Skin of breast | C0149538 | Body Part, Organ, or Organ Component
			//RN  part_of|
			//Skin of breast proper | C0929365 | Body Part, Organ, or Organ Component
			//******************************traversed.addWord("C0149538 Body Part, Organ, or Organ Component");
			//blockDescendents(mysqlConn, "C0231052", "Embryonic Structure", traversed); 
			//blockDescendents(mysqlConn, "C0231053", "Body Part, Organ, or Organ Component", traversed);			
			//blockDescendents(mysqlConn, "C0149538", "Body Part, Organ, or Organ Component", traversed); 
	    	copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			copyTraversedSS(traversed, urinarySS);
			
			//MSH	Pelvic Pain	CHD	isa	ICPC2P	C0009193	Coccygodynia	Sign or Symptom 
		 	//MSH	Pelvic Pain	CHD	isa	CCPSS	C0019559	HIP PAIN	Sign or Symptom 
		 	//MSH	Pelvic Pain	CHD	isa	MTH	C0034886	RECTUM PAIN	Sign or Symptom 
			blockDescendents(mysqlConn, "C0009193", "Sign or Symptom", traversed);
			traversed.addWord("C0009193 Sign or Symptom");
			blockDescendents(mysqlConn, "C0019559", "Sign or Symptom", traversed);
			traversed.addWord("C0019559 Sign or Symptom");
			blockDescendents(mysqlConn, "C0034886", "Sign or Symptom", traversed);
			traversed.addWord("C0034886 Sign or Symptom");
			
			//reproductive system | C1261210 | Body System
			//CHD  isaMale genital system | C1963704 | Body System
			//RN  part_ofMale internal genitalia, NOS | C0227923 | Body Part, Organ, or Organ Component
			//RN  part_ofProstate | C0033572 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteProstate observation | C0426730 | Finding
			//CHD  isaProstate swelling | C0577256 | Sign or Symptom
			//CHD  isa   C0437016(O/E - PR - prostatic swelling)
			
			//reproductive system | C1261210 | Body System
			//CHD  isaMale genital system | C1963704 | Body System
			//RO  anatomic_structure_is_physical_part_ofGenitalia, Male | C0017422 | Body Part, Organ, or Organ Component
			//RO  has_finding_sitemale reproductive system disorder | C0236099 | Disease or Syndrome
			//CHD  isaMale Non-Neoplastic Reproductive System Disease | C1334554 | Disease or Syndrome
			//CHD  isaInhibited male orgasm | C0033949 | Mental or Behavioral Dysfunction
			//CHD  isaMale erectile disorder | C0242350 | Disease or Syndrome
			//CHD  isa   C0234016(Impotence, psychogenic)
			blockDescendents(mysqlConn, "C0033949", "Mental or Behavioral Dysfunction", traversed);
			traversed.addWord("C0033949 Mental or Behavioral Dysfunction");
			
			//reproductive system | C1261210 | Body System
			//CHD  isaFemale genital system | C0700038 | Body System
			//RN  part_ofFemale internal genitalia | C0227748 | Body Part, Organ, or Organ Component
			//RN  part_ofUterus | C0042149 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteDisease, Uterine | C0042131 | Disease or Syndrome
			//CHD  isaPAIN PELVIC | C0850758 | Sign or Symptom
			//RQ  mapped_to   C0235808(PERINEAL PAIN MALE)
			///=====================================
			//reproductive system | C1261210 | Body System
			//CHD  isaFemale genital system | C0700038 | Body System
			//RN  part_ofFemale internal genitalia | C0227748 | Body Part, Organ, or Organ Component
			//RN  part_ofUterus | C0042149 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteDisease, Uterine | C0042131 | Disease or Syndrome
			//CHD  isaVaginismus | C2004487 | Finding
			//CHD  isa   C0042266(Vaginismus)
			//=====================================
			//reproductive system | C1261210 | Body System
			//CHD  isaFemale genital system | C0700038 | Body System
			//RN  part_ofFemale internal genitalia | C0227748 | Body Part, Organ, or Organ Component
			//RN  part_ofUterus | C0042149 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteDisease, Uterine | C0042131 | Disease or Syndrome
			//CHD  isaMenorrhalgia | C0013390 | Pathologic Function
			//CHD  isa   C0154555(Psychogenic dysmenorrhea)
			blockDescendents(mysqlConn, "C0154555", "Mental or Behavioral Dysfunction", traversed);
			traversed.addWord("C0154555 Mental or Behavioral Dysfunction");
			//=====================================
			//reproductive system | C1261210 | Body System
			//CHD  isaFemale genital system | C0700038 | Body System
			//RO  anatomic_structure_is_physical_part_ofFemale Genitalia | C0017421 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteFemale genital organ sympt.NOS | C0425816 | Sign or Symptom
			//CHD  isaPelvic Pain | C0030794 | Sign or Symptom
			//CHD  isaGenitourinary pain | C0423703 | Sign or Symptom
			//CHD  isaUrinary tract pain | C0423701 | Sign or Symptom
			//CHD  isaBLADDER PAIN | C0232849 | Sign or Symptom
			//CHD  isaTenesmus, urinary | C0423735 | Sign or Symptom
			//CHD  isa   C0232858(Dysuria, spastic)
			blockDescendents(mysqlConn, "C0423703", "Sign or Symptom", traversed);
			traversed.addWord("C0423703 Sign or Symptom");
			//=====================================
			//reproductive system | C1261210 | Body System
			//CHD  isaFemale genital system | C0700038 | Body System
			//RO  anatomic_structure_is_physical_part_ofFemale Genitalia | C0017421 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteFemale genital organ sympt.NOS | C0425816 | Sign or Symptom
			//CHD  isaPelvic Pa,,in | C0030794 | Sign or Symptom
			//CHD  isa[D] Perineal pain | C0240717 | Sign or Symptom
			//CHD  isa   C0238637(Anal pain)
			traversed.addWord("C0238637 Sign or Symptom");
			blockDescendents(mysqlConn, "C0240717", "Sign or Symptom", traversed);
			
		}else if (organSystem.equals("Genitourinary")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			copyTraversedSS(traversed, urinarySS);
			copyTraversedSS(traversed, reproductiveSS);
			
			//enuresis
			traversed.addWord("C0014394 Sign or Symptom");
			blockDescendents(mysqlConn, "C0014394", "Sign or Symptom", traversed); 
			
			//Genitourinary tract | C0042066 | Body System
			//RO  has_finding_siteUROGENITAL DISORDER | C0080276 | Disease or Syndrome
			//CHD  isaDisease, Urologic | C0042075 | Disease or Syndrome
			//CHD  isaKidney &/or ureter disorder NOS (disorder) | C1971734 | Disease or Syndrome
			//CHD  isaKidney disease | C0022658 | Disease or Syndrome
			//CHD  isaVascular disorders of kidney | C0268790 | Disease or Syndrome
			//CHD  isaRenal artery embolism | C0341708 | Pathologic Function
			//CHD  isaThromboembolism of renal arteries | C0268791 | Disease or Syndrome
			//RN  isaEMBOLISM ARTERIAL | C0549124 | Pathologic Function
			//CHD  isaTrunk arterial embolus | C0729946 | Acquired Abnormality
			//CHD  isaEmbolism, Pulmonary | C0034065 | Disease or Syndrome
			//RN  isacath pulmonary artery embolus right branch | C2921593 | Finding
			//CHD  isa   C2921595(cath pulmonary artery embolus right branch middle lobe (procedure))
			
			blockDescendents(mysqlConn, "C0549124", "Pathologic Function", traversed); 
			
			//Genitourinary tract | C0042066 | Body System
			//RO  has_finding_siteUROGENITAL DISORDER | C0080276 | Disease or Syndrome
			//CHD  isareproductive system disorder | C0178829 | Disease or Syndrome
			//CHD  isafemale reproductive system disorder | C0236100 | Disease or Syndrome
			//CHD  isaPREGNANCY DISORDER | C0151864 | Disease or Syndrome
			//RN  isaObstetrical Complication | C0564778 | Disease or Syndrome
			//CHD  isaDevelopmental malformation | C0000768 | Congenital Abnormality
			//CHD  isaCong abnorm low limb+pelv gird | C0456309 | Congenital Abnormality
			//CHD  isaABDOMEN ANOMALY CONGENITAL | C0740500 | Congenital Abnormality
			//CHD  isaAnomalies of spleen, congenital | C0700587 | Congenital Abnormality
			//CHD  isaCongenital malposition of spleen | C0685887 | Congenital Abnormality
			//CHD  isa   C3163752(Spleen in right sided position (disorder))
			
			blockDescendents(mysqlConn, "C0000768", "Congenital Abnormality", traversed); 
			
			//CHD  isaGenitourinary system | C1280973 | Body System
			//RN  part_ofFemale perineal structure | C0458947 | Body Location or Region
			//RN  part_ofAnal region | C1275631 | Body Location or Region
			//RN  part_ofAnal canal | C0227411 | Body Part, Organ, or Organ Component
			//RN  part_ofLumen of anal canal | C0736093 | Body Space or Junction
			//RN  part_ofAnus | C0003461 | Body Part, Organ, or Organ Component
			//RO  has_locationAnus | C0003462 | Disease or Syndrome
			//CHD  isaAnal injury | C2226910 | Injury or Poisoning
			//CHD  isadepth of wound of anus (physical finding) | C2235491 | Finding
			//CHD  isa   C2008912(full-thickness anal wound)
			blockDescendents(mysqlConn, "C1275631", "Body Location or Region", traversed);
					
			//Genitourinary tract | C0042066 | Body System
			//CHD  isaGenitourinary system | C1280973 | Body System
			//RN  part_ofPRODUCTS OF CONCEPTION AND EMBRYONIC STRUCTURES | C0230953 | Embryonic Structure
			//CHD  isaEmbryo | C1305370 | Embryonic Structure
			//RN  part_ofTrilaminar embryonic disc (body structure) | C1284022 | Embryonic Structure
			//RO  anatomic_structure_is_physical_part_ofEndoderm | C0014144 | Embryonic Structure
			//CHD  isaEntire endoderm (body structure) | C1285063 | Embryonic Structure
			//RN  part_ofMidgut | C0231052 | Embryonic Structure
			//RN  part_ofCecum | C0007531 | Body Part, Organ, or Organ Component
			//RO  has_location   C0940519(Stool in the colonic lumen)
			//=====================================
			//Genitourinary tract | C0042066 | Body System
			//CHD  isaGenitourinary system | C1280973 | Body System
			//RN  part_ofPRODUCTS OF CONCEPTION AND EMBRYONIC STRUCTURES | C0230953 | Embryonic Structure
			//CHD  isaEmbryo | C1305370 | Embryonic Structure
			//RN  part_ofTrilaminar embryonic disc (body structure) | C1284022 | Embryonic Structure
			//RO  anatomic_structure_is_physical_part_ofEndoderm | C0014144 | Embryonic Structure
			//CHD  isaEntire endoderm (body structure) | C1285063 | Embryonic Structure
			//RN  part_ofMidgut | C0231052 | Embryonic Structure
			//RN  part_ofIleum | C0020885 | Body Part, Organ, or Organ Component
			//CHD  isaIleostomy - stoma | C0740114 | Acquired Abnormality
			//RO  has_finding_siteileostomy present (physical finding) | C1281553 | Finding
			//CHD  isa   C2047913(ileostomy bag with intestinal content)
			//=====================================
			//Genitourinary tract | C0042066 | Body System
			//CHD  isaGenitourinary system | C1280973 | Body System
			//RN  part_ofPRODUCTS OF CONCEPTION AND EMBRYONIC STRUCTURES | C0230953 | Embryonic Structure
			//CHD  isaEmbryo | C1305370 | Embryonic Structure
			//RN  part_ofTrilaminar embryonic disc (body structure) | C1284022 | Embryonic Structure
			//RO  anatomic_structure_is_physical_part_ofEndoderm | C0014144 | Embryonic Structure
			//CHD  isaEntire endoderm (body structure) | C1285063 | Embryonic Structure
			//RN  part_ofHindgut | C0231053 | Body Part, Organ, or Organ Component
			//RN  part_ofRectum | C0034896 | Body Part, Organ, or Organ Component
			//RO  has_finding_siteRECTAL MASS | C0240873 | Finding
			//CHD  isa   C0424855(Pressure indents rectal mass)
		
			blockDescendents(mysqlConn, "C0231052", "Embryonic Structure", traversed); 
			blockDescendents(mysqlConn, "C0231053", "Body Part, Organ, or Organ Component", traversed); 
			blockDescendents(mysqlConn, "C0149538", "Body Part, Organ, or Organ Component", traversed); 
			
			//Genitourinary tract | C0042066 | Body System
			//CHD  isaGenitourinary system | C1280973 | Body System
			//RN  part_ofPRODUCTS OF CONCEPTION AND EMBRYONIC STRUCTURES | C0230953 | Embryonic Structure
			//CHD  isaEmbryo | C1305370 | Embryonic Structure
			//RN  part_ofEmbryonic blood vessel | C0586331 | Embryonic Structure
			//CHD  isaEmbryonic Arteries | C0586332 | Embryonic Structure
			//CHD  isaArch, Aortic | C0003489 | Body Part, Organ, or Organ Component
			//RO  has_finding_site   C2007123(catheterization aorta aneurysm aortic arch)
			blockDescendents(mysqlConn, "C1305370", "Embryonic Structure", traversed);
			
			//Female perineal structure | C0458947 | Body Location or Region
			//RN  part_of
			//Anal region | C1275631 | Body Location or Region
			//******************************traversed.addWord("C1275631 Body Location or Region");
			
			//CHD  isa
			//Entire endoderm (body structure) | C1285063 | Embryonic Structure
			//RN  part_of
			//******************************traversed.addWord("C0231052 Embryonic Structure");//Midgut | C0231052 | Embryonic Structure
			
			//CHD  isa
			//Kidney disease | C0022658 | Disease or Syndrome
			//CHD  isa
			//******************************traversed.addWord("C0268790 Disease or Syndrome");//Vascular disorders of kidney | 
			
			//RN  part_of|
			//Genital system | C0559522 | Body System
			//RN  isa|
			//Female genital system | C0700038 | Body System
			//RO  anatomic_structure_is_physical_part_of|
			//Female breast | C0222603 | Body Part, Organ, or Organ Component
			//RN  part_of|
			//Skin of breast | C0149538 | Body Part, Organ, or Organ Component
			//RN  part_of|
			//Skin of breast proper | C0929365 | Body Part, Organ, or Organ Component
			//******************************traversed.addWord("C0149538 Body Part, Organ, or Organ Component");	
		}else if (organSystem.equals("Lymphatic-Immune")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			copyTraversedSS(traversed, urinarySS);
			copyTraversedSS(traversed, reproductiveSS);
			copyTraversedSS(traversed, genitourinarySS);
			
		}else if (organSystem.equals("Cardiovascular")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			copyTraversedSS(traversed, urinarySS);
			copyTraversedSS(traversed, reproductiveSS);
			copyTraversedSS(traversed, genitourinarySS);
			copyTraversedSS(traversed, lymphaticSS);
			
			traversed.addWord("C0476280 Sign or Symptom");
			blockDescendents(mysqlConn, "C0476280", "Sign or Symptom", traversed);
	    	
			//Cardiovascular System | C0007226 | Body System
			//RO  has_locationDisease or syndrome of cardiovascular system | C0007222 | Disease or Syndrome
			//CHD  isaConduction disorders | C0264886 | Disease or Syndrome
			//CHD  isa   C0003813(Arrhythmia, Sinus)
			
			//=====================================
			//Cardiovascular System | C0007226 | Body System
			//RO  has_locationDisease or syndrome of cardiovascular system | C0007222 | Disease or Syndrome
			//CHD  isaVASCULAR PROBLEM | C0042373 | Disease or Syndrome
			//CHD  isaVEIN DISORDER | C0235522 | Disease or Syndrome
			//CHD  isavein occlusion | C0241669 | Pathologic Function
			//CHD  isa   C3532304(Obstruction of pulmonary great vein due to compression by right atrial dilatation (disorder))
			blockDescendents(mysqlConn, "C0241669", "Pathologic Function", traversed);
			//CHD  isaVASCULAR PROBLEM | C0042373 | Disease or Syndrome
			//CHD  isaEdemas | C0013604 | Pathologic Function
			//CHD  isaEdema, peripheral | C0085649 | Finding
			//CHD  isaPEDAL EDEMA | C0574002 | Sign or Symptom
			//CHD  isaswelling of right foot (physical finding) | C2201700 | Finding
			//CHD  isaswelling over metatarsal of right foot (physical finding) | C2177917 | Finding
			//CHD  isa   C2177918(swelling over first metatarsal of right foot (physical finding))
			//blockDescendents(mysqlConn, "C0013604", "Pathologic Function", traversed);
			
			//Cardiovascular System | C0007226 | Body System
			//RO  has_locationHigh blood pressure | C0020538 | Disease or Syndrome
			//CHD  isaHypertension, Pulmonary | C0020542 | Disease or Syndrome
			//CHD  isaHypertensive pulmonary venous disease | C0264933 | Disease or Syndrome
			//CHD  isaPulmonary venous hypertension due to compression of pulmonary great vein | C3532294 | Finding
			//CHD  isa   C3532297(Pulmonary venous hypertension due to compression of pulmonary great vein by neoplasm (disorder))
			blockDescendents(mysqlConn, "C0020542", "Disease or Syndrome", traversed);
			
			//Cardiovascular System | C0007226 | Body System
			//RO  has_locationDisease or syndrome of cardiovascular system | C0007222 | Disease or Syndrome
			//CHD  isaCoronary Artery Disease | C1956346 | Disease or Syndrome
			//CHD  isaChest Pain | C0008031 | Sign or Symptom
			//CHD  isa   C0476281(Non-cardiac chest pain)
			//=====================================
			blockDescendents(mysqlConn, "C0476281", "Sign or Symptom", traversed);
			traversed.addWord("C0476281 Sign or Symptom");
			
			//RO  has_locationDisease or syndrome of cardiovascular system | C0007222 | Disease or Syndrome
			//CHD  isaVASCULAR PROBLEM | C0042373 | Disease or Syndrome
			//RQ  isaARTERIAL OCCLUSION | C0264995 | Pathologic Function
			//CHD  isaOcclusion of cerebral arteries | C0028790 | Acquired Abnormality
			//CHD  isaMIS - Multiple infarct state | C0393962 | Disease or Syndrome
			//CHD  isaDementia, Multi Infarct | C0011263 | Disease or Syndrome
			//CHD  isaVascular Dementia, With Depressed Mood | C0236653 | Mental or Behavioral Dysfunction
			//RN  mapped_to   C0154318(Arteriosclerotic dementia with depressive features)
			blockDescendents(mysqlConn, "C0028790", "Acquired Abnormality", traversed);
			
	    }else if (organSystem.equals("Respiratory")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			copyTraversedSS(traversed, urinarySS);
			copyTraversedSS(traversed, reproductiveSS);
			copyTraversedSS(traversed, genitourinarySS);
			copyTraversedSS(traversed, lymphaticSS);
			copyTraversedSS(traversed, cardiovascularSS);
			
			//CHD  isadisorder of mediastinum, diaphragm, or chest wall (diagnosis) | C2103598 | Disease or Syndrome
			//CHD  isaDisease, Mediastinal | C0025061 | Disease or Syndrome
			//CHD  isaDisease or syndrome of heart | C0018799 | Disease or Syndrome
			//CHD  isaVALVULAR HEART DISEASE | C0018824 | Disease or Syndrome
			//CHD  isaDiseases of tricuspid valve | C0264882 | Disease or Syndrome
			//CHD  isaCONGENITAL TRICUSPID VALVE ANOMALY | C0478010 | Congenital Abnormality
			//CHD  isaCongenital abnormality of tricuspid leaflet | C3165185 | Congenital Abnormality
			//CHD  isaCleft leaflet of tricuspid valve | C0344747 | Congenital Abnormality
			//RN  isa   C2023051(the tricuspid valve was cleft)
			
			//blockDescendents(mysqlConn, "C0018799", "Disease or Syndrome", traversed);
			
			
			//Respiratory System | C0035237 | Body System
			//CHD  isa
			//Respiratory system | C1269561 | Body System
			//RN  part_of
			//Mouth | C1278910 | Body Location or Region
			//RN  part_of
			//TEETH, GUMS AND SUPPORTING STRUCTURES | C0702127 | Body Part, Organ, or Organ Component
			//******************************traversed.addWord("C1278910 Body Location or Region");
			
			//RN  part_of
			//Nose | C0028429 | Body Part, Organ, or Organ Component
			//RN  part_of
			//Internal nose | C0225425 | Body Location or Region
			//CHD  isa
			//Nasal Septum | C0027432 | Body Part, Organ, or Organ Component
			//CHD  isa
			//Columella | C0225431 | Body Part, Organ, or Organ Component
			//******************************traversed.addWord("C0028429 Body Part, Organ, or Organ Component");
			
			//CHD  isa
			//Respiratory system | C1269561 | Body System
			//RN  part_of
			//Nose | C1278896 | Body Part, Organ, or Organ Component
			//RN  part_of
			//nose parts | C1268200 | Body Part, Organ, or Organ Component
			//******************************traversed.addWord("C1278896 Body Part, Organ, or Organ Component");
			
			//******************************traversed.addWord("C0025061 Disease or Syndrome");// Mediastinal Disease
			
			//Respiratory observations | C0425442 | Finding
			//CHD  isa
			//Observation of breathing | C0577969 | Finding
			//CHD  isa
			//Finding of respiratory pattern | C1287673 | Finding
			//RN  isa
			//respiration rhythm and depth (physical finding) | C2186942 | Finding
			//CHD  isa
			//Painful respiration | C0423729 | Sign or Symptom
			//RN  mapped_to
			//CHEST WALL PAIN | C0008035 | Sign or Symptom
			//CHD  isa
			//CHEST PAIN SUBSTERNAL | C0151826 | Sign or Symptom
			//RQ  mapped_to
			//Precordial pain | C0232286 | Sign or Symptom
			//******************************traversed.addWord("C0008035 Sign or Symptom");//CHEST WALL PAIN | C0008035 | Sign or Symptom
			
			//RN  part_of
			//Nose, accessory sinus and nasopharynx | C0458578 | Body Part, Organ, or Organ Component
			//RO  has_finding_site
			//ENT symptoms | C0422833 | Sign or Symptom
			//CHD  isa
			//Jaw symptoms/complaints | C2108445 | Sign or Symptom
			//******************************traversed.addWord("C0422833 Sign or Symptom");
			
			//+++traversed.addWord("C0155909 Disease or Syndrome");//Abscess of mediastinum | C0155909 | Disease or Syndrome
			//+++traversed.addWord("C0178272 Disease or Syndrome");//Unspecified disease of pulmonary circulation | C0178272 | Disease or Syndrome
		
			//RO  anatomic_structure_is_physical_part_of
			//Arteries, Pulmonary | C0034052 | Body Part, Organ, or Organ Component
			//RO  has_location
			//Embolism, Pulmonary | C0034065 | Disease or Syndrome
			//+++traversed.addWord("C0034052 Body Part, Organ, or Organ Component");
			
			//	Dyspnea, paroxysmal nocturnal	Sign or Symptom
			//+++traversed.addWord("C1956415 Sign or Symptom"); // descendent from nocturnal dyspnea (C0344357)
			
			//+++traversed.addWord("C0014013 Disease or Syndrome");//Empyema, Pleural | C0014013 | Disease or Syndrome;
		
			//Nasopharynges | C0027442 | Body Part, Organ, or Organ Component
			//RN  part_of
			//Adenoid | C0001428 | Body Part, Organ, or Organ Component
			//+++traversed.addWord("C0001428 Body Part, Organ, or Organ Component");
			
			//CHD  isa
			//Respiratory obs of chest | C0577940 | Finding
			//CHD  isa
			//Finding of chest expansion | C1287660 | Finding
			//CHD  isa   C0577943
			//traversed.addWord("C1287660 Finding");
			//+++traversed.addWord("C0009681 Congenital Abnormality");
			//+++traversed.addWord("C0009681 Disease or Syndrome");
			//Anomalies of pulmonary artery, congenital | C0009681 | Congenital Abnormality
			//+++traversed.addWord("C0265912 Congenital Abnormality");//Anomalous origin of pulmonary artery | C0265912 | Congenital Abnormality
			
			//RO  has_finding_site
			//ENT symptoms | C0422833 | Sign or Symptom
			//CHD  isa
			//Jaw symptoms/complaints | C2108445 | Sign or Symptom
			//+++traversed.addWord("C2108445 Sign or Symptom");
			
			//RO  has_finding_site
			//Nose observation | C1269534 | Finding
			//CHD  isa
			//Sense of smell, absent | C0003126 | Sign or Symptom
			//+++traversed.addWord("C1269534 Finding");
			
			//RO  has_finding_site
			//Nose observation | C1269534 | Finding
			//CHD  isa
			//Observation of nasal deformity | C0577875 | Finding
			//traversed.addWord("C0577875 Finding");
			
			//CHD  isa
			//Injury of nose | C0272427 | Injury or Poisoning
			//CHD  isa
			//Open wound of nose | C0160500 | Injury or Poisoning
			//CHD  isa
			//laceration of nose | C1298677 | Injury or Poisoning
			//+++traversed.addWord("C0272427 Injury or Poisoning");			
			//+++traversed.addWord("C0235560 Disease or Syndrome");//RESPIRATORY TRACT HAEMORRHAGE | C0235560 | Disease or Syndrome
			
			//External nose | C0458561 | Body Location or Region
			//CHD  isa
			//External nose | C1280735 | Body Part, Organ, or Organ Component
			//+++traversed.addWord("C0458561 Body Location or Region");//
			//+++traversed.addWord("C1280735 Body Part, Organ, or Organ Component");//
			//+++traversed.addWord("C0504090 Body Part, Organ, or Organ Component");//Pleural sac | C0504090 | Body Part, Organ, or Organ Component
			//+++traversed.addWord("C0014591 Pathologic Function");//Epistaxis | C0014591 | Pathologic Function
			//traversed.addWord("C2143678 Finding");//purpura on nostril (physical finding) | C2143678 | Finding
			//+++traversed.addWord("C0595944 Body Space or Junction");//Naris | C0595944 | Body Space or Junction		
		    
			//Disease of upper respiratory system, NOS | C0264221 | Injury or Poisoning
			//CHD  isa
			//Disease of pharynx | C0031345 | Disease or Syndrome
            //CHD  isa
			//Disease, Nasopharyngeal | C0027438 | Disease or Syndrome
			//CHD  isa
			//Rhinopharyngitis | C0027441 | Disease or Syndrome
			//CHD  isa
			//+++traversed.addWord("C0001427 Disease or Syndrome");//Adenoiditis | C0001427 | Disease or Syndrome
		}
	    else if (organSystem.equals("Musculoskeletal")){
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			copyTraversedSS(traversed, urinarySS);
			copyTraversedSS(traversed, reproductiveSS);
			copyTraversedSS(traversed, genitourinarySS);
			copyTraversedSS(traversed, lymphaticSS);
			copyTraversedSS(traversed, cardiovascularSS);
			copyTraversedSS(traversed, respiratorySS);
			
	
			blockDescendents(mysqlConn, "C0010263", "Sign or Symptom", SSList);
			SSList.addWord("C0010263 Sign or Symptom");
			//C0154703	Monoplegia of upper limb	Disease or Syndrome 
			blockDescendents(mysqlConn, "C0154703", "Disease or Syndrome", SSList);
		 	//ICPC	Oth dis of neurol system	CHD	isa	ICPC2P	C0037763	Spasm	Sign or Symptom 
			blockDescendents(mysqlConn, "C0037763", "Sign or Symptom", SSList);
			SSList.addWord("C0037763 Sign or Symptom");
			
			
			blockDescendents(mysqlConn, "C0006111", "Disease or Syndrome", traversed);
			
			//blockDescendents(mysqlConn, "C0037274", "Disease or Syndrome", traversed); //Skin disease
			//blockDescendents(mysqlConn, "C1827170", "Finding", traversed); // Edema of extremety
			
			
			//---blockDescendents(mysqlConn, "C0027763","Body System",traversed); // Nervous system  //++++++
			
		} 
	    else if (organSystem.equals("Nervous")){
	    	copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			copyTraversedSS(traversed, urinarySS);
			copyTraversedSS(traversed, reproductiveSS);
			copyTraversedSS(traversed, genitourinarySS);
			copyTraversedSS(traversed, lymphaticSS);
			copyTraversedSS(traversed, cardiovascularSS);
			copyTraversedSS(traversed, respiratorySS);
			copyTraversedSS(traversed, musculoskeletalSS);
			
	    	//Nervous System | C0027763 | Body System
	    	//RN  part_ofCentral Nervous System | C0927232 | Body System
	    	//RO  has_locationCentral Nervous System Diseases | C0007682 | Disease or Syndrome
	    	//CHD  isaBrain | C0006111 | Disease or Syndrome
	    	//CHD  isaOrganic mental disorder NOS | C0029227 | Mental or Behavioral Dysfunction
	    	//---------inverse_isa	DSM3R	C0033882	PSYCHOACTIVE SUBSTANCE USE DISORDERS	Mental or Behavioral Dysfunction 
	    	//CHD  isaPsychoactive Substance-Induced Organic Mental Disorders | C0033883 | Mental or Behavioral Dysfunction
	    	//CHD  isaAlcohol induced organic mental disorder | C0236664 | Mental or Behavioral Dysfunction
	    	//CHD  isa   C0001969(Alcohol intoxication)
	    	blockDescendents(mysqlConn, "C0277579", "Mental or Behavioral Dysfunction", traversed); // Drug-related disorder
	    	
	     	//Nervous System | C0027763 | Body System
	     	//RN  part_ofPeripheral Nervous System | C0206417 | Body System
	     	//RO  has_finding_sitePeripheral nervous system disease or syndrome | C0031117 | Disease or Syndrome
	     	//RN  isadisorders of peripheral nerve, neuromuscular junction and muscle (diagnosis) | C2102996 | Disease or Syndrome
	     	//CHD  isaAtrophies, Muscular | C0026846 | Disease or Syndrome
	     	//RN  isa   C2070880(atrophy of both supraspinatus muscles (physical finding))

		}  else if (organSystem.equals("MentalHealth")){
			//******************************traversed.addWord("C0004623 Disease or Syndrome");
			copyTraversedSS(traversed, generalSS);
			copyTraversedSS(traversed, ENTSS);
			copyTraversedSS(traversed, EYESS);
			copyTraversedSS(traversed, integumentarySS);	
			copyTraversedSS(traversed, digestiveSS);
			copyTraversedSS(traversed, endocrineSS);
			copyTraversedSS(traversed, urinarySS);
			copyTraversedSS(traversed, reproductiveSS);
			copyTraversedSS(traversed, genitourinarySS);
			copyTraversedSS(traversed, lymphaticSS);
			copyTraversedSS(traversed, cardiovascularSS);
			copyTraversedSS(traversed, respiratorySS);
			copyTraversedSS(traversed, musculoskeletalSS);
			copyTraversedSS(traversed, nervousSS);
		}
		
		System.out.println("**********  " + organSystem + "  **************");
		for(int i =0; i < parentCUIs.length; i++){
			Maps(mysqlConn, 0, parentCUIs[i], parentTypes[i], SSList, traversed, queue);
			System.out.println("       PARENT CUI " + parentCUIs[i]);
			System.out.println("       Size of SS + FF  " + (SSList.getSize()));
		}
		
		System.out.println(" TOTAL NUMBER OF SIGNS AND SYMPTOMS ");
		System.out.println("       SSList size : " + SSList.getSize());
		
		printCUIsToFile(mysqlConn, SSList, organSystem+"SSO.txt");
	}
	
	//************* Print CUIs and Descriptions to a file
		public static void printCUIsToFile(MySQLConnection mysqlConn, DistinctLinkedList list, String filename) throws Exception{
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			String CUI;
			for(int i =0; i < list.getSize(); i++){
				CUI = list.getValue(i);
				bw.write(CUI+ "\n");
				//Statement s1 = mysqlConn.createStatement();
				//ResultSet rs1 = mysqlConn.executeQuery(s1, "SELECT STR FROM umls.mrconso where CUI='" + CUI +"'");
				//String symptomDesc ="";
				//if(rs1.next()){
				//	symptomDesc =  rs1.getString("STR");
				//}
				//s1.close();
				//rs1.close();
				//bw.write(symptomDesc +"\n");
			}
			bw.close();
		}


	public static void InsertMapFile(String organSystem, String filename, ArrayList list1, ArrayList list2) throws Exception{
		
		TXTFileReader txtFile = new TXTFileReader(filename);
		int count = 0;
		
		while(txtFile.readMoreLine()){
			count++;
			String line = txtFile.getLine().trim();
			String CUI = line.substring(0,8);
			int i = 0;
			while((i < list1.size()) && list1.get(i).toString().compareTo(CUI)<0){
				i++;
			}
			if(i==list1.size()){
				list1.add(CUI);
				list2.add(organSystem);
			}else if (list1.get(i).toString().compareTo(CUI) == 0){
				String neworgans = list2.get(i).toString()+", "+organSystem;
				list2.remove(i);
				list2.add(i,neworgans);
			}else{
				list1.add(i,CUI);
				list2.add(i,organSystem);
			}	
		}
		System.out.println(count + " Finish file "+ filename);
		txtFile.close();
	}
	
	
	public static void InsertOrganMappings(String organSystem, ArrayList list1, ArrayList list2) throws Exception{	
		InsertMapFile(organSystem, organSystem + "SSO.txt",  list1, list2);
	}
	
	
	public static void clearAndReinsertOrganMappings(MySQLConnection mysqlConn, String column) throws Exception{
		mysqlConn.executeUpdate("UPDATE umls.ltsymptommapping set "+column + "= ''");
		ArrayList list1 = new ArrayList();
		ArrayList list2 = new ArrayList();
		
		InsertOrganMappings("GeneralSymptom", list1, list2);
		InsertOrganMappings("ENT", list1, list2);
		InsertOrganMappings("EYE", list1, list2);
		InsertOrganMappings("Integumentary", list1, list2);
		InsertOrganMappings("Endocrine", list1, list2);
		InsertOrganMappings("Digestive", list1, list2);
		InsertOrganMappings("Urinary", list1, list2);
		InsertOrganMappings("Reproductive", list1, list2);
		InsertOrganMappings("Genitourinary", list1, list2); 
		InsertOrganMappings("Lymphatic-Immune", list1, list2);
		InsertOrganMappings("Cardiovascular", list1, list2);
		InsertOrganMappings("Respiratory", list1, list2);
		InsertOrganMappings("Nervous", list1, list2);
		InsertOrganMappings("Musculoskeletal", list1, list2); 
		InsertOrganMappings("MentalHealth", list1, list2);    
		
		String query;
		for(int i =0; i < list1.size();i++){
			String CUI = list1.get(i).toString();
			String organ = list2.get(i).toString();
			
			Statement s = mysqlConn.createStatement();
			ResultSet rs = mysqlConn.executeQuery(s, "SELECT * FROM umls.ltsymptommapping WHERE CUI='"+CUI+"'");
			if(rs.next()){
				query = "UPDATE umls.ltsymptommapping set "+ column + " = '"+organ+"' WHERE CUI = '"+CUI +"'";
				System.out.println(i + "   " + query);
				mysqlConn.executeUpdate(query);
			}
			else{
				Statement s1 = mysqlConn.createStatement();
				ResultSet rs1 = mysqlConn.executeQuery(s1, "SELECT STR FROM umls.mrconso where CUI='" + CUI +"'");
				String desc ="";
				if(rs1.next()){
					desc =  rs1.getString("STR");
				}
				s1.close();
				rs1.close();
				
				String CUI2Type ="";
				Statement s2 = mysqlConn.createStatement();			
				ResultSet rs2 = mysqlConn.executeQuery(s2, "SELECT STY FROM umls.mrsty where CUI='"+ CUI+"'");
				if (rs2.next()){
					CUI2Type = rs2.getString("STY");
				}
				
				desc = desc.replace("'", "''");
				query = "INSERT INTO umls.ltsymptommapping VALUES('"+CUI+"','"+desc+"','','"+CUI2Type+"')";
				System.out.println(query);
				mysqlConn.executeUpdate(query);
				query = "UPDATE umls.ltsymptommapping set "+ column + " = '"+organ+"' WHERE CUI = '"+CUI +"'";
				mysqlConn.executeUpdate(query);
			}
			s.close();
			rs.close();
		}
	}

	public static void pruneSuperfluousConcepts(MySQLConnection mysqlConn) throws Exception{
		String query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%(disorder)%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%(diagnosis)%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%history of%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%FH:%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%sign of%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%symptoms%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%DNA%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%pap smear%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%biopsy%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%X-ray%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%mammogram%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%echocardiography%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '% EKG %' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '% MRI %' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%electrocardiogram%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%computed tomography%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%orthopantogram%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%ultrasound%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '% ECG %' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%magnetic resonance%' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '% dental filling %' ";
		mysqlConn.executeUpdate(query);
		
		query = "DELETE FROM umls.ltsymptommapping WHERE Description like '%barium enema%' ";
		mysqlConn.executeUpdate(query);
		
	}
	
/*
 * This is to load the signs and symptoms that have been mapped from text file
 */
public static void loadSSFromFile(DistinctLinkedList list, String organSystem) throws Exception{
	TXTFileReader txtFile = new TXTFileReader(organSystem+"SSO.txt");
	int count = 0;
	
	while(txtFile.readMoreLine()){
		count++;
		String line = txtFile.getLine().trim();
		list.addWord(line);
	}
	System.out.println(count + " Finish file "+ organSystem);
	txtFile.close();
}


/*
 * This is the main routine for mapping signs and symptoms to the organ systems 
 */


public static void main(String [] args) {
		
		umlsRelationTable = new UMLSRelationshipTable();
		
		// TODO Auto-generated method stub
		try{  
			MySQLConnection mysqlConn = new MySQLConnection();
		
			String [] generalWellBeings = {"C1286942"};
			String [] gsTypes = {"Finding"};
		    MappingForOrganSystem(mysqlConn, "GeneralSymptom", generalWellBeings, gsTypes, generalSS);
			//loadSSFromFile(generalSS, "GeneralSymptom"); 
		    	
			String [] ENT = {"C0422833"};
			String [] ENTTypes = {"Sign or Symptom"};
			MappingForOrganSystem(mysqlConn, "ENT", ENT, ENTTypes, ENTSS);
			//loadSSFromFile(ENTSS, "ENT");
				
			String [] EYE = {"C0587900"};
			String [] EYETypes = {"Body Part, Organ, or Organ Component"};
			MappingForOrganSystem(mysqlConn, "EYE", EYE, EYETypes, EYESS);
			//loadSSFromFile(EYESS, "EYE");
				
			String [] integumentaryTopCUIs = {"C0037267"};
			String [] parentTypes4 ={"Body System"};
			MappingForOrganSystem(mysqlConn, "Integumentary", integumentaryTopCUIs, parentTypes4, integumentarySS);
			//loadSSFromFile(integumentarySS, "Integumentary");
				
			String [] endocrineTopCUIs = {"C0014136"};
			String [] parentTypes7     = {"Body System"};
			MappingForOrganSystem(mysqlConn, "Endocrine", endocrineTopCUIs, parentTypes7, endocrineSS);
			//loadSSFromFile(endocrineSS, "Endocrine");
				
			String [] digestiveTopCUIs = {"C0012240", "C1333803", "C0426737",  "C0432602", "C0424867"};
			String [] parentTypes      = {"Body System", "Finding",  "Finding",  "Finding", "Finding"};
			MappingForOrganSystem(mysqlConn, "Digestive", digestiveTopCUIs, parentTypes, digestiveSS);
			//loadSSFromFile(digestiveSS, "Digestive");
			
			String [] urologyCUIs = {"C1508753", "C0426391", "C0812426"};
			String [] parentTypes12 = {"Body System", "Finding", "Sign or Symptom"};
			MappingForOrganSystem(mysqlConn, "Urinary", urologyCUIs, parentTypes12, urinarySS);	
			//loadSSFromFile(urinarySS, "Urinary");
			
			String [] reproductiveTopCUIs = {"C1261210"}; 
			String [] parentTypes8 ={"Body System"};
			MappingForOrganSystem(mysqlConn, "Reproductive", reproductiveTopCUIs, parentTypes8, reproductiveSS); 
			//loadSSFromFile(reproductiveSS, "Reproductive");
				
			String [] genitourinaryTopCUIs = {"C0042066"};
			String [] parentTypes2 = {"Body System"};
		    MappingForOrganSystem(mysqlConn, "Genitourinary", genitourinaryTopCUIs, parentTypes2, genitourinarySS);
			//loadSSFromFile(genitourinarySS, "Genitourinary");
			
			String [] lymphaticTopCUIs = {"C0024235","C0020962"};
			String [] parentTypes6 ={"Body System","Body System"};
		    MappingForOrganSystem(mysqlConn, "Lymphatic-Immune", lymphaticTopCUIs, parentTypes6, lymphaticSS);
		    //loadSSFromFile(lymphaticSS, "Lymphatic-Immune");	
				
			String [] cardiovasTopCUIs = {"C0007226", "C0029854"};
			String [] parentTypes1 ={"Body System", "Sign or Symptom"};
			MappingForOrganSystem(mysqlConn, "Cardiovascular", cardiovasTopCUIs, parentTypes1, cardiovascularSS);
			//loadSSFromFile(cardiovascularSS, "Cardiovascular");	
				
			String [] respiratoryTopCUIs = {"C0035237"};
			String [] parentTypes3 ={"Body System"};
			MappingForOrganSystem(mysqlConn, "Respiratory", respiratoryTopCUIs, parentTypes3, respiratorySS);
			//loadSSFromFile(respiratorySS, "Respiratory");	
				
			String [] MusculoskeletalTopCUIs = {"C0026860", "C0029669","C0026859"}; 
			String [] parentTypes10 ={"Body System", "Sign or Symptom","Finding"};
			MappingForOrganSystem(mysqlConn, "Musculoskeletal", MusculoskeletalTopCUIs, parentTypes10,musculoskeletalSS); 
			//loadSSFromFile(musculoskeletalSS, "Musculoskeletal");	
				
			String [] nervousTopCUIs = {"C0027763", "C0596002", "C0422837"}; 
			String [] parentTypes5 ={"Body System", "Finding", "Sign or Symptom"};
			MappingForOrganSystem(mysqlConn, "Nervous", nervousTopCUIs, parentTypes5, nervousSS);
			//loadSSFromFile(nervousSS, "Nervous");	
				
			String [] PsychologicalTopCUIs = {"C0004936"}; 
			String [] parentTypes11 ={"Mental or Behavioral Dysfunction"};
			MappingForOrganSystem(mysqlConn, "MentalHealth", PsychologicalTopCUIs, parentTypes11,mentalHealthSS); 
			//loadSSFromFile(mentalHealthSS, "MentalHealth");	
				
		    clearAndReinsertOrganMappings(mysqlConn,"System"); 
			pruneSuperfluousConcepts(mysqlConn);
			
		}catch(Exception ex){
			ex.printStackTrace();			
		}
	}

}

