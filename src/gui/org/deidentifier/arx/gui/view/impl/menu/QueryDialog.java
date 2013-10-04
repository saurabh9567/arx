/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.gui.view.impl.menu;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataSelector;
import org.deidentifier.arx.DataType;
import org.deidentifier.arx.gui.resources.Resources;
import org.deidentifier.arx.gui.view.SWTUtil;
import org.deidentifier.arx.gui.view.impl.menu.QueryTokenizer.QueryTokenizerListener;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class QueryDialog extends TitleAreaDialog {

    private static enum Operator{
        EQUALS,
        GEQ,
        LEQ,
        LESS,
        GREATER
    }
    
    private Button           ok          = null;
    private Button           cancel      = null;
    private StyledText       text        = null;
    private Label            error       = null;
    private Data             data        = null;
    private String           initial     = null;
    private QueryTokenizer   highlighter = null;
    private List<StyleRange> styles      = new ArrayList<StyleRange>();

    public QueryDialog(final Data data, final Shell parent, String initial) {
        super(parent);
        this.initial = initial;
        this.data = data;
    }

    @Override
    public boolean close() {
        return super.close();
    }

    public String getQuery() {
        return text.getText();
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {

        parent.setLayoutData(SWTUtil.createFillGridData());

        // Create OK Button
        ok = createButton(parent, Window.OK, Resources.getMessage("ProjectDialog.3"), true); //$NON-NLS-1$
        ok.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.OK);
                close();
            }
        });
        ok.setEnabled(false);

        // Create Cancel Button
        cancel = createButton(parent, Window.CANCEL, Resources.getMessage("ProjectDialog.4"), false); //$NON-NLS-1$
        cancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                setReturnCode(Window.CANCEL);
                close();
            }
        });
    }

    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(Resources.getMessage("QueryDialog.0")); //$NON-NLS-1$
        setMessage(Resources.getMessage("QueryDialog.1"), IMessageProvider.NONE); //$NON-NLS-1$
        return contents;
    }

    @Override
    protected Control createDialogArea(final Composite parent) {

        parent.setLayout(new GridLayout());

        text = new StyledText(parent, SWT.BORDER);
        final GridData d = SWTUtil.createFillGridData();
        d.grabExcessHorizontalSpace = true;
        d.grabExcessVerticalSpace = true;
        text.setLayoutData(d);
        text.setText(this.initial);
        text.addModifyListener(new ModifyListener(){
            @Override
            public void modifyText(ModifyEvent arg0) {
                highlight();
                parse();
            }
        });
        

        error = new Label(parent, SWT.NONE);
        final GridData d2 = SWTUtil.createFillGridData();
        d2.grabExcessHorizontalSpace = true;
        error.setLayoutData(d2);
        error.setText("");
        
        return parent;
    }

    private void parse() {
        final String query = text.getText();
        final DataSelector selector = DataSelector.create(data);
        QueryTokenizer parser = new QueryTokenizer(new QueryTokenizerListener(){

                private Operator current = null;
                private DataType<?> type = null;
                
                private void setCurrent(Operator operator){
                    if (current != null) {
                        throw new RuntimeException("Syntax error near: "+operator);
                    } else {
                        current = operator;
                    }
                }
            
                @Override
                public void geq(int start, int length) {
                    setCurrent(Operator.GEQ);
                }

                @Override
                public void value(int start, int length) {
                    if (current == null){
                        throw new RuntimeException("Missing operator near: "+query.substring(start+1, start+length-1));
                    }
                    if (type == null){
                        throw new RuntimeException("Missing field for value: "+query.substring(start+1, start+length-1));
                    }
                    Object value = type.fromString(query.substring(start+1, start+length-1));
                    switch(current){
                    case EQUALS:
                        if (value instanceof Date){
                            selector.equals((Date)value);
                        } else if (value instanceof String){
                            selector.equals((String)value);
                        } else if (value instanceof Double){
                            selector.equals((Double)value);
                        }
                        break;
                    case GEQ:
                        if (value instanceof Date){
                            selector.geq((Date)value);
                        } else if (value instanceof String){
                            selector.geq((String)value);
                        } else if (value instanceof Double){
                            selector.geq((Double)value);
                        }
                        break;
                    case GREATER:
                        if (value instanceof Date){
                            selector.greater((Date)value);
                        } else if (value instanceof String){
                            selector.greater((String)value);
                        } else if (value instanceof Double){
                            selector.greater((Double)value);
                        }
                        break;
                    case LEQ:
                        if (value instanceof Date){
                            selector.leq((Date)value);
                        } else if (value instanceof String){
                            selector.leq((String)value);
                        } else if (value instanceof Double){
                            selector.leq((Double)value);
                        }
                        break;
                    case LESS:
                        if (value instanceof Date){
                            selector.less((Date)value);
                        } else if (value instanceof String){
                            selector.less((String)value);
                        } else if (value instanceof Double){
                            selector.less((Double)value);
                        }
                        break;
                    }
                    current = null;
                    type = null;
                }

                @Override
                public void field(int start, int length) {
                    String field = query.substring(start+1, start+length-1);
                    int index = data.getHandle().getColumnIndexOf(field);
                    if (index==-1){
                        throw new RuntimeException("Unknown field: "+field);
                    } else {
                        type = data.getHandle().getDataType(field);
                        selector.field(field);
                    }
                }

                @Override
                public void begin(int start) {
                    selector.begin();
                }

                @Override
                public void end(int start) {
                    selector.end();
                }

                @Override
                public void and(int start, int length) {
                    selector.and();
                }

                @Override
                public void or(int start, int length) {
                    selector.or();
                }

                @Override
                public void less(int start) {
                    setCurrent(Operator.LESS);
                }

                @Override
                public void greater(int start) {
                    setCurrent(Operator.GREATER);
                }

                @Override
                public void leq(int start, int length) {
                    setCurrent(Operator.LEQ);
                }

                @Override
                public void equals(int start) {
                    setCurrent(Operator.EQUALS);
                }
            });
        
        try {
            parser.tokenize(query);
            selector.compile();
        } catch (Exception e){
            error.setText(e.getMessage());
            e.printStackTrace();
            return;
        }
        error.setText("OK");
    }

    private void highlight() {
        
        if (highlighter==null){
            highlighter = new QueryTokenizer(new QueryTokenizerListener(){

                @Override
                public void geq(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void value(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_DARK_GRAY;
                    styles.add(style);
                }

                @Override
                public void field(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_RED;
                    styles.add(style);
                }

                @Override
                public void begin(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_GREEN;
                    styles.add(style);
                }

                @Override
                public void end(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_GREEN;
                    styles.add(style);
                }

                @Override
                public void and(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_GRAY;
                    styles.add(style);
                }

                @Override
                public void or(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_GRAY;
                    styles.add(style);
                }

                @Override
                public void less(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void greater(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void leq(int start, int length) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = length;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }

                @Override
                public void equals(int start) {
                    StyleRange style = new StyleRange();
                    style.start = start;
                    style.length = 1;
                    style.fontStyle = SWT.BOLD;
                    style.foreground = GUIHelper.COLOR_BLUE;
                    styles.add(style);
                }
            });
        }
        
        styles.clear();
        highlighter.tokenize(text.getText());

        text.setRedraw(false);
        text.setStyleRanges(styles.toArray(new StyleRange[styles.size()]));        
        text.setRedraw(true);
    }

    @Override
    protected boolean isResizable() {
        return false;
    }
}