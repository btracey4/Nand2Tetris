package com.btracey.jacktokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;

public class JackTokenizer {
	private String[] tokens;
	private int currTokenIndex = 0;
	
	public JackTokenizer(String jackFilePath) throws IOException{
		File jackFile = new File(jackFilePath);
		BufferedReader reader;
		try{
			reader = new BufferedReader(new FileReader(jackFile));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("The jack file: " + jackFilePath + " doesn't exist!");
		}
		List<String> tokenList = new ArrayList<>();
		String currLine;
		while((currLine = reader.readLine()) != null) {
			currLine = currLine.strip();
			for(String token : currLine.split(" "))
				tokenList.add(token);
		}
		tokens = (String[])tokenList.stream().toArray();
		reader.close();
	}
	
	public boolean hasMoreTokens() {
		return currTokenIndex < tokens.length;
	}
	
	public void advance() {
		
	}
	
	public TokenType tokenType() {
		return null;
	}
	
	public KeyWord keyWord() {
		return null;
	}
	
	public char symbol() {
		return ' ';
	}
	
	public String identifier() {
		return null;
	}
	
	public int intVal() {
		return 0;
	}
	
	public String stringVal() {
		return null;
	}
}