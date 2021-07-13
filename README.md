# Dynamic-Clustering


SimFunction class contains all the used similarity functions.

The Cluster class contains Navie, GreedyDB(Hill-climbing), incremental DB(Greedy), DBSCAN and k-means methods. 

The data preprocess codes are in DataPro class. And the Evaluate class is used to measure the clustering results.

The updates processing is controled by the DSframe class.

The ML model is built in python, and we call python script in the java programs which are in the MLbased class.

All the experiments are run in the main function of the Synthetic and Test classes. While the main function is about DynamicC, the other methods are tested in the main function of the Test class.
