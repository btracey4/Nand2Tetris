package com.btracey.vmtranslator;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class CodeWriter{
	private Writer writer;
	private long equalityCalls = 0L;
	private long greaterThanCalls = 0L;
	private long lessThanCalls = 0L;
	private long functionCalls = 0L;
	private Map<String, Integer> segmentRegisterMap;
	private String fileName;
	private String pushElementString;

	public CodeWriter(File outputFile) throws IOException{
		writer = new FileWriter(outputFile);
		initSegmentRegisterMap();
		initPushElementString();
	}

	public void setFileName(String fileName){
		this.fileName = fileName;
		this.equalityCalls = 0l;
		this.greaterThanCalls = 0l;
		this.lessThanCalls = 0l;
		this.functionCalls = 0l;
	}

	public void writeInit() throws IOException{
		writer.append("@256\n")
		.append("D=A\n")
		.append("@SP\n")
		.append("M=D\n");
		this.writeCall("Sys.init", 0);
	}

	public void writeLabel(String label) throws IOException{
		writer.append("("+label+")\n");
	}

	public void writeGoto(String label) throws IOException{
		writer.append("@"+label+"\n")
		.append("0;JMP\n");
	}

	public void writeIf(String label) throws IOException{
		writer.append("@R0\n")
		.append("A=M-1\n")
		.append("D=M\n")
		.append("@R0\n")
		.append("M=M-1\n")
		.append("@"+label+"\n")
		.append("D;JNE\n");
	}

	public void writeCall(String functionName, int numArgs) throws IOException{
		String returnAddress = fileName+"$ret."+functionCalls;
		functionCalls++;
		writer.append("@"+returnAddress+"\n")
			.append("D=A\n")
			.append(pushElementString)
			.append("@LCL\n")
			.append("D=M\n")
			.append(pushElementString)
			.append("@ARG\n")
			.append("D=M\n")
			.append(pushElementString)
			.append("@THIS\n")
			.append("D=M\n")
			.append(pushElementString)
			.append("@THAT\n")
			.append("D=M\n")
			.append(pushElementString)
			.append("@SP\n")
			.append("D=M\n")
			.append("@5\n")
			.append("D=D-A\n")
			.append("@"+numArgs+"\n")
			.append("D=D-A\n")
			.append("@ARG\n")
			.append("M=D\n")
			.append("@SP\n")
			.append("D=M\n")
			.append("@LCL\n")
			.append("M=D\n");
		this.writeGoto(functionName);
		writer.append("("+returnAddress+")\n");
		
	}

	public void writeReturn() throws IOException{
		writer.append("@LCL\n")
		.append("D=M\n")
		.append("@R13\n")
		.append("M=D\n") // R13 = endFrame
		.append("@5\n")
		.append("D=D-A\n")
		.append("A=D\n")
		.append("A=M\n")
		.append("D=A\n")
		.append("@R14\n")
		.append("M=D\n") // R14 = retAddr
		.append("@SP\n")
		.append("A=M-1\n")
		.append("D=M\n")
		.append("@ARG\n")
		.append("A=M\n")
		.append("M=D\n") // *ARG = pop
		.append("@ARG\n")
		.append("D=M+1\n")
		.append("@SP\n")
		.append("M=D\n") // SP = ARG + 1
		.append("@R13\n")
		.append("A=M-1\n")
		.append("D=M\n")
		.append("@THAT\n")
		.append("M=D\n") // THAT = *(endFrame-1)
		.append("@R13\n")
		.append("D=M\n")
		.append("@2\n")
		.append("A=D-A\n")
		.append("D=M\n")
		.append("@THIS\n")
		.append("M=D\n") // THIS = *(endFrame-2)
		.append("@R13\n")
		.append("D=M\n")
		.append("@3\n")
		.append("A=D-A\n")
		.append("D=M\n")
		.append("@ARG\n")
		.append("M=D\n") // ARG = *(endFrame-3)
		.append("@R13\n")
		.append("D=M\n")
		.append("@4\n")
		.append("A=D-A\n")
		.append("D=M\n")
		.append("@LCL\n")
		.append("M=D\n") // LCL = *(endFrame-4)
		.append("@R14\n")
		.append("A=M\n")
		.append("0;JMP\n"); // goto retAddr
		
	}

	public void writeFunction(String functionName, int numLocals) throws IOException{
		writer.append("("+functionName+")\n");
		for(int i=0;i<numLocals;i++)
			this.writePush(Parser.CONSTANT, 0);
	}


	public void writeArithmetic(String command) throws IOException{
		switch (command) {
		case Parser.ADD:  // (sp-2)* = (sp-2)* + (sp-1)*; sp--
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("A=A-1\n")
			.append("M=D+M\n")
			.append("@R0\n")
			.append("M=M-1\n");
			break;
		case Parser.SUB:  // (sp-2)* = (sp-2)* - (sp-1)*; sp--
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("A=A-1\n")
			.append("M=M-D\n")
			.append("@R0\n")
			.append("M=M-1\n");
			break;
		case Parser.NEG:  // (sp-1)* = 0 - (sp-1)*
			writer.append("@0\n")
			.append("D=A\n")
			.append("@R0\n")
			.append("A=M-1\n")
			.append("M=D-M\n");
			break;
		case Parser.EQ:   //  (sp-2)* = (sp-2)* == (sp-1)*
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("A=A-1\n")
			.append("D=M-D\n")
			.append("@EQUAL_"+equalityCalls+"\n")
			.append("D;JEQ\n")
			.append("@NOT_EQUAL_"+equalityCalls+"\n")
			.append("0;JMP\n")
			.append("\n(EQUAL_"+equalityCalls+")\n")
			.append("\t@R0\n")
			.append("\tA=M-1\n")
			.append("\tA=A-1\n")
			.append("\tM=-1\n")
			.append("\t@END_COMPARISON_"+equalityCalls+"\n")
			.append("\t0;JMP\n")
			.append("\n(NOT_EQUAL_"+equalityCalls+")\n")
			.append("\t@R0\n")
			.append("\tA=M-1\n")
			.append("\tA=A-1\n")
			.append("\tM=0\n")
			.append("\t@END_COMPARISON_"+equalityCalls+"\n")
			.append("\n(END_COMPARISON_"+equalityCalls+")\n")
			.append("\t@R0\n")
			.append("\tM=M-1\n");
			equalityCalls++;
			break;
		case Parser.GT:   // (sp-2)* = (sp-2)* > (sp-1)*
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("A=A-1\n")
			.append("D=M-D\n")
			.append("@GT_"+greaterThanCalls+"\n")
			.append("D;JGT\n")
			.append("@NOT_GT_"+greaterThanCalls+"\n")
			.append("0;JMP\n")
			.append("\n(GT_"+greaterThanCalls+")\n")
			.append("\t@R0\n")
			.append("\tA=M-1\n")
			.append("\tA=A-1\n")
			.append("\tM=-1\n")
			.append("\t@END_GT_"+greaterThanCalls+"\n")
			.append("\t0;JMP\n")
			.append("\n(NOT_GT_"+greaterThanCalls+")\n")
			.append("\t@R0\n")
			.append("\tA=M-1\n")
			.append("\tA=A-1\n")
			.append("\tM=0\n")
			.append("\t@END_GT_"+greaterThanCalls+"\n")
			.append("\n(END_GT_"+greaterThanCalls+")\n")
			.append("\t@R0\n")
			.append("\tM=M-1\n");
			greaterThanCalls++;
			break;
		case Parser.LT:   // (sp-2)* = (sp-2)* < (sp-1)*
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("A=A-1\n")
			.append("D=M-D\n")
			.append("@LT_"+lessThanCalls+"\n")
			.append("D;JLT\n")
			.append("@NOT_LT_"+lessThanCalls+"\n")
			.append("0;JMP\n")
			.append("\n(LT_"+lessThanCalls+")\n")
			.append("\t@R0\n")
			.append("\tA=M-1\n")
			.append("\tA=A-1\n")
			.append("\tM=-1\n")
			.append("\t@END_LT_"+lessThanCalls+"\n")
			.append("\t0;JMP\n")
			.append("\n(NOT_LT_"+lessThanCalls+")\n")
			.append("\t@R0\n")
			.append("\tA=M-1\n")
			.append("\tA=A-1\n")
			.append("\tM=0\n")
			.append("\t@END_LT_"+lessThanCalls+"\n")
			.append("\n(END_LT_"+lessThanCalls+")\n")
			.append("\t@R0\n")
			.append("\tM=M-1\n");
			lessThanCalls++;
			break;
		case Parser.AND:  // (sp-2)* = (sp-2)* && (sp-1)*
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("A=A-1\n")
			.append("M=D&M\n")
			.append("@R0\n")
			.append("M=M-1\n");
			break;
		case Parser.OR:   // (sp-2)* = (sp-2)* || (sp-1)*
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("A=A-1\n")
			.append("M=D|M\n")
			.append("@R0\n")
			.append("M=M-1\n");
			break;
		case Parser.NOT:  // (sp-1)* = !(sp-1)*
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("M=!M\n");
			break;
		default:
			assert false : "Parser should not have found the invalid command: " + command;
		}
	}

	public void writePushPop(String command, String segment, int index) throws IOException{
		if(command.equals(Parser.C_PUSH)){
			writePush(segment, index);
		} else if(command.equals(Parser.C_POP)){
			writePop(segment, index);
		}
	}

	private void writePush(String segment, int index) throws IOException{
		switch(segment){
		case Parser.CONSTANT:
			writer.append("@"+index+"\n")
			.append("D=A\n")
			.append(pushElementString);
			break;
		case Parser.STATIC:
			writer.append("@"+fileName+"."+index+"\n")
			.append("D=M\n")
			.append(pushElementString);
			break;
		case Parser.TEMP:
			writer.append("@R"+segmentRegisterMap.get(segment)+"\n")
			.append("D=A\n")
			.append("@"+index+"\n")
			.append("A=D+A\n")
			.append("D=M\n")
			.append(pushElementString);
			break;
		case Parser.POINTER:
			writer.append("@R"+segmentRegisterMap.get(Parser.THIS)+"\n");
			if(index==1) {writer.append("A=A+"+index+"\n");}
			writer.append("D=M\n")
			.append(pushElementString);
			break;
		default:
			writer.append("@R"+segmentRegisterMap.get(segment)+"\n")
			.append("D=M\n")
			.append("@"+index+"\n")
			.append("A=D+A\n")
			.append("D=M\n")
			.append(pushElementString);
			break;
		}
	}

	private String initPushElementString() {
		StringBuilder push = new StringBuilder();
		push.append("@R0\n")
		.append("A=M\n")
		.append("M=D\n")
		.append("@R0\n")
		.append("M=M+1\n");
		pushElementString = push.toString();
		return pushElementString;
	}

	private void writePop(String segment, int index) throws IOException{
		switch(segment){
		case Parser.STATIC:
			writer.append("@"+fileName+"."+index+"\n")
			.append("D=A\n")
			.append("@R13\n")
			.append("M=D\n")
			.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("@R13\n")
			.append("A=M\n")
			.append("M=D\n")
			.append("@R0\n")
			.append("M=M-1\n");
			break;
		case Parser.TEMP:
			writer.append("@R"+segmentRegisterMap.get(segment)+"\n")
			.append("D=A\n")
			.append("@"+index+"\n")
			.append("D=D+A\n")
			.append("@R13\n")
			.append("M=D\n")
			.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("@R13\n")
			.append("A=M\n")
			.append("M=D\n")
			.append("@R0\n")
			.append("M=M-1\n");
			break;
		case Parser.POINTER:
			writer.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("@R"+segmentRegisterMap.get(Parser.THIS)+"\n");
			if(index==1) {writer.append("A=A+"+index+"\n");}
			writer.append("M=D\n")
			.append("@R0\n")
			.append("M=M-1\n");
			break;
		default:
			writer.append("@R"+segmentRegisterMap.get(segment)+"\n")
			.append("D=M\n")
			.append("@"+index+"\n")
			.append("D=D+A\n")
			.append("@R13\n")
			.append("M=D\n")
			.append("@R0\n")
			.append("A=M-1\n")
			.append("D=M\n")
			.append("@R13\n")
			.append("A=M\n")
			.append("M=D\n")
			.append("@R0\n")
			.append("M=M-1\n");
			break;
		}
	}

	public void writeCurrentLine(String line) throws IOException{
		System.out.println(line);
		writer.append("\n//"+line+"\n");
	}

	public void close() throws IOException{
		writer.flush();
		writer.close();
	}

	private void initSegmentRegisterMap(){
		segmentRegisterMap = new HashMap<>();
		segmentRegisterMap.put(Parser.LOCAL, 1);
		segmentRegisterMap.put(Parser.ARGUMENT, 2);
		segmentRegisterMap.put(Parser.THIS, 3);
		segmentRegisterMap.put(Parser.THAT,4);
		segmentRegisterMap.put(Parser.TEMP,5);
	}
}