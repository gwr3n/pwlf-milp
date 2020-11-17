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

package mip_model.backorders;

import ilog.concert.IloException;
import ilog.opl.IloCplex;
import ilog.opl.IloCustomOplDataSource;
import ilog.opl.IloOplDataHandler;
import ilog.opl.IloOplDataSource;
import ilog.opl.IloOplErrorHandler;
import ilog.opl.IloOplFactory;
import ilog.opl.IloOplModel;
import ilog.opl.IloOplModelDefinition;
import ilog.opl.IloOplModelSource;
import ilog.opl.IloOplSettings;

import java.io.*;

import RS.RSCycleLinearizationParameters;
import simulation.SimulatePoliciesBackorders;
import umontreal.ssj.probdist.*;

public class MILPModelPenalty {
	
	int Nbmonths;
	Distribution[] demand; 
	RSCycleLinearizationParameters parameters;
	double[] expDemand;
	double ordercost;
	double holdingcost; 
	double penaltycost;
	double unitcost;
	double initialStock;

	int Nbpartitions;
	
	public MILPModelPenalty(
			int Nbmonths, 
			Distribution[] demand, 
			double ordercost, 
			double holdingcost, 
			double penaltycost,
			double unitcost,
			double initialStock,
			int Nbpartitions,
			long[] seed,
			int nbSamples,
			int population){
		this.Nbmonths = Nbmonths;
		this.demand = demand;
		this.ordercost = ordercost;
		this.holdingcost = holdingcost;
		this.penaltycost = penaltycost;
		this.unitcost = unitcost;
		this.initialStock = initialStock;
		this.Nbpartitions = Nbpartitions;
		
		parameters = new RSCycleLinearizationParameters(demand, seed, nbSamples, population, this.Nbpartitions);
		
		this.expDemand = new double[this.demand.length];
		for(int i = 0; i < this.demand.length; i++){
			for(int k = 0; k < this.Nbpartitions; k++){
				this.expDemand[i] += parameters.getConditionalExpectation(i, i)[k]*parameters.getProbabilityMasses()[k];	
			}
			System.out.print(this.expDemand[i]+"\t");
		}
		System.out.println();
	}
	
	private InputStream getMILPModelStream(File file){
		FileInputStream is = null;
		try{
			is = new FileInputStream(file);
		}catch(IOException e){
			e.printStackTrace();
		}
		return is;
	}
	
	public double[] solve(String model_name) throws IloException{
        IloOplFactory.setDebugMode(true);
        IloOplFactory oplF = new IloOplFactory();
        IloOplErrorHandler errHandler = oplF.createOplErrorHandler(System.out);
        IloCplex cplex = oplF.createCplex();
        IloOplModelSource modelSource=oplF.createOplModelSourceFromStream(getMILPModelStream(new File("./opl_models/backorders/"+model_name+".mod")),model_name);
        IloOplSettings settings = oplF.createOplSettings(errHandler);
        IloOplModelDefinition def=oplF.createOplModelDefinition(modelSource,settings);
        IloOplModel opl=oplF.createOplModel(def,cplex);
        cplex.setParam(IloCplex.IntParam.Threads, 4);
        cplex.setParam(IloCplex.IntParam.MIPDisplay, 2);
        /*cplex.setParam(IloCplex.IntParam.VarSel, 1);
        cplex.setParam(IloCplex.IntParam.ZeroHalfCuts, 2);
        cplex.setParam(IloCplex.IntParam.ImplBd, 2);
        cplex.setParam(IloCplex.IntParam.FracCuts, 2);
        cplex.setParam(IloCplex.IntParam.GUBCovers, 2);
        cplex.setParam(IloCplex.IntParam.DisjCuts, 2);
        cplex.setParam(IloCplex.IntParam.Covers, 2);
        cplex.setParam(IloCplex.IntParam.Cliques, 2);
        cplex.setParam(IloCplex.IntParam.FlowCovers, 2);
        cplex.setParam(IloCplex.IntParam.FlowPaths, 2);
        cplex.setParam(IloCplex.IntParam.MIRCuts, 2);
        cplex.setParam(IloCplex.IntParam.MIPEmphasis, 3);
        */

        IloOplDataSource dataSource = new MILPModelPenalty.MyData(oplF);
        opl.addDataSource(dataSource);
        opl.generate();

        //cplex.setOut(null);
        
        double start = cplex.getCplexImpl().getCplexTime();
        boolean status =  cplex.solve();
        double end = cplex.getCplexImpl().getCplexTime();
        if ( status )
        {	
        	double objective = cplex.getObjValue();
        	double time = end - start;
            System.out.println("OBJECTIVE: " + objective);  
            //s = new double[Nbmonths];
            double[] S = new double[Nbmonths];
            boolean[] R = new boolean[Nbmonths];
            for(int i = 0; i < Nbmonths; i++){
            	//s[i] = cplex.getValue(opl.getElement("sValue").asNumVarMap().get(1+i));
            	S[i] = cplex.getValue(opl.getElement("stock").asNumVarMap().get(1+i))+expDemand[i];
            	R[i] = Math.round(cplex.getValue(opl.getElement("purchase").asIntVarMap().get(1+i))) == 1 ? true : false;
            	//System.out.println("S["+(i+1)+"]="+S[i]);
            }
            opl.postProcess();
            opl.printSolution(System.out);
            //opl.end();
            oplF.end();
            //errHandler.end();
            //cplex.end();
            System.gc();
            
            double[] centerAndRadius = SimulatePoliciesBackorders.simulatePenalty(demand,R,S,ordercost,holdingcost,penaltycost,unitcost,initialStock,0.95,0.0001);
            
            double[] result = new double[4];
            result[0] = objective;
            result[1] = time;
            result[2] = centerAndRadius[0];
            result[3] = centerAndRadius[1];
            return result;
        } else {
            System.out.println("No solution!");
            //opl.end();
            oplF.end();
            //errHandler.end();
            //cplex.end();
            System.gc();
            double[] result = new double[2];
            result[0] = Double.NaN;
            result[1] = Double.NaN;
            return result;
        } 
        
    }
	
	
	class MyData extends IloCustomOplDataSource
    {
        MyData(IloOplFactory oplF)
        {
            super(oplF);
        }

        public void customRead()
        {
        	IloOplDataHandler handler = getDataHandler();
        	
        	handler.startElement("Nbmonths");
            handler.addIntItem(Nbmonths);
            handler.endElement();
            
            handler.startElement("expDemand");
        	handler.startArray();
            for (int j = 0 ; j<expDemand.length ; j++)
                handler.addNumItem(expDemand[j]);
            handler.endArray();
            handler.endElement();

            handler.startElement("ordercost");
            handler.addNumItem(ordercost);
            handler.endElement();
            
            handler.startElement("holdingcost");
            handler.addNumItem(holdingcost);
            handler.endElement();
            
            handler.startElement("penaltycost");
            handler.addNumItem(penaltycost);
            handler.endElement();
            
            handler.startElement("unitcost");
            handler.addNumItem(unitcost);
            handler.endElement();
            
            handler.startElement("initialStock");
            handler.addNumItem(initialStock);
            handler.endElement();
            
            handler.startElement("Nbpartitions");
            handler.addIntItem(Nbpartitions);
            handler.endElement();
            
            handler.startElement("probabilityMasses");
            handler.startArray();
            for (int j = 0 ; j < parameters.getProbabilityMasses().length ; j++)
                handler.addNumItem(parameters.getProbabilityMasses()[j]);
            handler.endArray();
            handler.endElement();
            
            handler.startElement("conditionalExpectations");
            handler.startArray();
            for (int i = 0 ; i < demand.length ; i++){
            	handler.startArray();
            	for (int j = 0 ; j < demand.length ; j++){
            		handler.startArray();
            			for (int k = 0 ; k < Nbpartitions ; k++)
            				handler.addNumItem(parameters.getConditionalExpectation(i, j)[k]);
            		handler.endArray();
            	}
            	handler.endArray();
            }
            handler.endArray();
            handler.endElement();
            
            handler.startElement("maxApproximationErrors");
            handler.startArray();
            for (int i = 0 ; i < demand.length ; i++){
            	handler.startArray();
            	for (int j = 0 ; j < demand.length ; j++){
            		handler.addNumItem(parameters.getMaximumApproximationError(i, j));
            	}
            	handler.endArray();
            }
            handler.endArray();
            handler.endElement();
        }
    };
}
