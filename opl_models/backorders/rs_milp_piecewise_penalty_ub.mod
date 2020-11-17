/*********************************************
 * OPL 12.3 Model
 * Author: Roberto Rossi
 * Creation Date: May 29, 2012 at 12:10:00 AM
 *********************************************
 
 This model extends the original MILP model presented in
 
 The Stochastic Dynamic Production/Inventory Lot-Sizing 
 Problem with Service-Level Constraints," S. A. Tarim and 
 B. G. Kingsman, International Journal of Production Economics, 
 Vol.88, pp.105-119, 2004.
 
 by embedding a piecewise linear upper and lower bound for the true 
 holding cost at the end of each period.
 
 *********************************************/

/*Inventory system parameters*/
int Nbmonths=...;
range months =1..Nbmonths;
float expDemand[months]=...;
float ordercost=...;
float holdingcost=...;
float penaltycost = ...;
float unitcost=...;
float initialStock=...;

/*Jensen's partitioning of a standard normally distributed variable*/
int Nbpartitions = ...;
range partitions = 1..Nbpartitions;
/*Conditional expectation over partitions*/
float conditionalExpectations[months, months, partitions]=...;
/*Probabilities associated with partitions*/
float probabilityMasses[partitions]=...;

/*Max error*/
float maxApproximationErrors[months, months]=...;


/*Decision variables*/
dvar float stock[0..Nbmonths];
//dvar float+ stockPlb[0..Nbmonths];
dvar float+ stockPub[0..Nbmonths];
//dvar float+ stockNlb[0..Nbmonths];
dvar float+ stockNub[0..Nbmonths];
dvar boolean purchase[months];
dvar boolean P[months, months];

minimize 
   (sum(t in months)((stock[t]+expDemand[t]-stock[t-1])*unitcost))+
   (sum(t in months)(purchase[t]*ordercost))+
   (sum(t in months)(stockPub[t]*holdingcost))+ /*CHANGE THIS TERM FOR UB/LB*/
   (sum(t in months)(stockNub[t]*penaltycost)); /*CHANGE THIS TERM FOR UB/LB*/
   
   /*USE THIS TERM IN THE OBJ FUNCTION FOR UB*/
   //(sum(t in months)(stockPub[t]*holdingcost));
   
   /*USE THIS TERM IN THE OBJ FUNCTION FOR LB*/
   //(sum(t in months)(stockPlb[t]*holdingcost));
 
 subject to{
   
   /*
   Initial conditions
   */
   stock[0]==initialStock;
   //stockPlb[0]==maxl(stock[0],0);
   stockPub[0]==maxl(stock[0],0);
   //stockNlb[0]==maxl(-stock[0],0);
   stockNub[0]==maxl(-stock[0],0);
   
   /*
   Reorder conditions
   */
   forall(t in months) 
   		purchase[t] == 0 => stock[t]+expDemand[t]-stock[t-1] == 0;
   forall(t in months) 
   		stock[t]+expDemand[t]-stock[t-1]>=0;  		
   		
   /*
   First order loss function (Jensen's lower bound)
   */
   /*forall(t in months)
   		forall(p in partitions) stockNlb[t] >= - stock[t] + ((sum(k in 1..p)probabilityMasses[k])*(stock[t]+sum(j in 1..t)(sum(k in j..t)expDemand[k])*P[j,t])) - (sum(j in 1..t) ((sum(k in 1..p)probabilityMasses[k]*conditionalExpectations[j,t,k])*P[j,t]));
   forall(t in months) stockNlb[t] >= - stock[t];*/
   
   forall(t in months)
   		forall(p in partitions) stockNub[t] >= - stock[t] + ((sum(k in 1..p)probabilityMasses[k])*(stock[t]+sum(j in 1..t)(sum(k in j..t)expDemand[k])*P[j,t])) - (sum(j in 1..t) ((sum(k in 1..p)probabilityMasses[k]*conditionalExpectations[j,t,k])*P[j,t])) + (sum(j in 1..t) maxApproximationErrors[j,t]*P[j,t]);
   forall(t in months) stockNub[t] >= - stock[t] + (sum(j in 1..t) maxApproximationErrors[j,t]*P[j,t]);
   
   
   forall(t in months) 
   		sum(j in 1..t) P[j,t] == 1;
   forall(t in months, j in 1..t) 
   		P[j,t] >= purchase[j]-sum(k in j+1..t)purchase[k];	
   		
   /*
   Complementary First order loss function (Jensen's lower bound)
   */
   /*forall(t in months)
   		forall(p in partitions) stockPlb[t] >= ((sum(k in 1..p)probabilityMasses[k])*(stock[t]+sum(j in 1..t)(sum(k in j..t)expDemand[k])*P[j,t])) - (sum(j in 1..t) ((sum(k in 1..p)probabilityMasses[k]*conditionalExpectations[j,t,k])*P[j,t]));
   forall(t in months) stockPlb[t] >= 0;*/
   
   /*
   UB based on shifting Jensen's lower bound by the maximum approximation error
   */
   forall(t in months)
        forall(p in partitions) stockPub[t] >= ((sum(k in 1..p)probabilityMasses[k])*(stock[t]+sum(j in 1..t)(sum(k in j..t)expDemand[k])*P[j,t])) - (sum(j in 1..t) ((sum(k in 1..p)probabilityMasses[k]*conditionalExpectations[j,t,k])*P[j,t])) + (sum(j in 1..t) maxApproximationErrors[j,t]*P[j,t]);
   forall(t in months) stockPub[t] >= (sum(j in 1..t) maxApproximationErrors[j,t]*P[j,t]);
 }