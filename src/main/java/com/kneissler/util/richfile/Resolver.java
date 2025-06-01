package com.kneissler.util.richfile;

import java.util.List;

public interface Resolver {

    /** determines if line can be resolved by this resolver */
    boolean canResolve(String line);

    /**
     * resolve list of lines, it can be assumed that the list is not empty and
     * first line is resolvable, i.e. canResolve(lines.get(0)) returns true.
     * The map of variables may change (as side effect of this method).
     */
    List<LineInFile> resolve(LineInFile firstLine, List<LineInFile> remainingLines);

}
