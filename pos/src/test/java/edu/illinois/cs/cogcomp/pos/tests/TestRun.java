package edu.illinois.cs.cogcomp.pos.tests;
import edu.illinois.cs.cogcomp.pos.*;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
/**
 *      args[0]: config file path (default.properties if not specified)
 *      args[1]: number of iterations to train (default 50, max. 200)
 *
 */
public class TestRun {
    public static void main(String[] args) throws Exception{
        POSTrain trainer;
        int iter = 50, model_iter = 50;
        boolean bEvaluation = true;

        if(args.length > 0) {
            System.out.printf("Use config file : %s\n", args[0]);
            if (args.length > 1) iter = Integer.parseInt(args[1]);
            else iter = 50;

            if (iter > 200) iter = 200;
            if (iter < 0) iter = 50;

            model_iter = bEvaluation ? 1 : iter;
            iter = bEvaluation ? iter : 1;
            trainer = new POSTrain(model_iter, args[0]);
        }
        else
            trainer = new POSTrain(1);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iter; ++i)
        {
            trainer.trainModels();

        }


        double timeElapsed = ((double) System.currentTimeMillis() - startTime) / (double) 1000;
        System.out.printf("Training Completed in %d seconds.\n", timeElapsed);
        System.out.printf("Average Training Time per Iteration is %d seconds.\n", timeElapsed/iter);

        trainer.writeModelsToDisk();


    }
}
