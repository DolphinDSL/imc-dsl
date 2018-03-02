package pt.lsts.imc.dsl.standalone;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
//import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.RUndoManager;

@SuppressWarnings("serial")
public class Console extends JFrame{
	
	static void saveToFile(String absolutePath, String text) {
		File file = new File(absolutePath);
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(file,false);
			byte[] bytes = text.getBytes();
			fo.write(bytes);
			fo.close();
		} catch (FileNotFoundException e1) {
			System.err.println("File not found");
			e1.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("Error Handling file: "+file.getName());
			e.printStackTrace();
		}

		
		
	}
	//Adapted from Neptus
    public static String getFileAsString(File f) {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;
        byte[] ba = null;
        String result = null;
        try {
            fis = new FileInputStream(f);
            ba = new byte[1024];
            while ((len = fis.read(ba)) > 0) {
                bos.write(ba, 0, len);
            }
            ba = bos.toByteArray();
            try {
                result = new String(ba, "UTF-8");
            }
            catch (UnsupportedEncodingException e1) {
                System.out.println(e1);
                result = new String(ba, "UTF-8");
            }
        }
        catch (FileNotFoundException e) {
        	 e.printStackTrace();
        	 
        }
        catch (IOException e) {
        	 e.printStackTrace();
        	 
        }
        finally {
            if (fis != null)
                try {
                    fis.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
        }

        return result;
    }

	protected static File scriptDir = new File("examples");
	protected static File script = null;//= new File("examples/plan");

	private Shell shell; 
	Border border;
	Border fontBorder;
	JScrollPane outputPanel;
	JTextArea output;
	RSyntaxTextArea editor; 
	JButton select,execButton,stop,saveFile,undo,redo;
	RTextScrollPane scroll;
	SpinnerModel model = new SpinnerNumberModel(14, 2, 32, 1);     
	JSpinner spinner = new JSpinner(model);
	RUndoManager undoManager; 
	
	public Console(){
    editor = new RSyntaxTextArea();
    undoManager   = new RUndoManager(editor);
    editor.getDocument().addUndoableEditListener(new UndoableEditListener() {
        
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            undoManager.addEdit(e.getEdit());                
        }
    });

    editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
    editor.setCodeFoldingEnabled(true);
    scroll = new RTextScrollPane(editor);
    
    Action saveAction = new AbstractAction("Save Script as",getIcon("icons/save.png",15,15)) {

        @Override
        public void actionPerformed(ActionEvent e) {
            File directory = scriptDir;
            final JFileChooser fc = new JFileChooser(directory);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            // Demonstrate "Save" dialog:
            int rVal = fc.showDialog(Console.this,"Save");
            if (rVal == JFileChooser.APPROVE_OPTION) {
                script = fc.getSelectedFile();
                Console.saveToFile(script.getAbsolutePath(), editor.getText());
            }

        }

    };
    
//    Action exportAction = new AbstractAction("Export Plan",getIcon("icons/save.png",15,15)) {
//
//        @Override
//        public void actionPerformed(ActionEvent e) {
//            if(shell!=null && script!=null){
//            	File directory = scriptDir;
//                final JFileChooser fc = new JFileChooser(directory);
//                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//                FileNameExtensionFilter filter = new FileNameExtensionFilter("Export Format","xml","json");
//                FileNameExtensionFilter filter1 = new FileNameExtensionFilter("xml","xml");
//                FileNameExtensionFilter filter2 = new FileNameExtensionFilter("json","json");
//                fc.setAcceptAllFileFilterUsed(false);
//                fc.setFileFilter(filter);
//                fc.addChoosableFileFilter(filter1);
//                fc.addChoosableFileFilter(filter2);
//                // Demonstrate "Save" dialog:
//                int rVal = fc.showDialog(Console.this,"Export");
//                if (rVal == JFileChooser.APPROVE_OPTION) {
//                   String path  = fc.getSelectedFile().getAbsolutePath();
//                   String file = path.substring(path.lastIndexOf("/"));
//                   String extension = file.substring(file.indexOf("."));
//                   shell.exportPlan(path,extension);
//                }
//            	
//            }
//        }
//    };

   
    
    //Output panel
    output = new JTextArea();
    border = BorderFactory.createTitledBorder("Script Output");
    //output.setBorder(border);
    output.setEditable(false);
    output.setVisible(true);
    output.append("IMC DSL for scripting plans\n");
    outputPanel = new JScrollPane(output);
    outputPanel.setBorder(border);

    
    
    Action selectAction = new AbstractAction("Select Script",getIcon("icons/filenew.png",15,15)) {

        @Override
        public void actionPerformed(ActionEvent e) {
            File directory = scriptDir;//new File("conf/dolphin/rep17");
            final JFileChooser fc = new JFileChooser(directory);
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = fc.showOpenDialog(Console.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                script = fc.getSelectedFile();
                if(fc.getSelectedFile().exists()){
                    System.out.println("Opening: " + script.getName() + "\n");
                    editor.setText(Console.getFileAsString(script));
                    editor.discardAllEdits();
                }
                else {
                    try {
                        if(script.createNewFile()){

                            editor.setText(getFileAsString(script));
                            System.out.println("Creating new script file: " + script.getName() + "\n");
                        }
                    }
                    catch(IOException e1){
                    	System.out.println("Error creating new script file\n");
                    }

                }
            }
        }
    };
    
    
    
    // Buttons AbstractActions
    Action execAction = new AbstractAction("Execute",getIcon("icons/forward.png",15,15)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            stop.setEnabled(true);
            if(script!=null)
            	Console.saveToFile(script.getAbsolutePath(), editor.getText());
            else {
                JFileChooser fc = new JFileChooser(scriptDir);
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int returnVal = fc.showOpenDialog(Console.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    script = fc.getSelectedFile();
                    if(!fc.getSelectedFile().exists()){
                        try {
                            if(script.createNewFile()){
                            	Console.saveToFile(script.getAbsolutePath(), editor.getText());
                                System.out.println("Creating new script file: " + script.getName() + "\n");
                            }
                        }
                        catch(IOException e1){
                        	System.out.println("Error creating new script file\n");
                        }

                    }
                }
            }
            shell.run(script.getAbsolutePath());
        }
    };

    Action stopAction = new AbstractAction("Stop",getIcon("icons/stop.png",15,15)) {
        @Override
        public void actionPerformed(ActionEvent e) {   
            output.append("Stopping script!\n");
            System.out.println("Stopping script!\n");
            try {
				shell.stop();
			} catch (InterruptedException e1) {
				appendOutput("Interruped script execution.\n");
			}
        }
    };
    
    Action undoAction = new AbstractAction("Undo",getIcon("icons/undo.png",15,15)) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(undoManager.canUndo())
                undoManager.undo();
            else
            	System.out.println("Unnable to undo typing at script editor");
        }
    };
    
    
    Action redoAction = new AbstractAction("Redo",getIcon("icons/redo.png",15,15)) {
        @Override
        public void actionPerformed(ActionEvent e) {   
            if(undoManager.canRedo())
                undoManager.redo();
            else
                System.out.println("Unnable to redo typing at script editor");
        }
    };
    
    //Buttons
    execButton = new JButton(execAction);
    select     = new JButton(selectAction);
    stop       = new JButton(stopAction);
    //export     = new JButton(exportAction);
    saveFile   = new JButton(saveAction);
    undo       = new JButton(undoAction);
    redo       = new JButton(redoAction);
    stop.setEnabled(false);


    JButton clear = new JButton(new AbstractAction("Clear Output",getIcon("icons/clear.png",15,15)) {

        @Override
        public void actionPerformed(ActionEvent e) {
            output.setText("");

        }
    });

    //Console layout
    JPanel top = new JPanel(new BorderLayout());
    JPanel buttons = new JPanel();
    buttons.setPreferredSize(new Dimension(600, 100));
    
    
    //Font size spinner
    ChangeListener listener = new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent e) {
                editor.setFont(new Font(Font.MONOSPACED, 0, (int) spinner.getValue()));
            
        }
      };        
    spinner.addChangeListener(listener);
    fontBorder = BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),"Font");
    ((TitledBorder) fontBorder).setTitlePosition(TitledBorder.BOTTOM);
    spinner.setBorder(fontBorder); 
    spinner.setPreferredSize(new Dimension(65,35));
    
    
    //middle section
    buttons.add(select);
    buttons.add(saveFile);
    //buttons.add(export);
    buttons.add(execButton);
    buttons.add(stop);
    buttons.add(spinner);
    buttons.add(undo);
    buttons.add(redo);
    buttons.setBorder(BorderFactory.createTitledBorder("Controls"));
    
    //onHover tool tip text
    select.setToolTipText("Select File");
    //export.setToolTipText("Export Plan Specification");
    spinner.setToolTipText("Adjust Editor's Font Size");
    saveFile.setToolTipText("Save Current File as...");
    undo.setToolTipText("Ctrl+Z");
    redo.setToolTipText("Ctrl+Y");

    outputPanel.setPreferredSize(new Dimension(600, 100));
    top.setPreferredSize(new Dimension(600, 400));
    top.add(outputPanel,BorderLayout.SOUTH);
    top.add(scroll,BorderLayout.CENTER);

    
    JPanel bottom = new JPanel(new BorderLayout());
    bottom.setPreferredSize(new Dimension(600, 120));
    bottom.add(clear,BorderLayout.SOUTH);
    bottom.add(buttons,BorderLayout.CENTER);

    add(bottom,BorderLayout.SOUTH);
    add(top,BorderLayout.CENTER);
    
    //Initialize shell and attach it to this console
    shell = new Shell(this);
	}
	
	static ImageIcon getIcon(String path,int width,int height){
		Image img = new ImageIcon(path).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
		return new ImageIcon(img);
	}
    /**
     * @param string script execution output
     */
    public void appendOutput(String string) {

            SwingUtilities.invokeLater(new Runnable(){
                public void run(){    
                    try {
                        output.getDocument().insertString(output.getDocument().getLength(), string, null);
                        output.setCaretPosition(output.getDocument().getLength());
                        //System.out.println("appended"+"\t"+string);
                    }
                    catch (BadLocationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                  }
                    });
    }
    

	public static void main(String[] args) {
		 Console imcDSL = new Console();
		 EventQueue.invokeLater(() -> {
			 	 
			 imcDSL.setSize(600, 600);
			 imcDSL.setLocationRelativeTo(null);
			 imcDSL.setDefaultCloseOperation(EXIT_ON_CLOSE);
			 imcDSL.setTitle("IMC DSL");
			 imcDSL.setIconImage(new ImageIcon("icons/imc.png").getImage());
			 imcDSL.setVisible(true);
			 
	        });
		 imcDSL.addWindowListener(new WindowAdapter()
		 {
		     public void windowClosing(WindowEvent e)
		     {
		    	 if(imcDSL.shell!=null)
					try {
						imcDSL.shell.stop();
					} catch (InterruptedException e1) {
					}
		     }
		 });
		
		
	}


}
