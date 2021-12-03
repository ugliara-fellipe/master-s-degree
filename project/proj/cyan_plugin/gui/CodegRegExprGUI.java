/*
 * Eduardo Romao da Rocha
 * */

package cyan_plugin.gui;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import net.miginfocom.swing.*;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import lexer.Lexer;

public class CodegRegExprGUI extends JDialog{
    /**
	 
	 */
	private static final long serialVersionUID = 7844331581647624702L;
	/**
	 
	 */
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
    private JTextField textField_regEx;
    private JButton btnTest;
    private JButton btnTest_1;
    private JButton btnTest_2;
    private JButton btnTest_3;
    private JButton btnTest_4;
    private JButton btnNewButton;
    private JLabel lblNewLabel;
    private JLabel lblNewLabel_1;
    private JLabel lblNewLabel_2;
    private JLabel lblNewLabel_3;
    private JLabel lblNewLabel_4;
    private JLabel lblRegex;
    private StringBuffer userInput1;
    private StringBuffer userInput2;
    private byte[] userInput;
    private boolean formClick;
    private int mouseCount;
    private String regExFromEditor;
    private String[] list;
    
    public CodegRegExprGUI(java.awt.Frame parent, byte[] previousUserInput, String refe, ArrayList<String> prevInputs){
        super(parent, true);
		//userInput = new StringBuffer(new String(previousUserInput, StandardCharsets.UTF_8));
		userInput = previousUserInput;
        //initList();
		this.regExFromEditor = refe.substring(1, refe.length() - 1);
		initComponents();
		setLocationRelativeTo(null);
		setTitle("Codeg Regular Expression");
		setVisible(true);
    }
    
    private void initComponents(){
    	//Converting from byte[] (userInput) to ArrayList<String>
    	//String[] list = null;
    	/*ByteArrayInputStream bais = new ByteArrayInputStream(userInput);
    	ObjectInputStream ois = null;
    	try{
    		ois = new ObjectInputStream(bais);
    	}catch(IOException e){
    		
    	}
    	
    	try{
    		list = (String[]) ois.readObject();
    	}catch(EOFException e){
    		
    	}catch(IOException e){
    		
    	}catch(ClassNotFoundException e){
    		
    	}finally{
    		try{
    			if(ois != null){
    				ois.close();
    			}
    		}catch(IOException e){
    			
    		}
    	}
    	
    	try{
    		bais.close();
    	}catch(IOException e){
    		
    	}*/
    	
    	textField = new JTextField();
		textField.setColumns(30);
		//textField.setText(list[0]);
		textField.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent evt){}
			@Override
			public void insertUpdate(DocumentEvent evt){
				TextFieldUpdate(evt);
			}
			@Override
			public void removeUpdate(DocumentEvent evt){
				TextFieldUpdate(evt);
			}
		});
		
		btnTest = new JButton("Test");
        btnTest.addActionListener(new ActionListener(){
            @Override
			public void actionPerformed(ActionEvent evt){
                jButtonBtnTestActionPerformed(evt);
            }
        });
		
		textField_1 = new JTextField();
		textField_1.setColumns(30);
		//textField_1.setText(list[1]);
		textField_1.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent evt){}
			@Override
			public void insertUpdate(DocumentEvent evt){
				TextFieldUpdate_1(evt);
			}
			@Override
			public void removeUpdate(DocumentEvent evt){
				TextFieldUpdate_1(evt);
			}
		});
		
        btnTest_1 = new JButton("Test");
		btnTest_1.addActionListener(new ActionListener(){
            @Override
			public void actionPerformed(ActionEvent evt){
                jButtonBtnTest_1ActionPerformed(evt);
            }
        });
        
		textField_2 = new JTextField();
		textField_2.setColumns(30);
		//textField_2.setText(list[2]);
		textField_2.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent evt){}
			@Override
			public void insertUpdate(DocumentEvent evt){
				TextFieldUpdate_2(evt);
			}
			@Override
			public void removeUpdate(DocumentEvent evt){
				TextFieldUpdate_2(evt);
			}
		});
		
		btnTest_2 = new JButton("Test");
        btnTest_2.addActionListener(new ActionListener(){
            @Override
			public void actionPerformed(ActionEvent evt){
                jButtonBtnTest_2ActionPerformed(evt);
            }
        });
		
		textField_3 = new JTextField();
		textField_3.setColumns(30);
		//textField_3.setText(list[3]);
		textField_3.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent evt){}
			@Override
			public void insertUpdate(DocumentEvent evt){
				TextFieldUpdate_3(evt);
			}
			@Override
			public void removeUpdate(DocumentEvent evt){
				TextFieldUpdate_3(evt);
			}
		});
		
		btnTest_3 = new JButton("Test");
        btnTest_3.addActionListener(new ActionListener(){
            @Override
			public void actionPerformed(ActionEvent evt){
                jButtonBtnTest_3ActionPerformed(evt);
            }
        });
		
		textField_4 = new JTextField();
		textField_4.setColumns(30);
		//textField_4.setText(list[4]);
		textField_4.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent evt){}
			@Override
			public void insertUpdate(DocumentEvent evt){
				TextFieldUpdate_4(evt);
			}
			@Override
			public void removeUpdate(DocumentEvent evt){
				TextFieldUpdate_4(evt);
			}
		});
		
		btnTest_4 = new JButton("Test");
		btnTest_4.addActionListener(new ActionListener(){
            @Override
			public void actionPerformed(ActionEvent evt){
                jButtonBtnTest_4ActionPerformed(evt);
            }
        });
        
		lblNewLabel = new JLabel("");
		
        lblNewLabel_1 = new JLabel("");
		
		lblNewLabel_2 = new JLabel("");
		
		lblNewLabel_3 = new JLabel("");
		
		lblNewLabel_4 = new JLabel("");
		
		btnNewButton = new JButton("Cancel");
        btnNewButton.addActionListener(new ActionListener(){
            @Override
			public void actionPerformed(ActionEvent evt){
                jButtonBtnNewButtonActionPerformed(evt);
            }
        });
        
        lblRegex = new JLabel("RegEx:");
        
        textField_regEx = new JTextField(this.regExFromEditor);
		textField_regEx.setColumns(40);
		
		
JLabel lblWhenThe = new JLabel("* When the OK Button is pressed, the RegEx will be copied to your clipboard.");
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent evt){
				OkButtonActionPerformed(evt);
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(lblRegex)
							.addGap(4)
							.addComponent(textField_regEx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(75)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblNewLabel)))
								//.addComponent(label)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(75)
							.addComponent(lblNewLabel_1))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(75)
							.addComponent(lblNewLabel_2))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(75)
							.addComponent(lblNewLabel_3))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(75)
							.addComponent(lblNewLabel_4))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(267)
							.addComponent(btnOk)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblWhenThe)))
					.addContainerGap(7, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(3)
							.addComponent(lblRegex))
						.addComponent(textField_regEx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(20)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							//.addComponent(label)
							.addComponent(lblNewLabel)))
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblNewLabel_1))
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblNewLabel_2))
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblNewLabel_3))
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblNewLabel_4))
					.addGap(33)
					.addComponent(lblWhenThe)
					.addGap(7)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewButton)
						.addComponent(btnOk)))
		);
		getContentPane().setLayout(groupLayout);
		/*GroupLayout groupLayout = new GroupLayout(getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(lblRegex)
							.addGap(4)
							.addComponent(textField_regEx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(267)
							.addComponent(btnOk)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton))
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblWhenThe))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
									.addComponent(lblNewLabel_1))
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
									.addGap(54)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(lblNewLabel_2)
										.addComponent(lblNewLabel_3)
										.addComponent(lblNewLabel_4)
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
											//.addComponent(label)
											.addComponent(lblNewLabel)))))))
					.addContainerGap(49, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(3)
							.addComponent(lblRegex))
						.addComponent(textField_regEx, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(20)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(1)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblNewLabel)))
						//.addComponent(label))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(5)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(lblNewLabel_1)))
					.addGap(11)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_2))
					.addGap(5)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_3))
					.addGap(5)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblNewLabel_4))
					.addGap(33)
					.addComponent(lblWhenThe)
					.addGap(7)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewButton)
						.addComponent(btnOk)
		);
		getContentPane().setLayout(groupLayout);*/
		pack();
		
		/*
		while(true){
			if(textField.getText().equals("")) lblNewLabel.setText("");
			else{
				if(textField.getText().matches(Lexer.escapeJavaString(textField_regEx.getText()))){
				 lblNewLabel.setForeground(Color.GREEN);
		         lblNewLabel.setText("MATCH");
				}else{
					lblNewLabel.setForeground(Color.RED);
					lblNewLabel.setText("NO MATCH");
				}
			}
			
			if(textField_1.getText().equals("")) lblNewLabel_1.setToolTipText("");
			else{
				if(textField_1.getText().matches(Lexer.escapeJavaString(textField_regEx.getText()))){
					 lblNewLabel_1.setForeground(Color.GREEN);
			         lblNewLabel_1.setText("MATCH");
				}else{
					lblNewLabel_1.setForeground(Color.RED);
					lblNewLabel_1.setText("NO MATCH");
				}				
			}
				
			if(textField_2.getText().equals("")) lblNewLabel_2.setText("");
			else{
				if(textField_2.getText().matches(Lexer.escapeJavaString(textField_regEx.getText()))){
					 lblNewLabel_2.setForeground(Color.GREEN);
			         lblNewLabel_2.setText("MATCH");
				}else{
					lblNewLabel_2.setForeground(Color.RED);
					lblNewLabel_2.setText("NO MATCH");
				}	
			}
			
			if(textField_3.getText().equals("")) lblNewLabel_3.setText("");
			else{
				if(textField_3.getText().matches(Lexer.escapeJavaString(textField_regEx.getText()))){
					 lblNewLabel_3.setForeground(Color.GREEN);
			         lblNewLabel_3.setText("MATCH");
				}else{
					lblNewLabel_3.setForeground(Color.RED);
					lblNewLabel_3.setText("NO MATCH");
				}
			}
			
			if(textField_4.getText().equals("")) lblNewLabel_4.setText("");
			else{
				if(textField_4.getText().matches(Lexer.escapeJavaString(textField_regEx.getText()))){
					 lblNewLabel_4.setForeground(Color.GREEN);
			         lblNewLabel_4.setText("MATCH");
				}else{
					lblNewLabel_4.setForeground(Color.RED);
					lblNewLabel_4.setText("NO MATCH");
				}
			}
		}*/
    }
    
    private void TextFieldUpdate(DocumentEvent evt){
    	String regEx = Lexer.escapeJavaString(textField_regEx.getText());
    	String txt = textField.getText();
    	
    	if(txt.equals("")) lblNewLabel.setText("");
    	else{
	    	if(txt.matches(regEx)){
	    		lblNewLabel.setForeground(Color.GREEN);
	    		lblNewLabel.setText("MATCH");
	    	}else{
	    		lblNewLabel.setForeground(Color.RED);
	    		lblNewLabel.setText("NO MATCH");
	    	}
    	}
    }
    
    private void TextFieldUpdate_1(DocumentEvent evt){
    	String regEx = Lexer.escapeJavaString(textField_regEx.getText());
    	String txt = textField_1.getText();
    	
    	if(txt.equals("")) lblNewLabel_1.setText("");
    	else{
	    	if(txt.matches(regEx)){
	    		lblNewLabel_1.setForeground(Color.GREEN);
	    		lblNewLabel_1.setText("MATCH");
	    	}else{
	    		lblNewLabel_1.setForeground(Color.RED);
	    		lblNewLabel_1.setText("NO MATCH");
	    	}
    	}
    }
    
    private void TextFieldUpdate_2(DocumentEvent evt){
    	String regEx = Lexer.escapeJavaString(textField_regEx.getText());
    	String txt = textField_2.getText();
    	
    	if(txt.equals("")) lblNewLabel_2.setText("");
    	else{
	    	if(txt.matches(regEx)){
	    		lblNewLabel_2.setForeground(Color.GREEN);
	    		lblNewLabel_2.setText("MATCH");
	    	}else{
	    		lblNewLabel_2.setForeground(Color.RED);
	    		lblNewLabel_2.setText("NO MATCH");
	    	}
    	}
    }
    
    private void TextFieldUpdate_3(DocumentEvent evt){
    	String regEx = Lexer.escapeJavaString(textField_regEx.getText());
    	String txt = textField_3.getText();
    	
    	if(txt.equals("")) lblNewLabel_3.setText("");
    	else{
	    	if(txt.matches(regEx)){
	    		lblNewLabel_3.setForeground(Color.GREEN);
	    		lblNewLabel_3.setText("MATCH");
	    	}else{
	    		lblNewLabel_3.setForeground(Color.RED);
	    		lblNewLabel_3.setText("NO MATCH");
	    	}
    	}
    }
    
    private void TextFieldUpdate_4(DocumentEvent evt){
    	String regEx = Lexer.escapeJavaString(textField_regEx.getText());
    	String txt = textField_4.getText();
    	
    	if(txt.equals("")) lblNewLabel_4.setText("");
    	else{
	    	if(txt.matches(regEx)){
	    		lblNewLabel_4.setForeground(Color.GREEN);
	    		lblNewLabel_4.setText("MATCH");
	    	}else{
	    		lblNewLabel_4.setForeground(Color.RED);
	    		lblNewLabel_4.setText("NO MATCH");
	    	}
    	}
    }
    
    private void jButtonBtnTestActionPerformed(ActionEvent evt){
        //Fazer o teste da string dada com o regex !!
        String str = textField.getText();
        String str2 = Lexer.escapeJavaString(textField_regEx.getText());
        boolean res = str.matches(str2);
        
        if(res){
            lblNewLabel.setForeground(Color.GREEN);
            lblNewLabel.setText("MATCH");
        }else{
            lblNewLabel.setForeground(Color.RED);
            lblNewLabel.setText("NO MATCH");
        }
    }
    
    private void jButtonBtnTest_1ActionPerformed(ActionEvent evt){
        //Fazer o teste da string dada com o regex !!
        String str = textField_1.getText();
        String str2 = Lexer.escapeJavaString(textField_regEx.getText());
        boolean res = str.matches(str2);
        
        if(res){
            lblNewLabel_1.setForeground(Color.GREEN);
            lblNewLabel_1.setText("MATCH");
        }else{
            lblNewLabel_1.setForeground(Color.RED);
            lblNewLabel_1.setText("NO MATCH");
        }
    }
    
    private void jButtonBtnTest_2ActionPerformed(ActionEvent evt){
        //Fazer o teste da string dada com o regex !!
        String str = textField_2.getText();
        String str2 = Lexer.escapeJavaString(textField_regEx.getText());
        boolean res = str.matches(str2);
    
        if(res){
            lblNewLabel_2.setForeground(Color.GREEN);
            lblNewLabel_2.setText("MATCH");
        }else{
            lblNewLabel_2.setForeground(Color.RED);
            lblNewLabel_2.setText("NO MATCH");
        }
    }
    
    private void jButtonBtnTest_3ActionPerformed(ActionEvent evt){
        //Fazer o teste da string dada com o regex !!
        String str = textField_3.getText();
        String str2 = Lexer.escapeJavaString(textField_regEx.getText());
        boolean res = str.matches(str2);
    
        if(res){
            lblNewLabel_3.setForeground(Color.GREEN);
            lblNewLabel_3.setText("MATCH");
        }else{
            lblNewLabel_3.setForeground(Color.RED);
            lblNewLabel_3.setText("NO MATCH");
        }
    }
    
    private void jButtonBtnTest_4ActionPerformed(ActionEvent evt){
        //Fazer o teste da string dada com o regex !!
        String str = textField_4.getText();
        String str2 = Lexer.escapeJavaString(textField_regEx.getText());
        boolean res = str.matches(str2);
        
        if(res){
            lblNewLabel_4.setForeground(Color.GREEN);
            lblNewLabel_4.setText("MATCH");
        }else{
            lblNewLabel_4.setForeground(Color.RED);
            lblNewLabel_4.setText("NO MATCH");
        }
    }
    
    private void jButtonBtnNewButtonActionPerformed(ActionEvent evt){
        this.dispose();
    }
    
    private void OkButtonActionPerformed(ActionEvent evt){
    	String str = Lexer.escapeJavaString(textField_regEx.getText());
    	StringSelection sel = new StringSelection(str);
    	Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
    	cb.setContents(sel, sel);
    	
    	this.dispose();
    }
    
    public String[] getStringsFromTests(){
    	//ArrayList<String> retList = new ArrayList<>();
    	String ret[] = {textField.getText(), textField_1.getText(),
    			textField_2.getText(), textField_3.getText(),
    			textField_4.getText()};
    	/*retList.add(textField.getText());
    	retList.add(textField_1.getText());
    	retList.add(textField_2.getText());
    	retList.add(textField_3.getText());
    	retList.add(textField_4.getText());*/
    	
    	return ret;
    }
    
    public void initList(){
    	ByteArrayInputStream bais = new ByteArrayInputStream(userInput);
    	ObjectInputStream ois = null;
    	try{
    		ois = new ObjectInputStream(bais);
    	}catch(IOException e){
    		
    	}
    	
    	try{
    		this.list = (String[]) ois.readObject();
    	}catch(EOFException e){
    		
    	}catch(IOException e){
    		
    	}catch(ClassNotFoundException e){
    		
    	}finally{
    		try{
    			if(ois != null){
    				ois.close();
    			}
    		}catch(IOException e){
    			
    		}
    	}
    	
    	try{
    		bais.close();
    	}catch(IOException e){
    		
    	}
    }
}
