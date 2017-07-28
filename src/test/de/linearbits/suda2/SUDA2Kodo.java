package de.linearbits.suda2;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.carrotsearch.hppc.BitSet;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntMap;
import com.carrotsearch.hppc.IntIntOpenHashMap;
import com.carrotsearch.hppc.IntObjectMap;
import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.IntOpenHashSet;
import com.carrotsearch.hppc.IntSet;

/**
 * Code from Kostya Demchuk provided at https://github.com/kostyademchuk/kodo
 *
 */
public class SUDA2Kodo {
    
 private static class Position {
        int pos = 0;
        public Position() {}
        public Position(int p) { pos = p; }
    }

    // dataset
    private int[][] X;

    // dataset size parameters
    private int n, m;

    // maximum size of MSU in the search
    private int kmax = 0;

    public void run(int[][] data, int kMax) {

        // build up a dataset
        X = data;

        // initialize n and m (at least the first line exists)
        n = data.length; m = data[0].length;
        kmax = kMax == 0 ? m : kMax;
        
        // TODO: FP: This is maybe needed because the algorithm may
        //           require that the same integer id must not be used
        //           in different columns. However, this seems to not help.
        int offset = 0;
        for (int column = 0; column < m; column++) {
            Set<Integer> set = new HashSet<>();
            for (int row = 0; row < n; row++) {
                set.add(data[row][column]);
                data[row][column] += offset;
            }
            offset += set.size();
        }

        // create the mapping of perfectly correlated items
        IntObjectMap<IntArrayList> perfCorItems = new
                IntObjectOpenHashMap<IntArrayList>();
        // create the list of minimal sample uniques in the dataset
        IntArrayList[] MSUs = new IntArrayList[kmax];
        for (int z = 0; z < kmax; z++) MSUs[z] = new IntArrayList();
        IntArrayList[] Cs = new IntArrayList[kmax];
        for (int z = 0; z < kmax; z++) Cs[z] = new IntArrayList();

        // create the list of item columns and frequencies
        IntIntMap cs = new IntIntOpenHashMap(n * m / 2);
        IntIntMap pccs = new IntIntOpenHashMap();
        IntIntMap fs = new IntIntOpenHashMap(n * m / 2);
        // create the mapping of item associated rows
        IntObjectMap<int[]> itemRows = new IntObjectOpenHashMap<int[]>();

        // build an ordered item list
        IntArrayList hs = getRankItemList(MSUs, Cs, perfCorItems, pccs,
                cs, fs, itemRows);

        // size of the rank item list
        int l = hs.size() - 1; // 21;
        // sizes of growing MSUs
        int[] ls = new int[kmax - 1];

        // create the list of items that have already been processed
        IntOpenHashSet[] history = new IntOpenHashSet[kmax];
        history[0] = new IntOpenHashSet(l, 1);

        // display time prior computation
        System.out.println(new Date());
        long time = System.currentTimeMillis();

        // for each item in the rank item list
        boolean[] columns;
        final int[] hsbuf = hs.buffer;
        for (int i = 0, h, c, f; i < l; i++) {
            // get item information
            h = hsbuf[i];
//          System.out.println(h);
            f = fs.get(h);
            c = cs.get(h);
            history[0].add(h);

            columns = new boolean[m]; columns[c] = true;

            // find potential MSUs
            SUDA2(itemRows.get(h), f, kmax - 1, columns, history,
                    Cs, MSUs, ls);

            // 1-MSUs + (-2) + it
            int size = MSUs[1].size();
            if (size > ls[0]) {
                // Rank Test
                for (int j = ls[0]; j < size; j++) {
                    if (fs.get(MSUs[1].buffer[j]) < f) {
                        Cs[1].set(j, -1);
                        MSUs[1].set(j, -1);
                    }
                }
                // add leader
                Cs[1].add(-2); Cs[1].add(c);
                MSUs[1].add(-2); MSUs[1].add(h);
                // increase the size of MSUs[1]
                ls[0] = size + 2;
            }

            IntObjectMap<BitSet> specRows = new
                    IntObjectOpenHashMap<BitSet>(l, 1);

            // for each potential MSU
            for (int z = 2; z < kmax; z++) {

                size = MSUs[z].size();
                if (size == ls[z - 1]) continue;

                // position equals to the current size of MSUs[z]
                Position pos = new Position(ls[z - 1]);
                ls[z - 1] = size + 2;
                IntArrayList MSU = new IntArrayList();
                final int[] bufMSUs = MSUs[z].buffer;
                while (isNextMSU(bufMSUs, pos, MSU, z, size)) {

                    if (MSU.isEmpty()) continue;
                    BitSet b = new BitSet(n - f);
                    final int[] bufMSU = MSU.buffer;

                    for (int j = z - 1, ih; j >= 0; j--) {
                        ih = bufMSUs[bufMSU[j]];
                        // Rank Test
                        if (!cs.containsKey(ih) && fs.get(ih) < f) {
                            // candidate is not an MSU
                            Cs[z].buffer[bufMSU[j]] = -1;
                            bufMSUs[bufMSU[j]] = -1;
                            break;
                        }
                        // Support Row Test
                        if (specRows.containsKey(ih)) {
                            b.or(specRows.get(ih));
                            if (b.cardinality() == n - f) {
                                // candidate is not an MSU
                                Cs[z].buffer[bufMSU[j]] = -1;
                                bufMSUs[bufMSU[j]] = -1;
                                break;
                            }
                        } else {
                            BitSet b2 = new BitSet(n - f);
                            // method to avoid int[] rows
                            final int[] hRows = itemRows.get(h);
                            int cih = cs.get(ih), index = 0, start = 0;
                            while (index < f) {
                                for (int r = start; r < hRows[index]; r++) {
                                    if (ih != X[r][cih]) {
                                        b2.set(r); b.set(r);
                                    }
                                }
                                start = hRows[index] + 1; index++;
                            }
                            for (int r = hRows[f - 1] + 1; r < n; r++) {
                                if (ih != X[r][cih]) {
                                    b2.set(r); b.set(r);
                                }
                            }

                            if (b.cardinality() == n - f) {
                                // candidate is not an MSU
                                Cs[z].buffer[bufMSU[j]] = -1;
                                bufMSUs[bufMSU[j]] = -1;
                                break;
                            }
                            specRows.put(ih, b2);
                        }
                    }
                    MSU.clear();
                }
                // put the current item on top of the cake
                Cs[z].add(-(z + 1)); Cs[z].add(c);
                MSUs[z].add(-(z + 1)); MSUs[z].add(h);

            }

        }

        // display time after computation
        time = System.currentTimeMillis() - time;
        System.out.println(new Date() + " (" + time + ")");

        // output minimal sample uniques
        printMSUs(MSUs, Cs, perfCorItems, pccs);

    }

    private IntArrayList getRankItemList(
            int[] tableRows, int tableSize,
            IntArrayList[] MSUs, IntArrayList[] Cs,
            boolean[] columns, int depthMinusOne,
            IntOpenHashSet[] history,
            IntIntMap cs, IntIntMap fs, IntObjectMap<int[]> itemRows) {

        // create the list of items
        IntArrayList hs = new IntArrayList();
        // create the list of item associated rows
        IntArrayList rows;
        // create the set of distinct column values
        IntSet columnValues;

        // go through item values in a subtable of X, s = items.size()
        int totalFrequency, s = 0, olds = 0;
        for (int j = 0, t; j < m; j++) {

            if (columns[j]) continue;

            columnValues = new IntOpenHashSet(tableSize / 2, 1);
            totalFrequency = 0;
            outer:
            for (int i = 0, rc; i < tableSize; i++) {
                // get the current matrix value
                t = X[tableRows[i]][j];

                // pass over if such an element has been considered
                if (columnValues.contains(t)) continue;

                // check if it has been already considered before
                for (int z = 0; z < depthMinusOne; z++) {
                    if (history[z].contains(t)) {
                        columnValues.add(t);
                        continue outer;
                    }
                }

                // count the repetition of the t value and
                // keep the track of associated rows
                rows = new IntArrayList();
                rows.add(tableRows[i]);
                for (int r = i + 1; r < tableSize; r++) {
                    if (X[tableRows[r]][j] == t)
                        rows.add(tableRows[r]);
                }
                rc = rows.size(); totalFrequency += rc;

                // drop items occurring in all rows (Uniform Support Property)
                if (rc == tableSize) break;

                // save a unique item (1-MSU)
                if (rc == 1) {
                    Cs[depthMinusOne].add(j);
                    MSUs[depthMinusOne].add(t);
                    // no need to add a unique item to the column values list
                    continue;
                }

                columnValues.add(t);

                // save item information
                hs.add(t);
                cs.put(t, j); fs.put(t, rc);
                itemRows.put(t, rows.buffer);

                s++; // increase the size of items array

                // jump out if there are million of records with only few items
                // and the total column frequency has been reached
                if (totalFrequency == tableSize) break;
            }

            // skip this column in future if there were no new items
            if (olds == s)
                columns[j] = true;
            else olds = s;
        }

        // sort hs list
        final int[] hsbuf = hs.buffer;
        boolean swapped = true;
        int i = 0;
        while (swapped) {
            swapped = false;
            i++;
            for (int j = 0; j < s - i; j++) {
                if (fs.get(hsbuf[j]) > fs.get(hsbuf[j + 1])) {
                    hsbuf[j] ^= hsbuf[j + 1];
                    hsbuf[j + 1] ^= hsbuf[j];
                    hsbuf[j] ^= hsbuf[j + 1];
                    swapped = true;
                }
            }
        }

        return hs;

    }

    private IntArrayList getRankItemList(
            IntArrayList[] MSUs, IntArrayList[] Cs,
            IntObjectMap<IntArrayList> perfCorItems, IntIntMap pccs,
            IntIntMap cs, IntIntMap fs, IntObjectMap<int[]> itemRows) {

        // create the list of items
        IntArrayList hs = new IntArrayList();
        // create the list of item associated rows
        IntArrayList rows;
        // create the set of distinct column values
        IntSet columnValues = new IntOpenHashSet();

        // go through item values in X, s = items.size()
        int totalFrequency, s = 0;
        for (int j = 0, x; j < m; j++) {

            columnValues.clear(); totalFrequency = 0;
            for (int i = 0, rc; i < n; i++) {
                // get the current matrix value
                x = X[i][j];

                // pass over if such an element has been considered
                if (columnValues.contains(x)) continue;

                // count the repetition of the x value and
                // keep the track of associated rows
                rows = new IntArrayList();
                rows.add(i);
                for (int r = i + 1; r < n; r++) {
                    if (X[r][j] == x) rows.add(r);
                }
                rc = rows.size(); totalFrequency += rc;

                // drop items occurring in all rows (Uniform Support Property)
                if (rc == n) break;

                // save a unique item (1-MSU)
                if (rc == 1) {
                    Cs[0].add(j);
                    MSUs[0].add(x);
                    // no need to add a unique item to the column values list
                    continue;
                }

                columnValues.add(x);

                // go through the list of already saved items
                int here = 0;
                boolean isCorrelated = false, isPositionSet = false;
                for (int p = s - 1, f, h, c; p >= 0; p--) {
                    // get item information
                    h = hs.get(p);
                    f = fs.get(h);
                    c = cs.get(h);

                    // two items from distinct columns
                    // with the same repetition count
                    if (f == rc && c != j) {
                        // check for perfect correlation
                        isCorrelated = true;
                        final int[] buf = itemRows.get(h);
                        for (int r = 0; r < f; r++) {
                            if (x != X[buf[r]][j]) {
                                isCorrelated = false;
                                break;
                            }
                        }
                        // save a perfectly correlated item
                        if (isCorrelated) {
                            // check if an item has perfectly correlated items
                            if (perfCorItems.containsKey(h)) {
                                perfCorItems.get(h).add(x);
                            } else {
                                IntArrayList itemset = new IntArrayList();
                                itemset.add(x);
                                perfCorItems.put(h, itemset);
                            }
                            pccs.put(x, j);
                            // do not save it to the items list
                            break;
                        }
                    }

                    // set the position to add it to the items list
                    if (f <= rc && !isPositionSet) {
                        here = p + 1;
                        isPositionSet = true;
                    }

                    // no more perfectly correlated items
                    // and the position has been set
                    if (f < rc) break;
                }

                // do not save perfectly correlated items
                if (isCorrelated) continue;

                hs.add(x);
                if (here < s) {
                    final int[] hsBuffer = hs.buffer;
                    for (int u = s; u > here; u--) {
                        hsBuffer[u] = hsBuffer[u - 1];
                    }
                    hsBuffer[here] = x;
                }

                // save item information
                cs.put(x, j); fs.put(x, rc);
                itemRows.put(x, rows.buffer);

                s++; // increase the size of items array

                // jump out if there are million of records with only few items
                // and the total column frequency has been reached
                if (totalFrequency == n) break;
            }
        }

        return hs;

    }

    private boolean isNextMSU(
            final int[] MSUs, Position pos, IntArrayList MSU,
            int z, int size) {

        for (int i = pos.pos; i < size; i++) {
            if (MSUs[i] == -1) continue;
            if (MSUs[i] > 0) {
                // find the next MSU position
                pos.pos = size;
                for (int j = i + 1; j < size; j++) {
                    if (MSUs[j] == -1) continue;
                    if (MSUs[j] > 0) {
                        // we found it
                        pos.pos = j; break;
                    } else j++;
                }
                // extract an MSU
                MSU.add(i);
                int depth = 2;
                for (int j = i + 1; j < size; j++) {
                    if (MSUs[j] == -depth) {
                        j++;
                        if (MSUs[j] == -1) {
                            MSU.clear(); break;
                        }
                        MSU.add(j);
                        depth++;
                        if (depth == z + 1) break;
                    }
                }
                // process current MSU
                return true;
            } else i++;
        }

        return false;

    }

    private boolean isNextMSU(
            final int[] MSUs, Position pos, IntArrayList MSU,
            int z, int size, IntArrayList groundSet) {

        // find ground set
        for (int i = pos.pos; i < size; i++) {
            if (MSUs[i] == -1) continue;
            if (MSUs[i] > 0) groundSet.add(i);
            else {
                i++;
                if (MSUs[i] == -1) {
                    groundSet.clear();
                    continue;
                }
                if (!groundSet.isEmpty()) {
                    MSU.add(i);
                    pos.pos = i + 1;
                    break;
                }
            }
        }
        if (groundSet.isEmpty()) return false;

        // find the next ground set starting position
        boolean isFirst = true;
        int p = size, depth = 3;
        outer:
        for (int i = pos.pos; i < size; i++) {
            if (MSUs[i] > 0 && isFirst) {
                // this is important condition
                if (MSUs[i - 1] < -1) continue;
                p = i;
                isFirst = false;
            }
            if (MSUs[i] == -depth) {
                i++;
                if (MSUs[i] == -1) {
                    MSU.clear(); groundSet.clear();
                    if (isFirst)
                        for (int j = ++i; j < size; j++) {
                            if (MSUs[j] == -1) continue;
                            if (MSUs[j] > 0) {
                                p = j; break outer;
                            } else j++;
                        }
                    break;
                }
                MSU.add(i);
                depth++;
                if (depth == z) {
                    if (isFirst)
                        for (int j = ++i; j < size; j++) {
                            if (MSUs[j] == -1) continue;
                            if (MSUs[j] > 0) {
                                p = j; break outer;
                            } else j++;
                        }
                    break;
                }
            }
        }
        pos.pos = p;

        return true;

    }

    private boolean isNextMSUPrint(IntArrayList MSUs, Position pos,
            IntArrayList MSU, int z) {
        final int[] buf = MSUs.buffer;
        final int size = MSUs.size();
        for (int i = pos.pos; i < size; i++) {
            if (buf[i] == -1) continue;
            if (buf[i] > 0) {
                // find the next MSU position
                int nextPos = pos.pos;
                for (int j = i + 1; j < size; j++) {
                    if (buf[j] == -1) continue;
                    // we found it
                    if (buf[j] > 0) {
                        nextPos = j; break;
                    } else j++;
                }
                if (nextPos == pos.pos) pos.pos = size;
                else pos.pos = nextPos;
                // extract an MSU
                MSU.add(i);
                int depth = 2;
                for (int j = i + 1; j < size; j++) {
                    if (buf[j] == -depth) {
//                      if (depth == z + 1) break;
                        if (buf[j + 1] == -1) {
                            MSU.clear(); break;
                        }
                        MSU.add(j + 1);
                        if (depth == z + 1) break;
                        depth++;
                    }
                }
                // process current MSU
                return true;
            } else i++;
        }
        return false;
    }

    private void printMSUs(IntArrayList[] MSUs, IntArrayList[] Cs,
            IntObjectMap<IntArrayList> perfCorItems, IntIntMap pccs) {

        System.out.println("Minimal sample uniques are:");
//      System.exit(1);
        int c = 0;
        for (int z = 0; z < kmax; z++) {

            if (MSUs[z].isEmpty()) continue;

            Position pos = new Position();
            IntArrayList MSU = new IntArrayList();
            while (isNextMSUPrint(MSUs[z], pos, MSU, z)) {

                if (MSU.isEmpty()) continue;

                for (int i = 0; i < MSU.size(); i++) {
//                    int ih = MSUs[z].buffer[MSU.buffer[i]];
//                    int ic = Cs[z].buffer[MSU.buffer[i]];
//                    values += Integer.toString((ih - ic) / m) + " ";
//                    columns += Integer.toString(ic + 1) + " ";
                }
                c++;
//              System.out.println(values + "at columns " + columns);
                // String values = "", columns = "";
                // perfectly correlated items
                for (int i = 0; i < MSU.size(); i++) {
                    int ih = MSUs[z].buffer[MSU.buffer[i]];
                    if (perfCorItems.containsKey(ih)) {
//                      System.out.println("pc ih = " + (ih - Cs[z].buffer[MSU.buffer[i]]) / m);
                        IntArrayList pcitems = perfCorItems.get(ih);
                        for (int pcj = 0, pcl = pcitems.size(); pcj < pcl; pcj++) {
//                            int pcih = pcitems.get(pcj);
//                            int pcic = pccs.get(pcitems.get(pcj));
//                            values += Integer.toString((pcih - pcic) / m) + " ";
//                            columns += Integer.toString(pcic + 1) + " ";
                            for (int i2 = 0; i2 < MSU.size(); i2++) {
                                int ih2 = MSUs[z].buffer[MSU.buffer[i2]];
                                if (ih2 == ih) continue;
//                                int ic2 = Cs[z].buffer[MSU.buffer[i2]];
//                                values += Integer.toString((ih2 - ic2) / m) + " ";
//                                columns += Integer.toString(ic2 + 1) + " ";
                            }                           
                            c++;
//                          System.out.println(values + "at columns " + columns);
//                            values = ""; columns = "";
                        }
                    }
                }
                MSU.clear();
            }
//            System.out.println("z = " + z + " c = " + c);
        }
        System.out.println("c = " + c);

    }

    private void SUDA2(
            int[] tableRows, int tableSize, int k,
            boolean[] columns, IntOpenHashSet[] history,
            IntArrayList[] Cs, IntArrayList[] MSUs, int[] topls) {

        // prepare local data structures
        int depth = kmax - k + 1;
        boolean[] columnsCopy = new boolean[m];
        System.arraycopy(columns, 0, columnsCopy, 0, m);
        IntIntMap cs = new IntIntOpenHashMap();
        IntIntMap fs = new IntIntOpenHashMap();
        IntObjectMap<int[]> itemRows = new IntObjectOpenHashMap<int[]>();

        // build an ordered item list for a subtable
        IntArrayList hs = getRankItemList(tableRows, tableSize, MSUs, Cs,
                columnsCopy, depth - 1, history, cs, fs, itemRows);

        if (k == 1) return;

        // size of the rank item list
        int l = hs.size() - 1;
        if (l < 1) return;

        // create the list of items that do not yield MSUs
        history[kmax - k] = new IntOpenHashSet(l, 1);

        // sizes of growing MSUs
        int[] ls = new int[kmax - depth];
        for (int z = depth; z < kmax; z++) {
            ls[z - depth] = topls[z - depth + 1];//MSUs[z].size();
        }

        // for each item in the rank item list
        final int[] hsbuf = hs.buffer;
        for (int i = 0, h, c, f; i < l; i++) {
            // get item information
            h = hsbuf[i];
            f = fs.get(h);
            c = cs.get(h);
            history[kmax - k].add(h);

            columnsCopy[c] = true;

            // find potential MSUs
            SUDA2(itemRows.get(h), f, k - 1, columnsCopy,
                    history, Cs, MSUs, ls);

            columnsCopy[c] = false;

            // 1-MSUs + (-2) + it
            int size = MSUs[depth].size();
            if (size > ls[0]) {
                // Rank Test
                for (int j = ls[0]; j < size; j++) {
                    if (fs.get(MSUs[depth].buffer[j]) < f) {
                        Cs[depth].set(j, -1);
                        MSUs[depth].set(j, -1);
                    }
                }
                // add leader
                Cs[depth].add(-2); Cs[depth].add(c);
                MSUs[depth].add(-2); MSUs[depth].add(h);
                // increase the size of MSUs[1]
                ls[0] = size + 2;
            }

            IntObjectMap<BitSet> specRows = new
                    IntObjectOpenHashMap<BitSet>(l, 1);

            // for each potential MSU
            for (int z = depth + 1; z < kmax; z++) {

                size = MSUs[z].size();
                if (size == ls[z - depth]) continue;

                // position equals to the current size of MSUs[z]
                Position pos = new Position(ls[z - depth]);
                ls[z - depth] = size + 2;
                IntArrayList groundSet = new IntArrayList();
                IntArrayList MSU = new IntArrayList();
                final int[] bufMSUs = MSUs[z].buffer;
                int sizeMSU = -bufMSUs[size - 2] - 2;

                while (isNextMSU(bufMSUs, pos, MSU, z, size, groundSet)) {

                    if (MSU.isEmpty()) continue;
                    BitSet bMSU = new BitSet(tableSize - f);
                    boolean isMSU = true;
                    final int[] bufMSU = MSU.buffer;

                    for (int j = sizeMSU, ih; j >= 0; j--) {
                        ih = bufMSUs[bufMSU[j]];
                        // Rank Test
                        if (fs.get(ih) < f) {
                            // candidate is not an MSU
                            Cs[z].buffer[bufMSU[j]] = -1;
                            bufMSUs[bufMSU[j]] = -1;
                            isMSU = false;
                            break;
                        }
                        // Support Row Test
                        if (specRows.containsKey(ih)) {
                            bMSU.or(specRows.get(ih));
                            if (bMSU.cardinality() == tableSize - f) {
                                // candidate is not an MSU
                                Cs[z].buffer[bufMSU[j]] = -1;
                                bufMSUs[bufMSU[j]] = -1;
                                isMSU = false;
                                break;
                            }
                        } else {
                            BitSet b = new BitSet(tableSize - f);
                            // method to avoid int[] rows (problem here)
                            final int[] hRows = itemRows.get(h);
                            int cih = cs.get(ih), index = 0, start = 0;
                            while (index < f) {
                                int r = start;
                                while (tableRows[r] < hRows[index]) {
                                    if (ih != X[tableRows[r]][cih]) {
                                        b.set(r); bMSU.set(r);
                                    }
                                    r++;
                                }
                                start = r + 1; index++;
                            }
                            for (int r = start; r < tableSize; r++) {
                                if (ih != X[tableRows[r]][cih]) {
                                    b.set(r); bMSU.set(r);
                                }
                            }
                            if (bMSU.cardinality() == tableSize - f) {
                                // candidate is not an MSU
                                Cs[z].buffer[bufMSU[j]] = -1;
                                bufMSUs[bufMSU[j]] = -1;
                                isMSU = false;
                                break;
                            }
                            specRows.put(ih, b);
                        }
                    }
                    MSU.clear();
                    // check ground set
                    if (isMSU) {
                        final int[] groundSetBuf = groundSet.buffer;
                        for (int j = groundSet.size() - 1, ih; j >= 0; j--) {
                            ih = bufMSUs[groundSetBuf[j]];
                            // Rank Test
                            if (fs.get(ih) < f) {
                                // candidate is not an MSU
                                Cs[z].buffer[groundSetBuf[j]] = -1;
                                bufMSUs[groundSetBuf[j]] = -1;
                                continue;
                            }
                            // Support Row Test
                            if (specRows.containsKey(ih)) {
                                BitSet b2 = new BitSet(tableSize - f);
                                b2.or(specRows.get(ih));
                                b2.or(bMSU);
                                if (b2.cardinality() == tableSize - f) {
                                    // candidate is not an MSU
                                    Cs[z].buffer[groundSetBuf[j]] = -1;
                                    bufMSUs[groundSetBuf[j]] = -1;
                                }
                            } else {
                                BitSet b2 = new BitSet(tableSize - f);
                                // method to avoid int[] rows (problem here)
                                final int[] hRows = itemRows.get(h);
                                int cih = cs.get(ih), index = 0, start = 0;
                                while (index < f) {
                                    int r = start;
                                    while (tableRows[r] < hRows[index]) {
                                        if (ih != X[tableRows[r]][cih])
                                            b2.set(r);
                                        r++;
                                    }
                                    start = r + 1; index++;
                                }
                                for (int r = start; r < tableSize; r++) {
                                    if (ih != X[tableRows[r]][cih])
                                        b2.set(r);
                                }
                                b2.or(bMSU);
                                if (b2.cardinality() == tableSize - f) {
                                    // candidate is not an MSU
                                    Cs[z].buffer[groundSetBuf[j]] = -1;
                                    bufMSUs[groundSetBuf[j]] = -1;
                                    continue;
                                }
                            }
                        }
                    }
                    groundSet.clear();
                }
                // put the current item on top of the cake
                sizeMSU = -(sizeMSU + 3);
                Cs[z].add(sizeMSU); Cs[z].add(c);
                MSUs[z].add(sizeMSU); MSUs[z].add(h);
            }
        }

    }

}
