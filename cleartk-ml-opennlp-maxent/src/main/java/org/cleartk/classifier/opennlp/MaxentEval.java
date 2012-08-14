/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package org.cleartk.classifier.opennlp;

import java.io.File;
import java.io.IOException;

import opennlp.maxent.io.SuffixSensitiveGISModelReader;
import opennlp.model.Event;
import opennlp.model.MaxentModel;
import opennlp.model.RealValueFileEventStream;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * 
 * @author Philipp Wetzler
 * @deprecated Use the cleartk-eval module instead.
 */
@Deprecated
public class MaxentEval {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      usage();
      System.exit(1);
    }

    MaxentModel model = (new SuffixSensitiveGISModelReader(new File(args[0]))).getModel();
    RealValueFileEventStream eventStream = new RealValueFileEventStream(new File(args[1]));

    int numberOfOutcomes = model.getNumOutcomes();
    int[][] confusionMatrix = new int[numberOfOutcomes][numberOfOutcomes];
    int totalEvents = 0;
    int unknownEvents = 0;

    while (eventStream.hasNext()) {
      Event event = eventStream.next();
      int realOutcome = model.getIndex(event.getOutcome());
      int modelOutcome = model.getIndex(model.getBestOutcome(model.eval(event.getContext())));
      if (realOutcome >= 0) {
        confusionMatrix[realOutcome][modelOutcome] += 1;
        totalEvents++;
      } else {
        unknownEvents += 1;
      }

      if ((totalEvents % 50000) == 0) {
        System.out.print(".");
        System.out.flush();
      }
    }
    System.out.println();

    System.out.println("Outcomes:");
    for (int i = 0; i < numberOfOutcomes; i++) {
      System.out.format("%3d - %s\n", i + 1, model.getOutcome(i));
    }
    System.out.println();

    System.out.println("Total Number of Events:");
    System.out.println(totalEvents);
    System.out.println();

    if (unknownEvents > 0) {
      System.out.println("Events of an Unknown Class:");
      System.out.println(unknownEvents);
      System.out.println();
    }

    if ((model.getOutcome(0).equals("true") || model.getOutcome(0).equals("false"))
        && (model.getOutcome(1).equals("true") || model.getOutcome(1).equals("false"))
        && (!model.getOutcome(0).equals(model.getOutcome(1)))) {
      int trueOutcome;
      if (model.getOutcome(0) == "true")
        trueOutcome = 0;
      else
        trueOutcome = 1;

      int falseOutcome = 1 - trueOutcome;

      double tp = confusionMatrix[trueOutcome][trueOutcome];
      double fp = confusionMatrix[falseOutcome][trueOutcome];
      // double tn = confusionMatrix[falseOutcome][falseOutcome];
      double fn = confusionMatrix[trueOutcome][falseOutcome];

      double precision = tp / (tp + fp);
      double recall = tp / (tp + fn);

      System.out.format("Precision: %1.3f\n", precision);
      System.out.format("Recall:    %1.3f\n", recall);
      System.out.println();
    }

    System.out.println("Confusion Matrix:");

    printMatrix(confusionMatrix, numberOfOutcomes, 0);

    printMatrix(confusionMatrix, numberOfOutcomes, totalEvents);

    System.out.println();
  }

  private static void printMatrix(int matrix[][], int numberOfClasses, int totalEvents) {
    System.out.format("     ");
    for (int i = 0; i < numberOfClasses; i++) {
      System.out.format("%7d ", i + 1);
    }
    System.out.println();

    for (int j = 0; j < numberOfClasses; j++) {
      System.out.format("%7d ", j + 1);
      for (int i = 0; i < numberOfClasses; i++) {
        if (totalEvents > 0) {
          double val = ((double) matrix[j][i]) / ((double) totalEvents) * 100.0;
          System.out.format("%6.2f%% ", val);
        } else {
          System.out.format("%7d ", matrix[j][i]);
        }
      }
      System.out.println();
    }
    System.out.println();

  }

  private static void usage() {
    System.err.println("Usage:");
    System.err.println("java [...] org.cleartk.classifier.OpenNLPMaxentEval <model file> <data file>");
    System.err.println();
  }

}
