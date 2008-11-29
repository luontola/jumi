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

package net.orfjackal.dimdwarf.gc;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class MockGraphSpec extends Specification<Object> {

    private MockGraph graph;

    public void create() throws Exception {
        graph = new MockGraph();
    }


    public class WhenAGraphIsEmpty {

        public void itContainsNoNodes() {
            specify(graph.getAllNodes(), should.containExactly());
        }

        public void itContainsNoRootNodes() {
            specify(graph.getRootNodes(), should.containExactly());
        }
    }

    public class WhenANodeIsCreated {

        public void create() {
            graph.createNode("A");
        }

        public void theGraphContainsThatNode() {
            specify(graph.getAllNodes(), should.containExactly("A"));
        }

        public void thatNodeIsNotRootNode() {
            specify(graph.getRootNodes(), should.containExactly());
        }

        public void afterRemovalTheNodeDoesNotExist() {
            graph.removeNode("A");
            specify(graph.getAllNodes(), should.containExactly());
            specify(graph.getRootNodes(), should.containExactly());
        }
    }

    public class WhenARootNodeIsCreated {

        public void create() {
            graph.createRootNode("A");
        }

        public void theGraphContainsThatNode() {
            specify(graph.getAllNodes(), should.containExactly("A"));
        }

        public void thatNodeIsRootNode() {
            specify(graph.getRootNodes(), should.containExactly("A"));
        }

        public void afterRemovalTheNodeDoesNotExist() {
            graph.removeNode("A");
            specify(graph.getAllNodes(), should.containExactly());
            specify(graph.getRootNodes(), should.containExactly());
        }
    }

    public class WhenNodesAreConnectedWithADirectedEdge {

        public void create() {
            graph.createNode("A");
            graph.createNode("B");
            graph.createDirectedEdge("A", "B");
        }

        public void theFirstNodeConnectsToTheSecondNode() {
            specify(graph.getConnectedNodesOf("A"), should.containExactly("B"));
        }

        public void theSecondNodeDoesNotConnectToTheFirstNode() {
            specify(graph.getConnectedNodesOf("B"), should.containExactly());
        }

        public void afterNodeRemovalTheEdgeDoesNotExist() {
            graph.removeNode("A");
            specify(graph.getConnectedNodesOf("A"), should.containExactly());
        }

        public void afterEdgeRemovalTheEdgeDoesNotExist() {
            graph.removeDirectedEdge("A", "B");
            specify(graph.getConnectedNodesOf("A"), should.containExactly());
        }
    }

    public class WhenANodeIsConnectedToManyOtherNodes {

        public void create() {
            graph.createNode("A");
            graph.createNode("B");
            graph.createDirectedEdge("A", "B");
            graph.createDirectedEdge("A", "C");
        }

        public void itIsConnectedToAllOfThem() {
            specify(graph.getConnectedNodesOf("A"), should.containExactly("B", "C"));
        }

        public void afterRemovalOfOneEdgeTheOtherEdgesStillExist() {
            graph.removeDirectedEdge("A", "B");
            specify(graph.getConnectedNodesOf("A"), should.containExactly("C"));
        }
    }

    public class WhenANodeIsConnectedToAnotherNodeManyTimes {

        public void create() {
            graph.createNode("A");
            graph.createNode("B");
            graph.createDirectedEdge("A", "B");
            graph.createDirectedEdge("A", "B");
        }

        public void itIsConnectedToItManyTimes() {
            specify(graph.getConnectedNodesOf("A"), should.containExactly("B", "B"));
        }

        public void afterRemovalOfOneEdgeTheOtherEdgesStillExist() {
            graph.removeDirectedEdge("A", "B");
            specify(graph.getConnectedNodesOf("A"), should.containExactly("B"));
        }
    }

    public class WhenTheStatusOfANodeIsSet {

        public void create() {
            graph.createNode("A");
            graph.createNode("B");
            graph.setStatus("A", 1L);
        }

        public void itHasThatStatus() {
            specify(graph.getStatus("A"), should.equal(1L));
        }

        public void theStatusOfOtherNodesIsUnaffected() {
            specify(graph.getStatus("B"), should.equal(Graph.NULL_STATUS));
        }

        public void afterNodeRemovalTheStatusIsRemoved() {
            graph.removeNode("A");
            specify(graph.getStatus("A"), should.equal(Graph.NULL_STATUS));
        }
    }
}
