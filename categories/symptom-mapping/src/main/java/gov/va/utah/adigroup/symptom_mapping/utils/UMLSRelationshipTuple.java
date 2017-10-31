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

package gov.va.utah.adigroup.symptom_mapping.utils;

/**
 * A tuple (parentType, childType, relation) to present a relationship `relation` between
 * the type of the parent concept `parentType` and the type of the child concept `childType`  
 * -------------------------------------------------------------------------
 *
 * @author Le-Thuy Tran {@code <ltran@cs.utah.edu>}
 * @version June 15, 2013
 */

public class UMLSRelationshipTuple {
	String parentType;
	String childType;
	String relation;
	
	public UMLSRelationshipTuple(String parentType, String childType, String relation){
		this.parentType = parentType;
		this.childType = childType;
		this.relation = relation;
	}
	
	public String getParentType(){
		return this.parentType;
	}
	
	public String getChildType(){
		return this.childType;
	}
	
	public String getrelation(){
		return this.relation;
	}

}

