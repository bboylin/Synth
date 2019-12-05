package xyz.bboylin.demo.b;

import xyz.bboylin.demo.Utils;

/**
 * Created by bboylin on 2019-09-25.
 */
public class OuterBase {
    protected Double mBaseProtectedDouble = 1.2;

    protected void logForOuterBase() {
        Utils.log("logForOuterBase");
    }
}
