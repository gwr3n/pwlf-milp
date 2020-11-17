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

import umontreal.ssj.probdist.Distribution;
import umontreal.ssj.probdist.NormalDist;

public class TestSimulatePoliciesBackorders {
   public static void main(String args[]){
      testPenalty();
   }
   
   public static void testPenalty(){
      double[] demandMean = {110,40,10,62,12,80,122,130};
      double[] demandStd = {22,8,2,12.4,2.4,16,24.4,26};
      Distribution[] distribution = new Distribution[demandMean.length];
      for(int i = 0; i < demandMean.length; i++){
         distribution[i] = new NormalDist(demandMean[i],demandStd[i]);
      }
      
      double a = 48;
      double h = 0.5;
      double p = 12;
      double[] v = {5.6,4.2,3.0,2.0,1.2,0.6,0.2,0};
      double initialStock = 98;
      
      boolean[] R = {true,true,false,true,false,true,true,true};
      //double[] S = {130.2,57.072,0,85.597,0,102.363,156.103,185.484};
      double[] S = {128.5,56.9,0,84.6,0,101.9,155.4,165.6};
      
      double[] centerAndRadius = SimulatePoliciesBackorders.simulatePenalty(distribution,R,S,a,h,p,v,initialStock,0.95,0.0001);
      System.out.println(centerAndRadius[0]+" "+centerAndRadius[1]);
   }
}
