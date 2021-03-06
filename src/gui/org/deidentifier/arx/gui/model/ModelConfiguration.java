/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.RowSet;
import org.deidentifier.arx.aggregates.HierarchyBuilder;
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.metric.Metric;

public class ModelConfiguration implements Serializable, Cloneable {

    private static final long      serialVersionUID = -2887699232096897527L;

    private Map<String, Integer>   min              = new HashMap<String, Integer>();
    private Map<String, Integer>   max              = new HashMap<String, Integer>();
    private transient Data         input            = null;
    private ARXConfiguration       config           = ARXConfiguration.create();
    private boolean                removeOutliers   = true;
    private boolean                modified         = false;
    private Map<String, Hierarchy> hierarchies      = new HashMap<String, Hierarchy>();
    private RowSet                 researchSubset   = null;

    /**
     * Delegates to an instance of ARXConfiguration
     * @param c
     * @return
     */
    public ARXConfiguration addCriterion(PrivacyCriterion c) {
        setModified();
        return config.addCriterion(c);
    }

    @Override
    public ModelConfiguration clone() {

        final ModelConfiguration c = new ModelConfiguration();
        c.removeOutliers = removeOutliers;
        c.input = input;
        c.min = new HashMap<String, Integer>(min);
        c.max = new HashMap<String, Integer>(max);
        c.config = config.clone();
        c.hierarchies = new HashMap<String, Hierarchy>(hierarchies);
        c.researchSubset = this.getCriterion(DPresence.class).getSubset().getSet();
        return c;
    }
    
    /**
     * Minimum generalization
     * @param attribute
     * @param min
     */
    public void setMinimumGeneralization(String attribute, Integer min){
        if (this.min == null) {
            this.min = new HashMap<String, Integer>();
        }
        this.min.put(attribute, min);
    }

    /**
     * Maximum generalization
     * @param attribute
     * @param min
     */
    public void setMaximumGeneralization(String attribute, Integer max){
        if (this.max == null) {
            this.max = new HashMap<String, Integer>();
        }
        this.max.put(attribute, max);
    }

    /**
     * Minimum generalization
     * @param attribute
     * @param min
     */
    public Integer getMinimumGeneralization(String attribute){
        if (this.min == null) {
            return null;
        }
        return this.min.get(attribute);
    }

    /**
     * Maximum generalization
     * @param attribute
     * @param min
     */
    public Integer getMaximumGeneralization(String attribute){
        if (this.max == null) {
            return null;
        }
        return this.max.get(attribute);
    }
    
    /**
     * Delegates to an instance of ARXConfiguration
     * @param clazz
     * @return
     */
    public boolean containsCriterion(Class<? extends PrivacyCriterion> clazz) {
        return config.containsCriterion(clazz);
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public double getAllowedOutliers() {
        return config.getMaxOutliers();
    }

    /**
     * Returns the current config
     * @return
     */
    public ARXConfiguration getConfig(){
    	return config;
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public Set<PrivacyCriterion> getCriteria() {
        return config.getCriteria();
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> Set<T> getCriteria(Class<T> clazz) {
        return config.getCriteria(clazz);
    }
    
    /**
     * Delegates to an instance of ARXConfiguration
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> T getCriterion(Class<T> clazz) {
        return config.getCriterion(clazz);
    }

    /**
     * Returns the set of all assigned hierarchies
     * @return
     */
    public Map<String, Hierarchy> getHierarchies(){
        return this.hierarchies;
    }

    /**
     * Returns the assigned hierarchy, if any. Else null.
     * @param attribute
     */
    public Hierarchy getHierarchy(String attribute){
        return this.hierarchies.get(attribute);
    }

    /**
     * @return the input
     */
    public Data getInput() {
        return input;
    }
    
    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public Metric<?> getMetric() {
        return config.getMetric();
    }
    
    /**
     * Returns the current research subset
     * @return
     */
	public RowSet getResearchSubset() {
		return researchSubset;
	}
    
    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public boolean isCriterionMonotonic() {
        return config.isCriterionMonotonic();
    }

    /**
     * Has the config been modified
     * @return
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @return
     */
    public boolean isPracticalMonotonicity() {
        return config.isPracticalMonotonicity();
    }

    /**
	 * Protect sensitive associations
	 * @return
	 */
	public boolean isProtectSensitiveAssociations() {
		return config.isProtectSensitiveAssociations();
	}

    /**
     * Should outliers be removed
     * @return
     */
    public boolean isRemoveOutliers() {
        return removeOutliers;
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param clazz
     * @return
     */
    public <T extends PrivacyCriterion> boolean removeCriterion(PrivacyCriterion c) {
        setModified();
        return config.removeCriterion(c);
    }

    /**
     * Removes all criteria
     */
    public void removeAllCriteria() {
        this.getCriteria().clear();
    }
    
    /**
     * Delegates to an instance of ARXConfiguration
     * @param supp
     */
    public void setAllowedOutliers(double supp) {
        setModified();
        config.setMaxOutliers(supp);
    }

    /**
     * Assigns a hierarchy
     * @param attribute
     * @param hierarchy
     */
    public void setHierarchy(String attribute, Hierarchy hierarchy){
        this.hierarchies.put(attribute, hierarchy);
        this.setModified();
    }

    /**
     * @param data
     *            the input to set
     */
    public void setInput(final Data data) {
        input = data;
        setModified();
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param metric
     */
    public void setMetric(Metric<?> metric) {
        setModified();
        config.setMetric(metric);
    }

    /**
     * Delegates to an instance of ARXConfiguration
     * @param assumeMonotonicity
     */
    public void setPracticalMonotonicity(boolean assumeMonotonicity) {
        setModified();
        config.setPracticalMonotonicity(assumeMonotonicity);
    }
    
    /**
	 * Protect sensitive associations
	 * @param selection
	 */
	public void setProtectSensitiveAssociations(boolean selection) {
		config.setProtectSensitiveAssociations(selection);
	}

    /**
     * Sets whether outliers should be removed
     * @param removeOutliers
     */
    public void setRemoveOutliers(final boolean removeOutliers) {
        this.removeOutliers = removeOutliers;
        setModified();
    }
	
	/**
	 * Sets the current research subset
	 * @param subset
	 */
	public void setResearchSubset(RowSet subset) {
	    this.researchSubset = subset;
	}

	/**
     * Sets the config unmodified
     */
    public void setUnmodified() {
        modified = false;
    }
	
	/**
     * Mark as modified
     */
    private void setModified() {
        modified = true;
    }

    private Map<String, HierarchyBuilder<?>> hierarchyBuilders = null;
    
    /**
     * Sets the given hierarchy builder
     * @param attr
     * @param builder
     */
    public void setHierarchyBuilder(String attr, HierarchyBuilder<?> builder) {
        if (hierarchyBuilders==null){
            hierarchyBuilders = new HashMap<String, HierarchyBuilder<?>>();
        }
        hierarchyBuilders.put(attr, builder);
    }
    
    /**
     * Returns the according builder
     * @param attr
     * @return
     */
    public HierarchyBuilder<?> getHierarchyBuilder(String attr) {
        if (hierarchyBuilders==null) return null;
        else return hierarchyBuilders.get(attr);
    }

    /**
     * Removes the builder for the given attribute
     * @param attr
     * @return
     */
    public void removeHierarchyBuilder(String attr){
        if (hierarchyBuilders==null) return;
        hierarchyBuilders.remove(attr);
    }
}