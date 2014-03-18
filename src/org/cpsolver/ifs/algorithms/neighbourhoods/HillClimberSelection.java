package org.cpsolver.ifs.algorithms.neighbourhoods;

import org.cpsolver.ifs.heuristics.NeighbourSelection;

/**
 * This interface may be implemented by a {@link NeighbourSelection} to indicate that it is employed by a hill climber.
 * <br>
 * 
 * @version IFS 1.3 (Iterative Forward Search)<br>
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
 **/
public interface HillClimberSelection {
    
    /**
     * True if employed by a hill climber, e.g., worsening moves may be skipped.
     */
    public void setHcMode(boolean hcMode);
}
