package net.sf.cpsolver.ifs.example.jobshop;

import java.util.*;

import net.sf.cpsolver.ifs.model.*;

/**
 * Operation.
 * <br><br>
 * Each operation has its number, job, machine and processing time
 *
 * @version
 * IFS 1.1 (Iterative Forward Search)<br>
 * Copyright (C) 2006 Tomas Muller<br>
 * <a href="mailto:muller@ktiml.mff.cuni.cz">muller@ktiml.mff.cuni.cz</a><br>
 * Lazenska 391, 76314 Zlin, Czech Republic<br>
 * <br>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <br><br>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <br><br>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
public class Operation extends Variable {
    private Job iJob = null;
    private Machine iMachine = null;
    private int iProcessingTime = 0;
    private int iOperationNumber = 0;
    
    /**
     * Constructor
     * @param job job
     * @param machine machine
     * @param operationNumber operation number
     * @param processingTime processing time
     */
    public Operation(Job job, Machine machine, int operationNumber, int processingTime) {
        super(null);
        iJob = job;
        iMachine = machine;
        iProcessingTime = processingTime;
        iOperationNumber = operationNumber;
    }
    
    /** Get job */
    public Job getJob() { return iJob; }
    
    /** Get job number */
    public int getJobNumber() { return iJob.getJobNumner(); }
    
    /** Get operation number */
    public int getOperationNumber() { return iOperationNumber; }
    
    /** Get machine */
    public Machine getMachine() { return iMachine; }
    
    /** Get machine number */
    public int getMachineNumber() { return iMachine.getMachineNumber(); }
    
    /** Get processing time */
    public int getProcessingTime() { return iProcessingTime; }
    
    /** Get the preceeding operation (if any) */
    public Operation getPrecedingOperation() { return (iOperationNumber==0?null:iJob.getOperation(iOperationNumber-1)); }
    
    /** Get the subsequent operation (if any) */
    public Operation getSubsequentOperation() { return (iOperationNumber+1==iJob.countOperations()?null:iJob.getOperation(iOperationNumber+1)); }
    
    /** Get minimal starting time */
    public int getMinStartTime() {
        if (iOperationNumber==0) return 0;
        else return getPrecedingOperation().getMinStartTime()+iProcessingTime;
    }
    
    /** Get maximal starting time */
    public int getMaxStartTime() {
        if (iOperationNumber+1==iJob.countOperations()) return ((JobShopModel)getModel()).getTotalNumberOfSlots()-iProcessingTime;
        else return getSubsequentOperation().getMaxStartTime()-iProcessingTime;
    }
    
    /** Compares two operations -- job number and operation number must match */
    public boolean equals(Object o) {
        if (o==null || !(o instanceof Operation)) return false;
        Operation op = (Operation)o;
        return getJobNumber()==op.getJobNumber() && getOperationNumber()==op.getOperationNumber();
    }
    
    /** Initialozation -- fills the variable's domain */
    public void init() {
        setValues(computeValues());
    }
    
    private Vector computeValues() {
        Vector ret = new Vector();
        for (int i=getMinStartTime();i<=getMaxStartTime();i++)
            ret.addElement(new Location(this,i));
        return ret;
    }
    
    /** string representation -- for debuging and printing purposes */
    public String toString() { return getName(); }

    /** Operation's name (e.g., O[2,4] where 2 is the job number and 4 is the operation number */
    public String getName() { return "O["+getJobNumber()+","+getOperationNumber()+"]"; }
}
