package net.sf.cpsolver.ifs.algorithms;

import org.apache.log4j.Logger;

import net.sf.cpsolver.ifs.assignment.Assignment;
import net.sf.cpsolver.ifs.assignment.context.AssignmentContext;
import net.sf.cpsolver.ifs.assignment.context.NeighbourSelectionWithContext;
import net.sf.cpsolver.ifs.heuristics.NeighbourSelection;
import net.sf.cpsolver.ifs.heuristics.StandardNeighbourSelection;
import net.sf.cpsolver.ifs.model.Neighbour;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.model.Variable;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.termination.TerminationCondition;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;

/**
 * Simple search neighbour selection. <br>
 * <br>
 * It consists of the following three phases:
 * <ul>
 * <li>Construction phase ({@link StandardNeighbourSelection} until all variables are assigned)
 * <li>Hill-climbing phase ({@link HillClimber} until the given number if idle iterations)
 * <li>Simulated annealing phase ({@link SimulatedAnnealing} until timeout is reached)
 * <ul>
 * <li>Or great deluge phase (when Search.GreatDeluge is true, {@link GreatDeluge} until timeout is reached)
 * </ul>
 * <li>At the end (when {@link TerminationCondition#canContinue(Solution)} is false),
 * the search is finished with one sweep of final phase ({@link HillClimber} until the given number if idle iterations).
 * </ul>
 * <br>
 * <br>
 * 
 * @version IFS 1.2 (Iterative Forward Search)<br>
 *          Copyright (C) 2014 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a><br>
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 */
public class SimpleSearch<V extends Variable<V, T>, T extends Value<V, T>> extends NeighbourSelectionWithContext<V,T,SimpleSearch<V,T>.SimpleSearchContext> {
    private Logger iLog = Logger.getLogger(SimpleSearch.class);
    private NeighbourSelection<V, T> iCon = null;
    private boolean iConstructionUntilComplete = false; 
    private StandardNeighbourSelection<V, T> iStd = null;
    private SimulatedAnnealing<V, T> iSA = null;
    private HillClimber<V, T> iHC = null;
    private GreatDeluge<V, T> iGD = null;
    private boolean iUseGD = true;
    private Progress iProgress = null;

    /**
     * Constructor
     * 
     * @param properties
     *            problem properties
     */
    public SimpleSearch(DataProperties properties) throws Exception {
        String construction = properties.getProperty("Construction.Class"); 
        if (construction != null) {
            try {
                @SuppressWarnings("unchecked")
                Class<NeighbourSelection<V, T>> constructionClass = (Class<NeighbourSelection<V, T>>)Class.forName(properties.getProperty("Construction.Class"));
                iCon = constructionClass.getConstructor(DataProperties.class).newInstance(properties);
                iConstructionUntilComplete = properties.getPropertyBoolean("Construction.UntilComplete", iConstructionUntilComplete);
            } catch (Exception e) {
                iLog.error("Unable to use " + construction + ": " + e.getMessage());
            }
        }
        iStd = new StandardNeighbourSelection<V, T>(properties);
        iSA = new SimulatedAnnealing<V, T>(properties);
        if (properties.getPropertyBoolean("Search.CountSteps", false))
            iHC = new StepCountingHillClimber<V, T>(properties);
        else
            iHC = new HillClimber<V, T>(properties);
        iGD = new GreatDeluge<V, T>(properties);
        iUseGD = properties.getPropertyBoolean("Search.GreatDeluge", iUseGD);
    }

    /**
     * Initialization
     */
    @Override
    public void init(Solver<V, T> solver) {
        super.init(solver);
        iStd.init(solver);
        if (iCon != null)
            iCon.init(solver);
        iSA.init(solver);
        iHC.init(solver);
        iGD.init(solver);
        iProgress = Progress.getInstance(solver.currentSolution().getModel());
    }

    /**
     * Neighbour selection. It consists of the following three phases:
     * <ul>
     * <li>Construction phase ({@link StandardNeighbourSelection} until all variables are assigned)
     * <li>Hill-climbing phase ({@link HillClimber} until the given number if idle iterations)
     * <li>Simulated annealing phase ({@link SimulatedAnnealing} until timeout is reached)
     * </ul>
     */
    @SuppressWarnings("fallthrough")
    @Override
    public Neighbour<V, T> selectNeighbour(Solution<V, T> solution) {
        SimpleSearchContext context = getContext(solution.getAssignment());
        Neighbour<V, T> n = null;
        switch (context.getPhase()) {
            case -1:
                context.incPhase();
                iLog.info("***** construction phase *****");
                if (solution.getModel().nrUnassignedVariables(solution.getAssignment()) > 0)
                    iProgress.setPhase("Searching for initial solution...", solution.getModel().variables().size());
            case 0:
                if (iCon != null && solution.getModel().nrUnassignedVariables(solution.getAssignment()) > 0) {
                    iProgress.setProgress(solution.getModel().variables().size() - solution.getModel().getBestUnassignedVariables());
                    n = iCon.selectNeighbour(solution);
                    if (n != null || iConstructionUntilComplete)
                        return n;
                }
                context.incPhase();
                iLog.info("***** ifs phase *****");
            case 1:
                if (iStd != null && solution.getModel().nrUnassignedVariables(solution.getAssignment()) > 0) {
                    iProgress.setProgress(solution.getModel().variables().size() - solution.getModel().getBestUnassignedVariables());
                    return iStd.selectNeighbour(solution);
                }
                context.incPhase();
                iLog.info("***** hill climbing phase *****");
            case 2:
                if (solution.getModel().nrUnassignedVariables(solution.getAssignment()) > 0)
                    return (iCon == null ? iStd : iCon).selectNeighbour(solution);
                n = iHC.selectNeighbour(solution);
                if (n != null)
                    return n;
                context.incPhase();
                iLog.info("***** " + (iUseGD ? "great deluge" : "simulated annealing") + " phase *****");
            case 3:
                if (solution.getModel().nrUnassignedVariables(solution.getAssignment()) > 0)
                    return (iCon == null ? iStd : iCon).selectNeighbour(solution);
                if (iUseGD)
                    return iGD.selectNeighbour(solution);
                else
                    return iSA.selectNeighbour(solution);
            default:
                return null;
        }
    }

    @Override
    public SimpleSearchContext createAssignmentContext(Assignment<V, T> assignment) {
        return new SimpleSearchContext();
    }

    public class SimpleSearchContext implements AssignmentContext {
        private int iPhase = -1;
        
        public int getPhase() { return iPhase; }
        public void incPhase() { iPhase++; }
        public void setPhase(int phase) { iPhase = phase; }
    }
}
