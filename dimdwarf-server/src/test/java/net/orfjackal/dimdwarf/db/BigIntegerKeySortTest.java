/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.db;

import net.orfjackal.dimdwarf.db.inmemory.RevisionCounter;
import net.orfjackal.dimdwarf.db.inmemory.RevisionMap;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 29.10.2008
 */
public class BigIntegerKeySortTest {

    public static void main(String[] args) {
        // TODO: the entity IDs do not sort in numeric order when converted to bytes - would it be necessary?
        ConvertBigIntegerToBytes conv = new ConvertBigIntegerToBytes();
        RevisionMap<Blob, Blob> map = new RevisionMap<Blob, Blob>(new RevisionCounter());

        putKeys(0, 1000, conv, map);
        putKeys(10000, 11000, conv, map);
        putKeys(30000, 31000, conv, map);
        putKeys(40000, 41000, conv, map);

        for (Blob key = map.firstKey(); key != null; key = map.nextKeyAfter(key)) {
            System.out.println(conv.back(key) + "\t" + key);
        }

        System.out.println("\n-- Errors --");
        for (Blob prev = map.firstKey(), current = map.nextKeyAfter(prev);
             current != null;
             prev = current, current = map.nextKeyAfter(current)) {
            BigInteger prevInt = conv.back(prev);
            BigInteger currentInt = conv.back(current);

            if (!prevInt.add(BigInteger.ONE).equals(currentInt)) {
                System.out.println("\nWrong order:");
                System.out.println(conv.back(prev) + "\t" + prev);
                System.out.println(conv.back(current) + "\t" + current);
            }
        }
    }

    private static void putKeys(int start, int end, ConvertBigIntegerToBytes conv, RevisionMap<Blob, Blob> map) {
        for (int i = start; i <= end; i++) {
            Blob key = conv.forth(BigInteger.valueOf(i));
            map.put(key, Blob.EMPTY_BLOB);
        }
    }

/* Output:

0	Blob[length=1,bytes=[00]]
128	Blob[length=2,bytes=[00 80]]
129	Blob[length=2,bytes=[00 81]]
130	Blob[length=2,bytes=[00 82]]
...
155	Blob[length=2,bytes=[00 9B]]
156	Blob[length=2,bytes=[00 9C]]
40064	Blob[length=3,bytes=[00 9C 80]]
40065	Blob[length=3,bytes=[00 9C 81]]
...
40831	Blob[length=3,bytes=[00 9F 7F]]
160	Blob[length=2,bytes=[00 A0]]
40960	Blob[length=3,bytes=[00 A0 00]]
...
40999	Blob[length=3,bytes=[00 A0 27]]
41000	Blob[length=3,bytes=[00 A0 28]]
161	Blob[length=2,bytes=[00 A1]]
162	Blob[length=2,bytes=[00 A2]]
...
255	Blob[length=2,bytes=[00 FF]]
1	Blob[length=1,bytes=[01]]
384	Blob[length=2,bytes=[01 80]]
...
116	Blob[length=1,bytes=[74]]
117	Blob[length=1,bytes=[75]]
30080	Blob[length=2,bytes=[75 80]]
30081	Blob[length=2,bytes=[75 81]]
...
30999	Blob[length=2,bytes=[79 17]]
31000	Blob[length=2,bytes=[79 18]]
122	Blob[length=1,bytes=[7A]]
123	Blob[length=1,bytes=[7B]]
124	Blob[length=1,bytes=[7C]]
125	Blob[length=1,bytes=[7D]]
126	Blob[length=1,bytes=[7E]]
127	Blob[length=1,bytes=[7F]]

*/

}
