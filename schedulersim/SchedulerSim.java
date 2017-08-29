/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package schedulersim;

/**
 * Darren Reifler
 * CMSC 412 Final Project
 * Jul 26, 2015
 * Purpose: Create a simulation of an operating system scheduler
 * that includes 6 worker threads, a CPU bound, a IO bound and 
 * one that is a combination of the two (IO/CPU bound).
 */
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;

public class SchedulerSim {
    
    public static void main(String[] args) throws FileNotFoundException{
      
      File output = new File("output.txt");
      PrintStream prt = new PrintStream(output);
      
      String [] sa = {"a", "b", "c", "d", "e", "f"};
      double [] b = {110, 10, 75, 56, 35, 25};
      double [] c = {10, 25, 35, 56, 75, 110};
     
      Instant start;
      Counter count = new Counter();
      
      ExecutorService executorSim = Executors.newFixedThreadPool(6);
      
      boolean FCFS = false;
      
      if (FCFS == true){
       System.out.println("********START FCFS SIMULATION*************");
       System.out.println();
       System.out.println("Thread size: ");
       //Print out starting process burst length
       for(int i = 0; i < 6; i++)
           System.out.println(sa[i] + ": " + b[i]);
       
       System.out.println();
       //Record starting point of simulation
       start = Instant.now();
       
       for(int i = 0; i < 6; i++){
          Process pCpu = new Process(sa[i], b[i], start);
          Runnable simCPU = new Sim(pCpu, count);
          Thread tCPU = new Thread(simCPU);
          executorSim.execute(tCPU);
       }
      executorSim.shutdown();
      }
    
      if(FCFS == false){
       System.out.println("********START SJF SIMULATION*************");
       System.out.println();
       //Print out starting process burst length
       for(int i = 0; i < 6; i++)
           System.out.println(sa[i] + ": " + c[i]);
       
       System.out.println();
       //Record starting point of simulation
       start = Instant.now();
       
       for(int i = 0; i < 6; i++){
          Process pCpu = new Process(sa[i], c[i], start);
          Runnable simCPU = new Sim(pCpu, count);
          Thread tCPU = new Thread(simCPU);
          tCPU.setPriority(10-i);
          executorSim.execute(tCPU);
       }
      executorSim.shutdown();
      }
    } // end main
 } // end class SchedulerSim
 
 class Process {
   String name;
   double burst = 0;
   Instant startTime;
   Instant stopTime;
   long duration;
   boolean busy = false;
   boolean cpuDone = false;
   boolean ioDone = false;
   boolean ioCpuDone = false;
   boolean requestCPU = false;
   boolean requestIO = false;
   boolean requestIOCPU = false;
   boolean begin = false;
   
   
   public Process (String c, double n, Instant start) {
     name = c;
     burst = n;
     startTime = start;
    } // end constructor
  }   

 class Sim implements Runnable { 
      boolean cpuBusy = false;
      Process worker;
      Instant stop;
      boolean complete = false;
      Counter c;
      
      public Sim(){}
      
      public Sim(Process pr, Counter counter){
          worker = pr;
          c = counter;
      }
      public void run(){
        
        synchronized(worker){
            
        //record each thread actual start time
        if(worker.begin == false){
            c.tStart.put(worker.name,Instant.now().toEpochMilli());
            worker.begin = true;
            if(worker.begin == true)
                System.out.println("Thread Started: " + worker.name); 
        }    
        //Loop until all three have been done
        while((worker.cpuDone == false) || (worker.ioCpuDone == false) || (worker.ioDone == false)){
            
        //Pass Through CPU
        if(worker.cpuDone == false && worker.busy == false){
         //log first time requesting
            if(worker.requestCPU == false){
                worker.requestCPU = true;
                System.out.println(worker.name + " requesting CPU."); 
            }
        //After logging request proceed if cpu is not busy
            if(c.cpuBusy == false && worker.busy == false){
        //update flags
                worker.busy = true;
                c.cpuBusy = true;
        //simulate performing work
                System.out.println(worker.name + " now in CPU. ");
                try {
                 Thread.sleep ((long)worker.burst * 20);
                } catch (InterruptedException e) {}       
        //record duration after stop to keep for logging completion
                worker.stopTime = Instant.now();
                worker.duration = (worker.stopTime.toEpochMilli() - worker.startTime.toEpochMilli());
                System.out.println(worker.name + " out of CPU. " + "Time: " + worker.duration);
        //update flags
                worker.busy = false;
                c.cpuBusy = false;
                worker.cpuDone = true;
            }//Cpu if 
        }//thread if
        
        //Pass through IO/CPU
         if(worker.ioCpuDone == false && worker.busy == false){
         //log first time requesting
            if(worker.requestIOCPU == false){
                System.out.println(worker.name + " requesting IO/CPU."); 
                worker.requestIOCPU = true;
            }
        //After logging request proceed if cpu is not busy
             if(c.ioBusy == false && c.cpuBusy == false && worker.busy == false){ 
        //Update flags
                worker.busy = true;
                c.cpuBusy = true;
                c.ioBusy = true;
        //simulate performing work
                System.out.println(worker.name + " now in IO/CPU.");
                 try {
                         Thread.sleep ((long)worker.burst * 10);
                    } catch (InterruptedException e) {}      
        //record duration after stop to keep for logging completion        
                worker.stopTime = Instant.now();
                worker.duration = (worker.stopTime.toEpochMilli() - worker.startTime.toEpochMilli());
                System.out.println(worker.name + " out of IO/CPU. Time: " + worker.duration);
        //update flags
                worker.busy = false;
                c.cpuBusy = false;
                c.ioBusy = false;
                worker.ioCpuDone = true;
             }
         }   
         
        //Pass Through IO
        if(worker.ioDone == false && worker.busy == false){
        //log first time requesting
            if(worker.requestIO == false){
                System.out.println(worker.name + " requesting IO."); 
                worker.requestIO = true;
            }
        //After logging request proceed if cpu is not busy
            if(worker.busy == false && c.ioBusy == false){   
        //Update flags
                worker.busy = true;
                c.ioBusy = true;
                System.out.println(worker.name + " now in IO.");
                     try {
                      Thread.sleep ((long)worker.burst * 10);
                     } catch (InterruptedException e) {}
        //update duration after stop to keep for logging completion
                worker.stopTime = Instant.now();
                worker.duration = (worker.stopTime.toEpochMilli() - worker.startTime.toEpochMilli());
                System.out.println(worker.name + " out of IO. Time: " + worker.duration);
        //update flags
                worker.busy = false;
                c.ioBusy = false;
                worker.ioDone = true;
                }
            } 
        }
        
        //Print out line as each thread completes
        c.durList.add(worker.duration);
        c.tStop.put(worker.name, Instant.now().toEpochMilli());
        System.out.println();
        System.out.println("Worker " + worker.name + " COMPLETE. Threads Complete: " +
                c.durList.size());
        System.out.println("Time for thread " + worker.name + ": " + (c.tStop.get(worker.name) 
                    - c.tStart.get(worker.name)));
        System.out.println();
    
        //When all threads complete print results
        if(c.durList.size() == 6){
            System.out.println();
            System.out.println("************RESULTS*************");
            System.out.println();
            
        //Print actual time of all threads (minus time waiting to start)
           // System.out.println("Time for thread" + worker.name + ": " + (c.tStop.get(worker.name) 
           //         - c.tStart.get(worker.name)));
       
            System.out.println();
            System.out.println("Time to process all Threads: " + c.durList.get(5));
            System.out.println();
            System.out.println("Time for thread a: " + (c.tStop.get("a") - c.tStart.get("a")));
            System.out.println("Time for thread b: " + (c.tStop.get("b") - c.tStart.get("b")));
            System.out.println("Time for thread c: " + (c.tStop.get("c") - c.tStart.get("c")));
            System.out.println("Time for thread d: " + (c.tStop.get("d") - c.tStart.get("d")));
            System.out.println("Time for thread e: " + (c.tStop.get("e") - c.tStart.get("e")));
            System.out.println("Time for thread f: " + (c.tStop.get("f") - c.tStart.get("f")));
            System.out.println();
            
            for(int i = 0; i < 6; i++){
                c.total += c.durList.get(i);
             }
            c.average = c.total/6;
            System.out.println("Process Average: " + c.average);
            }    
      }
    }
 }

 class Counter {
        boolean cpuBusy = false;
        boolean ioBusy = false;
        int cpuCount = 0;
        int ioCount = 0;
        int ioCpuCount = 0;
        ArrayList<Long> durList = new ArrayList<Long>();
        HashMap<String,Long> tStart = new HashMap<String, Long>();
        HashMap<String,Long> tStop = new HashMap<String, Long>();
        long total = 0;
        long average;
 }