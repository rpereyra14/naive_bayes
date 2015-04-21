### Overview

To determine whether an email is spam or not based on the words it contains. For example, emails mentioning money being offered by a Nigerian price would be deemed more likely to be spam based on words contained in the body of the email.

### Format of train, test, and vocab files

+ The vocab.txt file contains a mapping of words to integers.
+ The train and test files are of the format: [SPAM=1|NOT=0],word_from_vocab,word_count(,word_from_vocab,word_count)*
+ The train file was used to train the naive bayes model. The test file was used to test the accuracy of the model (more on that below).

### Compilation Notes:

	Use 

		$ javac Naive_Bayes.java 

	to compile.


### Execution Notes:

	Use 

		$ java Naive_Bayes 

	to execute.


	CrossValidation can be turned on at runtime with the command-line argument "-crossV". Laplacian Smoothing can be turned on at runtime with the command-line argument "-lapl". Both Laplacian Smoothing and Cross Validation may be turned on at the same time. The order of the flags does not matter.

### Accuracy/Correctness Notes:

	Single runs hover around the 97% correctness margin (calculated via 5-fold CrossValidation). If laplacian smoothing is added, correctness increases to around 98% (calculated via 5-fold CrossValidation).

### Design Notes:

	Vocabulary, TrainData, and TestData are loaded through their respective "load" methods. Prediction of a spam or ham email is based on Bayesian Inference as determined by the Maximum Likelihood estimate of the Naive Bayes model outlined in HW 2 question 8. Labeles are checked for correctness and banners describing the results are printed after each run.
