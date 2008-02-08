package org.lunivore.tyburn;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.junit.Test;
import org.lunivore.tyburn.threaded.TimeoutException;
import org.mockito.CustomMatcher;


public class WindowControlBehaviour extends Behaviour {

    @Test
	public void shouldClickAButtonOnAWindow() throws Exception {
		checkForHeadless();
		WindowControl control = new WindowControl(AFrame.FRAME_NAME);
        
		try {
            AFrame frame = new AFrame();
    		frame.setName(AFrame.FRAME_NAME);
    		
    		JButton button = new JButton("Press Me!");
    		button.setName("a.button");
    		
    		ActionListener actionListener = mock(ActionListener.class);
    		
    		button.addActionListener((ActionListener)actionListener);    		
    		
    		frame.getContentPane().add(button);
    		frame.pack();
    		frame.setVisible(true);
    		
    		control.clickButton("a.button");
    		
    		verify(actionListener).actionPerformed(With.a(ActionEvent.class));
    		
        } finally {
            control.closeWindow();
        }
	}

    @Test
	public void shouldEnterTextIntoTextComponents() throws Exception {
        checkForHeadless();
        WindowControl control = new WindowControl(AFrame.FRAME_NAME);

        try {
            AFrame frame = new AFrame();
    		
    		JTextComponent textField = new JTextField();
    		textField.setName("a.textfield");
    		
    		JTextComponent textArea = new JTextArea();
    		textArea.setName("b.textarea");
    		
    		frame.getContentPane().setLayout(new FlowLayout());
    		
    		frame.getContentPane().add(textField);
    		frame.getContentPane().add(textArea);
    		frame.pack();
    		
    		
    		frame.setVisible(true);
    		control.enterText("a.textfield", "Text1");
    		control.enterText("b.textarea", "Text2");
    		
    		ensureThat(textField.getText(), eq("Text1"));
    		ensureThat(textArea.getText(), eq("Text2"));
            
        } finally {
            control.closeWindow();
        }
	}
    
    @Test
	public void shouldEnterTextIntoAComboBox() throws Exception {
        checkForHeadless();
        WindowControl control = new WindowControl(AFrame.FRAME_NAME);
        
        try {
            AFrame frame = new AFrame();
            
            JComboBox comboBox = new JComboBox(new Object[] {"horse", "cow", "sheep"});
            comboBox.setName("a.combobox");
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(comboBox);
            frame.pack();
            frame.setVisible(true);
            
            control.enterText("a.combobox", "cow");
            
            ensureThat(comboBox.getSelectedItem(), eq((Object)"cow"));
            
        } finally {
            control.closeWindow();
        }
    }

    @Test
    public void shouldEnterTextIntoAnEditableComboBox() throws Exception {
        checkForHeadless();
        WindowControl control = new WindowControl(AFrame.FRAME_NAME);
        
        try {
            AFrame frame = new AFrame();
            
            JComboBox comboBox = new JComboBox(new Object[] {"horse", "cow", "sheep"});
            comboBox.setName("a.combobox");
            comboBox.setEditable(true);
            comboBox.setSelectedItem("horse");
            frame.getContentPane().setLayout(new FlowLayout());
            frame.getContentPane().add(comboBox);
            frame.pack();
            frame.setVisible(true);

            control.enterText("a.combobox", "cow");
            
            // Due to the different focusing behaviour of eg: macs, PCs, this could say
            // "cow" or "cowhorse"
            ensureThat(comboBox.getEditor().getItem().toString(), contains("cow"));
            
        } finally {
            control.closeWindow();
        }
    }

    @Test
    public void shouldFindComponent() throws ComponentFinderException, TimeoutException  {
	    checkForHeadless();
	    WindowControl control = new WindowControl(AFrame.FRAME_NAME);
        try {

            AFrame frame = new AFrame();
    		
    		JPanel panel = new JPanel();
    		panel.setName("a.panel");
    		
    		frame.getContentPane().add(panel);
    		frame.setVisible(true);
    		
    		ensureThat(control.findComponent("a.panel"), eq((Component)panel));
        } finally {
            control.closeWindow();
        }
	}

    @Test
    public void shouldCloseWindows() throws TimeoutException {
        checkForHeadless();
        WindowControl control = new WindowControl(AFrame.FRAME_NAME);

        AFrame frame = new AFrame();
        
        control.closeWindow();
        ensureThat(!frame.isShowing());
        frame.dispose();
    }

    @Test
    public void shouldSimulateKeyPressesForInputMap() throws TimeoutException {
        checkForHeadless();
		WindowControl control = new WindowControl(AFrame.FRAME_NAME);
		
        try {
            AFrame frame = new AFrame();            

            Action action = mock(Action.class);
            stub(action.isEnabled()).toReturn(true);
            
            frame.contentPanel.getActionMap().put(AFrame.ACTION_KEY, (Action) action);
            
            control.pressKeychar(' ');
            
            verify(action).actionPerformed(With.an(ActionEvent.class));
        } finally {
            control.closeWindow();
        }
    }

    @Test
    public void shouldSimulateKeyPressesForKeyListeners() throws TimeoutException {
        checkForHeadless();
        WindowControl control = new WindowControl(AFrame.FRAME_NAME);
        
        try {
            AFrame frame = new AFrame();
            
            CustomMatcher<KeyEvent> matchesSpaceKey = new CustomMatcher<KeyEvent>() {
                public boolean matches(KeyEvent arg) {
                    return ((KeyEvent)arg).getKeyCode() == KeyEvent.VK_SPACE ||
                        ((KeyEvent)arg).getKeyChar() == ' ';
                }
            };
            KeyListener keyListener = mock(KeyListener.class);
            frame.contentPanel.addKeyListener((KeyListener) keyListener);    
            
            control.pressKeychar(' ');
            
            verify(keyListener).keyReleased(With.argThat(matchesSpaceKey));
        } finally {
            control.closeWindow();
        }
    }

    private void checkForHeadless() {
        new HeadlessChecker().check();
    }

    public static class AFrame extends JFrame {
		private static final long serialVersionUID = 1L;
		private static final String FRAME_NAME = "a.window";
        private static final String ACTION_KEY = "AFrame.action";
        
        
        private JPanel contentPanel = new JPanel();
        public AFrame() {
            setName(FRAME_NAME);
            setContentPane(contentPanel);

            contentPanel.getInputMap().put(KeyStroke.getKeyStroke(' '), ACTION_KEY);
            
            this.pack();
            this.setVisible(true);
            
            this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
    }
}
