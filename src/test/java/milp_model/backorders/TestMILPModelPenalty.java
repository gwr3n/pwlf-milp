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

package milp_model.backorders;

import ilog.concert.IloException;
import mip_model.backorders.MILPModelPenalty;
import umontreal.ssj.probdist.Distribution;
import umontreal.ssj.probdist.EmpiricalDist;
import umontreal.ssj.probdist.ExponentialDist;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.probdist.UniformDist;

public class TestMILPModelPenalty {
   
   /**
    * Environment variable
    * 
    * DYLD_LIBRARY_PATH=/Applications/CPLEX_Studio128/opl/bin/x86-64_osx/
    * 
    * VM arguments
    * 
    * -Djava.library.path=/Applications/CPLEX_Studio1210/opl/bin/x86-64_osx/
    * 
    * @author Roberto Rossi
    *
    */
   
   public static void main(String[] args){
      
      /**
       * mu: mean demand
       * sigma: standard deviation demand
       */
      
      double[] meanDemand = {10,20,30,40}; // mu[t]
      double cv = 0.25; // coefficient of variation: mu[t]*cv = sigma[t] 
      
      int Nbmonths = meanDemand.length;
      double ordercost = 30;
      double holdingcost = 1;
      double penaltycost = 5;
      double unitcost = 0;
      double initialStock = 0;
      
      Distribution[] distributions = new Distribution[Nbmonths];
      
      /* Some distributions
      distributions[0] = new EmpiricalDist(observations);
      distributions[1] = new PoissonDist(20);
      distributions[2] = new NormalDist(50,10);
      distributions[3] = new ExponentialDist(1.0/50);
      distributions[4] = new UniformDist(0,100);*/
      
      for(int i = 0; i < distributions.length; i++)
         distributions[i] = new PoissonDist(meanDemand[i]); // use Poisson
         //distributions[i] = new NormalDist(meanDemand[i],meanDemand[i]*cv); // use Normal
      
      int Nbpartitions = 10;
      long[] seed = {1,2,3,4,5,6};
      int nbSamples = 1000;
      int population = 100000;
      
      double[] lb = null;
      double[] ub = null;
      try{
         MILPModelPenalty model = new MILPModelPenalty(
               Nbmonths, 
               distributions, 
               ordercost, 
               holdingcost,
               penaltycost,
               unitcost, 
               initialStock, 
               Nbpartitions,
               seed,
               nbSamples,
               population);
         lb = model.solve("rs_milp_piecewise_penalty_lb");
         
      }catch(IloException e){
         e.printStackTrace();
      }
      
      try{
         MILPModelPenalty model = new MILPModelPenalty(
               Nbmonths, 
               distributions, 
               ordercost, 
               holdingcost, 
               penaltycost,
               unitcost, 
               initialStock, 
               Nbpartitions,
               seed,
               nbSamples,
               population);
         ub = model.solve("rs_milp_piecewise_penalty_ub");
      }catch(IloException e){
         e.printStackTrace();
      }
      
      System.out.println("LB: "+String.format("%.2f", lb[0])+"\t Time: "+String.format("%.2f", lb[1])+"\t Sim: "+String.format("%.2f", lb[2])+"+/-"+String.format("%.2f", lb[3])+"@ 95% confidence");
      System.out.println("UB: "+String.format("%.2f", ub[0])+"\t Time: "+String.format("%.2f", ub[1])+"\t Sim: "+String.format("%.2f", ub[2])+"+/-"+String.format("%.2f", ub[3])+"@ 95% confidence");
   }
   
   private static final double[] observations = {28.0741, 37.0565, 17.8413, 36.5158, 21.6293, 20.4246, 71.4112, 
         37.6059, 37.9011, 36.325, 33.5892, 25.9398, 40.6084, 11.3667, 
         15.0024, 19.465, 27.265, 78.504, 27.1685, 76.4571, 72.0118, 23.7986, 
         70.5609, 26.463, 25.3521, 17.4925, 37.513, 22.7177, 32.0754, 17.4422, 
         33.2551, 23.8737, 47.2574, 67.5549, 29.6037, 22.3234, 54.5201, 
         73.9199, 32.543, 17.1827, 59.1714, 39.2098, 35.6647, 19.1226, 
         64.8445, 33.8207, 36.1044, 28.4903, 83.8897, 29.9214, 21.8565, 
         27.2275, 34.6711, 54.8081, 19.7576, 50.0901, 37.721, 33.0879, 
         57.5642, 35.861, 68.1631, 20.3139, 84.9478, 47.0687, 37.5119, 
         21.7852, 14.3257, 10.6876, 33.1993, 28.261, 33.2155, 72.4989, 
         32.2685, 19.1746, 73.9071, 20.9411, 23.4219, 26.4588, 34.7484, 
         28.6204, 83.3349, 27.4877, 25.5364, 31.3102, 40.1026, 32.2763, 
         33.9677, 31.4265, 21.5841, 80.9962, 73.9571, 38.615, 56.6494, 
         64.2206, 33.9953, 37.6291, 31.3204, 26.6406, 28.5466, -2.56407, 
         35.7539, 28.754, 60.4775, 69.5395, 34.5684, 31.4762, 32.1759, 19.9471,
           30.4914, 15.3123, 17.905, 27.962, 25.1847, 25.2175, 42.4135, 
         25.4947, 70.2815, 14.2841, 82.3108, 19.0916, 49.0102, 65.6284, 
         18.0347, 18.5975, 47.6527, 37.7432, 24.0594, 26.3102, 23.7305, 
         12.6009, 21.9906, 32.4108, 23.6196, 9.21787, 54.7988, 42.6507, 
         73.5334, 25.6067, 40.7631, 41.9349, 33.7569, 35.2573, 24.3099, 
         84.2028, 25.7688, 34.3607, 26.5855, 72.5383, 18.8661, 43.656, 
         68.8784, 60.6869, 71.5258, 75.1431, 39.7538, 29.4983, 25.2816, 
         23.6204, 56.6435, 22.9425, 31.8486, 70.2084, 11.9978, 44.7344, 
         34.2046, 33.7131, 31.5913, 28.6397, 22.9598, 23.4668, 26.7419, 
         22.5111, 41.7394, 13.3714, 88.2541, 39.1714, 72.4239, 45.7921, 
         51.9322, 32.2951, 37.3388, 20.0179, 76.4912, 24.1718, 77.2087, 
         65.8098, 19.62, 66.0744, 82.2651, 71.2201, 31.6994, 23.6553, 33.0635, 
         40.4197, 32.6747, 19.7996, 36.0052, 38.047, 3.42543, 42.1742, 
         47.4269, 66.4148, 30.5918, 72.4994, 19.8016, 24.2546, 18.7883, 
         32.4965, 65.2762, 63.9234, 30.3478, 16.0009, 32.2717, 1.65196, 
         38.535, 18.3457, 67.7012, 65.6919, 23.9415, 46.8337, 16.2651, 
         30.1157, 31.8795, 32.3591, 67.1413, 57.5025, 71.8455, 75.0887, 
         36.4344, 73.0453, 27.7212, 61.0768, 28.8377, 53.9263, 31.5856, 
         16.1044, 3.69301, 32.019, 57.5973, 23.2975, 18.7782, 24.2909, 
         34.7884, 11.5381, 68.7499, 26.5432, 34.1972, 10.5637, 31.3054, 
         52.754, 39.1474, 18.1672, 31.4205, 30.3261, 18.3458, 80.6453, 
         48.8155, 11.3507, 80.1665, 37.2467, 32.2537, 33.6072, 18.2034, 
         72.6979, 38.2461, 32.6766, 61.6329, 25.8569, 47.2018, 28.3907, 
         82.2836, 33.8426, 15.9098, 55.6322, 28.8267, 36.0302, 47.4414, 
         26.6971, 64.2641, 64.7451, 33.8348, 70.7965, 36.2064, 75.1953, 
         31.2499, 20.7991, 21.3809, 27.5349, 24.9914, 39.1093, 19.0464, 
         73.5757, 15.6159, 34.3066, 24.6083, 17.9586, 75.8947, 91.4498, 
         36.1667, 40.1685, 23.598, 21.148, 24.6226, 35.7321, 63.3948, 74.7273, 
         28.7412, 68.3333, 28.5688, 21.9989, 21.1213, 26.1011, 35.5599, 
         28.1384, 20.021, 75.1544, 35.1936, 25.2616, 1.70038, 17.8895, 
         76.8902, 35.1254, 73.2399, 15.0038, 29.8682, 14.2947, 22.5822, 
         39.2031, 28.0553, 24.9845, 14.5433, 27.4424, 22.7428, 24.0465, 
         23.6355, 61.8469, 26.462, 39.7285, 46.4482, 32.341, 25.0918, 38.3904, 
         26.284, 30.667, 35.4871, 16.3957, 39.1846, 17.7234, 15.7191, 25.2957, 
         15.7521, 70.6436, 82.3403, 45.3663, 11.2882, 13.1438, 41.1792, 
         33.1248, 56.0817, 27.1867, 25.2638, 76.2728, 62.2844, 58.8604, 
         22.7928, 53.4659, 48.3358, 37.8583, 26.3246, 33.9605, 39.0206, 
         25.6244, 32.7355, 20.9361, 19.0955, 43.0133, 15.0526, 32.7747, 
         44.5282, 23.7317, 72.6272, 47.6351, 75.0638, 28.8967, 22.3245, 
         24.9527, 29.5371, 71.4213, 17.6563, 45.5411, 43.5561, 21.7395, 
         58.4722, 72.613, 33.405, 32.6981, 52.7684, 72.994, 23.1276, 28.9063};
}
