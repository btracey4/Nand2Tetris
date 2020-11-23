package com.btracey.vmtranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VMTranslator {
	private Parser parser;
	private CodeWriter codeWriter;
	private File vmFileOrDirectory;
	private List<String> fileNames;

	private void setCodeWriter(CodeWriter codeWriter){
		this.codeWriter = codeWriter;
	}

	private void setParser(Parser parser){
		this.parser = parser;
	}

	public void initialize(String inputFileOrDirectoryName) throws IOException{		
		vmFileOrDirectory = new File(inputFileOrDirectoryName);
		fileNames = new ArrayList<>();
		String fileNameNoPrefix;
		String outputFileName;
		if(vmFileOrDirectory.isDirectory()) {
			fileNameNoPrefix = vmFileOrDirectory.getName();
			for(String fileName : vmFileOrDirectory.list()) {
				if(fileName.endsWith(".vm"))
					fileNames.add(vmFileOrDirectory.getAbsolutePath() + "\\" + fileName);
			}
			outputFileName = vmFileOrDirectory.getAbsolutePath() + "\\" + fileNameNoPrefix + ".asm";
		} else {
			fileNameNoPrefix = this.stripFileName(vmFileOrDirectory.getName());
			outputFileName = vmFileOrDirectory.getParent() + "\\" + fileNameNoPrefix + ".asm";
			fileNames.add(vmFileOrDirectory.getAbsolutePath());
		}
		
		setCodeWriter(new CodeWriter(new File(outputFileName)));
	}
	
	public void process() throws IOException{
		codeWriter.setFileName("Sys");
		codeWriter.writeInit();
		for(String fileName : fileNames) {
			File currentFile = new File(fileName);
			parser = new Parser(currentFile);
			codeWriter.setFileName(this.stripFileName(currentFile.getName()));
			while(parser.hasMoreCommands()){
				parser.advance();
				codeWriter.writeCurrentLine(parser.currentLine());
				if(parser.commandType().equals(Parser.COMMENT)) {continue;}
	
				String commandType = parser.commandType();
				String arg1;
				int arg2;
				switch (commandType) {
				case Parser.C_PUSH:
				case Parser.C_POP:
					arg1 = parser.arg1();
					arg2 = parser.arg2();
					codeWriter.writePushPop(commandType, arg1, arg2);
					break;
				case Parser.C_ARITHMETIC:
					arg1 = parser.arg1();
					codeWriter.writeArithmetic(arg1);
					break;
				case Parser.C_FUNCTION:
					arg1 = parser.arg1();
					arg2 = parser.arg2();
					codeWriter.writeFunction(arg1, arg2);
					break;
				case Parser.C_CALL:
					arg1 = parser.arg1();
					arg2 = parser.arg2();
					codeWriter.writeCall(arg1, arg2);
					break;
				case Parser.C_LABEL:
					arg1 = parser.arg1();
					codeWriter.writeLabel(arg1);
					break;
				case Parser.C_GOTO:
					arg1 = parser.arg1();
					codeWriter.writeGoto(arg1);
					break;
				case Parser.C_IF:
					arg1 = parser.arg1();
					codeWriter.writeIf(arg1);
					break;
				case Parser.C_RETURN:
					codeWriter.writeReturn();
					break;
				default:
					throw new IllegalStateException("Parser should not have this invalid command: " + commandType);
				}
			}
		}
		codeWriter.close();
	}

	private String stripFileName(String fileName) {
		return fileName.substring(0,fileName.length() -3);
	}

	public static void main(String[] args){
		if(args.length != 1){
			throw new IllegalArgumentException("Expecting 1 argument, directory name or file name of .vm file!");
		}
		VMTranslator translator = new VMTranslator();

		try{
			translator.initialize(args[0]);
			translator.process();
		} catch(IOException e){
			System.out.println("Error: " +e);
		}
	}
}