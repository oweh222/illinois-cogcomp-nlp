/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.Token;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pos.lbjava.*;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Simple class to build and train models from existing training data, as opposed to using the
 * prepackaged jar.
 *
 * @author James Chen
 * @author Christos Christodoulopoulos
 */
public class POSTrain {
    private static Logger logger = LoggerFactory.getLogger(POSTrain.class);

    private static final String NAME = POSTrain.class.getCanonicalName();
    private int iter; // Number of training iterations
    private POSTaggerKnown taggerKnown;
    private POSTaggerUnknown taggerUnknown;
    private MikheevTable mikheevTable;
    private BaselineTarget baselineTarget;
    private ResourceManager rm;

    public POSTrain() {
        this(50);
    }

    public POSTrain(int iter) {
        this.iter = iter;
        rm = new POSConfigurator().getDefaultConfig();
        this.init();
    }

    public POSTrain(int iter, String configFile) throws IOException {
        this.iter = iter;
        rm = new POSConfigurator().getConfig(new ResourceManager(configFile));
        this.init();
    }

    /**
     * Known and unknown taggers to be trained later.
     */
    private void init() {
        String knownModelFile = rm.getString("knownModelPath");
        String knownLexFile = rm.getString("knownLexPath");
        String unknownModelFile = rm.getString("unknownModelPath");
        String unknownLexFile = rm.getString("unknownLexPath");
        String baselineModelFile = rm.getString("baselineModelPath");
        String baselineLexFile = rm.getString("baselineLexPath");
        String mikheevModelFile = rm.getString("mikheevModelPath");
        String mikheevLexFile = rm.getString("mikheevLexPath");
        baselineTarget = new BaselineTarget(baselineModelFile, baselineLexFile);
        mikheevTable = new MikheevTable(mikheevModelFile, mikheevLexFile);
        taggerKnown = new POSTaggerKnown(knownModelFile, knownLexFile, baselineTarget);
        taggerUnknown = new POSTaggerUnknown(unknownModelFile, unknownLexFile, mikheevTable);
    }

    /**
     * Trains the taggers with the default training data found in POSConfigurator.java
     */
    public void trainModels() {
        logger.info("Using default training data: " + rm.getString("trainingAndDevData"));
        trainModels(rm.getString("trainingAndDevData"));
    }

    /**
     * Trains the taggers with specified, labeled training data.
     * 
     * @param trainingData The labeled training data
     */
    public void trainModels(String trainingData) {
        // Set up the data
        Parser trainingParser = new POSBracketToToken(trainingData);
        Parser trainingParserUnknown = new POSLabeledUnknownWordParser(trainingData);

        MikheevTable.isTraining = true;
        BaselineTarget.isTraining = true;

        Object ex;
        // baseline and mikheev just count, they don't learn -- so one iteration should be enough
        while ((ex = trainingParser.next()) != null) {
            baselineTarget.learn(ex);
            mikheevTable.learn(ex);
        }

        baselineTarget.doneLearning();
        mikheevTable.doneLearning();
        trainingParser.reset();

        POSTaggerUnknown.isTraining = true;
        POSTaggerKnown.isTraining = true;

        Parser testDataParser = new POSBracketToToken(rm.getString("testData"));

        String accPath = rm.getString("modelPath") + "accuracyPerIteration.txt";
        try(FileWriter fw = new FileWriter(accPath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            // Run the learner
            for (int i = 0; i < iter; i++) {
                System.out.println("Training round " + i);
                while ((ex = trainingParser.next()) != null) {
                    taggerKnown.learn(ex);
                }
                System.out.println("\tFinished training " + rm.getString("knownName"));
                while ((ex = trainingParserUnknown.next()) != null) {
                    taggerUnknown.learn(ex);
                }
                System.out.println("\tFinished training " + rm.getString("unknownName"));
                trainingParser.reset();
                trainingParserUnknown.reset();
                taggerKnown.doneWithRound();
                taggerUnknown.doneWithRound();

                double accuracy = evaluate(testDataParser);
                System.out.printf("\tThe Accuracy after this round is %f\n",accuracy);
                out.println(accuracy);
                testDataParser.reset();
            }
            taggerUnknown.doneLearning();
            taggerKnown.doneLearning();
        }
        catch (IOException e)
        {
            logger.warn("Failed to record accuracy into the output file!");
            e.printStackTrace();
        }


    }

    /**
     * Saves the ".lc" and ".lex" models to disk in the modelPath specified by the constructor
     */
    public void writeModelsToDisk() {
        baselineTarget.save();
        mikheevTable.save();
        taggerKnown.save();
        taggerUnknown.save();
        logger.info("Done training, wrote models to disk.");
    }
    /**
     * evaluate model against test set
     */
    private double evaluate(Parser labeledTestParser) {
        int numSeen = 0;
        int numEqual = 0;
        Token labeledWord = null;
        try{
            labeledWord = (Token) labeledTestParser.next();
        }
        catch (Exception e){System.out.print("POSTrain.evaluate: Failed to Load parser from argument. ");}

        String labeledTag = null;
        String testTag = null;
        for (; labeledWord != null; labeledWord = (Token) labeledTestParser.next()) {

            labeledTag = labeledWord.label;
            testTag = taggerKnown.discreteValue(labeledWord);

            if (labeledTag.equals(testTag)) {
                numEqual++;
            }
            numSeen++;
        }
        return (double) numEqual / (double) numSeen;
    }

    public static void main(String[] args) throws Exception{
        POSTrain trainer;
        int iter = 50;

        if(args.length > 0) {
            System.out.printf("Use config file : %s\n", args[0]);

            if (args.length > 1) iter = Integer.parseInt(args[1]);
            else iter = 50;

            if (iter > 200) iter = 200;
            if (iter < 0) iter = 50;

            trainer = new POSTrain(iter, args[0]);
        }
        else
        {
            trainer = new POSTrain(50);
        }

        long startTime = System.currentTimeMillis();



        trainer.trainModels();

        float timeElapsed = (float) (System.currentTimeMillis() - startTime) / 1000;
        System.out.printf("Training Completed in %2f seconds.\n", timeElapsed);
        System.out.printf("Average Training Time per Iteration is %2f seconds.\n", timeElapsed/iter);

        trainer.writeModelsToDisk();


    }
}
