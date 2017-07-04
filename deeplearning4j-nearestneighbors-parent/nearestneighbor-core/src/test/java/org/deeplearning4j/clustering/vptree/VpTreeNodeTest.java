/*-
 *
 *  * Copyright 2015 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.clustering.vptree;

import org.deeplearning4j.clustering.sptree.DataPoint;
import org.junit.Test;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anatoly Borisov
 */
public class VpTreeNodeTest {


    private static class DistIndex implements Comparable<DistIndex> {
        public double dist;
        public int index;

        public int compareTo(DistIndex r) {
            return Double.compare(dist, r.dist);
        }
    }

    @Test
    public void testKnnK() {
        INDArray arr = Nd4j.randn(10,5);
        VPTree t = new VPTree(arr,false);
        List<DataPoint> resultList = new ArrayList<>();
        List<Double> distances = new ArrayList<>();
        t.search(arr.getRow(0),5,resultList,distances);
        assertEquals(5,resultList.size());
    }

    @Test
    public void knnManual() {
        INDArray arr = Nd4j.randn(3, 5);
        VPTree t = new VPTree(arr, false);
        int k = 1;
        int m = arr.rows();
        for (int targetIndex = 0; targetIndex < m; targetIndex++) {
            // Do an exhaustive search
            TreeSet<Integer> s = new TreeSet<>();
            PriorityQueue<DistIndex> pq = new PriorityQueue<>();
            for (int j = 0; j < m; j++) {
                double d = arr.getRow(targetIndex).distance2(arr.getRow(j));
                DistIndex di = new DistIndex();
                di.dist = d;
                di.index = j;
                pq.add(di);
            }

            // keep closest k
            for (int i = 0; i < k; i++) {
                DistIndex di = pq.poll();
                System.out.println("exhaustive d=" + di.dist);
                s.add(di.index);
            }

            // Check what VPTree gives for results
            List<DataPoint> results = new ArrayList<>();
            List<Double> distances = new ArrayList<>();
            t.search(arr.getRow(targetIndex), k, results, distances);
            //List<DataPoint> items = t.getItems();
            TreeSet<Integer> resultSet = new TreeSet<>();

            // keep k in a set
            for (int i = 0; i < k; ++i) {
                DataPoint result = results.get(i);
                int r = result.getIndex();
                resultSet.add(r);
            }

            // check
            for (int r : resultSet) {
                assertTrue(String.format("VPTree result %d is not in the closest %d " + " from the exhaustive search.",
                                r, k), s.contains(r));
            }
            for (int r : s) {
                assertTrue(String.format("VPTree result %d is not in the closest %d " + " from the exhaustive search.",
                                r, k), s.contains(r));
            }
        }
    }


    @Test
    public void vpTreeTest() {
        List<DataPoint> points = new ArrayList<>();
        points.add(new DataPoint(0, Nd4j.create(new double[] {55, 55})));
        points.add(new DataPoint(1, Nd4j.create(new double[] {60, 60})));
        points.add(new DataPoint(2, Nd4j.create(new double[] {65, 65})));
        VPTree tree = new VPTree(points);
        List<DataPoint> add = new ArrayList<>();
        List<Double> distances = new ArrayList<>();
        tree.search(Nd4j.create(new double[] {50, 50}), 1, add, distances);
        DataPoint assertion = add.get(0);
        assertEquals(new DataPoint(0, Nd4j.create(new double[] {55, 55})), assertion);


    }

}
