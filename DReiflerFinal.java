/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dreiflerfinal;

/**
 *
 * @author darrenreifler
 */
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.*;

public class DreiflerFinal {
    
    public static void main(String[] args) throws FileNotFoundException{
      
      File output = new File("output.txt");
      PrintStream prt = new PrintStream(output);
      
      String [] sa = {"a", "b", "c", "d", "e", "f"};
      double [] b = {110, 10, 75, 56, 35, 25};
      double [] c = {10, 25, 35, 56, 75, 110};
     
      Instant start;
      Counter count = new Counter();
     
      ExecutorService executorSim = Executors.newFixedThreadPool(6);
      ExecutorService executorSim2 = Executors.newFixedThreadPool(6);
      
      boolean FCFS = true;
      
      if (FCFS == true){
       System.out.println("********START FCFS SIMULATION*************");
       System.out.println();
       System.out.println("Thread size: ");
       for(int i = 0; i < 6; i++)
           System.out.println(sa[i] + ": " + b[i]);
       System.out.println();
       start = Instant.now();
       
       for(int i = 0; i < 6; i++){
          Process pCpu = new Process(sa[i], b[i], start);
          Runnable simCPU = new Cpu(pCpu, count);
          Thread tCPU = new Thread(simCPU);
          executorSim.execute(tCPU);
          
         // Process pComb = new Process(sa[i], b[i], start);
         // Runnable simComb = new Comb(pComb, count);
         /// Thread tCombio = new Thread(simComb);
         // Thread tCombcpu = new Thread(simComb);
         // executorSim.execute(tCombio);
         // executorSim.execute(tCombcpu);
          
         // Process pIo = new Process(sa[i], b[i], start);
         // Runnable simIo = new Io(pIo, count);
         // Thread tIo = new Thread(simIo);
         // executorSim.execute(tIo);
       }
       executorSim.shutdown();
      }
    
      if(FCFS == false){
       System.out.println("********START SJF SIMULATION*************");
       System.out.println();
       for(int i = 0; i < 6; i++)
           System.out.println(sa[i] + ": " + c[i]);
       System.out.println();
       start = Instant.now();
       
       for(int i = 0; i < 6; i++){
          Process pCpu2 = new Process(sa[i], c[i], start);
          Runnable simCPU2 = new Cpu(pCpu2, count);
          Thread tCPU2 = new Thread(simCPU2);  
          tCPU2.setPriority(10 - i);
          
          //Process pComb2 = new Process(sa[i], c[i], start);
          //Runnable simComb2 = new Comb(pCpu2, count);
          //Thread tComb2 = new Thread(simComb2);
          //tComb2.setPriority(10 - i);
          
          //Process pIo2 = new Process(sa[i], c[i], start);
          //Runnable simIo2 = new Io(pCpu2,count);
          //Thread tIo2 = new Thread(simIo2);
          //tIo2.setPriority(10 - i);
          
          executorSim2.execute(tCPU2);
          //executorSim2.execute(tComb2);
          //executorSim2.execute(tIo2);
       }
      executorSim2.shutdown();
      }
    } // end main
 } // end class SchedulerSim
 
 class Process {
   String name;
   double burst = 0;
   Instant startTime;
   Instant stopTime;
   long duration;
   Cpu cpu;
   Io in;
   boolean busy = false;
   
   
   public Process (String c, double n, Instant start) {
     name = c;
     burst = n;
     startTime = start;
    } // end constructor
  } 

 class Cpu implements Runnable { 
      boolean cpuBusy = false;
      Process worker;
      Instant stop;
      boolean complete = false;
      Counter c;
      
      public Cpu(){}
      
      public Cpu(Process pr, Counter counter){
          worker = pr;
          c = counter;
      }
      public void run(){
         while(c.cpuBusy || worker.busy){};
            System.out.println(worker.name + " requesting CPU.");
            while (c.cpuBusy) {}  
        
        worker.busy = true;
        c.cpuBusy = true;
        System.out.println(worker.name + " now in CPU. ");
                try {
                 Thread.sleep ((long)worker.burst * 20);
                } catch (InterruptedException e) {}
        worker.stopTime = Instant.now();
        worker.duration += (worker.stopTime.toEpochMilli() - worker.startTime.toEpochMilli());
        System.out.println(worker.name + " out of CPU. " + "Time: " + worker.duration);
        worker.busy = false;
        c.cpuBusy = false;
        
        while(c.ioBusy || worker.busy){};
        
        System.out.println(worker.name + " requesting IO. ");
        
        worker.busy = true;
        c.ioBusy = true;
         System.out.println(worker.name + " now in IO.");
            try {
                 Thread.sleep ((long)worker.burst * 10);
                } catch (InterruptedException e) {}
         worker.stopTime = Instant.now();
         worker.duration += (worker.stopTime.toEpochMilli() - worker.startTime.toEpochMilli());
         System.out.println(worker.name + " out of IO. Time: " + worker.duration);
         worker.busy = false;
         c.ioBusy = false;
         
         while (c.cpuBusy || c.ioBusy || worker.busy){}
            System.out.println(worker.name + " requesting IO/CPU.");
         } else return;
          
         worker.busy = true;
         c.cpuBusy = true;
         c.ioBusy = true;
         System.out.println(worker.name + " now in IO/CPU.");
            try {
                 Thread.sleep ((long)worker.burst * 10);
                } catch (InterruptedException e) {}
         worker.stopTime = Instant.now();
         worker.duration += (worker.stopTime.toEpochMilli() - worker.startTime.toEpochMilli());
         System.out.println(worker.name + " out of IO/CPU. Time: " + worker.duration);
         worker.busy = false;
         c.cpuBusy = false;
         c.ioBusy = false;
      }
 

 class Io implements Runnable{ 
     boolean ioBusy = false;
     Process worker;
     Counter c;
     
     public Io(){}
     
     public Io(Process pr, Counter counter){
         worker = pr;
         c = counter;
     }
     
     public void run(){
         if(worker.busy == false){
            System.out.println(worker.name + " requesting IO.");
            while (c.ioBusy) {} 
         }
         else while(c.ioBusy || worker.busy){};
        worker.busy = true;
        c.ioBusy = true;
         System.out.println(worker.name + " now in IO.");
            try {
                 Thread.sleep ((long)worker.burst * 10);
                } catch (InterruptedException e) {}
         worker.stopTime = Instant.now();
         worker.duration += (worker.stopTime.toEpochMilli() - worker.startTime.toEpochMilli());
         System.out.println(worker.name + " out of IO. Time: " + worker.duration);
         worker.busy = false;
         c.ioBusy = false;
     }
 }

class Comb implements Runnable{ 
     int combWork = 0;
     Process worker;
     Counter c;
     
     public Comb(){}
     
     public Comb(Process pr, Counter counter){
         worker = pr;
         c = counter;
     }
     
     public void run(){
         if(worker.busy == false){
            System.out.println(worker.name + " requesting IO/CPU.");
            while (c.cpuBusy || c.ioBusy) {}
         } else return;
          
         worker.busy = true;
         c.cpuBusy = true;
         c.ioBusy = true;
         System.out.println(worker.name + " now in IO/CPU.");
            try {
                 Thread.sleep ((long)worker.burst * 10);
                } catch (InterruptedException e) {}
            worker.stopTime = Instant.now();
            worker.duration += (worker.stopTime.toEpochMilli() - worker.startTime.toEpochMilli());
            System.out.println(worker.name + " out of IO/CPU. Time: " + worker.duration);
            worker.busy = false;
            c.cpuBusy = false;
            c.ioBusy = false;
     }
 
 }

 class Counter {
        boolean cpuBusy = false;
        boolean ioBusy = false;
        boolean t1Busy = false;
        boolean t2Busy = false;
        boolean t3Busy = false;
        boolean t4Busy = false;
        boolean t5Busy = false;
        boolean t6Busy = false;
 }
