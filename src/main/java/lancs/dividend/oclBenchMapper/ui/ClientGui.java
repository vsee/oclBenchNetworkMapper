package lancs.dividend.oclBenchMapper.ui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.userCmd.ExitCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

public class ClientGui implements UserInterface {

	// TODO clean up initialise
	
	private JFrame frame;
	private JComboBox<RodiniaBin> workloadcbox;
	private JComboBox<DataSetSize> datacbox;
	
	private ClientConnectionHandler cmdHandler;
	
	public ClientGui() {
		initialise();
	}
	
	private void initialise() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	        	assert cmdHandler != null : "Command handler must not be null at this point.";
	        	cmdHandler.closeConnections();
	            System.exit(0);
	        }
	    });
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.columnWidths = new int[]{0};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder(null, "Workload Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_controlpanel = new GridBagConstraints();
		gbc_controlpanel.weighty = 2;
		gbc_controlpanel.fill = GridBagConstraints.BOTH;
		gbc_controlpanel.gridx = 0;
		gbc_controlpanel.gridy = 0;
		frame.getContentPane().add(controlPanel, gbc_controlpanel);
		
		workloadcbox = new JComboBox<>();
		workloadcbox.setModel(new DefaultComboBoxModel<>(RodiniaBin.values()));
		controlPanel.add(workloadcbox);
		
		datacbox = new JComboBox<>();
		datacbox.setModel(new DefaultComboBoxModel<>(DataSetSize.values()));
		controlPanel.add(datacbox);
		
		JButton btnRun = new JButton("run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	        	assert cmdHandler != null : "Command handler must not be null at this point.";
				
				UserCommand cmd = receiveCommand();
				Hashtable<ServerConnection, ExecutionItem> executionMap = new Hashtable<>();

				if(!cmdHandler.handleUserCommand(cmd, executionMap)) {
					if(cmd.getType() == CmdType.EXIT) {
						JOptionPane.showMessageDialog(null, "Shutting down client.", 
								"Exit", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "Server communication failed. Shutting Down client.", 
								"Communication Error", JOptionPane.ERROR_MESSAGE);
					}
					frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
				}
				
				updateDisplay(executionMap, cmd);
			}
		});
		controlPanel.add(btnRun);

		JPanel wlPanel = new JPanel();
		wlPanel.setBorder(new TitledBorder(null, "Execution Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_wlpanel = new GridBagConstraints();
		gbc_wlpanel.weighty = 8;
		gbc_wlpanel.fill = GridBagConstraints.BOTH;
		gbc_wlpanel.gridx = 0;
		gbc_wlpanel.gridy = 1;
		frame.getContentPane().add(wlPanel, gbc_wlpanel);
	}
	
	@Override
	public void run(ClientConnectionHandler cmdHandler) {
		if(cmdHandler == null)
			throw new IllegalArgumentException("Given command handler must not be null.");
		
		this.cmdHandler = cmdHandler;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public UserCommand receiveCommand() {
		// TODO add command handling
		
		return new ExitCmd();
	}

	@Override
	public void updateDisplay(
			Hashtable<ServerConnection, ExecutionItem> executionMap, UserCommand cmd) {
		
		// TODO add graph update
	}

}
