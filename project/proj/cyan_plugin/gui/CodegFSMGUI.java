/*
 * Eduardo Romao da Rocha
 * */

package cyan_plugin.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;
import com.mxgraph.view.mxGraph;
import saci.Tuple2;
import saci.Tuple4;

public class CodegFSMGUI extends JDialog{
	private JDialog jDialog;
	private StringBuffer userInput;
	private JLabel lblMenu;
	private JButton btnAddNewState;
	private JButton btnAddNewTransition;
	private JButton btnRemoveState;
	private JButton btnRemoveTransition;
	private JButton btnEditState;
	private JButton btnEditTransition;
	private JButton btnOk;
	private JButton btnCancel;
	private JEditorPane editorPane;
	private mxGraph fsm;
	private ArrayList<Tuple2<String, Boolean>> stateList;
	private ArrayList<Tuple4<String, String, String, String>> transitionList;
	
	public CodegFSMGUI(java.awt.Frame parent, byte[] previousUserInput){
        super(parent, true);
		userInput = new StringBuffer(new String(previousUserInput, StandardCharsets.UTF_8));
		this.fsm = new mxGraph();
		this.stateList = new ArrayList<>();
		this.transitionList = new ArrayList<>();
		initComponents();
		setLocationRelativeTo(null);
		setTitle("Codeg FSM");
		setVisible(true);
    }
	
	public void initComponents(){
		//setTitle("Codeg FSM");
		//jDialog = new JDialog();
		//jDialog.setTitle("Codeg FSM");
		
		lblMenu = new JLabel("Menu:");
		add(lblMenu);
		
		btnAddNewState = new JButton("Add new state");
		btnAddNewState.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent evt){
				
				createNewState(evt);
			}
		});
		
		btnAddNewTransition = new JButton("Add new transition");
		btnAddNewState.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent evt){
				
				createNewTransition(evt);
			}
		});
		
		editorPane = new JEditorPane();
		btnOk = new JButton("OK");
		btnCancel = new JButton("Cancel");
		btnRemoveState = new JButton("Remove State");
		btnRemoveTransition = new JButton("Remove Transition");
		btnEditState = new JButton("Edit State");
		btnEditTransition = new JButton("Edit Transition");
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(lblMenu)
							.addGap(4)
							.addComponent(btnAddNewState)
							.addGap(37)
							.addComponent(btnRemoveState)
							.addGap(35)
							.addComponent(btnEditState))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(41)
							.addComponent(btnAddNewTransition)
							.addGap(17)
							.addComponent(btnRemoveTransition)
							.addGap(15)
							.addComponent(btnEditTransition))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(326)
							.addComponent(btnOk)
							.addGap(6)
							.addComponent(btnCancel))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(7)
							.addComponent(editorPane, GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(7)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(4)
							.addComponent(lblMenu))
						.addComponent(btnAddNewState)
						.addComponent(btnRemoveState)
						.addComponent(btnEditState))
					.addGap(4)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnAddNewTransition)
						.addComponent(btnRemoveTransition)
						.addComponent(btnEditTransition))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(editorPane, GroupLayout.PREFERRED_SIZE, 297, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(btnOk)
						.addComponent(btnCancel)))
		);
		getContentPane().setLayout(groupLayout);
		pack();
	}
	
	public void createNewState(ActionEvent evt){
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				JDialog newStateMenu = new JDialog();
				JPanel ctPanel = new JPanel();
				JTextField textField;
				JRadioButton rdbtnYes;
				JRadioButton rdbtnNo;
				boolean isFinalState = false;
				
				newStateMenu.setModalityType(ModalityType.APPLICATION_MODAL);
				newStateMenu.setTitle("New State Menu");
				newStateMenu.setBounds(100, 100, 450, 150);
				getContentPane().setLayout(new BorderLayout());
				ctPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				getContentPane().add(ctPanel, BorderLayout.CENTER);
				SpringLayout sl_contentPanel = new SpringLayout();
				ctPanel.setLayout(sl_contentPanel);
				{
					JLabel lblStateName = new JLabel("State name:");
					sl_contentPanel.putConstraint(SpringLayout.NORTH, lblStateName, 15, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, lblStateName, 12, SpringLayout.WEST, ctPanel);
					ctPanel.add(lblStateName);
				}
				{
					textField = new JTextField();
					sl_contentPanel.putConstraint(SpringLayout.NORTH, textField, 12, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, textField, 75, SpringLayout.WEST, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.EAST, textField, 422, SpringLayout.WEST, ctPanel);
					ctPanel.add(textField);
					textField.setColumns(10);
				}
				{
					JLabel lblFinalState = new JLabel("Final state:");
					sl_contentPanel.putConstraint(SpringLayout.NORTH, lblFinalState, 48, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, lblFinalState, 12, SpringLayout.WEST, ctPanel);
					ctPanel.add(lblFinalState);
				}
				{
					rdbtnYes = new JRadioButton("Yes");
					sl_contentPanel.putConstraint(SpringLayout.NORTH, rdbtnYes, 44, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, rdbtnYes, 75, SpringLayout.WEST, ctPanel);
					ctPanel.add(rdbtnYes);
				}
				{
					rdbtnNo = new JRadioButton("No");
					sl_contentPanel.putConstraint(SpringLayout.NORTH, rdbtnNo, 44, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, rdbtnNo, 122, SpringLayout.WEST, ctPanel);
					ctPanel.add(rdbtnNo);
				}
				{
					JPanel buttonPane = new JPanel();
					buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
					getContentPane().add(buttonPane, BorderLayout.SOUTH);
					{
						JButton okButton = new JButton("OK");
						okButton.setActionCommand("OK");
						buttonPane.add(okButton);
						getRootPane().setDefaultButton(okButton);
					}
					{
						JButton cancelButton = new JButton("Cancel");
						cancelButton.setActionCommand("Cancel");
						buttonPane.add(cancelButton);
						
						String stateName = textField.getText();
						
						if(rdbtnYes.isSelected()) isFinalState = true;
						final boolean finalIFS = isFinalState;
						
						cancelButton.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent evt){
								//Tuple2<String, Boolean> auxTuple = new Tuple2<String, Boolean>();
								stateList.add(new Tuple2<String, Boolean>(stateName, 
										new Boolean(finalIFS)));
								drawStateCircle(stateName, finalIFS);;
							} 
						});
					}
				}
			}

		});
		pack();
		
	}
	
	public void createNewTransition(ActionEvent evt){
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				JDialog newTransitionMenu = new JDialog();
				JPanel ctPanel = new JPanel();
				JTextField srcStateNameField;
				JTextField dstStateNameField;
				JTextField trsInputField;
				
				newTransitionMenu.setModalityType(ModalityType.APPLICATION_MODAL);
				newTransitionMenu.setTitle("New Transition Menu");
				newTransitionMenu.setBounds(100, 100, 450, 200);
				getContentPane().setLayout(new BorderLayout());
				ctPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				getContentPane().add(ctPanel, BorderLayout.CENTER);
				SpringLayout sl_contentPanel = new SpringLayout();
				ctPanel.setLayout(sl_contentPanel);
				{
					JLabel lblSourceState = new JLabel("Source State:");
					sl_contentPanel.putConstraint(SpringLayout.NORTH, lblSourceState, 15, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, lblSourceState, 33, SpringLayout.WEST, ctPanel);
					ctPanel.add(lblSourceState);
				}
				{
					srcStateNameField = new JTextField();
					sl_contentPanel.putConstraint(SpringLayout.NORTH, srcStateNameField, 12, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, srcStateNameField, 103, SpringLayout.WEST, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.EAST, srcStateNameField, 422, SpringLayout.WEST, ctPanel);
					ctPanel.add(srcStateNameField);
					srcStateNameField.setColumns(10);
				}
				{
					JLabel lblDestinationState = new JLabel("Destination State:");
					sl_contentPanel.putConstraint(SpringLayout.NORTH, lblDestinationState, 39, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, lblDestinationState, 12, SpringLayout.WEST, ctPanel);
					ctPanel.add(lblDestinationState);
				}
				{
					dstStateNameField = new JTextField();
					sl_contentPanel.putConstraint(SpringLayout.NORTH, dstStateNameField, 36, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, dstStateNameField, 103, SpringLayout.WEST, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.EAST, dstStateNameField, 422, SpringLayout.WEST, ctPanel);
					ctPanel.add(dstStateNameField);
					dstStateNameField.setColumns(10);
				}
				{
					JLabel lblTransitionInput = new JLabel("Transition Input");
					sl_contentPanel.putConstraint(SpringLayout.NORTH, lblTransitionInput, 63, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, lblTransitionInput, 23, SpringLayout.WEST, ctPanel);
					ctPanel.add(lblTransitionInput);
				}
				{
					trsInputField = new JTextField();
					sl_contentPanel.putConstraint(SpringLayout.NORTH, trsInputField, 60, SpringLayout.NORTH, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.WEST, trsInputField, 103, SpringLayout.WEST, ctPanel);
					sl_contentPanel.putConstraint(SpringLayout.EAST, trsInputField, 422, SpringLayout.WEST, ctPanel);
					ctPanel.add(trsInputField);
					trsInputField.setColumns(10);
				}
				{
					JPanel buttonPane = new JPanel();
					buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
					getContentPane().add(buttonPane, BorderLayout.SOUTH);
					{
						JButton okButton = new JButton("OK");
						okButton.setActionCommand("OK");
						buttonPane.add(okButton);
						getRootPane().setDefaultButton(okButton);
						
						String src = srcStateNameField.getText();
						String dst = dstStateNameField.getText();
						String input = trsInputField.getText();
						
						okButton.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent evt){
								drawTransitionArrow(src, dst, input);
							}
						});
					}
					{
						JButton cancelButton = new JButton("Cancel");
						cancelButton.setActionCommand("Cancel");
						buttonPane.add(cancelButton);
					}
				}
				pack();
			}
		});
	}
	
	public void removeState(ActionEvent evt){
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				JPanel ctPanel = new JPanel();
				JDialog rmvState = new JDialog();
				
				rmvState.setModalityType(ModalityType.APPLICATION_MODAL);
				rmvState.setTitle("Remove State");
				rmvState.setBounds(100, 100, 450, 300);
				getContentPane().setLayout(new java.awt.BorderLayout());
				ctPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				getContentPane().add(ctPanel, BorderLayout.CENTER);
				
				JLabel lblSelectState = new JLabel("Select state: ");
				JComboBox comboBox = new JComboBox();
				GroupLayout gl_contentPanel = new GroupLayout(ctPanel);
				gl_contentPanel.setHorizontalGroup(
					gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addComponent(lblSelectState)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addContainerGap(328, Short.MAX_VALUE))
				);
				gl_contentPanel.setVerticalGroup(
					gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblSelectState)
								.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addContainerGap(199, Short.MAX_VALUE))
				);
				ctPanel.setLayout(gl_contentPanel);
				{
					JPanel buttonPane = new JPanel();
					buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
					getContentPane().add(buttonPane, BorderLayout.SOUTH);
					{
						JButton okButton = new JButton("OK");
						okButton.setActionCommand("OK");
						buttonPane.add(okButton);
						getRootPane().setDefaultButton(okButton);
					}
					{
						JButton cancelButton = new JButton("Cancel");
						cancelButton.setActionCommand("Cancel");
						buttonPane.add(cancelButton);
					}
				}
				pack();
			}
		});
	}
	
	public void removeTransition(ActionEvent evt){
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run(){
				
			}
		});
	}
	
	public void drawStateCircle(String stateName, boolean isFinalState){
		Object parent = fsm.getDefaultParent();
		this.fsm.getModel().beginUpdate();
		
		try{
			//Object ns = fsm.addCell();
			//editorPane.add();
		}finally{
			fsm.getModel().endUpdate();
		}
	}
	
	public void drawTransitionArrow(String src, String dst, String input){
		
	}

}
