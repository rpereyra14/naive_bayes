/* 
 * University of North Carolina at Chapel Hill
 * Spring 2013 Artificial Intelligence & Machine Learning
 * Homework 2 - 3/18/2013
 * Renato Pereyra
 */

The Naive Bayes model as described in HW 2 was implemented in Naive_Bayes.java

Compilation Notes:

	Use 

		$ javac Naive_Bayes.java 

	to compile.


Execution Notes:

	Use 

		$ java Naive_Bayes 

	to execute.


	CrossValidation can be turned on at runtime with the command-line argument "-crossV". Laplacian Smoothing can be turned on at runtime with the command-line argument "-lapl". Both Laplacian Smoothing may be turned on at the same time. The order of the flags does not matter.

Accuracy/Correctness Notes:

	Single runs hover around the 97% correctness margin (calculated via 5-fold CrossValidation). If laplacian smoothing is added, correctness increases to around 98% (calculated via 5-fold CrossValidation).

Design Notes:

	Vocabulary, TrainData, and TestData are loaded through their respective "load" methods. Prediction of a spam or ham email is based on Bayesian Inference as determined by the Maximum Likelihood estimate of the Naive Bayes model outlined in HW 2 question 8. Labeles are checked for correctness and banners describing the results are printed after each run.
