package misc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import dc.GP.AbstractNode;
import dc.GP.Const;
import dc.GP.TreeHelperClass;

import dc.ga.PreProcess;
import dc.ga.DCCurve.Event;
import dc.ga.DCCurve.Type;
import dc.io.FReader;
import dc.io.FReader.FileMember2;
import files.FWriter;

public class DCCurveRandomGP  extends DCCurveRegression{

	public DCCurveRandomGP()  {
		super();
	}

	/**
	 * 
	 * @param values
	 *            The array with prices/tick data
	 * @param delta
	 *            The threshold value
	 * @param GPTreeFileName
	 *            the name of the file where GP tree is stored
	 */
	public void build(Double[] values, double delta, String GPTreeFileName, Event[] trainingEvents,
			PreProcess preprocess) {

		String thresholdStr = String.format("%.8f", delta);
		thresholdString = thresholdStr;
		
		if (trainingEvents == null || trainingEvents.length < 1)
			return;

		this.trainingEvents = Arrays.copyOf(trainingEvents, trainingEvents.length);
		
		
		if (upwardTrendTreeString != null && !upwardTrendTreeString.isEmpty() && 
				downwardTrendTreeString != null && !downwardTrendTreeString.isEmpty()){
			return;
			
		}
		
		// if (SymbolicRegression.OsFunctionEnum ==
		// SymbolicRegression.function_code.eGP) {
		if (Const.splitDatasetByTrendType) {

			// get upward dc first

			String gpTreeName = Const.UPWARD_EVENT_STRING + thresholdStr + Const.FUNCTION_NODE_DEFINITION
					+ "_preprocessed.txt";
			String thisLine = null;

			Vector<Event> trendOfChoiceVecClassifier = new Vector<Event>();

			for (int i = 0; i < trainingEvents.length - 1; i++) {
				if (trainingEvents[i].type == Type.Upturn) {
					// Instance and event classifier object have the same size.

					String classificationStr = "no";
					Random randomno = new Random();
					double decisionNum =  randomno.nextDouble();
					
					if (decisionNum >= 0.5)
						classificationStr = "yes";				

					// Use decision from Random()
					if ((classificationStr.compareToIgnoreCase("no") == 0)) {
						continue;
					}
					trendOfChoiceVecClassifier.add(trainingEvents[i]);

				}
			}

			//System.out.println("upward event for classifier " + trendOfChoiceVecClassifier.size());
			Event[] classifierBaseduptrendEvents = trendOfChoiceVecClassifier
					.toArray(new Event[trendOfChoiceVecClassifier.size()]);
			if (Const.REUSE_EXISTING_TREE) {

				try {
					// open input stream test.txt for reading purpose.
					BufferedReader br = new BufferedReader(
							new FileReader(Const.log.publicFolder + gpTreeName));
					while ((thisLine = br.readLine()) != null) {
						Const.thresholdGPStringUpwardMap.put(delta, thisLine);
						// System.out.println(thisLine);
					}
				} catch (FileNotFoundException fileNotFound) {
					// System.out.println(SymbolicRegression.log.publicFolder
					// + gpTreeName + " not found. Will rebuild GP tree.");
					// fileNotFound.printStackTrace();
				} catch (IOException io) {
					// System.out.println("IO excption occured. Will
					// loading" + SymbolicRegression.log.publicFolder +
					// gpTreeName + ". Will rebuild GP tree.");
					// io.printStackTrace();
				} catch (Exception e) {
					// System.out.println("Unknown error occured. Will
					// loading" + SymbolicRegression.log.publicFolder +
					// gpTreeName + ". Will rebuild GP tree.");
					// e.printStackTrace();
				}
			} else {
				FWriter writer = new FWriter(Const.log.publicFolder + gpTreeName);
			}
			TreeHelperClass treeHelperClass = new TreeHelperClass();
			if (Const.thresholdClassifcationBasedGPStringUpwardMap.containsKey(delta)) {
				if (upwardTrendTreeString == null || upwardTrendTreeString.isEmpty() ){
					gpTreeInFixNotation = Const.thresholdClassifcationBasedGPStringUpwardMap.get(delta);
					upwardTrendTreeString = gpTreeInFixNotation;
				}

			} else {
				if (treeHelperClass.bestTreesInRuns != null)
					treeHelperClass.bestTreesInRuns.clear();
				// SymbolicRegression.log.save("Testing.txt", "test");
				treeHelperClass.getBestTreesForThreshold(classifierBaseduptrendEvents, Const.POP_SIZE, 1,
						Const.MAX_GENERATIONS, thresholdStr);

				if (treeHelperClass.bestTreesInRuns.isEmpty() || treeHelperClass.bestTreesInRuns.size() < 1) {
					System.out.println("treeHelperClass.bestTreesInRuns.isEmpty()");
					System.exit(-1);
				}
				// get best tree
				if (upwardTrendTreeString == null || upwardTrendTreeString.isEmpty()){
					Comparator<AbstractNode> comparator = Collections.reverseOrder();
					Collections.sort(treeHelperClass.bestTreesInRuns, comparator);
					AbstractNode tree = treeHelperClass.bestTreesInRuns.get(treeHelperClass.bestTreesInRuns.size() - 1);
					String treeAsInfixNotationString = tree.printAsInFixFunction();
	
					bestclassifierBasedUpWardEventTree = treeHelperClass.bestTreesInRuns
							.get(treeHelperClass.bestTreesInRuns.size() - 1);
					upwardTrendTreeString = bestclassifierBasedUpWardEventTree.printAsInFixFunction();
	
					curve_bestTreesInRunsUpward.setSize(treeHelperClass.bestTreesInRuns.size());
					Collections.copy(curve_bestTreesInRunsUpward, treeHelperClass.bestTreesInRuns);
					Const.log.save(gpTreeName, treeAsInfixNotationString);
				}
			}

			// Downward trend GP here
			thresholdStr = String.format("%.8f", delta);
			gpTreeName = Const.DOWNWARD_EVENT_STRING + thresholdStr + Const.FUNCTION_NODE_DEFINITION
					+ "_preprocessed.txt";
			thisLine = null;
			trendOfChoiceVecClassifier.clear();

			for (int i = 0; i < trainingEvents.length; i++) {
				if (trainingEvents[i].type == Type.Downturn) {

					String classificationStr = "no";
					Random randomno = new Random();
					double decisionNum =  randomno.nextDouble();
					if (decisionNum < osToDcEventRatio)
						classificationStr = "yes";
					
					

					if ((classificationStr.compareToIgnoreCase("no") == 0)) {
						continue;
					}
					trendOfChoiceVecClassifier.add(trainingEvents[i]);

				}
			}

			System.out.println(
					"number of records trendOfChoiceVecClassifier downward" + trendOfChoiceVecClassifier.size());
			Event[] downtrendEvent = trendOfChoiceVecClassifier.toArray(new Event[trendOfChoiceVecClassifier.size()]);

			if (Const.REUSE_EXISTING_TREE) {

				try {
					// open input stream test.txt for reading purpose.
					BufferedReader br = new BufferedReader(
							new FileReader(Const.log.publicFolder + gpTreeName));
					while ((thisLine = br.readLine()) != null) {
						Const.thresholdClassifcationBasedGPStringDownwardMap.put(delta, thisLine);
						// System.out.println(thisLine);
					}
				} catch (FileNotFoundException fileNotFound) {
					System.out.println(
							Const.log.publicFolder + gpTreeName + " not found. Will rebuild GP tree.");
					if (treeHelperClass.bestTreesInRuns != null)
						treeHelperClass.bestTreesInRuns.clear();

					// fileNotFound.printStackTrace();
				} catch (IOException io) {
					System.out.println("IO excption occured. Will loading" + Const.log.publicFolder
							+ gpTreeName + ". Will rebuild GP tree.");
					// io.printStackTrace();
				} catch (Exception e) {
					System.out.println("Unknown error occured. Will loading" + Const.log.publicFolder
							+ gpTreeName + ". Will rebuild GP tree.");
					// e.printStackTrace();
				}
			} else {
				FWriter writer = new FWriter(Const.log.publicFolder + gpTreeName);

			}

			if (Const.thresholdClassifcationBasedGPStringDownwardMap.containsKey(delta)) {
				if (downwardTrendTreeString == null || downwardTrendTreeString.isEmpty()){
				gpTreeInFixNotation = Const.thresholdClassifcationBasedGPStringDownwardMap.get(delta);
				downwardTrendTreeString = gpTreeInFixNotation;
				}
			} else {
				if (treeHelperClass.bestTreesInRuns != null)
					treeHelperClass.bestTreesInRuns.clear();
				// SymbolicRegression.log.save("DownLoadTesting.txt",
				// "test");
				treeHelperClass.getBestTreesForThreshold(downtrendEvent, Const.POP_SIZE, 1, Const.MAX_GENERATIONS,
						thresholdStr);

				if (treeHelperClass.bestTreesInRuns.isEmpty() || treeHelperClass.bestTreesInRuns.size() < 1) {
					System.out.println("treeHelperClass.bestTreesInRuns.isEmpty()");
					System.exit(-1);
				}

				if (downwardTrendTreeString == null || downwardTrendTreeString.isEmpty()){
					// get best tree
					Comparator<AbstractNode> comparator = Collections.reverseOrder();
					Collections.sort(treeHelperClass.bestTreesInRuns, comparator);
					AbstractNode tree = treeHelperClass.bestTreesInRuns.get(treeHelperClass.bestTreesInRuns.size() - 1);
					String treeAsInfixNotationString = tree.printAsInFixFunction();
	
					bestclassifierBasedDownWardEventTree = treeHelperClass.bestTreesInRuns
							.get(treeHelperClass.bestTreesInRuns.size() - 1);
				
					downwardTrendTreeString = bestclassifierBasedDownWardEventTree.printAsInFixFunction();
			
					curve_bestTreesInRunsDownward.setSize(treeHelperClass.bestTreesInRuns.size());
					Collections.copy(curve_bestTreesInRunsDownward, treeHelperClass.bestTreesInRuns);
					Const.log.save(gpTreeName, treeAsInfixNotationString);
				}
			}

		}
		// }
	}

	/**
	 * 
	 * @param values
	 *            The array with prices/tick data
	 * @param delta
	 *            The threshold value
	 * @param GPTreeFileName
	 *            the name of the file where GP tree is stored
	 * @param
	 */
	public void testbuild(int lastTrainingPricePosition, Double[] values, double delta, Event[] testEvents,
			PreProcess preprocess) {
		lastTrainingPrice = lastTrainingPricePosition;
		if (testEvents == null || testEvents.length < 1)
			return;

		testingEvents = Arrays.copyOf(testEvents, testEvents.length);
		

		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		predictionWithClassifier = new double[testEvents.length];

		for (int outputIndex = 0; outputIndex < testEvents.length - 1; outputIndex++) {
			String foo = "";
			if (Const.splitDatasetByTrendType) {
				if (testEvents[outputIndex].type == Type.Upturn) {
					foo = upwardTrendTreeString;

				} else if (testEvents[outputIndex].type == Type.Downturn) {
					foo = downwardTrendTreeString;

				} else {
					System.out.println("Invalid event");
					System.exit(0);
				}
			} else {
				foo = trendTreeString;
			}

			foo = foo.replace("X0", Integer.toString(testEvents[outputIndex].length()));
			foo = foo.replace("X1", Double.toString(testEvents[outputIndex].high - testEvents[outputIndex].low) );
			double eval = 0.0;

			String classificationStr = "no";
			Random randomno = new Random();
			double decisionNum =  randomno.nextDouble();
			if (decisionNum >= 0.5)
				classificationStr = "yes";
	
			// Use decision from Random()
			if ((classificationStr.compareToIgnoreCase("no") == 0)) {
				;// System.out.println("no");
			} else {
				Double javascriptValue = Double.MAX_VALUE;
				try {
					javascriptValue = (Double) engine.eval(foo);
					eval = javascriptValue.doubleValue();
				} catch (ScriptException e) {
					e.printStackTrace();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}

			BigDecimal bd = null;
			BigDecimal bd2 = null;
			BigDecimal zeroDouble = new BigDecimal("0.0");
			try {
				bd = new BigDecimal(eval);
				bd2 = new BigDecimal(Double.toString(eval));
				 int retval =  bd.compareTo(zeroDouble);
				 if (retval <= 0)
					 eval = 0.0;
			} catch (NumberFormatException e) {
				Integer integerObject = new Integer(testEvents[outputIndex].length());
				eval = integerObject.doubleValue() * (double) Const.NEGATIVE_EXPRESSION_REPLACEMENT;
			}

			predictionWithClassifier[outputIndex] = eval;
		}
	}

	// RMSE cannot be calculated because it is random
	private String calculateRMSEClassifier(Event[] trendEvent, double delta, double[] runPrediction) {
		
		double rmse = 0.0;
		for (int eventCount = 1; eventCount < trendEvent.length; eventCount++) {
			double os = 0.0;

			if (trendEvent.length != runPrediction.length) {
				System.out.println("Event and prediction not equal");
				System.exit(0);
			}

			if (trendEvent[eventCount].overshoot != null) {
				os = trendEvent[eventCount].overshoot.length();
			}

			
			double prediction = runPrediction[eventCount];

			// System.out.println("DC:" + trendEvent[eventCount].length() + "
			// OS:" + os + " prediction:" + prediction);
			rmse = rmse + ((os - prediction) * (os - prediction));

			if (rmse == Double.MAX_VALUE || rmse == Double.NEGATIVE_INFINITY || rmse == Double.NEGATIVE_INFINITY
					|| rmse == Double.NaN || Double.isNaN(rmse) || Double.isInfinite(rmse)
					|| rmse == Double.POSITIVE_INFINITY) {
				System.out.println("DCCurveRandomGP -  Invalid RMSE: " + rmse + ". discarding ");
				predictionRmse= 10.0;
				//predictionRmse = Double.MAX_VALUE;
				return Double.toString(predictionRmse);
			}
		}

		predictionRmse = Math.sqrt(rmse / (trendEvent.length - 1));
		BigDecimal bd = null;
		BigDecimal bd2 = null;
		try {
			bd = new BigDecimal(predictionRmse);
			bd2 = new BigDecimal(Double.toString(predictionRmse));
			if (predictionRmse >= Double.MAX_VALUE)
				return Double.toString(10.0);
		} catch (NumberFormatException e) {
			System.out.println("Invalid predictionRmseClassifier: " + predictionRmse + " discarding ");
			predictionRmse = 10.0;
		}
		// System.out.println(predictionRmse);
		return Double.toString(predictionRmse);
	}
	////

	public String reportTestClassifier(Double[] values, double delta, String GPTreeFileName) {
		return calculateRMSEClassifier(testingEvents, delta, predictionWithClassifier);

	}

	@Override
	String report(Double[] values, double delta, String GPTreeFileName) {
		return calculateRMSEClassifier(testingEvents, delta, predictionWithClassifier);

	}
/*
	@Override
	double trade(PreProcess preprocess) {
		boolean isPositionOpen = false;
		double myPrice = 0.0;
		double transactionCost = 0.025 / 100;
		simpleDrawDown.Calculate(OpeningPosition);
		simpleSharpeRatio.addReturn(0);
		double lastUpDCCend = 0.0;
		for (int i = 1; i < testingEvents.length; i++) {

			
			Double dcPt = new Double(predictionWithClassifier[i]);
			Double zeroOs = new Double(0.0);
			int tradePoint = 0;

			if (dcPt.equals(zeroOs)) // Skip DC classified as not having
										// overshoot
				tradePoint = testingEvents[i].end;
			else
				tradePoint = testingEvents[i].end + (int) Math.floor(predictionWithClassifier[i]);

			
			if (testingEvents[i] == null)
				continue;

			if (i + 1 > testingEvents.length - 1)
				continue;

			if (testingEvents[i + 1] == null)
				continue;

			if (tradePoint > testingEvents[i + 1].end) // If a new DC is
															// encountered
															// before the
															// estimation point
															// skip trading
				continue;

			FReader freader = new FReader();
			FileMember2 fileMember2 = freader.new FileMember2();

		
			if (tradePoint > FReader.dataRecordInFileArray.size() || (lastTrainingPrice - 1) + tradePoint == FReader.dataRecordInFileArray.size()) {
				System.out.println(" DCCurveRandomClassification: predicted datapoint "
						+ ((lastTrainingPrice - 1) + tradePoint) + " is beyond the size of price array  "
						+ FReader.dataRecordInFileArray.size() + " . Trading ended");
				continue;
			} else {
				// I am opening my position in base currency
				try {
					fileMember2 = FReader.dataRecordInFileArray.get((lastTrainingPrice - 1) + tradePoint);

					LinkedHashMap<Integer, Integer> anticipatedTrendMap = new LinkedHashMap<Integer, Integer>();
					LinkedHashMap<Integer, Integer> actualTrendMap = new LinkedHashMap<Integer, Integer>();

					if (testingEvents[i].type == Type.Upturn && !isPositionOpen) {
						// Now position is in quote currency
						// I sell base currency in bid price
						double askQuantity = OpeningPosition;
						double zeroTransactionCostAskQuantity = OpeningPosition;
						double transactionCostPrice = 0.0;
						myPrice = Double.parseDouble(fileMember2.askPrice);
						
						
						transactionCost = askQuantity * (0.025/100);
						transactionCostPrice = transactionCost * myPrice;
						askQuantity =  (askQuantity -transactionCost) *myPrice;
						zeroTransactionCostAskQuantity = zeroTransactionCostAskQuantity *myPrice;
						
						
						if (Double.compare(transactionCostPrice, (zeroTransactionCostAskQuantity - askQuantity)) < 0){
								
							OpeningPosition = askQuantity;
							isPositionOpen = true;
							positionArrayQuote.add(new Double(OpeningPosition));
							
							tradedPrice.add(new Double(myPrice));
							anticipatedTrendMap.put(testingEvents[i].start, tradePoint);
							anticipatedTrend.add(anticipatedTrendMap);
							lastUpDCCend = Double.parseDouble(FReader.dataRecordInFileArray.get((lastTrainingPrice - 1) + testingEvents[i].end).bidPrice);
							if (testingEvents[i].overshoot == null || testingEvents[i].overshoot.length() < 1)
								actualTrendMap.put(testingEvents[i].start, testingEvents[i].end );
							else
								actualTrendMap.put(testingEvents[i].start, testingEvents[i].overshoot.end );
							
							actualTrend.add(actualTrendMap);
						}

					} else if (testingEvents[i].type == Type.Downturn && isPositionOpen) {
						// Now position is in base currency
						// I buy base currency
						double bidQuantity = OpeningPosition;
						double zeroTransactionCostBidQuantity = OpeningPosition;
						double transactionCostPrice = 0.0;
						myPrice = Double.parseDouble(fileMember2.bidPrice);

						transactionCost = bidQuantity * (0.025 / 100);
						transactionCostPrice = transactionCost * myPrice;
						bidQuantity = (bidQuantity - transactionCost) * myPrice;
						zeroTransactionCostBidQuantity = zeroTransactionCostBidQuantity * myPrice;
						
						if (Double.compare(transactionCostPrice, (zeroTransactionCostBidQuantity - bidQuantity)) < 0 ){
								//&& myPrice < lastUpDCCend){
									
									
							OpeningPosition = (OpeningPosition - transactionCost) / myPrice;
							
							isPositionOpen = false;
							positionArrayBase.add(new Double(OpeningPosition));

							tradedPrice.add(new Double(myPrice));
							anticipatedTrendMap.put(testingEvents[i].start, tradePoint);
							anticipatedTrend.add(anticipatedTrendMap);

							if (testingEvents[i].overshoot == null || testingEvents[i].overshoot.length() < 1)
								actualTrendMap.put(testingEvents[i].start, testingEvents[i].end);
							else
								actualTrendMap.put(testingEvents[i].start, testingEvents[i].overshoot.end);

							actualTrend.add(actualTrendMap);
						}
					}
				} catch (ArrayIndexOutOfBoundsException exception) {
					System.out.println(" DCCurveClassiifcation: Search for element " + ((lastTrainingPrice - 1) + tradePoint)
							+ " is beyond the size of price array  " + 
							FReader.dataRecordInFileArray.size() + " . Trading ended") ;
					break;	
				}
			}
		}

		if (isPositionOpen) {
			tradedPrice.remove(tradedPrice.size() - 1);
			anticipatedTrend.remove(anticipatedTrend.size() - 1);
			actualTrend.remove(actualTrend.size() - 1);
			OpeningPosition = positionArrayBase.get(positionArrayBase.size() - 1);
			positionArrayQuote.remove(positionArrayQuote.size() - 1);
			isPositionOpen = false;
		}
		
		otherTradeCalculations();

		return OpeningPosition;
	}
*/
	double getMddPeak() {
		return simpleDrawDown.getPeak();
	}

	double getMddTrough() {
		return simpleDrawDown.getTrough();
	}

	double getMaxMddBase() {
		return simpleDrawDown.getMaxDrawDown();
	}

	double getMddPeakQuote() {
		return simpleDrawDownQuote.getPeak();
	}

	double getMddTroughQuote() {
		return simpleDrawDownQuote.getTrough();
	}

	double getMaxMddQuote() {
		return simpleDrawDownQuote.getMaxDrawDown();
	}

	int getNumberOfQuoteCcyTransactions() {

		return positionArrayQuote.size() - 1;
	}

	int getNumberOfBaseCcyTransactions() {

		return positionArrayBase.size() - 1;
	}

	double getBaseCCyProfit() {
		double profit = 0.00;
		ArrayList<Double> profitList = new ArrayList<Double>();
		if (positionArrayBase.size() == 1)
			return 0.00;
		for (int profitLossCount = 1; profitLossCount < positionArrayBase.size(); profitLossCount++) {
			double profitCalculation = positionArrayBase.get(profitLossCount)
					- positionArrayBase.get(profitLossCount - 1) / positionArrayBase.get(profitLossCount - 1);
			profitList.add(profitCalculation);
		}
		profit = profitList.stream().mapToDouble(i -> i.doubleValue()).sum();
		return profit;
	}

	double getQuoteCCyProfit() {
		double profit = 0.00;
		ArrayList<Double> profitList = new ArrayList<Double>();
		if (positionArrayQuote.size() == 1)
			return 0.00;
		// Start from 3rd element because first element is zero
		for (int profitLossCount = 1; profitLossCount < positionArrayQuote.size(); profitLossCount++) {
			double profitCalculation = positionArrayQuote.get(profitLossCount)
					- positionArrayQuote.get(profitLossCount - 1) / positionArrayQuote.get(profitLossCount - 1);
			profitList.add(profitCalculation);
		}
		profit = profitList.stream().mapToDouble(i -> i.doubleValue()).sum();
		return profit;
	}

	@Override
	public String getDCCurveName() {

		return "DCCurveRandomGP";
	}

	@Override
	double trainingTrading(PreProcess preprocess) {
		boolean isPositionOpen = false;
		double myPrice = 0.0;
		double lastClosedPosition = 0.0;
		double transactionCost = 0.025 / 100;
		
		
		for (int i = 1; i < trainingEvents.length; i++) {

			Double dcPt = new Double(trainingGpPrediction[i]);
			Double zeroOs = new Double(0.0);
			int tradePoint = 0;

			if (trainingEvents[i] == null)
				continue;
			if (dcPt.equals(zeroOs)) // Skip DC classified as not having
										// overshoot
				tradePoint = trainingEvents[i].end;
			else
				tradePoint = trainingEvents[i].end + (int) Math.floor(trainingGpPrediction[i]) + 1;

			if (i + 1 > trainingEvents.length - 1)
				continue;

			if (trainingEvents[i + 1] == null)
				continue;

			if (tradePoint > trainingEvents[i + 1].start) // If a new DC is
															// encountered
															// before the
															// estimation point
															// skip trading
				continue;

			FReader freader = new FReader();
			FileMember2 fileMember2 = freader.new FileMember2();

			if (tradePoint >= FReader.dataRecordInFileArray.size()) {
				continue;
			}

			// I am opening my position in base currency
			try {
			fileMember2 = FReader.dataRecordInFileArray.get(tradePoint);
			}
			catch (ArrayIndexOutOfBoundsException e){
				System.out.println(e.getMessage());
				continue;
			}
			

			if (trainingEvents[i].type == Type.Upturn && !isPositionOpen) {
				// Now position is in quote currency
				// I sell base currency in bid price
				// I sell base currency in bid price
				double askQuantity = trainingOpeningPosition;
				double zeroTransactionCostAskQuantity = trainingOpeningPosition;
				double transactionCostPrice = 0.0;
				myPrice = Double.parseDouble(fileMember2.askPrice);
				
				
				transactionCost = askQuantity * (0.025/100);
				transactionCostPrice = transactionCost * myPrice;
				askQuantity =  (askQuantity -transactionCost) *myPrice;
				zeroTransactionCostAskQuantity = zeroTransactionCostAskQuantity *myPrice;
				
				
				if (Double.compare(transactionCostPrice, (zeroTransactionCostAskQuantity - askQuantity)) < 0 ){
//				
					trainingOpeningPosition = askQuantity;
					isPositionOpen = true;
				}

			} else if (trainingEvents[i].type == Type.Downturn && isPositionOpen) {
				// Now position is in base currency
				// I buy base currency
				double bidQuantity = trainingOpeningPosition;
				double zeroTransactionCostBidQuantity = trainingOpeningPosition;
				double transactionCostPrice = 0.0;
				myPrice = Double.parseDouble(fileMember2.bidPrice);
				
				
				transactionCost = bidQuantity * (0.025/100);
				transactionCostPrice = transactionCost * myPrice;
				bidQuantity =  (bidQuantity -transactionCost) *myPrice;
				zeroTransactionCostBidQuantity = zeroTransactionCostBidQuantity *myPrice;
				
				if (Double.compare(transactionCostPrice, (zeroTransactionCostBidQuantity - bidQuantity)) < 0){

					trainingOpeningPosition =  (trainingOpeningPosition -transactionCost) /myPrice;
					lastClosedPosition = trainingOpeningPosition;
					isPositionOpen = false;
				}
			}

		}

		if (isPositionOpen) {
			trainingOpeningPosition = lastClosedPosition;
		}

		return trainingOpeningPosition;

	}

	@Override
	void estimateTraining(PreProcess preprocess) {
		trainingGpPrediction = new double[trainingEvents.length];
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");

		for (int outputIndex = 0; outputIndex < trainingEvents.length; outputIndex++) {
			String foo = "";
			if (Const.splitDatasetByTrendType) {
				if (trainingEvents[outputIndex].type == Type.Upturn) {
					foo = upwardTrendTreeString;

				} else if (trainingEvents[outputIndex].type == Type.Downturn) {
					foo = downwardTrendTreeString;

				} else {
					System.out.println("Invalid event");
					System.exit(0);
				}
			} else {
				foo = trendTreeString;
			}

			foo = foo.replace("X0", Integer.toString(trainingEvents[outputIndex].length()));
			foo = foo.replace("X1", Double.toString(trainingEvents[outputIndex].high - trainingEvents[outputIndex].low) );
			double eval = 0.0;

			String classificationStr = "no";
			Random randomno = new Random();
			double decisionNum =  randomno.nextDouble();
			if (decisionNum < osToDcEventRatio)
				classificationStr = "yes";
			
			

			// Use decision from Random()
			if ((classificationStr.compareToIgnoreCase("no") == 0)) {
				;// System.out.println("no");
			} else {
				Double javascriptValue = Double.MAX_VALUE;
				try {
					javascriptValue = (double) engine.eval(foo);
					eval = javascriptValue.doubleValue();
				} catch (ScriptException e) {
					e.printStackTrace();
				} catch (ClassCastException e) {
					e.printStackTrace();
				}
			}

			BigDecimal bd = null;
			BigDecimal bd2 = null;
			try {
				bd = new BigDecimal(eval);
				bd2 = new BigDecimal(Double.toString(eval));
			} catch (NumberFormatException e) {
				Integer integerObject = new Integer(trainingEvents[outputIndex].length());
				eval = integerObject.doubleValue() * (double) Const.NEGATIVE_EXPRESSION_REPLACEMENT;
			}

			trainingGpPrediction[outputIndex] = eval;
		}

	}

	@Override
	public String getActualTrend() {
		return actualTrendString;
	}

	@Override
	public String getPredictedTrend() {
		return predictedTrendString;
	}
	
	@Override
	protected int getMaxTransactionSize() {
		// TODO Auto-generated method stub
		return positionArrayBase.size();
	}

	
	@Override
	protected double getTransanction(int i) {
		if ( i >= positionArrayBase.size())
			return 0.0;
		
		return positionArrayBase.get(i);
	}
	
	public  double calculateSD(){
		return calculateBaseSD(positionArrayBase);
	}
	
	public  double getMaxValue(){
		  
		  return getMaxValue(positionArrayBase);
		}
	public   double getMinValue(){
	 
	  return   getMinValue(positionArrayBase);
	}
	
	@Override
	public <E> void assignPerfectForesightRegressionModel(E[] inputArray) {
		downwardTrendTreeString = (String) inputArray[0];
		upwardTrendTreeString = (String) inputArray[1];
		
	}

}
