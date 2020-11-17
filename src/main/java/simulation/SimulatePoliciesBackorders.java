/**
 * pwlf-milp: Piecewise linear approximations for the static-dynamic 
 *            uncertainty strategy in stochastic lot-sizing"
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Roberto Rossi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package simulation;

import java.util.Random;

import umontreal.ssj.stat.Tally;
import umontreal.ssj.probdist.Distribution;
import umontreal.ssj.probdist.NormalDist;

public class SimulatePoliciesBackorders {
	
	public static double[] simulatePenalty(
			Distribution[] demand,
			boolean[] R,
			double[] S,
			double a,
			double h,
			double p,
			double v,
			double initialStock,
			double confidence,
			double error){
		double[] vArray = new double[demand.length];
		for(int i = 0; i < vArray.length; i++) vArray[i] = v;
		return simulatePenalty(demand,R,S,a,h,p,vArray,initialStock,0.95,0.0001);
	}
	
	public static double[] simulatePenalty(
			Distribution[] demand,
			boolean[] R,
			double[] S,
			double a,
			double h,
			double p,
			double[] v,
			double initialStock,
			double confidence,
			double error){
		Tally costTally = new Tally();
		Tally[] stockPTally = new Tally[demand.length];
		Tally[] stockNTally = new Tally[demand.length];
		Tally[] serviceTally = new Tally[demand.length];
		for(int i = 0; i < demand.length; i++) {
			stockPTally[i] = new Tally();
			stockNTally[i] = new Tally();
			serviceTally[i] = new Tally();
		}
		double[] centerAndRadius = new double[2];
		Random rnd = new Random();
		int iterations = 0;
		do{
			double[] realizations = new double[demand.length];  
			for(int i = 0; i < realizations.length; i++){
				realizations[i] = demand[i].inverseF(rnd.nextDouble());
			}
			double cost = simulateOneRunPenalty(realizations,R,S,a,h,p,v,initialStock,stockPTally,stockNTally);
			costTally.add(cost);
		}while(iterations++<1000);
		
		do{
			double[] realizations = new double[demand.length];  
			for(int i = 0; i < realizations.length; i++){
				realizations[i] = demand[i].inverseF(rnd.nextDouble());
			}
			double cost = simulateOneRunPenalty(realizations,R,S,a,h,p,v,initialStock,stockPTally,stockNTally);
			costTally.add(cost);
			costTally.confidenceIntervalNormal(confidence, centerAndRadius);
			iterations++;
		}while(centerAndRadius[1]>=centerAndRadius[0]*error);
		System.out.println("Simulation runs: "+iterations);
		for(int i = 0; i < demand.length; i++) System.out.print("Period: "+i+"\t");System.out.println();
		for(int i = 0; i < demand.length; i++) System.out.print(String.format("%.2f", stockPTally[i].average())+"\t");System.out.println();
		for(int i = 0; i < demand.length; i++) System.out.print(String.format("%.2f", stockNTally[i].average())+"\t");System.out.println();
		return centerAndRadius;
	}
	
	public static double simulateOneRunPenalty(
			double[] demand,
			boolean[] R,
			double[] S,
			double a,
			double h,
			double p,
			double[] v,
			double initialStock,
			Tally[] stockPTally,
			Tally[] stockNTally){
		double cost = 0;
		double stock = initialStock;
		for(int i = 0; i < demand.length; i++){
			if(R[i]==true){
				cost += a;
				cost += Math.max(S[i] - stock, 0)*v[i];
				stock = Math.max(stock, S[i])-demand[i];
				cost += Math.max(stock*h, 0);
				cost += Math.max(-stock*p, 0);
			}else{
				stock = stock-demand[i];
				cost += Math.max(stock*h, 0);
				cost += Math.max(-stock*p, 0);
			}
			stockPTally[i].add(Math.max(stock, 0));
			stockNTally[i].add(Math.max(-stock, 0));
		}
		return cost;
	}
}
