package xyz.bboylin.demo.a;

import android.text.TextUtils;

import xyz.bboylin.demo.Utils;
import xyz.bboylin.demo.b.OuterBase;

public class Outer extends OuterBase {
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
        Utils.log("logForOuter");
    }

    private static void staticLogForOuter() {
        Utils.log("staticLogForOuter");
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
        Utils.assertTrue(inner.mInnerDoubleA < inner.mInnerDoubleB);
        Utils.assertTrue(inner.mInnerLongA < inner.mInnerLongB);
        inner.logForInner();
        Utils.log(inner.getInnerDouble());
        Utils.log(inner.getInnerLong());
        Utils.assertTrue(TextUtils.equals(inner.mInnerString, inner.getInnerStr()));
        inner.test();
        inner.mInnerLongB = 1;
        inner.mInnerDoubleB = 1.0;
        Utils.assertTrue(inner.mInnerDoubleA > inner.mInnerDoubleB);
        Utils.assertTrue(inner.mInnerLongA > inner.mInnerLongB);

        Utils.assertTrue(TextUtils.equals(getOuterStr(), "mOuterStr"));
        Utils.assertTrue(getOuterLong() > 1);
        Utils.assertTrue(getOuterDouble() > 1);
        logForOuter();
        logForOuterBase();
        staticLogForOuter();
    }

    public static void testInnerStatic() {
        Utils.assertTrue(Inner2.sInnerDoubleA < Inner2.sInnerDoubleB);
        Utils.assertTrue(Inner2.sInnerLongA < Inner2.sInnerLongB);
        Utils.assertTrue(TextUtils.equals(Inner2.sInnerStr, Inner2.getStaticInnerStr()));
        Inner2.staticLogForInner2();
        Utils.log(Inner2.getStaticInnerDouble());
        Utils.log(Inner2.getStaticInnerLong());
        Inner2.test();
        Inner2.sInnerDoubleB = 1.0;
        Inner2.sInnerLongB = 1L;
        Utils.assertTrue(Inner2.sInnerDoubleA > Inner2.sInnerDoubleB);
        Utils.assertTrue(Inner2.sInnerLongA > Inner2.sInnerLongB);

        Utils.assertTrue(TextUtils.equals(getOuterStaticStr(), "sOuterStr"));
        Utils.assertTrue(getOuterStaticLong() > 1);
        Utils.assertTrue(getOuterStaticDouble() > 1);
        staticLogForOuter();
    }

    private class Inner {
        private Double mInnerDoubleA = 2.0;
        private double mInnerDoubleB = 3.0;
        private Long mInnerLongA = 2L;
        private long mInnerLongB = 3L;
        private String mInnerString = "mInnerString";

        private void logForInner() {
            Utils.log("logForInner");
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
            Utils.assertTrue(mBaseProtectedDouble < 2.0);
            Utils.assertTrue(mOuterDoubleA < mOuterDoubleB);
            Utils.assertTrue(mOuterLongA < mOuterLongB);
            Utils.assertTrue(mOuterFloatA < mOuterFloatB);
            Utils.assertTrue(mOuterIntA + mOuterIntB == 5);
            Utils.assertTrue(TextUtils.equals(mOuterStr, getOuterStr()));
            logForOuterBase();
            logForOuter();
            Utils.log(getOuterDouble());
            Utils.log(getOuterLong());
            mOuterLongB = 1;
            mOuterDoubleB = 1.0;
            mOuterFloatB = 1;
            mOuterIntB = 1;
            mOuterStr = "not_mOuterStr";
            Utils.assertTrue(mOuterDoubleA > mOuterDoubleB);
            Utils.assertTrue(mOuterLongA > mOuterLongB);
            Utils.assertTrue(mOuterFloatA > mOuterFloatB);
            Utils.assertTrue(mOuterIntA + mOuterIntB == 3);
            Utils.assertTrue(!TextUtils.equals(mOuterStr, getOuterStr()));

            Utils.assertTrue(getInnerDouble() > 1);
            Utils.assertTrue(getInnerLong() > 1);
            Utils.assertTrue(TextUtils.equals(getInnerStr(), mInnerString));
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
            Utils.log("staticLogForInner2");
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
            Utils.assertTrue(sOuterDoubleA < sOuterDoubleB);
            Utils.assertTrue(sOuterLongA < sOuterLongB);
            Utils.assertTrue(sOuterFloatA < sOuterFloatB);
            Utils.assertTrue(sOuterIntA + sOuterIntB == 5);
            Utils.assertTrue(TextUtils.equals(sOuterStr, getOuterStaticStr()));
            staticLogForOuter();
            Utils.log(getOuterStaticDouble());
            Utils.log(getOuterStaticLong());
            sOuterDoubleB = 1.0;
            sOuterLongB = 1L;
            sOuterFloatB = 1F;
            sOuterIntB = 1;
            sOuterStr = "not_sOuterStr";
            Utils.assertTrue(sOuterDoubleA > sOuterDoubleB);
            Utils.assertTrue(sOuterLongA > sOuterLongB);
            Utils.assertTrue(sOuterFloatA > sOuterFloatB);
            Utils.assertTrue(sOuterIntA + sOuterIntB == 3);
            Utils.assertTrue(!TextUtils.equals(sOuterStr, getOuterStaticStr()));

            Utils.assertTrue(TextUtils.equals(getStaticInnerStr(), sInnerStr));
            Utils.assertTrue(getStaticInnerLong() > 1);
            Utils.assertTrue(getStaticInnerDouble() > 1);
            staticLogForInner2();
        }
    }
}