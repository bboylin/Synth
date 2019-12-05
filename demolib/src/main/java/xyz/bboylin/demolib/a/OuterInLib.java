package xyz.bboylin.demolib.a;

import android.text.TextUtils;

import xyz.bboylin.demolib.UtilsInLib;
import xyz.bboylin.demolib.b.OuterBaseInLib;

public class OuterInLib extends OuterBaseInLib {
    private Double mOuterDoubleA = 2.0;
    private double mOuterDoubleB = 3.0;
    private static Double sOuterDoubleA = 2.0;
    private static double sOuterDoubleB = 3.0;
    private Long mOuterLongA = 2L;
    private long mOuterLongB = 3L;
    private static Long sOuterLongA = 2L;
    private static long sOuterLongB = 3;
    private Float mOuterFloatA = 2F;
    private float mOuterFloatB = 3;
    private static Float sOuterFloatA = 2F;
    private static float sOuterFloatB = 3F;
    private Integer mOuterIntA = 2;
    private int mOuterIntB = 3;
    private static Integer sOuterIntA = 2;
    private static int sOuterIntB = 3;
    private String mOuterStr = "mOuterStr";
    private static String sOuterStr = "sOuterStr";

    private void logForOuter() {
        UtilsInLib.log("logForOuter");
    }

    private static void staticLogForOuter() {
        UtilsInLib.log("staticLogForOuter");
    }

    private double getOuterDouble() {
        return 2.0;
    }

    private static Double getOuterStaticDouble() {
        return 2.0;
    }

    private long getOuterLong() {
        return 2L;
    }

    private static Long getOuterStaticLong() {
        return 2L;
    }

    private String getOuterStr() {
        return "mOuterStr";
    }

    private static String getOuterStaticStr() {
        return "sOuterStr";
    }

    public void testInner() {
        Inner inner = new Inner();
        UtilsInLib.assertTrue(inner.mInnerDoubleA < inner.mInnerDoubleB);
        UtilsInLib.assertTrue(inner.mInnerLongA < inner.mInnerLongB);
        inner.logForInner();
        UtilsInLib.log(inner.getInnerDouble());
        UtilsInLib.log(inner.getInnerLong());
        UtilsInLib.assertTrue(TextUtils.equals(inner.mInnerString, inner.getInnerStr()));
        inner.test();
        inner.mInnerLongB = 1;
        inner.mInnerDoubleB = 1.0;
        UtilsInLib.assertTrue(inner.mInnerDoubleA > inner.mInnerDoubleB);
        UtilsInLib.assertTrue(inner.mInnerLongA > inner.mInnerLongB);

        UtilsInLib.assertTrue(TextUtils.equals(getOuterStr(), "mOuterStr"));
        UtilsInLib.assertTrue(getOuterLong() > 1);
        UtilsInLib.assertTrue(getOuterDouble() > 1);
        logForOuter();
        logForOuterBase();
        staticLogForOuter();
    }

    public static void testInnerStatic() {
        UtilsInLib.assertTrue(Inner2.sInnerDoubleA < Inner2.sInnerDoubleB);
        UtilsInLib.assertTrue(Inner2.sInnerLongA < Inner2.sInnerLongB);
        UtilsInLib.assertTrue(TextUtils.equals(Inner2.sInnerStr, Inner2.getStaticInnerStr()));
        Inner2.staticLogForInner2();
        UtilsInLib.log(Inner2.getStaticInnerDouble());
        UtilsInLib.log(Inner2.getStaticInnerLong());
        Inner2.test();
        Inner2.sInnerDoubleB = 1.0;
        Inner2.sInnerLongB = 1L;
        UtilsInLib.assertTrue(Inner2.sInnerDoubleA > Inner2.sInnerDoubleB);
        UtilsInLib.assertTrue(Inner2.sInnerLongA > Inner2.sInnerLongB);

        UtilsInLib.assertTrue(TextUtils.equals(getOuterStaticStr(), "sOuterStr"));
        UtilsInLib.assertTrue(getOuterStaticLong() > 1);
        UtilsInLib.assertTrue(getOuterStaticDouble() > 1);
        staticLogForOuter();
    }

    private class Inner {
        private Double mInnerDoubleA = 2.0;
        private double mInnerDoubleB = 3.0;
        private Long mInnerLongA = 2L;
        private long mInnerLongB = 3L;
        private String mInnerString = "mInnerString";

        private void logForInner() {
            UtilsInLib.log("logForInner");
        }

        private Double getInnerDouble() {
            return 2.0;
        }

        private Long getInnerLong() {
            return 2L;
        }

        private String getInnerStr() {
            return mInnerString;
        }

        void test() {
            UtilsInLib.assertTrue(mBaseProtectedDouble < 2.0);
            UtilsInLib.assertTrue(mOuterDoubleA < mOuterDoubleB);
            UtilsInLib.assertTrue(mOuterLongA < mOuterLongB);
            UtilsInLib.assertTrue(mOuterFloatA < mOuterFloatB);
            UtilsInLib.assertTrue(mOuterIntA + mOuterIntB == 5);
            UtilsInLib.assertTrue(TextUtils.equals(mOuterStr, getOuterStr()));
            logForOuterBase();
            logForOuter();
            UtilsInLib.log(getOuterDouble());
            UtilsInLib.log(getOuterLong());
            mOuterLongB = 1;
            mOuterDoubleB = 1.0;
            mOuterFloatB = 1;
            mOuterIntB = 1;
            mOuterStr = "not_mOuterStr";
            UtilsInLib.assertTrue(mOuterDoubleA > mOuterDoubleB);
            UtilsInLib.assertTrue(mOuterLongA > mOuterLongB);
            UtilsInLib.assertTrue(mOuterFloatA > mOuterFloatB);
            UtilsInLib.assertTrue(mOuterIntA + mOuterIntB == 3);
            UtilsInLib.assertTrue(!TextUtils.equals(mOuterStr, getOuterStr()));

            UtilsInLib.assertTrue(getInnerDouble() > 1);
            UtilsInLib.assertTrue(getInnerLong() > 1);
            UtilsInLib.assertTrue(TextUtils.equals(getInnerStr(), mInnerString));
            logForInner();
        }
    }

    private static class Inner2 {
        private static double sInnerDoubleA = 2.0;
        private static double sInnerDoubleB = 3.0;
        private static Long sInnerLongA = 2L;
        private static Long sInnerLongB = 3L;
        private static String sInnerStr = "sOuterStr";

        private static void staticLogForInner2() {
            UtilsInLib.log("staticLogForInner2");
        }

        private static double getStaticInnerDouble() {
            return 2.0;
        }

        private static long getStaticInnerLong() {
            return 2L;
        }

        private static String getStaticInnerStr() {
            return sInnerStr;
        }

        private static void test() {
            UtilsInLib.assertTrue(sOuterDoubleA < sOuterDoubleB);
            UtilsInLib.assertTrue(sOuterLongA < sOuterLongB);
            UtilsInLib.assertTrue(sOuterFloatA < sOuterFloatB);
            UtilsInLib.assertTrue(sOuterIntA + sOuterIntB == 5);
            UtilsInLib.assertTrue(TextUtils.equals(sOuterStr, getOuterStaticStr()));
            staticLogForOuter();
            UtilsInLib.log(getOuterStaticDouble());
            UtilsInLib.log(getOuterStaticLong());
            sOuterDoubleB = 1.0;
            sOuterLongB = 1L;
            sOuterFloatB = 1F;
            sOuterIntB = 1;
            sOuterStr = "not_sOuterStr";
            UtilsInLib.assertTrue(sOuterDoubleA > sOuterDoubleB);
            UtilsInLib.assertTrue(sOuterLongA > sOuterLongB);
            UtilsInLib.assertTrue(sOuterFloatA > sOuterFloatB);
            UtilsInLib.assertTrue(sOuterIntA + sOuterIntB == 3);
            UtilsInLib.assertTrue(!TextUtils.equals(sOuterStr, getOuterStaticStr()));

            UtilsInLib.assertTrue(TextUtils.equals(getStaticInnerStr(), sInnerStr));
            UtilsInLib.assertTrue(getStaticInnerLong() > 1);
            UtilsInLib.assertTrue(getStaticInnerDouble() > 1);
            staticLogForInner2();
        }
    }
}