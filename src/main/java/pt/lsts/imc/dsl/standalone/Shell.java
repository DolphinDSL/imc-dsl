package pt.lsts.imc.dsl.standalone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import groovy.lang.GroovyShell;


public class Shell {
	private CompilerConfiguration config;
	private ImportCustomizer imports;
	private GroovyShell shell;
	private Thread running;
	private Console imcDSL;
	//Script output
    private Writer  scriptOutput;
    private  PrintWriter ps;
    private StringBuffer buffer;
	
	public Shell(Console console){
		config = new CompilerConfiguration();
		imports = new ImportCustomizer();
		imports.addStarImports("pt.lsts.imc.dsl","pt.lsts.imc");
		imports.addStaticStars("pt.lsts.imc.dsl.standalone.PlanStandalone");
		config.addCompilationCustomizers(imports);
		shell = new GroovyShell(config);
		imcDSL = console;
		setupOutput();
	}

	private void setupOutput() {
		buffer = new StringBuffer();
        scriptOutput = new Writer(){

            @Override
            public void write(char[] cbuf){
                buffer.append(cbuf);
            }
            @Override
            public void write(int c){
              //console.appendOutput(String.valueOf((char)c));
              buffer.append((char)c);
            }
            @Override
            public void write(String str){
                //System.out.println("Str: "+ str);
                //console.appendOutput(str);
                buffer.append(str);
            }

            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                    //System.out.println("Char buffer: "+ String.valueOf(cbuf, off, len));
                    //console.appendOutput(String.valueOf(cbuf, off, len));
                    buffer.append(cbuf, off, len);
            }

            @Override
            public void flush() throws IOException{
                imcDSL.appendOutput(buffer.toString());
                buffer = new StringBuffer();
                //System.out.println("Flushed!");

            }

            @Override
            public void close() throws IOException {
            }
    };
    ps = new PrintWriter(scriptOutput,true);
		
	}

	public void run(String fileName) {
	    
		if(running == null){
		    	System.out.println("Running "+fileName);
		    	running = new Thread(){
		    		 @Override
		             public void run() {
		    			 try {
		    				shell.setVariable("out",ps);
							shell.evaluate(new File(fileName));
							running = null;
						} catch(FileNotFoundException e1){
	    			    	System.out.println( "The script file does not exist.\n Please verify the path and name of the script.");
	    			    	e1.printStackTrace();
	    			    } 
		    			 catch (CompilationFailedException | IOException e) {
							System.out.println("Error running "+fileName+":\n\t"+e.getMessage());
							imcDSL.appendOutput("Error running "+fileName+":\nPlease verify the error description on the terminal.\n");
						}
		    			 finally {
		    				 running = null;
		    				 imcDSL.stop.setEnabled(false);
		    			 }
		    		 }
		    	};
				running.start();
		}
		else {
	    	System.out.println("Another script is already running.\n");
	    	imcDSL.appendOutput("Another script is already running.\nPlease stop the execution before running a new script.\n");
		}
		    
	  }
	
	public void stop() throws InterruptedException{
		if(running != null && running.isAlive()){
			running = null;
			running.interrupt();
		}
	}

	public void exportPlan(String path,String extension) {
		
		if(extension.equalsIgnoreCase("json")){
			File exported = new File(path);
			//TODO
			
		}
		else if(extension.equalsIgnoreCase("xml")){
			
		} else
			try {
				stop();
			} catch (InterruptedException e) {
				imcDSL.appendOutput("Interruped script execution.\n");
			}
			System.out.println("Unsupported export file extension: "+extension+"\n");
			imcDSL.appendOutput("Unsupported export file extension: "+extension+"\n");
		
	}
	  
  
}
