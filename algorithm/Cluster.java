package algorithm;

import java.io.*;
import java.util.*;
import java.util.Hashtable;


public class Cluster {


    static class Parameter {
        static double threshold=0.7;
        static int minDis = 10;
        static int minPst = 2;
    }

    static class ClusterData {
        ArrayList<ArrayList<Integer>> inter= new ArrayList<ArrayList<Integer>>();
        int merge; // To store the number of merge operation
        int split; // To store split
        int move; // To store move
        int loop; // To record the number of computation for DBindex
        public ClusterData(ArrayList<ArrayList<Integer>> inter, int merge, int split, int move, int loop)
        {
            this.inter = inter;
            this.merge = merge;
            this.split = split;
            this.move = move;
            this.loop = loop;
        }
    }

    static class Para {
        double intervalue;
        int cluster;
        public Para(double intervalue, int cluster)
        {
            this.intervalue = intervalue;
            this.cluster = cluster;
        }
    }

    static class ClusterVorQ {
        int VorQ;
        int cluster;
        double inter;
        public ClusterVorQ(int VorQ, int cluster, double inter)
        {
            this.VorQ = VorQ;
            this.cluster = cluster;
            this.inter = inter;
        }
    }
    //create a data structure for the parameters of clusters, e.g. intra distance (similarity) or inter distance
    //this structure will cost more memory to store these parameters but will improve efficiency
    static class ClusterParameter {
        double sum; //store the DBindex value
        ArrayList<Double> intra_vector;
        ArrayList<ArrayList<Double>> inter;
        public ClusterParameter(double sum, ArrayList<Double> intra_vector, ArrayList<ArrayList<Double>> inter){
            this.sum = sum;
            this.intra_vector = intra_vector;
            this.inter = inter;
        }
    }

    public static ArrayList<ArrayList<Integer>> static_DBSCAN(int m1, int m2, ArrayList<Hashtable> adj) {  //m1 and m2 are used to define the start and end point to be visited
        //note that all the outliers are store in the 1-th row of the returned Arraylist, we care about it when we use ML model to predict for this outlier cluster.
        // It is better to take each of the outliers in the 1-th row as a single cluster when we apply the ML model to predict.
        //it is quite easy to do DBSCAN when we have the similarity graph of the data i.e. the variable adj
        //building the similarity graph needs O(N^2) since we compare n data with n data and get a nxn matrix
        //In other words, the complexity include the similarity computing
        //query algorithm randomly choose a data to check its neighboors so that it might not need to check all the data since the labels can be propagated to its neighbors.
        ArrayList<Integer> Points = new ArrayList<Integer>();//store all the points to be visited in an array
        ArrayList<ArrayList<Integer>> cluster = new ArrayList<ArrayList<Integer>>(); //store the final clustering result in a 2-d arraylist
        for (int i = m1; i< m2; i++){
            Points.add(i);
        }
        //https://dl.acm.org/doi/pdf/10.1145/3068335
        //In this algorithm, if a node belong two multiple clusters, it will only be assigned to the first cluster that it involves.
        cluster.add(new ArrayList<>());
        while(Points.size()!=0){
            if(Defined( Points.get(0), cluster)){
                Points.remove(0);
                continue;
            }
            ArrayList<Integer> neighbor = new ArrayList<>();  //store the neighbor points
            Enumeration enu = adj.get(Points.get(0)).keys();

            while (enu.hasMoreElements()) {
                int p = (int) enu.nextElement();
                if (p!=Points.get(0)) //exclude the point i itself
                    neighbor.add(p);
                //System.out.print(enu.nextElement()+" ");
            }
            if (adj.get(Points.get(0)).size()<Parameter.minPst){
                cluster.get(0).add(Points.get(0)); //use the 0th row to store the noise points; here we are checking the point i
                Points.remove(0);
                continue;
            }
            cluster.add(new ArrayList<>()); //create a new cluster or a new label
            cluster.get(cluster.size()-1).add(Points.remove(0));
            while(neighbor.size()!=0){
                ArrayList<Integer> neighbor1 = new ArrayList<>();
                if(IsIn(neighbor.get(0), cluster.get(0))){
                    cluster.get(cluster.size()-1).add(neighbor.get(0));  //if label(q) = Noise then label(q) ??? c
                }
                if(Defined(neighbor.get(0), cluster))
                { //currently we are on neighbor.get(j)
                    neighbor.remove(0); //if label(q)  undefined then continue
                    continue;
                }
                //System.out.println(neighbor.get(0));
                Enumeration enu1 = adj.get(neighbor.get(0)).keys(); // Neighbors N ??? RangeQuery(DB, dist,q,??)
                while (enu1.hasMoreElements()) {
                    int q = (int) enu1.nextElement();
                    if (q!=neighbor.get(0)) //exclude the point i itself
                        neighbor1.add(q);
                }
                cluster.get(cluster.size()-1).add(neighbor.get(0));   //label(q) ??? c

                if (adj.get(neighbor.get(0)).size()<Parameter.minPst) {
                    neighbor.remove(0);
                    continue;
                }
                neighbor.remove(0);
                for (int s = 0; s< neighbor1.size(); s++){  //S ??? S ??? N
                    neighbor.add(neighbor1.get(s));
                }
            }
        }
//        for (int i =0; i< Points.size(); i++){
//            if (adj.get(i).size()>=Parameter.minPst){  //judge if a point has at least minPst-1 neighbor points i.e. core point
//
//                if() { //judge if the cluster already exists
//                    cluster.add(new ArrayList<>()); //create a new cluster
//                }
//                ArrayList<Integer> temp_cluster = new ArrayList<>();  //create a initial cluster
//                ArrayList<Integer> temp = new ArrayList<>();  //store the points around a core point
//                Enumeration enu = adj.get(i).keys();
//
//                while (enu.hasMoreElements()) {
//                    temp.add((int) enu.nextElement());
//                    temp_cluster.add((int) enu.nextElement());
//                    Points.remove((int) enu.nextElement()); //remove those points meaning that they are visited
//                    //System.out.print(enu.nextElement()+" ");
//                }
//                while (temp.size()!=0){
//
//                }
//
//            }
//
//        }
        return cluster;
    }

    public static boolean IsIn(int k, ArrayList<Integer> t){
        boolean x=false;
        for (int i = 0; i< t.size(); i++){
            if (k==t.get(i)) {
                x = true;
                break;
            }
        }
        return x;
    }
    public static int P_Position(int k, ArrayList<ArrayList<Integer>> t){  //find which cluster a point is in
        int x=-1;
        for (int i = 0; i< t.size(); i++){
            for (int j = 0; j<t.get(i).size(); j++) {
                if (k == t.get(i).get(j)) {
                    x = i;
                    break;
                }
            }
        }
        return x;
    }

    public static boolean Defined(Integer k, ArrayList<ArrayList<Integer>> t){
        boolean x=false;
        for (int i = 0; i< t.size(); i++){
            for(int j = 0; j<t.get(i).size(); j++){
                if (k==t.get(i).get(j)) {
                    x = true;
                    break;
                }
            }
        }
        return x;
    }


    public static ArrayList<ArrayList<Integer>> dynamic_DBSCAN(ArrayList<ArrayList<Integer>> cluster, ArrayList<Integer> update, ArrayList<Hashtable> adj){
        //dynamic method with ML model for DBSCAN
        //cluster is used to store the clustering result, update is used to store the update
        //for the dynamic algorithm, we use an auxiliary program in DSframe to help decide the size of updates

        /* idea:
        step 1: Given an initial clustering and the updates i.e. cluster and array update.
        step 2: For each item in array, do merge, i.e. assign it to an existing cluster or create a new cluster. But how? may force the cluster to follow some constraints in DBSCAN.
           add: first, run the batch static algorithm on the updates  //only outlier would be core or board points and cluster might be merged, no other change will happen
                we need to fi nd the most "close" cluster to the new point, note that this is not that straightfoward in the batch algorithm
           remove:
           update:
        step 3: Check
        step 4:
         */

        //current algorithm is only for increment i.e. add new points
        int m1 =  update.get(0); //get the first value of update so that we can preprocess adj
        int m2 = update.get(update.size()-1);

        ArrayList<ArrayList<Integer>> cluster_temp = static_DBSCAN(m1, m2, adj);  //do the static DBSCAN on update, since adj_temp include the existing points, it seems that we need not do anything more
        //do merge and split between cluster_temp and cluster
        // I think the key idea here with ML model is to find out the core point distribution so that we can avoid checking more overlapping points
        Collection<Integer> list1 = new ArrayList<Integer>();
        Collection<Integer> list2 = new ArrayList<Integer>();
        for (int i =0; i<cluster.size(); i++)
            list1.addAll(cluster.get(i));
        for (int i =0; i<cluster_temp.size(); i++)
            list2.addAll(cluster_temp.get(i));

        list1.retainAll(list2);   //intersection
        ArrayList<Integer> intersection = (ArrayList<Integer>) list1;

       //do merge by checking whether the overlapping point is core point or not
        for (int i =0; i<intersection.size(); i++){
            if(adj.get(intersection.get(i)).size()>Parameter.minPst){
                int p = P_Position(intersection.get(i), cluster_temp);//find its position in cluster_temp
                if (p==-1)  //i.e. no such value in cluster_temp anymore due to merge operation
                    continue;
                int q = P_Position(intersection.get(i), cluster);//find its position in cluster
                if (q==0) { //i.e. outlier point in cluster (previous clustering)
                    cluster_temp.get(p).add(intersection.get(i));  // note that we did not check all the outliers in cluster.get(0) for efficiency, maybe we will do that later
                    for (int j = 0; j< cluster.get(0).size(); j++) {
                        if (cluster.get(0).get(j) == intersection.get(i)) {
                            cluster.get(0).remove(j);
                            break;
                        }
                    }
                }
                cluster.get(q).addAll(cluster_temp.get(p));   //merge the p-th row of cluster_temp to the q-th row of cluster
                cluster_temp.remove(p);
            }
        }
        cluster.get(0).addAll(cluster_temp.get(0));  //add all the outliers of cluster_temp to cluster.get(0)
        for (int i = 1; i< cluster_temp.size(); i++){
            cluster.add(cluster_temp.get(i));
        }
        return cluster;
    }



    public static void ClusterMerge(ArrayList<Integer> cluster, ArrayList<Integer> cluster_temp){ //actually we need to carefully check the conditions of merge since the board point may be
        //mistakenly taken as a bridge to merge to clusters. In this case, we need to check whether is overlapping points are core points.
        for (int i = 0; i< cluster_temp.size(); i++){
            if (IsIn(cluster_temp.get(i), cluster)){
                continue;
            }
            else{
                cluster_temp.add(cluster.get(i));
            }
        }
    }


    public static void static_Kmeans(ArrayList<ArrayList<Integer>> vector, ArrayList<Integer> array, double k, ArrayList<Hashtable> adj){

    }



    public static ClusterData IncrementalDB(ArrayList<ArrayList<Integer>> vector, ArrayList<Integer> array, double k, ArrayList<Hashtable> adj) throws IOException {
        //array is used to store the updated records, e.g. newly added records, if the existing records are removed, the variable "array"  then is empty and we just need to predict which clusters will change.
    //k?????????
    //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    int i = 0;
    int j = 0;
    int p = 0;
    int merge = 0;
    int split = 0;
    int move = 0;
    int loop = 0;

    // write println into ChangeHistory file


    //File file = new File("/Users/binbingu/Documents/Codes/Write-test/Change-IncreForDB.txt");
    //File file = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/Change-IncreForCorr.txt");
    File file = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/DB-Merge1000.txt");
    File file1 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/DB-Split1000.txt");
    File file2 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/Intra.txt");
    File file3 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/Inter.txt");
    File file4 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/change.txt");

    File file5 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/Intra_S.txt");
    File file6 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/Inter_S.txt");
    File file7 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/change_S.txt");

    File file8 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/Size1.txt");
    File file9 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/Size2.txt");
    File file10 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/Size1_S.txt");
    File file11 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/Size2_S.txt");
    File file111 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/train/DBindexHistroy.txt");
//    File file2 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/Intra800.txt");
//    File file3 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/Inter800.txt");
//    File file4 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/DB/change800.txt");


    FileWriter xw = null;
    FileWriter xw1 = null; FileWriter xw2 = null; FileWriter xw3 = null; FileWriter xw4 = null;

    FileWriter xw5 = null; FileWriter xw6 = null; FileWriter xw7 = null; FileWriter xw8 = null; FileWriter xw9 = null;
    FileWriter xw10 = null; FileWriter xw11 = null;
        FileWriter xw111= null;

    xw = new FileWriter(file, true);
    xw1 = new FileWriter(file1, true);
    xw2 = new FileWriter(file2, true);
    xw3 = new FileWriter(file3, true);
    xw4 = new FileWriter(file4, true);
    xw5 = new FileWriter(file5, true);
    xw6 = new FileWriter(file6, true);
    xw7 = new FileWriter(file7, true);
    xw8 = new FileWriter(file8, true);
    xw9 = new FileWriter(file9, true);
    xw10 = new FileWriter(file10, true);
    xw11 = new FileWriter(file11, true);
        xw111 = new FileWriter(file111, true);

    PrintWriter pw = new PrintWriter(xw);
    PrintWriter pw1 = new PrintWriter(xw1);
    PrintWriter pw2 = new PrintWriter(xw2);
    PrintWriter pw3 = new PrintWriter(xw3);
    PrintWriter pw4 = new PrintWriter(xw4);
    PrintWriter pw5 = new PrintWriter(xw5);
    PrintWriter pw6 = new PrintWriter(xw6);
    PrintWriter pw7 = new PrintWriter(xw7);
    PrintWriter pw8 = new PrintWriter(xw8);
    PrintWriter pw9 = new PrintWriter(xw9);
    PrintWriter pw10 = new PrintWriter(xw10);
    PrintWriter pw11 = new PrintWriter(xw11);
    PrintWriter pw111 = new PrintWriter(xw111);


    ArrayList<ArrayList<Integer>> queue = new ArrayList<ArrayList<Integer>>();  //store the updates with a queue
    for (i = 0; i < array.size(); i++) {
        queue.add(new ArrayList<Integer>());
        queue.get(i).add(array.get(i));
    }
    //ClusterParameter initialization = DbindexPara1(vector, queue, array1);

    int z1 = 0;
    int z2 = 0;
    int ne_sample_size = 0;
    int ne_sample_size1 = 0;

    while (queue.size() > 0) {
        double temp = DBindex111(vector, queue, adj);
        boolean change = false;  //????????????cluster????????????
        ArrayList<Integer> b111;
        for (i = vector.size() - 1; i >= 0; i--) {          //compare queue.get(0) with vector
            if (IsConnected(queue.get(0), vector.get(i), k, adj) == false) {    //we do need to judge IsConnected every time.
                continue;
            } else {
                loop++;
                ArrayList<Integer> bb = new ArrayList<Integer>();  //use it to store the original cluster
                int a111 = vector.get(i).size();
                vector.get(i).addAll(queue.get(0));         //try to Merge   ;   add the arraylist b111 i.e. the first arraylist in queue
                if (DBindex222(vector, queue, adj) < temp) {
                    for (j = 0; j < a111; j++)
                        bb.add(vector.get(i).get(j));

                    System.out.println(queue.get(0) + " Merge With " + bb + " into " + vector.get(i));
                    pw.println(queue.get(0)+ " Merge With " +bb + " into "+ vector.get(i));
                    z1++;
                    //pw.println(z1 + " "+Intra_Cluster(vector.get(i), array1));
                    pw2.print(Intra_Cluster(vector.get(i), adj) + ",");
                    pw3.print(MinInter(queue.get(0),bb, vector, queue, adj) + ",");
                    //pw3.print(Inter_Cluster(queue.get(0), bb, array1) + ",");
                    pw4.print("1,");
                    pw8.print(queue.get(0).size() + ",");
                    pw9.print(MinInterCluster(queue.get(0),bb, vector, queue, adj) + ",");
                    pw111.println(temp);
                    pw111.flush();

//                    pw2.println(Intra_Cluster(vector.get(i), array1) + ",");
//                    pw3.println(Inter_Cluster(queue.get(0), bb, array1) + ",");
//                    pw4.println("1,");
                    //record when the merge happens
//                        System.out.println(queue.get(0)+ " Merge With " +bb + " into "+ vector.get(i)+" ; The inter-similarity between them is: "+ (1- Inter_Cluster(queue.get(0), bb, array1))
//                        +"  ; The intra-similarity of the new cluster change from "+ Intra_Cluster(bb, array1)+" to "+Intra_Cluster(vector.get(i),array1));
//                        pw.println(queue.get(0)+ " Merge With " +bb + " into "+ vector.get(i)+" ; The inter-similarity between them is: "+ (1- Inter_Cluster(queue.get(0), bb, array1))
//                                +"  ; The intra-similarity of the new cluster change from "+ (1- Intra_Cluster(bb, array1))+" to "+ (1- Intra_Cluster(vector.get(i),array1)));
                    pw.flush();
                    pw2.flush();
                    pw3.flush();
                    pw4.flush(); pw8.flush(); pw9.flush();
                    bb.clear();
                    //temp = DBindex222(vector, queue, k, array1);
                    queue.add(vector.get(i));
                    queue.remove(0);
                    vector.remove(i);
                    change = true;
                    merge++;
                    break;
                } else {
                    removeFrom(vector.get(i), a111);      //maybe there is an existing method to do this
                    if (ne_sample_size < 100) {
                        pw2.print(Intra_Cluster(queue.get(0), adj) + ",");
                        pw3.print(MinInter1(queue.get(0), vector, queue, adj) + ",");
                        //pw3.print(Inter_Cluster(queue.get(0), vector.get(i), array1) + ",");
                        pw4.print("0,");
                        pw8.print(queue.get(0).size() + ",");
                        pw9.print(MinInter1Cluster(queue.get(0), vector, queue, adj) + ",");
//                        pw2.println(Intra_Cluster(vector.get(i), array1) + ",");
//                        pw3.println(Inter_Cluster(queue.get(0), vector.get(i), array1) + ",");
//                        pw4.println("0,");
                        ne_sample_size++;
                        pw2.flush();
                        pw3.flush();
                        pw4.flush(); pw8.flush(); pw9.flush();
                    }
                }
            }
        }
        if (change == false) {
            for (i = queue.size() - 1; i > 0; i--) {          //compare queue.get(0) with queue except itself
                if (IsConnected(queue.get(0), queue.get(i), k, adj) == false)    //we do need to judge IsConnected every time.
                {
                    continue;
                } else {
                    loop++;
                    int a11 = vector.size();
                    ArrayList<Integer> cc = new ArrayList<Integer>();
                    int a1111 = queue.get(i).size();     //???????????????????????????remove
                    queue.get(i).addAll(queue.get(0));         //try to Merge   ;   add the arraylist b111 i.e. the first arraylist in queue
                    if (DBindex222(vector, queue, adj) < temp) {
                        //use it to store the original cluster
                        for (j = 0; j < a1111; j++)
                            cc.add(queue.get(i).get(j));

                        System.out.println(queue.get(0) + " Merge With " + cc + " into " + queue.get(i));
                        pw.println(queue.get(0)+ " Merge With " +cc + " into "+ queue.get(i));
                        z1++;
                        pw2.print(Intra_Cluster(queue.get(0), adj) + ",");
                        pw3.print(MinInter(queue.get(0), cc, vector, queue, adj) + ",");
                        pw8.print(queue.get(0).size() + ",");
                        pw9.print(MinInterCluster(queue.get(0),cc, vector, queue, adj) + ",");
                        //pw3.print(Inter_Cluster(queue.get(0), cc, array1) + ",");
                        pw4.print("1,");
                        pw111.println(temp);
                        pw111.flush();

//                        pw2.println(Intra_Cluster(queue.get(0), array1) + ",");
//                        pw3.println(Inter_Cluster(queue.get(0), cc, array1) + ",");
//                        pw4.println("1,");

//                            System.out.println(queue.get(0)+ " Merge With " +cc + " into "+ queue.get(i)+" ; The inter-similarity between them is: "+ (1- Inter_Cluster(queue.get(0), cc, array1))
//                                    +"  ; The intra-similarity of the new cluster change from "+ Intra_Cluster(cc, array1)+" to "+Intra_Cluster(queue.get(i),array1));
//                            pw.println(queue.get(0)+ " Merge With " +cc + " into "+ queue.get(i)+" ; The inter-similarity between them is: "+ (1- Inter_Cluster(queue.get(0), cc, array1))
//                                    +"  ; The intra-similarity of the new cluster change from "+ (1- Intra_Cluster(cc, array1))+" to "+ (1- Intra_Cluster(queue.get(i),array1)));
                        pw.flush();
                        pw2.flush();
                        pw3.flush();
                        pw4.flush(); pw8.flush(); pw9.flush();
                        cc.clear();
                        //temp = DBindex222(vector, queue, k, array1);
                        queue.add(queue.get(i));
                        queue.remove(i);
                        queue.remove(0);
                        change = true;
                        merge++;
                        break;
                    } else {
                        removeFrom(queue.get(i), a1111);      //maybe there is an existing method to do this
                        if (ne_sample_size < 100) {
                            pw2.print(Intra_Cluster(queue.get(i), adj) + ",");
                            pw3.print(MinInter1(queue.get(0), vector, queue, adj) + ",");
                            //pw3.print(Inter_Cluster(queue.get(i), queue.get(0), array1) + ",");
                            pw4.print("0,");
                            pw8.print(queue.get(0).size() + ",");
                            pw9.print(MinInter1Cluster(queue.get(0), vector, queue, adj) + ",");
//                            pw2.println(Intra_Cluster(queue.get(i), array1) + ",");
//                            pw3.println(Inter_Cluster(queue.get(i), queue.get(0), array1) + ",");
//                            pw4.println("0,");
                            ne_sample_size++;
                            pw2.flush();
                            pw3.flush();
                            pw4.flush();
                        }
                    }
                }
            }
        }

        if (change == false & queue.get(0).size() > 1) {      //try to Split, if b111.size()=1, we do not need to split
            vector.add(new ArrayList<>());
            for (i = 0; i < queue.get(0).size(); i++)
                vector.get(vector.size() - 1).add(queue.get(0).get(i));
            //vector.add(queue.get(0));       //note that if we directly add queue.get(0), we actually are using queue.get(0), it is dynamic
            vector.add(new ArrayList<>());
            for (p = queue.get(0).size() - 1; p >= 0; p--) {
                loop++;
                vector.get(vector.size() - 1).add(vector.get(vector.size() - 2).get(0));    //add b111.get(p) to next row
                vector.get(vector.size() - 2).remove(0);   //delete b111.get(p) from the last row
                //System.out.println("Db Split "+DBindex111(vector, queue, array1));
                //System.out.println("temp "+temp);
                if (DBindex222(vector, queue, adj) < temp) {
                    System.out.println(queue.get(0) + " Split into " + vector.get(vector.size() - 1) + " and " + vector.get(vector.size() - 2));
                    pw1.println(queue.get(0)+ " Split into "+ vector.get(vector.size() - 1)+ " and " + vector.get(vector.size() - 2));
                    z2++;
                    //pw1.println(z2 + " "+Intra_Cluster(queue.get(0), array1));
//                        System.out.println(queue.get(0)+ " Split into "+ vector.get(vector.size() - 1)+ " and " + vector.get(vector.size() - 2)+" ; " +
//                                "The inter-similarity between them is: "+ (1- Inter_Cluster(vector.get(vector.size() - 1), vector.get(vector.size() - 2), array1))
//                                +"  ; The intra-similarity of the new cluster change from "+
//                                (1- Intra_Cluster(queue.get(0), array1))+" to "+(1-Intra_Cluster(vector.get(vector.size() - 1),array1))+
//                                " and "+(1-Intra_Cluster(vector.get(vector.size() - 1),array1)));
//                        pw.println(queue.get(0)+ " Split into "+ vector.get(vector.size() - 1)+ " and " + vector.get(vector.size() - 2));
                    pw1.flush();
                    temp = DBindex222(vector, queue, adj);
                    change = true;
                    split++;

                    pw111.println(temp);
                    pw111.flush();
                } else {
                    vector.get(vector.size() - 2).add(vector.get(vector.size() - 1).get(vector.get(vector.size() - 1).size() - 1));  // else put the value into its original position.
                    vector.get(vector.size() - 1).remove(vector.get(vector.size() - 1).size() - 1);
                }
            }

        if (change == true) {

            queue.add(vector.get(vector.size() - 1));
            queue.add(vector.get(vector.size() - 2));
            vector.remove(vector.size() - 1);
            vector.remove(vector.size() - 1);

            pw5.print(Intra_Cluster(queue.get(0), adj) + ",");
            pw6.print(MinInter2(queue.get(0), vector, queue, adj) + ",");
            pw7.print("1,");
            pw10.print(queue.get(0).size()+ ",");
            pw5.flush();
            pw6.flush();
            pw7.flush();
            pw10.flush();

            queue.remove(0);

        } else {
            vector.remove(vector.size() - 1);
            vector.remove(vector.size() - 1);
            if (ne_sample_size1 < 8) {
                pw5.print(Intra_Cluster(queue.get(0), adj) + ",");
                pw6.print(MinInter1(queue.get(0), vector, queue, adj) + ",");
                pw7.print("0,");
                pw10.print(queue.get(0).size()+ ",");
                ne_sample_size1++;
                pw5.flush();
                pw6.flush();
                pw7.flush();
                pw10.flush();
            }
        }
        }

 //        move operation
//        if (change==false & queue.get(0).size() > 1 ) {
//            int xyz = queue.get(0).get(0);
//            labelA:
//            for (int p1 = 0; p1 < queue.get(0).size(); p1++) {
//                for (p = 0; p < vector.size() - 1; p++) {  //need not remove to itself
//                    if (IsConnectedValue(queue.get(0).get(0), vector.get(p), k, array1) == false)    //we do need to judge IsConnected every time.
//                    {
//                        continue;
//                    }else {
//                        loop++;
//                        vector.get(p).add(xyz);
//                        queue.get(0).remove(0);
//                        if (DBindex111(vector, queue, array1) < temp) {
//                            System.out.println(" Move from " + queue.get(0) + " to " + vector.get(p));
//                            //temp = DBindex111(vector, queue, array1);
//                            change = true;
//                            move++;
//                            queue.add(queue.get(0));
//                            queue.add(vector.get(p));
//                            vector.remove(p);
//                            queue.remove(0);
//                            break labelA;
//                        } else {
//                            vector.get(p).remove(vector.get(p).get(vector.get(p).size() - 1));
//                        }
//                    }
//                }
//            }
//
//        }

        if (change == false){
        //if (change == false & q3==q11)
            //System.out.println("No change any more");
            vector.add(queue.get(0));
            queue.remove(0);
        }

    }
    //System.out.println("Merge= "+ merge);
    //System.out.println("Split= "+ split);
    try {
        xw.flush();
        xw1.flush();
        xw2.flush();xw3.flush();xw4.flush();
        xw5.flush();xw6.flush();xw7.flush(); pw8.flush(); pw9.flush(); pw10.flush();
        pw.close();
        pw1.close();
        pw2.close();pw3.close();pw4.close();
        pw5.close();pw6.close();pw7.close(); pw8.close(); pw9.close(); pw10.close();
        xw.close();
        xw1.close();
        xw2.close();xw3.close();xw4.close();
        xw5.close();xw6.close();xw7.close(); xw8.close(); xw9.close(); xw10.close();

        pw111.flush(); pw111.close(); xw111.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    return new ClusterData(vector, merge, split, move, loop);
    }

    public static ClusterData DBGreedy(ArrayList <ArrayList<Integer>> array, int k, ArrayList<Hashtable> adj) throws IOException {
        int i = 0;
        int j = 0;
        int s = 0;
        int merge = 0;
        int split = 0;
        int move = 0;
        int loop = 0;
        // write println into ChangeHistory file
        FileWriter xw = null;
        try {
            File file = new File("/Users/binbingu/Documents/Codes/Write-test/Change-Batch.txt");
            xw = new FileWriter(file, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(xw);
        int q1 = 0; //????????????temp?????????????????????
        //vector????????????????????????????????????????????????inter?????????????????????
        int fix = array.get(k).size();
        //for (k = 0; k < array.size(); k++) {
        for (i = 0; i < fix; i++) {
            double temp = DBindex(array, adj);
            double temp111 = temp;
            if (array.get(k).size()>1) {
                int temp1 = array.get(k).get(0);
                array.get(k).remove(0);
                //???????????????????????????????????????????????????attribute??????????????????
                //array.add(new Vector <String>());
                array.add(new ArrayList<Integer>());     //create a new empty cluster, this is like split a cluster into two cluters

                for (j = 0; j < array.size(); j++) {
                    ArrayList <Integer> cc = new ArrayList<Integer>();
                    int a111 = array.get(j).size();
                    if (j == k)
                        continue;
                    array.get(j).add(temp1); //??????????????????????????????????????????????????????DBindex???
                    if (DBindex(array, adj) < temp) {
                        move++;
                        for (s=0; s< a111; s++)
                            cc.add(array.get(j).get(s));
                        System.out.println("Move " + temp1 + " to " + cc);
                        pw.println("Move " + temp1 + " to " + cc);
                        pw.flush();
                        cc.clear();
                        temp = DBindex(array, adj);
                        q1 = j;    //?????????????????????????????????
                    }
                    array.get(j).remove(array.get(j).size() - 1);
                }
                if (temp == temp111) {
                    q1 = k;
                }
                if (q1 != array.size() - 1) {
                    array.remove(array.size() - 1);
                }
                array.get(q1).add(temp1); //?????????????????????
            }
        }
        /*
        //????????????????????????
        for (i = array.size()-1; i >=0; i--) {
            if (array.get(i).isEmpty())
            {
                array.remove(i);
            }
        }
         */
        try {
            xw.flush();
            pw.close();
            xw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ClusterData(array, merge, split, move, loop);
        /*
        int i = 0;
        int j = 0;
        int s = 0;
        int merge = 0;
        int split = 0;
        int move = 0;
        int loop = 0;
        // write println into ChangeHistory file
        File file = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/Batch/Histroy1000.txt");
        File file1 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/Batch/Move1000.txt");

        FileWriter xw = null;
        FileWriter xw1 = null;
        xw = new FileWriter(file, true);
        xw1 = new FileWriter(file1, true);

        PrintWriter pw = new PrintWriter(xw);
        PrintWriter pw1 = new PrintWriter(xw1);

        int q1 = 0; //record the position of the maximal temp
        int z1 = 0;
        //for (k = 0; k < array.size(); k++) {
        if (array.get(k).size()>1) {
            for (i = array.get(k).size() - 1; i >= 0; i--) {
                boolean change = false;
                double temp = DBindex(array, array1);
                int temp1 = array.get(k).get(i);
                array.get(k).remove(i);

                for (j = 0; j < array.size(); j++) {
                    if (j == k) continue;
                    if (IsConnectedValue(temp1, array.get(j), k, array1) == false) {    //we do need to judge IsConnected every time.
                        continue;
                    } else {
                        array.get(j).add(temp1);
                        if (DBindex(array, array1) < temp) {
                            move++;
//                                for (s = 0; s < a111; s++)
//                                cc.add(array.get(j).get(s));
//                            System.out.println("Move " + temp1 + " to " + cc);
//                            pw.println("Move " + temp1 + " to " + cc);
//                            z1++;
//                            pw1.println(z1 + " " + Intra_ClusterBatch(temp1, cc, array1));
//                            pw.flush();
//                            cc.clear();
                            change = true;
                            temp = DBindex(array, array1);
                            array.get(j).remove(array.get(j).size()-1);
                            q1 = j;    //record the cluster which has the maximal value
                        }
                        else{
                            array.get(j).remove(array.get(j).size()-1);
                        }
                    }
                    if ( (j == array.size() - 1) && array.get(k).size()!=1) {  //create a new empty cluster
                        array.add(new ArrayList<Integer>());
                        array.get(array.size() - 1).add(temp1);
                        if (DBindex(array, array1) < temp) {
                            temp = DBindex(array, array1);
                            q1 = array.size();
                            change = true;
                        }
                        else{
                            array.remove(array.size()-1);
                        }
                    }

                }
                if (change == false) {
                    array.get(k).add(temp1);
                }
                if (change == true) {
                    array.get(q1).add(temp1);
                }

            }
        }

       // release empty arraylist
//        for (i = array.size()-1; i >=0; i--) {
//            if (array.get(i).isEmpty())
//            {
//                array.remove(i);
//            }
//        }

        try {
            xw.flush();
            pw.close();
            xw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
         return new ClusterData(array, merge, split, move, loop);
        */
    }


    public static double DBindex(ArrayList <ArrayList<Integer>> array1, ArrayList<Hashtable> adj) {
        double array2[] = new double[array1.size()]; //store maximal separation measure for each cluster
        Arrays.fill(array2, 0); //initialization
        int i = 0;
        int j = 0;
        //skip empty set
        for (i = 0; i < array1.size(); i++) {
            if (array1.get(i).isEmpty())
                continue;
            else {
                double temp = 0;
                for (j = 0; j < array1.size(); j++) {
                    if (j == i) continue;
                    else {
                        if (array1.get(j).isEmpty())
                            continue;
                        else {
                            double a= Intra_Cluster(array1.get(i), adj);
                            double b= Intra_Cluster(array1.get(j), adj);
                            double c= Inter_Cluster(array1.get(i), array1.get(j), adj);
//                            if (temp < Fraction( a+b + 0.2, c + 0.1))
//                            {
//                                temp = Fraction(a+b + 0.2, c+ 0.1);
//                            }
                            if (temp < Fraction( a+b + 0.01, c + 0.001))
                            {
                                temp = Fraction(a+b + 0.01, c+ 0.001);
                            }
                        }
                    }
                }
                array2[i] = temp;
            }
        }
        double sum = 0;
        for (i = 0; i < array2.length; i++) {
            sum = sum + array2[i];
        }
        return sum;
    }

    public static double DBindex111(ArrayList <ArrayList<Integer>> array1, ArrayList<ArrayList <Integer>> queue, ArrayList<Hashtable> adj) {
        double array2[] = new double[array1.size()+queue.size()]; //??????n???cluster???separation measure?????????
        Arrays.fill(array2, 0); //????????????????????????0
        int i = 0;
        int j = 0;
        ArrayList <ArrayList<Integer>> vectorqueue = new ArrayList <ArrayList<Integer>>();
        for (i = 0; i < queue.size(); i++) {      //move queue to array1 we start from 1 because we do not want to remove 0 as it will change its original
            array1.add(queue.get(i));                 //position due to the property of arraylist
        }
        //??????????????????continue??????
        for (i = 0; i < array1.size(); i++) {
            if (array1.get(i).isEmpty())
                continue;
            else {
                double temp = 0;
                for (j = 0; j < array1.size(); j++) {
                    if (j == i) continue;
                    else {
                        if (array1.get(j).isEmpty())
                            continue;
                        else {
                            double a= Intra_Cluster(array1.get(i), adj);
                            double b= Intra_Cluster(array1.get(j), adj);
                            double c= Inter_Cluster(array1.get(i), array1.get(j), adj);
                            if (temp < Fraction( a+b + 0.01, c + 0.001))
                            {
                                temp = Fraction(a+b + 0.01, c+ 0.001);
                            }
                        }
                    }
                }
                array2[i] = temp;
            }
        }

        double sum = 0;
        for (i = 0; i < array2.length; i++) {
            sum = sum + array2[i];
        }
        removeFrom(array1, array1.size()-queue.size());  //because DBindex111 will change the array1 even if we do not return array1

        return sum;
    }

    public static double DBindex222(ArrayList <ArrayList<Integer>> array1, ArrayList<ArrayList <Integer>> queue, ArrayList<Hashtable> adj) {
        double array2[] = new double[array1.size()+queue.size()]; //??????n???cluster???separation measure?????????
        Arrays.fill(array2, 0); //????????????????????????0
        int i = 0;
        int j = 0;
        ArrayList <ArrayList<Integer>> vectorqueue = new ArrayList <ArrayList<Integer>>();
        for (i = 1; i < queue.size(); i++) {      //move queue to array1 we start from 1 because we do not want to remove 0 as it will change its original
            array1.add(queue.get(i));                 //position due to the property of arraylist
        }
        for (i = 0; i < array1.size(); i++) {
            if (array1.get(i).isEmpty())
                continue;
            else {
                double temp = 0;
                for (j = 0; j < array1.size(); j++) {
                    if (j == i) continue;
                    else {
                        if (array1.get(j).isEmpty())
                            continue;
                        else {
                            double a = Intra_Cluster(array1.get(i), adj);
                            double b = Intra_Cluster(array1.get(j), adj);
                            double c = Inter_Cluster(array1.get(i), array1.get(j), adj);
                        // if (temp < Fraction( a+b + 0.01, c + 0.001))
                            if (temp < Fraction(a + b + 0.01, c + 0.001))  //find the minimal DB-index
                            {
                                // if (temp < Fraction( a+b + 0.01, c + 0.001))
                                temp = Fraction(a + b + 0.01, c + 0.001);
                            }

                        }
                    }
                }
                array2[i] = temp;
            }
        }

        double sum = 0;
        for (i = 0; i < array2.length; i++) {
            sum = sum + array2[i];
        }
        removeFrom(array1, array1.size()-queue.size()+1);  //because DBindex111 will change the array1 even if we do not return array1

        return sum;
    }


    public static double Intra_Cluster(ArrayList <Integer> array1, ArrayList<Hashtable> adj){
        double avg=0;
        double sum=0;
        int i=0;
        int j=0;
        if (array1.size()==1){
            avg=0;
        }
        else {
            for (i = 0; i < array1.size()-1; i++) {
                for (j = i + 1; j < array1.size(); j++) {
                    if(adj.get(array1.get(i)).get(array1.get(j))==null){
                        continue;
                    }
                    else
                        sum = sum + (double)adj.get(array1.get(i)).get(array1.get(j));
                }
            }
            avg = 1 - sum / (array1.size() * (array1.size() - 1) / 2);
        }
        return avg;
    }

    //this function is used to compute intra if new records are inserted

    public static double Inter_Cluster(ArrayList <Integer> array1, ArrayList <Integer> array2, ArrayList<Hashtable> adj) {
        double avg=0;
        double sum=0;
        int i=0;
        int j=0;
        for (i=0; i<array1.size(); i++) {
            for (j=0; j< array2.size(); j++) {
                if(adj.get(array1.get(i)).get(array2.get(j))==null){
                    continue;
                }
                else
                    sum = sum + (double)adj.get(array1.get(i)).get(array2.get(j));
            }
        }
        avg=1- sum/(array1.size()*array2.size());
        return avg;
    }
    // array2 is a 1d array


    public static double Fraction(double a, double b){
        double c= 0;
        c = a/b;
        return c;

    }


    public static ArrayList<Integer> NeighborNode( ArrayList<Integer> array1, ArrayList<Hashtable> adj, int l) {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i=0; i< array1.size(); i++) {
            Hashtable tt = adj.get(i);
            Enumeration enkey = tt.keys();
            while (enkey.hasMoreElements()) {
                Object aa = enkey.nextElement();
                if((int) aa<l)  //this is because we use the adjacent matrix of all records, when we test a small fraction of the whole dataset, we still use the whole adjacent matrix
                    temp.add((int) aa);
            }
        }
        return temp;
    }

    public static boolean IsConnected( ArrayList<Integer> array1,  ArrayList<Integer> array2, double k, ArrayList<Hashtable> adj) {
        int i=0;
        int j=0;
        boolean a= false;
        labelA:
        for (i=0; i< array1.size(); i++){
            for (j=0; j< array2.size(); j++){
                if(adj.get(array1.get(i)).get(array2.get(j))==null){
                    continue;
                }
                else if ((double)adj.get(array1.get(i)).get(array2.get(j))>=k )
                {
                    a=true;
                    break labelA;  //??????????????????
                }
            }
    }
        return a;
    }

    public static boolean IsConnected1( ArrayList<Integer> array1,  ArrayList<Integer> array2, double k, ArrayList<Hashtable> adj) {
        int i=0;
        int j=0;
        boolean a= false;
        labelA:
        for (i=0; i< array1.size(); i++) {
            for (j = 0; j < array2.size(); j++) {
                if (array1.get(i) == array2.get(j)) {
                    a = true;
                    break labelA;  //??????????????????
                }
            }
        }
        return a;
    }

    public static void removeFrom(ArrayList list, int pos) {
        List sublist = list.subList(pos, list.size());
        list.removeAll(sublist);
    }

    public static ArrayList<ArrayList<Integer>> OneByOne(ArrayList<ArrayList<Integer>> vector1, ArrayList<Integer> array1, double k,  ArrayList<Hashtable> adj) {
        int m= vector1.size();
        int n = array1.size();
        int q2=0;
        int q3=0;
        int q4=0;
        int merge = 0;
        int split = 0;
        int move = 0;
        // write println into ChangeHistory file
        FileWriter xw = null;
        try {
            //File file = new File("/Users/binbingu/Documents/Codes/Write-test/Change-Batch.txt");
            File file = new File("/Users/binbingu/Documents/Codes/Write-test/Music/Change-Batch.txt");

            xw = new FileWriter(file, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        PrintWriter pw = new PrintWriter(xw);
        ArrayList<Integer> label= new ArrayList<Integer>();
        label=new ArrayList<Integer>(array1);  //???array1??????????????????label???
        double matrix[] [] = new double [m][n];
        for (int i=0; i< m; i++) {      //?????????????????????
            for (int j=0; j< n; j++) {
                double sum= 0;
                for (int p = 0; p < vector1.get(i).size(); p++) {
                    //sum = sum + SimFunction.Jaccardsim(vector1.get(i).get(p), array1.get(j));
                    if(adj.get(vector1.get(i).get(p)).get(array1.get(j))==null){
                        continue;
                    }
                    else
                    sum = sum + (double) adj.get(vector1.get(i).get(p)).get(array1.get(j));

                }
                matrix[i][j] = sum/vector1.get(i).size();
                //System.out.println(+i+"|"+j+"|"+matrix[i][j]);
            }
        }

        for (int i=0; i< m; i++) {
            double q1 = 0;   //???????????????????????????m,??????????????????
            for (int j =0; j<n; j++) {
                if (matrix[i][j] > q1) {
                    q1 = matrix[i][j];
                    q2 = j;              //????????????????????????
                }
            }
            if (q1 > k) {
                vector1.get(i).add(array1.get(q2));
                pw.println("Move "+ array1.get(q2)+ " to "+ vector1.get(i));
                pw.flush();
                //System.out.println("Jaccardsim" + "(" + vector1.get(i)  + ") = " + q1);
                label.set(q2, null);
                //array1[q2]= String.valueOf(0);       //???????????????????????????
            }

        }
        //array1?????????????????????????????????vector1???
        for (int j = 0; j < label.size(); j++) {
            if (label.get(j)!=null) {
                vector1.add(new ArrayList<Integer>());  //????????????vector
                vector1.get(m + q3).add(label.get(j));
                //System.out.println("j = " + j + ". Single attribute" + vector1.get(m+q3) );
                q3++;
            }

        }

        try {
            xw.flush();
            pw.close();
            xw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return vector1;
    }

//    public static ArrayList<ArrayList<Integer>> OneByOne(ArrayList<ArrayList<Integer>> vector1, ArrayList<Integer> array1, double k,  double array12[][]) {
//        int m= vector1.size();
//        int n = array1.size();
//        int i=0;
//        int j=0;
//        int p=0;
//        int q2=0;
//        int q3=0;
//        int q4=0;
//        int merge = 0;
//        int split = 0;
//        int move = 0;
//        // write println into ChangeHistory file
//        FileWriter xw = null;
//        try {
//            File file = new File("/Users/binbingu/Documents/Codes/Write-test/Change-Batch.txt");
//            xw = new FileWriter(file, true);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        PrintWriter pw = new PrintWriter(xw);
//        ArrayList<Integer> label= new ArrayList<Integer>();
//        label=new ArrayList<Integer>(array1);  //???array1??????????????????label???
//        double matrix[] [] = new double [m][n];
//        for (i=0; i< m; i++) {      //?????????????????????
//            for (j=0; j< n; j++) {
//                double sum= 0;
//                for (p = 0; p < vector1.get(i).size(); p++) {
//                    //sum = sum + SimFunction.Jaccardsim(vector1.get(i).get(p), array1.get(j));
//                    sum = sum + array12[vector1.get(i).get(p)][array1.get(j)];
//
//                }
//                matrix[i][j] = sum/vector1.get(i).size();
//                //System.out.println(+i+"|"+j+"|"+matrix[i][j]);
//            }
//        }
//
//        for (i=0; i< m; i++) {
//            double q1 = 0;   //???????????????????????????m,??????????????????
//            for (j =0; j<n; j++) {
//                if (matrix[i][j] > q1) {
//                    q1 = matrix[i][j];
//                    q2 = j;              //????????????????????????
//                }
//            }
//            if (q1 > k) {
//                vector1.get(i).add(array1.get(q2));
//                pw.println("Move "+ array1.get(q2)+ " to "+ vector1.get(i));
//                pw.flush();
//                //System.out.println("Jaccardsim" + "(" + vector1.get(i)  + ") = " + q1);
//                label.set(q2, null);
//                //array1[q2]= String.valueOf(0);       //???????????????????????????
//            }
//
//        }
//        //array1?????????????????????????????????vector1???
//        for (j = 0; j < label.size(); j++) {
//            if (label.get(j)!=null) {
//                vector1.add(new ArrayList<Integer>());  //????????????vector
//                vector1.get(m + q3).add(label.get(j));
//                //System.out.println("j = " + j + ". Single attribute" + vector1.get(m+q3) );
//                q3++;
//            }
//
//        }
//
///*
//        for (int l = 0; l < vector1.size(); l++) {
//            System.out.println(vector1.get(l));
//        }
//
// */
//        try {
//            xw.flush();
//            pw.close();
//            xw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return vector1;
//    }

    //Correlation Clustering
    public static double ScoreForCorr(ArrayList <ArrayList<Integer>> array1, ArrayList<Hashtable> adj) {
        int i = 0;
        int j = 0;
        double sum = 0;
        double intra_sum = 0;
        double inter_sum = 0;
        //??????????????????continue??????
        for (i = 0; i < array1.size(); i++) {
            if (array1.get(i).isEmpty())
                continue;
            else {
                intra_sum = intra_sum + IntraForCorr(array1.get(i), adj);
                for (j = 0; j < array1.size(); j++) {
                    if (j == i) continue;
                    else {
                        if (array1.get(j).isEmpty())
                            continue;
                        else {
                            inter_sum = inter_sum + InterForCorr(array1.get(i), array1.get(j), adj);
                        }
                    }
                }
            }
        }
        System.out.println("Intra_sum "+ intra_sum);
        System.out.println("Inter_sum "+inter_sum);
        sum = intra_sum + (inter_sum/2);
        return sum;
    }

    public static double IntraForCorr(ArrayList <Integer> array1, ArrayList<Hashtable> adj){
        double avg=0;
        double sum=0;
        int i=0;
        int j=0;
        if (array1.size()==1){
            avg=0;
        }
        else {
            for (i = 0; i < array1.size()-1; i++) {

                for (j = i + 1; j < array1.size(); j++) {
                    if(adj.get(array1.get(i)).get(array1.get(j))==null){
                        continue;
                    }
                    else
                        sum = sum + (double) adj.get(array1.get(i)).get(array1.get(j));
                }

            }
            avg = (array1.size() * (array1.size() - 1) / 2) - sum;
        }
        return avg;
    }

    public static double IntraCorrAvg(ArrayList <Integer> array1, ArrayList<Hashtable> adj){
        double avg=0;
        double sum=0;
        int i=0;
        int j=0;
        if (array1.size()==1){
            avg=0;
        }
        else {
            for (i = 0; i < array1.size()-1; i++) {

                for (j = i + 1; j < array1.size(); j++) {
                    if(adj.get(array1.get(i)).get(array1.get(j))==null){
                        continue;
                    }
                    else
                        sum = sum + (double) adj.get(array1.get(i)).get(array1.get(j));
                }

            }
            avg = 1-( sum/ (array1.size() * (array1.size() - 1) / 2));
        }
        return avg;
    }

    public static double InterForCorr(ArrayList <Integer> array1, ArrayList <Integer> array2, ArrayList<Hashtable> adj) {
        double sum=0;
        int i=0;
        int j=0;
        for (i=0; i<array1.size(); i++) {
            for (j=0; j< array2.size(); j++) {
                if(adj.get(array1.get(i)).get(array2.get(j))==null){
                    continue;
                }
                else
                    sum = sum + (double) adj.get(array1.get(i)).get(array2.get(j));
            }
        }
        return sum;
    }

    public static double ScoreForCorr1(ArrayList <ArrayList<Integer>> array1, ArrayList<ArrayList <Integer>> queue, ArrayList<Hashtable> adj) {
        int i = 0;
        int j = 0;
        double sum = 0;
        double intra_sum = 0;
        double inter_sum = 0;
        ArrayList <ArrayList<Integer>> vectorqueue = new ArrayList <ArrayList<Integer>>();
        for (i = 0; i < queue.size(); i++) {      //move queue to array1 we start from 1 because we do not want to remove 0 as it will change its original
            array1.add(queue.get(i));                 //position due to the property of arraylist
        }
        //??????????????????continue??????
        for (i = 0; i < array1.size(); i++) {
            if (array1.get(i).isEmpty())
                continue;
            else {
                double temp = 0;
                intra_sum = intra_sum + IntraForCorr(array1.get(i), adj);
                for (j = 0; j < array1.size(); j++) {
                    if (j == i) continue;
                    else {
                        if (array1.get(j).isEmpty())
                            continue;
                        else {
                            inter_sum = inter_sum + InterForCorr(array1.get(i), array1.get(j), adj);
                        }
                    }
                }
            }
        }
        sum = intra_sum + inter_sum/2;
        removeFrom(array1, array1.size()-queue.size());  //because DBindex111 will change the array1 even if we do not return array1
        return sum;
    }

    public static double ScoreForCorr2(ArrayList <ArrayList<Integer>> array1, ArrayList<ArrayList <Integer>> queue, ArrayList<Hashtable> adj) {
        int i = 0;
        int j = 0;
        double sum = 0;
        double intra_sum = 0;
        double inter_sum = 0;
        ArrayList <ArrayList<Integer>> vectorqueue = new ArrayList <ArrayList<Integer>>();
        for (i = 1; i < queue.size(); i++) {      //move queue to array1 we start from 1 because we do not want to remove 0 as it will change its original
            array1.add(queue.get(i));                 //position due to the property of arraylist
        }
        //??????????????????continue??????
        for (i = 0; i < array1.size(); i++) {
            if (array1.get(i).isEmpty())
                continue;
            else {
                intra_sum = intra_sum + IntraForCorr(array1.get(i), adj);
                for (j = 0; j < array1.size(); j++) {
                    if (j == i) continue;
                    else {
                        if (array1.get(j).isEmpty())
                            continue;
                        else {
                            inter_sum = inter_sum + InterForCorr(array1.get(i), array1.get(j), adj);
                        }
                    }
                }
            }
        }

        sum = intra_sum + (inter_sum/2);
        removeFrom(array1, array1.size()-queue.size()+1);  //because DBindex111 will change the array1 even if we do not return array1
        return sum;
    }

    public static ClusterData IncreForCorr(ArrayList<ArrayList<Integer>> vector, ArrayList<Integer> array, double k, ArrayList<Hashtable> adj) throws IOException {
        int i = 0;
        int j = 0;
        int p = 0;
        int merge = 0;
        int split = 0;
        int move = 0;
        int loop = 0;
        // write println into ChangeHistory file

        //File file = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/Change-IncreForCorr.txt");
        //File file = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/Corr/Corr-Merge2000.txt");
        //File file1 = new File("/Users/binbingu/Documents/Codes/Write-test/Synthetic/Corr/Corr-Split2000.txt");
        File file = new File("/Users/binbingu/Documents/Codes/Write-test/Music/Corr/Corr-Merge2000.txt");
        File file1 = new File("/Users/binbingu/Documents/Codes/Write-test/Music/Corr/Corr-Split2000.txt");

//        if (file.exists()) {
//            file.delete();
//        }
//        if (file1.exists()) {
//            file.delete();
//        }
//        file.createNewFile();
//        file1.createNewFile();

        FileWriter xw = null;
        FileWriter xw1 = null;
        xw = new FileWriter(file, true);
        xw1 = new FileWriter(file1, true);

        PrintWriter pw = new PrintWriter(xw);
        PrintWriter pw1 = new PrintWriter(xw1);

        ArrayList<ArrayList<Integer>> queue = new ArrayList<ArrayList<Integer>>();  //??????????????????schema?????????
        for (i = 0; i < array.size(); i++) {
            queue.add(new ArrayList<Integer>());
            queue.get(i).add(array.get(i));
        }
        //pw.println("The smaller value, the larger similarity as we use the complement of similarity here.");

        int z1 = 0; //used to define the sequence of merge change
        int z2 = 0; //used to define the sequence of merge change
        while (queue.size() > 0) {
            double temp = ScoreForCorr1(vector, queue, adj);
            //System.out.println("3 "+temp);
            boolean change = false;  //????????????cluster????????????
            ArrayList<Integer> b123 = NeighborNode(queue.get(0), adj, 4000);
            if(!b123.isEmpty()) {
                for (i = vector.size() - 1; i >= 0; i--) {          //compare queue.get(0) with vector
                    if (IsConnected1(b123, vector.get(i), k, adj) == false) {    //we do need to judge IsConnected every time.
                        continue;
                    } else {
                        loop++;
                        ArrayList<Integer> bb = new ArrayList<Integer>();  //use it to store the original cluster
                        int a111 = vector.get(i).size();
                        vector.get(i).addAll(queue.get(0));         //try to Merge   ;   add the arraylist b111 i.e. the first arraylist in queue
                        //System.out.println(vector);
                        System.out.println(queue);
//                    System.out.println("4 "+ScoreForCorr2(vector, queue, array1));
//                    System.out.println("5 "+temp);
//                    System.out.println("6 "+array1[0][2]);
                        if (ScoreForCorr2(vector, queue, adj) < temp) {
                            for (j = 0; j < a111; j++)
                                bb.add(vector.get(i).get(j));

                            System.out.println(queue.get(0) + " Merge With " + bb + " into " + vector.get(i));
                            //pw.println(queue.get(0)+ " Merge With " +bb + " into "+ vector.get(i));
                            z1++;
                            pw.println(z1 + " " + IntraCorrAvg(vector.get(i), adj));
                            //pw.println("Merge Parameters: (c0, c1,c2) "+ IntraCorrAvg(vector.get(i), array1)+" "+ IntraCorrAvg(queue.get(0), array1)+" "+ IntraCorrAvg(bb, array1));
                            pw.flush();
                            bb.clear();
                            queue.add(vector.get(i));
                            queue.remove(0);
                            vector.remove(i);
                            change = true;
                            merge++;
                            break;
                        } else {
                            removeFrom(vector.get(i), a111);      //maybe there is an existing method to do this
                        }
                    }
                }

                if (change == false) {
                    for (i = queue.size() - 1; i > 0; i--) {          //compare queue.get(0) with queue except itself
                        if (IsConnected1(b123, queue.get(i), k, adj) == false)    //we do need to judge IsConnected every time.
                        {
                            continue;
                        } else {
                            loop++;
                            int a11 = vector.size();
                            ArrayList<Integer> cc = new ArrayList<Integer>();
                            int a1111 = queue.get(i).size();     //???????????????????????????remove
                            queue.get(i).addAll(queue.get(0));         //try to Merge   ;   add the arraylist b111 i.e. the first arraylist in queue
                            if (ScoreForCorr2(vector, queue, adj) < temp) {
                                //use it to store the original cluster
                                for (j = 0; j < a1111; j++)
                                    cc.add(queue.get(i).get(j));

                                System.out.println(queue.get(0) + " Merge With " + cc + " into " + queue.get(i));
                                //pw.println(queue.get(0)+ " Merge With " +cc + " into "+ queue.get(i));
                                z1++;
                                pw.println(z1 + " " + IntraCorrAvg(queue.get(0), adj));
                                //pw.println("Merge Parameters: (c0, c1,c2) "+ IntraCorrAvg(queue.get(i), array1)+" "+ IntraCorrAvg(queue.get(0), array1)+" "+ IntraCorrAvg(cc, array1));
                                pw.flush();
                                cc.clear();
                                queue.add(queue.get(i));
                                queue.remove(i);
                                queue.remove(0);
                                change = true;
                                merge++;
                                break;
                            } else {
                                removeFrom(queue.get(i), a1111);      //maybe there is an existing method to do this
                            }
                        }
                    }
                }
            }

                if (change == false & queue.get(0).size() > 1) {      //try to Split, if b111.size()=1, we do not need to split
                    vector.add(new ArrayList<>());
                    for (i = 0; i < queue.get(0).size(); i++)
                        vector.get(vector.size() - 1).add(queue.get(0).get(i));
                    //vector.add(queue.get(0));       //note that if we directly add queue.get(0), we actually are using queue.get(0), it is dynamic
                    vector.add(new ArrayList<>());
                    for (p = queue.get(0).size() - 1; p >= 0; p--) {
                        loop++;
                        vector.get(vector.size() - 1).add(vector.get(vector.size() - 2).get(0));    //add b111.get(p) to next row
                        vector.get(vector.size() - 2).remove(0);   //delete b111.get(p) from the last row
                        //System.out.println("Db Split "+DBindex111(vector, queue, array1));
                        //System.out.println("temp "+temp);
                        if (ScoreForCorr2(vector, queue, adj) < temp) {
                            System.out.println(queue.get(0) + " Split into " + vector.get(vector.size() - 1) + " and " + vector.get(vector.size() - 2));
                            //pw1.println(queue.get(0)+ " Split into "+ vector.get(vector.size() - 1)+ " and " + vector.get(vector.size() - 2));
                            z2++;
                            pw1.println(z2 + " " + IntraCorrAvg(queue.get(0), adj));
                            //pw.println("Split Parameters: (c0, c1,c2) "+ IntraCorrAvg(queue.get(0), array1)+" "+ IntraCorrAvg(vector.get(vector.size() - 1), array1)+" "+ IntraCorrAvg(vector.get(vector.size() - 2), array1));
                            pw1.flush();
                            temp = ScoreForCorr2(vector, queue, adj);
                            change = true;
                            split++;
                        } else {
                            vector.get(vector.size() - 2).add(vector.get(vector.size() - 1).get(vector.get(vector.size() - 1).size() - 1));  // else put the value into its original position.
                            vector.get(vector.size() - 1).remove(vector.get(vector.size() - 1).size() - 1);
                        }
                    }
                    if (change == true) {
                        queue.add(vector.get(vector.size() - 1));
                        queue.add(vector.get(vector.size() - 2));
                        vector.remove(vector.size() - 1);
                        vector.remove(vector.size() - 1);
                        queue.remove(0);

                    } else {
                        vector.remove(vector.size() - 1);
                        vector.remove(vector.size() - 1);
                    }
                }

/*
            if (change==false ) {    //try to move
                {
                }
            }
 */
            if (change == false){
                vector.add(queue.get(0));
                queue.remove(0);
            }

        }
        try {
            xw.flush();
            xw1.flush();
            pw.close();
            pw1.close();
            xw.close();
            xw1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ClusterData(vector, merge, split, move, loop);
    }

    public static double MinInter(ArrayList <Integer> array1, ArrayList <Integer> array2,  ArrayList<ArrayList<Integer>> vector, ArrayList<ArrayList<Integer>> queue, ArrayList<Hashtable> adj) {
        //double min=0;
        double inter0=1;
        int i=0;
        int j=0;
        if (Inter_Cluster(array1, array2, adj)<inter0){
            inter0 = Inter_Cluster(array1, array2, adj);
        }
        for (i=0; i<vector.size(); i++) {
            if(Inter_Cluster(array1, vector.get(i), adj)<inter0){
                inter0 = Inter_Cluster(array1, vector.get(i), adj);
            }
        }
        for (j = 1; j < queue.size(); j++) {
            if (Inter_Cluster(array1, queue.get(j), adj) < inter0) {
                inter0 = Inter_Cluster(array1, queue.get(j), adj);
            }
        }

        return inter0;
    }

    public static Para MinInterForMerge(ArrayList <Integer> array1, ArrayList<ArrayList<Integer>> queue, ArrayList<Hashtable> adj) {
        double inter0=1;
        int k=0;

        for (int j = 1; j < queue.size(); j++) {
            if (Inter_Cluster(array1, queue.get(j), adj) <= inter0) {        //because the similarity is set as 0 when it is smaller than a threshold, so we need to use <=
            //if (Inter_Cluster(array1, queue.get(j), array) < 0.2) {
                inter0 = Inter_Cluster(array1, queue.get(j), adj);
                k = j;
            }
        }
        return new Para(inter0, k);
    }

    public static Para MinInterForSplit(ArrayList <Integer> array0, ArrayList<Hashtable> adj) { //find the most unrelevant record
        double inter0=1000000;
        int k=0;
        double sum=0;

        for (int i = 0; i < array0.size(); i++) {
            for (int j = 0; j < array0.size(); j++) {
                if (i==j) continue;
                else {
                    if(adj.get(array0.get(i)).get(array0.get(j))==null){
                        continue;
                    }
                    else
                        sum = sum + (double)adj.get(array0.get(i)).get(array0.get(j));
                    k=i;
                }
            }
            if (sum<inter0) {
                inter0 =sum;
                k =i;
            };
        }
        return new Para(inter0, k);
    }

    public static Integer MinInterCluster(ArrayList <Integer> array1, ArrayList <Integer> array2,  ArrayList<ArrayList<Integer>> vector, ArrayList<ArrayList<Integer>> queue, ArrayList<Hashtable> adj) {
        //double min=0;
        double inter0 = 1;
        int x = 0; //used to judge which cluster has the smallest score.
        int y = 0;
        int i = 0;
        int j = 0;
        if (Inter_Cluster(array1, array2, adj) < inter0) {
            x = 1;
            inter0 = Inter_Cluster(array1, array2, adj);
        }
        for (i = 0; i < vector.size(); i++) {
            if (Inter_Cluster(array1, vector.get(i), adj) < inter0) {
                x = 2;
                y = i;
                inter0 = Inter_Cluster(array1, vector.get(i), adj);
            }
        }
        for (j = 1; j < queue.size(); j++) {
            if (Inter_Cluster(array1, queue.get(j), adj) < inter0) {
                x = 3;
                y = j;
                inter0 = Inter_Cluster(array1, queue.get(j), adj);
            }
        }

        if (x == 1) {
            return array2.size();
        }
        else if (x == 2) {
            return vector.get(y).size();
        }
        else if (x == 3) {
            return queue.get(y).size();
        }
        else return null;
    }

    public static double MinInter1(ArrayList <Integer> array1, ArrayList<ArrayList<Integer>> vector, ArrayList<ArrayList<Integer>> queue, ArrayList<Hashtable> adj) {
        //double min=0;
        double inter0=1;
        int i=0;
        int j=0;
        for (i=0; i<vector.size(); i++) {
            if(Inter_Cluster(array1, vector.get(i), adj)<=inter0){
                inter0 = Inter_Cluster(array1, vector.get(i), adj);
            }
        }
        for (j = 1; j < queue.size(); j++) {
            if (Inter_Cluster(array1, queue.get(j), adj) <= inter0) {
                inter0 = Inter_Cluster(array1, queue.get(j), adj);
            }
        }
        return inter0;
    }

    public static Integer MinInter1Cluster(ArrayList <Integer> array1, ArrayList<ArrayList<Integer>> vector, ArrayList<ArrayList<Integer>> queue, ArrayList<Hashtable> adj) {
        //double min=0;
        double inter0=1;
        int x = 0; //used to judge which cluster has the smallest score.
        int y = 0;
        int i=0;
        int j=0;
        for (i=0; i<vector.size(); i++) {
            if(Inter_Cluster(array1, vector.get(i), adj)<=inter0){
                x=1; y=i;
                inter0 = Inter_Cluster(array1, vector.get(i), adj);
            }
        }

        for (j = 1; j < queue.size(); j++) {
            if (Inter_Cluster(array1, queue.get(j), adj) <= inter0) {
                x=2; y=j;
                inter0 = Inter_Cluster(array1, queue.get(j), adj);
            }
        }

        if(x==1){return  vector.get(y).size();}
        else if (x==2){return queue.get(y).size();}
        else return null;
    }

    public static ClusterVorQ WhichCluster(ArrayList <Integer> array1, ArrayList<ArrayList<Integer>> vector, ArrayList<ArrayList<Integer>> queue, ArrayList<Hashtable> adj) {
        //double min=0;
        double inter0=1;
        int x = 0; //used to judge which cluster has the smallest score.
        int y = 0;
        int i=0;
        int j=0;
        for (i=0; i<vector.size(); i++) {
            if(Inter_Cluster(array1, vector.get(i), adj)<=inter0){
                x=1; y=i;
                inter0 = Inter_Cluster(array1, vector.get(i), adj);
            }
        }
        for (j = 1; j < queue.size(); j++) {
            if (Inter_Cluster(array1, queue.get(j), adj) <= inter0) {
                x=2; y=j;
                inter0 = Inter_Cluster(array1, queue.get(j), adj);
            }
        }
        return new ClusterVorQ(x, y, inter0);

    }

    public static double MinInter2(ArrayList <Integer> array1, ArrayList<ArrayList<Integer>> vector, ArrayList<ArrayList<Integer>> queue, ArrayList<Hashtable> adj) {
        //double min=0;
        double inter0=1;
        int i=0;
        int j=0;
        for (i=0; i<vector.size(); i++) {
            if(Inter_Cluster(array1, vector.get(i), adj)<=inter0){
                inter0 = Inter_Cluster(array1, vector.get(i), adj);
            }
        }
        for (j = 1; j < queue.size()-2; j++) {
            if (Inter_Cluster(array1, queue.get(j), adj) <= inter0) {
                inter0 = Inter_Cluster(array1, queue.get(j), adj);
            }
        }

        return inter0;
    }

    //the method only consider one 2-d arraylist.
    public static double MinInter3(int s, ArrayList<ArrayList<Integer>> vector, ArrayList<Hashtable> adj) {
        //double min=0;
        double inter0=1;
        int i=0;
        int j=0;
        for (i=0; i<vector.size(); i++) {
            if (i==s) continue;
            if(Inter_Cluster(vector.get(s), vector.get(i), adj)<=inter0){
                inter0 = Inter_Cluster(vector.get(s), vector.get(i), adj);
            }
        }

        return inter0;
    }

    public static Integer MinInter3Cluster(int s, ArrayList<ArrayList<Integer>> vector, ArrayList<Hashtable> adj) {
        //double min=0;
        double inter0=1;
        int m = 0;
        int i=0;
        int j=0;
        for (i=0; i<vector.size(); i++) {
            if (i==s) continue;
            if(Inter_Cluster(vector.get(s), vector.get(i), adj)<=inter0){
                m = vector.get(i).size();
                inter0 = Inter_Cluster(vector.get(s), vector.get(i), adj);
            }
        }

        return m;
    }



}




