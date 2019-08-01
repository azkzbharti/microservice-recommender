package com.ibm.research.msr.cli;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class MSRCli {
   public static void main(String[] args) throws ParseException {

      //***Definition Stage***
      // create Options object
      Options options = new Options();

      // add option "-df"
      options.addOption("cl", false, "Cluster All");
      
      // automatically generate the help statement
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("Dependency Migration Command Line Interface", options );

      //***Parsing Stage***
      //Create a parser
      CommandLineParser parser = new DefaultParser();

      //parse the options passed as command line arguments
      CommandLine cmd = parser.parse( options, args);

      //***Interrogation Stage***
      //hasOptions checks if option is present or not
      if(cmd.hasOption("cl")) { 
         System.out.println("MSR cluster: ");
         String[] vals = cmd.getArgs();
         for (int i = 0; i < vals.length; i++) {
        	 System.out.println(vals[i]);
		}
      }
   }

   public static int getSum(String[] args) {
      int sum = 0;
      for(int i = 1; i < args.length ; i++) {
         sum += Integer.parseInt(args[i]);
      } 
      return sum;
   }

}