// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.util.Objects;
import org.jmock.Expectations;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class MockGraphSpec extends Specification<Object> {

    private MockGraph graph;
    private MutatorListener<String> listener;

    public void create() throws Exception {
        listener = Objects.uncheckedCast(mock(MutatorListener.class));
        graph = new MockGraph();
        graph.addMutatorListener(listener);
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
            checking(new Expectations() {{
                one(listener).onNodeCreated("A");
            }});
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
            checking(new Expectations() {{
                one(listener).onNodeCreated("A");
                one(listener).onReferenceCreated(null, "A");
            }});
            graph.createNode("A");
            graph.createDirectedEdge(null, "A");
        }

        public void theGraphContainsThatNode() {
            specify(graph.getAllNodes(), should.containExactly("A"));
        }

        public void thatNodeIsARootNode() {
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
            checking(new Expectations() {{
                one(listener).onNodeCreated("A");
                one(listener).onNodeCreated("B");
                one(listener).onReferenceCreated("A", "B");
            }});
            graph.createNode("A");
            graph.createNode("B");
            graph.createDirectedEdge("A", "B");
        }

        public void theSourceNodeConnectsToTheTargetNode() {
            specify(graph.getConnectedNodesOf("A"), should.containExactly("B"));
        }

        public void theTargetNodeDoesNotConnectToTheSourceNode() {
            specify(graph.getConnectedNodesOf("B"), should.containExactly());
        }

        public void afterNodeRemovalTheEdgeDoesNotExist() {
            checking(new Expectations() {{
                one(listener).onReferenceRemoved("A", "B");
            }});
            graph.removeNode("A");
            specify(graph.getConnectedNodesOf("A"), should.containExactly());
        }

        public void afterEdgeRemovalTheEdgeDoesNotExist() {
            checking(new Expectations() {{
                one(listener).onReferenceRemoved("A", "B");
            }});
            graph.removeDirectedEdge("A", "B");
            specify(graph.getConnectedNodesOf("A"), should.containExactly());
        }
    }

    public class WhenANodeIsConnectedToManyOtherNodes {

        public void create() {
            checking(new Expectations() {{
                one(listener).onNodeCreated("A");
                one(listener).onNodeCreated("B");
                one(listener).onNodeCreated("C");
                one(listener).onReferenceCreated("A", "B");
                one(listener).onReferenceCreated("A", "C");
            }});
            graph.createNode("A");
            graph.createNode("B");
            graph.createNode("C");
            graph.createDirectedEdge("A", "B");
            graph.createDirectedEdge("A", "C");
        }

        public void itIsConnectedToAllOfThem() {
            specify(graph.getConnectedNodesOf("A"), should.containExactly("B", "C"));
        }

        public void afterRemovalOfOneEdgeTheOtherEdgesStillExist() {
            checking(new Expectations() {{
                one(listener).onReferenceRemoved("A", "B");
            }});
            graph.removeDirectedEdge("A", "B");
            specify(graph.getConnectedNodesOf("A"), should.containExactly("C"));
        }
    }

    public class WhenANodeIsConnectedToAnotherNodeManyTimes {

        public void create() {
            checking(new Expectations() {{
                one(listener).onNodeCreated("A");
                one(listener).onNodeCreated("B");
                exactly(2).of(listener).onReferenceCreated("A", "B");
            }});
            graph.createNode("A");
            graph.createNode("B");
            graph.createDirectedEdge("A", "B");
            graph.createDirectedEdge("A", "B");
        }

        public void itIsConnectedToItManyTimes() {
            specify(graph.getConnectedNodesOf("A"), should.containExactly("B", "B"));
        }

        public void afterRemovalOfOneEdgeTheOtherEdgesStillExist() {
            checking(new Expectations() {{
                one(listener).onReferenceRemoved("A", "B");
            }});
            graph.removeDirectedEdge("A", "B");
            specify(graph.getConnectedNodesOf("A"), should.containExactly("B"));
        }
    }

    public class WhenTheMetadataOfANodeIsSet {
        private byte[] value = {0x01};
        private byte[] nullValue = {};

        public void create() {
            checking(new Expectations() {{
                one(listener).onNodeCreated("A");
                one(listener).onNodeCreated("B");
            }});
            graph.createNode("A");
            graph.createNode("B");
            graph.setMetadata("A", "status", value);
        }

        public void itHasThatMetadata() {
            specify(graph.getMetadata("A", "status"), should.containInOrder(value));
        }

        public void anyOtherMetadataItMayHaveIsUnaffected() {
            specify(graph.getMetadata("A", "foobar"), should.containInOrder(nullValue));
        }

        public void theMetadataOfOtherNodesIsUnaffected() {
            specify(graph.getMetadata("B", "status"), should.containInOrder(nullValue));
        }

        public void afterNodeRemovalTheMetadataIsRemoved() {
            graph.removeNode("A");
            specify(graph.getMetadata("A", "status"), should.containInOrder(nullValue));
        }
    }
}
