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
package org.deidentifier.arx.aggregates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.AttributeType.Hierarchy;

/**
 * This class enables building hierarchies for categorical and non-categorical values
 * using redaction. Data items are 1) aligned left-to-right or right-to-left, 2) differences in
 * length are filled with a padding character, 3) then, equally long values are redacted character by character
 * from left-to-right or right-to-left.
 * 
 * @author Fabian Prasser
 *
 */
public class HierarchyBuilderRedactionBased<T> extends HierarchyBuilder<T> implements Serializable {

    public static enum Order {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT
    }

    private static final long serialVersionUID = 3625654600380531803L;

    /**
     * Values are aligned left-to-right and redacted right-to-left. Redacted characters
     * are replaced with the given character. The same character is used for padding.
     * @param redactionCharacter
     */
    public static <T> HierarchyBuilderRedactionBased<T> create(char redactionCharacter){
        return new HierarchyBuilderRedactionBased<T>(redactionCharacter);
    }
    /**
     * Loads a builder specification from the given file
     * @param file
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static <T> HierarchyBuilderRedactionBased<T> create(File file) throws IOException{
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(file));
            HierarchyBuilderRedactionBased<T> result = (HierarchyBuilderRedactionBased<T>)ois.readObject();
            return result;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (ois != null) ois.close();
        }
    }
    
    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder. 
     * Redacted characters are replaced with the given character. The same character is used for padding.
     * @param alignmentOrder
     * @param redactionOrder
     * @param redactionCharacter
     */
    public static <T> HierarchyBuilderRedactionBased<T> create(Order alignmentOrder, 
                                                               Order redactionOrder, 
                                                               char redactionCharacter){
        return new HierarchyBuilderRedactionBased<T>(alignmentOrder, redactionOrder, redactionCharacter);
    }
    
    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder. 
     * Redacted characters are replaced with the given character. The padding character is used for padding.
     * @param alignmentOrder
     * @param redactionOrder
     * @param paddingCharacter
     * @param redactionCharacter
     */
    public static <T> HierarchyBuilderRedactionBased<T> create(Order alignmentOrder, 
                                                               Order redactionOrder, 
                                                               char paddingCharacter, 
                                                               char redactionCharacter){
        return new HierarchyBuilderRedactionBased<T>(alignmentOrder, redactionOrder, paddingCharacter, redactionCharacter);
    }

    /**
     * Loads a builder specification from the given file
     * @param file
     * @return
     * @throws IOException
     */
    public static <T> HierarchyBuilderRedactionBased<T> create(String file) throws IOException{
        return create(new File(file));
    }
    
    private Order                aligmentOrder      = Order.LEFT_TO_RIGHT;
    private char                 paddingCharacter   = '*';
    private char                 redactionCharacter = '*';
    private Order                redactionOrder     = Order.RIGHT_TO_LEFT;
    private transient String[][] result;
    
    /**
     * Values are aligned left-to-right and redacted right-to-left. Redacted characters
     * are replaced with the given character. The same character is used for padding.
     * @param redactionCharacter
     */
    private HierarchyBuilderRedactionBased(char redactionCharacter){
        super(Type.REDACTION_BASED);
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = redactionCharacter;
    }
    
    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder. 
     * Redacted characters are replaced with the given character. The same character is used for padding.
     * @param alignmentOrder
     * @param redactionOrder
     * @param redactionCharacter
     */
    private HierarchyBuilderRedactionBased(Order alignmentOrder, 
                                          Order redactionOrder, 
                                          char redactionCharacter){
        super(Type.REDACTION_BASED);
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = redactionCharacter;
        this.aligmentOrder = alignmentOrder;
        this.redactionOrder = redactionOrder;
    }

    /**
     * Values are aligned according to the alignmentOrder and redacted according to the redactionOrder. 
     * Redacted characters are replaced with the given character. The padding character is used for padding.
     * @param alignmentOrder
     * @param redactionOrder
     * @param paddingCharacter
     * @param redactionCharacter
     */
    private HierarchyBuilderRedactionBased(Order alignmentOrder, 
                                          Order redactionOrder, 
                                          char paddingCharacter, 
                                          char redactionCharacter){
        super(Type.REDACTION_BASED);
        this.redactionCharacter = redactionCharacter;
        this.paddingCharacter = paddingCharacter;
        this.aligmentOrder = alignmentOrder;
        this.redactionOrder = redactionOrder;
    }
    /**
     * Creates a new hierarchy, based on the predefined specification
     * @param data
     * @return
     */
    public Hierarchy build(String[] data){
        prepare(data);
        return build();
    }
    
    /**
     * Creates a new hierarchy, based on the predefined specification
     * @return
     */
    public Hierarchy build(){
        
        // Check
        if (result == null) {
            throw new IllegalArgumentException("Please call prepare() first");
        }
        
        // Return
        Hierarchy h = Hierarchy.create(result);
        this.result = null;
        return h;
    }

    /**
     * Returns the alignment order
     * @return
     */
    public Order getAligmentOrder() {
        return aligmentOrder;
    }
    
    /**
     * Returns the padding character
     * @return
     */
    public char getPaddingCharacter() {
        return paddingCharacter;
    }

    /**
     * Returns the redaction character
     * @return
     */
    public char getRedactionCharacter() {
        return redactionCharacter;
    }

    /**
     * Returns the redaction order
     * @return
     */
    public Order getRedactionOrder() {
        return redactionOrder;
    }

    /**
     * Prepares the builder. Returns a list of the number of equivalence classes per level
     * @return
     */
    public int[] prepare(String[] data){
        
        // Check
        if (this.result == null) {
            prepareResult(data);
        }
        
        // Compute
        int[] sizes = new int[this.result[0].length];
        for (int i=0; i < sizes.length; i++){
            Set<String> set = new HashSet<String>();
            for (int j=0; j<this.result.length; j++) {
                set.add(result[j][i]);
            }
            sizes[i] = set.size();
        }
        
        // Return
        return sizes;
    }

    /**
     * Computes the hierarchy
     */
    private void prepareResult(String[] data){

        // Determine length
        int length = Integer.MIN_VALUE;
        for (String s : data) {
            length = Math.max(length, s.length());
        }
        
        // Build padding string
        StringBuilder paddingBuilder = new StringBuilder();
        for (int i=0; i<length; i++) paddingBuilder.append(paddingCharacter);
        String padding = paddingBuilder.toString();

        // Build list of base strings
        String[] base = new String[data.length];
        for (int i=0; i<data.length; i++) {
            if (data[i].length()<length) {
                String pad = padding.substring(0, length - data[i].length());
                if (aligmentOrder == Order.RIGHT_TO_LEFT) {
                    base[i] =  pad + data[i];
                } else {
                    base[i] =  data[i] + pad;
                }
            } else {
                base[i] = data[i];
            }
        }
        
        // Build padding string
        StringBuilder redactionBuilder = new StringBuilder();
        for (int i=0; i<length; i++) redactionBuilder.append(redactionCharacter);
        String redaction = redactionBuilder.toString();

        // Build result
        this.result = new String[base.length][length + 1];
        for (int i=0; i<base.length; i++){
            result[i] = new String[length + 1];
            result[i][0] = data[i];
            for (int j=1; j<length + 1; j++){
                String redact = redaction.substring(0, j);
                if (redactionOrder == Order.RIGHT_TO_LEFT) {
                    result[i][j] =  base[i].substring(0, length - j) + redact;
                } else {
                    result[i][j] =  redact + base[i].substring(0, length - j);
                }
            }
        }
    }
}
