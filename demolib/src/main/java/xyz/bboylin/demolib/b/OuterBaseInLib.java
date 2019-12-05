package xyz.bboylin.demolib.b;


import xyz.bboylin.demolib.UtilsInLib;

/**
 * Created by bboylin on 2019-09-25.
 */
public class OuterBaseInLib {
    protected Double mBaseProtectedDouble = 1.2;

    protected void logForOuterBase() {
        UtilsInLib.log("logForOuterBase");
    }
}
