package com.snakeinalake.virtualdice;

import java.util.ArrayList;

public class OutcomeAnalyzer {
	private static OutcomeAnalyzer instance = null;
	public static OutcomeAnalyzer getInstance(){
		if(instance == null)
			instance = new OutcomeAnalyzer();
		return instance;
	}
	
	public class Outcome{
		public int taps;
		public int dCount;
		public int out[];
		public Outcome(int Count, int Out[], int Taps){
			dCount = Count;
			out = Out;
			taps = Taps;
		}
	};
	ArrayList<Outcome> outcomes;
	private OutcomeAnalyzer(){
		outcomes = new ArrayList<Outcome>();
	}
	public void registerOutcome(int Count, int Out[], int Taps){
		outcomes.add(new Outcome(Count, Out, Taps));
	}
}
