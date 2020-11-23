package com.btracey.vmtranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

public class Parser{
	public static final String C_ARITHMETIC = "C_ARITHMETIC";
	public static final String C_PUSH = "C_PUSH";
	public static final String C_POP = "C_POP";
	public static final String C_LABEL = "C_LABEL";
	public static final String C_GOTO = "C_GOTO";
	public static final String C_IF = "C_IF";
	public static final String C_FUNCTION = "C_FUNCTION";
	public static final String C_RETURN = "C_RETURN";
	public static final String C_CALL = "C_CALL";

	public static final String ADD = "add";
	public static final String SUB = "sub";
	public static final String NEG = "neg";
	public static final String EQ = "eq";
	public static final String GT = "gt";
	public static final String LT = "lt";
	public static final String AND = "and";
	public static final String OR = "or";
	public static final String NOT = "not";

	public static final String STATIC = "static";
	public static final String THIS = "this";
	public static final String LOCAL = "local";
	public static final String ARGUMENT = "argument";
	public static final String THAT = "that";
	public static final String CONSTANT = "constant";
	public static final String POINTER = "pointer";
	public static final String TEMP = "temp";

	public static final String COMMENT = "comment";

	private BufferedReader vmFileReader;
	private String currentLine;
	private String arg1;
	private int arg2;
	private String commandType;

	public Parser(File vmFile) throws FileNotFoundException{
		this.vmFileReader = new BufferedReader(new FileReader(vmFile));
	}

	public boolean hasMoreCommands() throws IOException{
		currentLine = vmFileReader.readLine();
		return !(currentLine == null || currentLine.split(" ").length == 0);
	}

	public void advance(){
		if(currentLine.contains("//")){
			String[] commentLine = currentLine.split("//");
			if(commentLine.length == 1 || (commentLine.length > 1 && commentLine[0].isBlank())) {
				commandType = COMMENT;
				return;
			}
			else if(commentLine.length > 1) {
				currentLine = commentLine[0];
			}
		} else if(currentLine.isBlank()) {
			commandType = COMMENT;
			return;
		}
		String[] commands = currentLine.split(" ");
		switch(commands[0]){
		case ADD:
		case SUB:
		case NEG:
		case EQ:
		case GT:
		case LT:
		case AND:
		case OR:
		case NOT:
			commandType = C_ARITHMETIC;
			arg1 = commands[0];
			break;
		case "push":
			commandType = C_PUSH;
			arg1 = commands[1];
			arg2 = Integer.parseInt(commands[2].strip());
			break;
		case "pop":
			commandType = C_POP;
			arg1 = commands[1];
			arg2 = Integer.parseInt(commands[2].strip());
			break;
		case "label":
			commandType = C_LABEL;
			arg1 = commands[1];
			break;
		case "goto":
			commandType = C_GOTO;
			arg1 = commands[1];
			break;
		case "if-goto":
			commandType = C_IF;
			arg1 = commands[1];
			break;
		case "function":
			commandType = C_FUNCTION;
			arg1 = commands[1];
			arg2 = Integer.parseInt(commands[2].strip());
			break;
		case "return":
			commandType = C_RETURN;
			break;
		case "call":
			commandType = C_CALL;
			arg1 = commands[1];
			arg2 = Integer.parseInt(commands[2].strip());
			break;
		}
	}

	public String commandType(){
		return commandType;
	}

	public String arg1(){
		return arg1;
	}

	public int arg2(){
		return arg2;
	}

	public String currentLine(){
		return currentLine;
	}
}