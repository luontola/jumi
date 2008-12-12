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

package net.orfjackal.dimdwarf.gc.cms;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.gc.*;
import static net.orfjackal.dimdwarf.gc.cms.Color.*;
import org.junit.runner.RunWith;

import java.util.*;

/**
 * @author Esko Luontola
 * @since 29.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ConcurrentMarkSweepCollectorSpec extends Specification<Object> {

    private static final int MAX_NODES_PER_TASK = 1;

    private static final int STAGE_1_STEPS = 2;
    private static final int STAGE_2_STEPS = 5;
    private static final int STAGE_3_STEPS = 5;

    private MockGraph graph;
    private ConcurrentMarkSweepCollector<String> collector;

    private Collection<? extends IncrementalTask> stage1;
    private Collection<? extends IncrementalTask> stage2;
    private Collection<? extends IncrementalTask> stage3;

    public void create() throws Exception {
        graph = new MockGraph();
        graph.createNode("A");
        graph.createNode("B");
        graph.createNode("C");
        graph.createNode("D");
        graph.createDirectedEdge(null, "A");
        graph.createDirectedEdge("A", "B");
        graph.createDirectedEdge("B", "A");
        graph.createDirectedEdge("B", "C");

        collector = new ConcurrentMarkSweepCollector<String>(graph, MAX_NODES_PER_TASK);
        graph.addMutatorListener(collector.getMutatorListener());

        Iterator<? extends IncrementalTask> stages = collector.getCollectorStagesToExecute().iterator();
        stage1 = Arrays.asList(stages.next());
        stage2 = Arrays.asList(stages.next());
        stage3 = Arrays.asList(stages.next());
        specify(stages.hasNext(), should.equal(false));
    }

    private static Collection<IncrementalTask> executeManySteps(Collection<? extends IncrementalTask> tasks, int count) {
        Collection<IncrementalTask> tmp = new ArrayList<IncrementalTask>();
        tmp.addAll(tasks);
        for (int i = 0; i < count; i++) {
            tmp = executeOneStep(tmp);
        }
        return tmp;
    }

    private static Collection<IncrementalTask> executeOneStep(Collection<? extends IncrementalTask> tasks) {
        Collection<IncrementalTask> nextStep = new ArrayList<IncrementalTask>();
        for (IncrementalTask task : tasks) {
            nextStep.addAll(task.step());
        }
        return nextStep;
    }


    public class WhenCollectorHasNotBeenRun {

        public void newNodesAreByDefaultWhite() {
            specify(collector.getColor("A"), should.equal(WHITE));
            specify(collector.getColor("B"), should.equal(WHITE));
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("D"), should.equal(WHITE));
        }
    }

    public class InTheFirstStage {

        public void create() {
            stage1 = executeManySteps(stage1, STAGE_1_STEPS);
        }

        public void allRootNodesAreMarkedGray() {
            specify(collector.getColor("A"), should.equal(GRAY));
            specify(collector.getColor("B"), should.equal(WHITE));
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("D"), should.equal(WHITE));
        }

        public void thenTheStageEnds() {
            specify(stage1, should.containExactly());
        }

        public void ifMutatorsCreateNewRootsTheyAreMarkedGray() {
            graph.createDirectedEdge(null, "C");
            specify(collector.getColor("C"), should.equal(GRAY));
        }
    }

    public class InTheSecondStage {
        private int remainingSteps;

        public void create() {
            stage1 = executeManySteps(stage1, STAGE_1_STEPS);
            stage2 = executeOneStep(stage2);
            remainingSteps = STAGE_2_STEPS - MAX_NODES_PER_TASK;
        }

        public void grayNodesAreScannedBlack() {
            specify(collector.getColor("A"), should.equal(BLACK));
        }

        public void nodesReachableFromScannedBlackNodesAreMarkedGray() {
            specify(collector.getColor("B"), should.equal(GRAY));
        }

        public void notYetReachedNodesRemainWhite() {
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("D"), should.equal(WHITE));
        }

        public void theAlgorithmProceedsRecursivelyToNotYetScannedNodes() {
            stage2 = executeOneStep(stage2);
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("B"), should.equal(BLACK));
            specify(collector.getColor("C"), should.equal(GRAY));
            specify(collector.getColor("D"), should.equal(WHITE));
        }

        public void finallyAllReachableNodesAreBlackAndUnreachableNodesAreWhite() {
            stage2 = executeManySteps(stage2, remainingSteps);
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("B"), should.equal(BLACK));
            specify(collector.getColor("C"), should.equal(BLACK));
            specify(collector.getColor("D"), should.equal(WHITE));
        }

        public void thenTheStageEnds() {
            stage2 = executeManySteps(stage2, remainingSteps);
            specify(stage2, should.containExactly());
        }

        public void ifMutatorsCreateNewNodesTheyAreMarkedGrayAndLaterScannedBlack() {
            graph.createNode("X");
            graph.createDirectedEdge("A", "X");
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("X"), should.equal(GRAY));

            stage2 = executeManySteps(stage2, remainingSteps);
            specify(collector.getColor("A"), should.equal(BLACK));
//            specify(collector.getColor("X"), should.equal(BLACK)); // TODO
        }

        public void ifMutatorsRedirectEdgesTheTargetNodesAreMarkedGrayAndLaterScannedBlack() {
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("C"), should.equal(WHITE));

            graph.createDirectedEdge("A", "C");
            graph.removeDirectedEdge("B", "C");
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("C"), should.equal(GRAY));

            stage2 = executeManySteps(stage2, remainingSteps);
            specify(collector.getColor("A"), should.equal(BLACK));
//            specify(collector.getColor("C"), should.equal(BLACK)); // TODO
        }
    }

    public class InTheThirdStage {

        public void create() {
            stage1 = executeManySteps(stage1, STAGE_1_STEPS);
            stage2 = executeManySteps(stage2, STAGE_2_STEPS);
            mutatorCreatesANewNode();
            stage3 = executeManySteps(stage3, STAGE_3_STEPS);
        }

        private void mutatorCreatesANewNode() {
            graph.createNode("X");
            graph.createDirectedEdge("A", "X");
            stage3 = executeOneStep(stage3);
        }

        public void blackNodesAreKept() {
            specify(graph.getAllNodes(), should.containAll("A", "B", "C"));
        }

        public void whiteNodesAreRemoved() {
            specify(graph.getAllNodes(), should.not().contain("D"));
        }

        public void ifMutatorsCreateNewNodesTheyAreKept() {
            specify(graph.getAllNodes(), should.contain("X"));
        }

        public void theRemainingNodesAreClearedWhite() {
            specify(collector.getColor("A"), should.equal(WHITE));
            specify(collector.getColor("B"), should.equal(WHITE));
            specify(collector.getColor("C"), should.equal(WHITE));
        }

        public void thenTheStageEnds() {
            specify(stage3, should.containExactly());
        }
    }

    public class WhenMutatorsCreateANewReference {

        public void whiteTargetNodeBecomesGray() {
            specify(collector.getColor("A"), should.equal(WHITE));
            specify(collector.getColor("C"), should.equal(WHITE));
            graph.createDirectedEdge("A", "C");
            specify(collector.getColor("A"), should.equal(WHITE));
            specify(collector.getColor("C"), should.equal(GRAY));
        }

        public void grayTargetNodeRemainsGray() {
            stage1 = executeManySteps(stage1, STAGE_1_STEPS);
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("A"), should.equal(GRAY));
            graph.createDirectedEdge("C", "A");
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("A"), should.equal(GRAY));
        }

        public void blackTargetNodeRemainsBlack() {
            stage1 = executeManySteps(stage1, STAGE_1_STEPS);
            stage2 = executeOneStep(stage2);
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("A"), should.equal(BLACK));
            graph.createDirectedEdge("C", "A");
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("A"), should.equal(BLACK));
        }

        public void theSourceNodeColorIsNotChanged() {
            stage1 = executeManySteps(stage1, STAGE_1_STEPS);
            stage2 = executeOneStep(stage2);
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("B"), should.equal(GRAY));
            specify(collector.getColor("C"), should.equal(WHITE));
            graph.createNode("E");
            graph.createDirectedEdge("A", "E");
            graph.createDirectedEdge("B", "E");
            graph.createDirectedEdge("C", "E");
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("B"), should.equal(GRAY));
            specify(collector.getColor("C"), should.equal(WHITE));
        }
    }

    public class WhenMutatorsRemoveAReference {

        public void theNodeColorsAreNotChanged() {
            stage1 = executeManySteps(stage1, STAGE_1_STEPS);
            stage2 = executeOneStep(stage2);
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("B"), should.equal(GRAY));
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("D"), should.equal(WHITE));
            graph.removeDirectedEdge("B", "C");
            graph.removeDirectedEdge("B", "A");
            graph.removeDirectedEdge("A", "B");
            graph.removeDirectedEdge(null, "A");
            specify(collector.getColor("A"), should.equal(BLACK));
            specify(collector.getColor("B"), should.equal(GRAY));
            specify(collector.getColor("C"), should.equal(WHITE));
            specify(collector.getColor("D"), should.equal(WHITE));
        }
    }
}
